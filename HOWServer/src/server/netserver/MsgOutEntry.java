package server.netserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import util.Bits;
import util.Util;

/**
 * 消息包体的 封装发送类 有缓冲功能 调用flush发送
 * 
 * @author lzg
 * 
 */
public class MsgOutEntry {
	private static Logger logger = Logger.getLogger(MsgOutEntry.class);
	private NioSession session;// 发送该消息的客户handler

	public MsgOutEntry(NioSession session) {
		this.session = session;
	}
    private static Map<Integer,Integer> cmdCount=new HashMap<Integer,Integer>();
    private static long lastTimes=0;
    public static long MAX_MSG_LENGTH=0;
    public static int MAX_MSG_CMD=0;
	/**
	 * @author liuzg
	 * @throws IOException
	 *             socket发送
	 */
	public void flush(byte data[]) {
		if (data == null) {
			return;
		}
		if (session == null) {
			return;
		}
		int cmd = Bits.getInt(data, 0);
		if (cmd < 0xFFFF) {
			logger.error("发送错误的服务器命令:0x" + Integer.toHexString(cmd) + ",数据长度"
					+ (data.length - 4) + "字节");
			return;
		}
		if(cmdCount.get(cmd)!=null){
			cmdCount.put(cmd, cmdCount.get(cmd)+1);
		}else{
			cmdCount.put(cmd, 1);
		}
		if(System.currentTimeMillis()-lastTimes>Util.ONE_MIN*10){
			lastTimes=System.currentTimeMillis();
			for(Map.Entry<Integer, Integer> entry:cmdCount.entrySet()){
				logger.info("cmd:"+Integer.toHexString(entry.getKey())+",发送次数:"+entry.getValue());
			}
			cmdCount.clear();
		}
		if(data.length>MAX_MSG_LENGTH){
			MAX_MSG_LENGTH=data.length;
			MAX_MSG_CMD=cmd;
		}
		// logger.info("服务器发送数据命令:0x"+Integer.toHexString(cmd)+",数据长度"+(data.length-4)+"字节");
		if(data.length+12>1024*1024*2){
			logger.error(session+"请求发送的数据过长,lenght="+data.length+",cmd="+Integer.toHexString(cmd));
			logger.error(session+"请求发送的数据过长,关闭session...");
			session.close();
			return;
		}
		logger.info("发送命令:0x"+Integer.toHexString(cmd)+",长度:"+data.length);
		if (!session.isClosing()) {
			MsgInAndOutThread.getInstance().send(
					new DataPackEntry(session, DataPackEntry.CONN_TYPE_SOCKET,
							cmd, data));
		}
	}

	/**
	 * @author liuzg
	 * @throws IOException
	 *             http发送
	 */
	public void flushForHttp(byte data[]) {
		try {
			if (data == null) {
				return;
			}
			if (session == null) {
				return;
			}
			// logger.info("服务器发送数据长度"+Integer.toHexString(cmd)+"命令发送数据"+(getMsgBytes().length-4)+"字节");
			if (!session.isClosing()) {
				MsgInAndOutThread.getInstance().send(
						new DataPackEntry(session, DataPackEntry.CONN_TYPE_HTTP, 0, data));
			}
		} catch (Exception e) {
			logger.error("发送http协议时出现异常:",e);
		}
	}
}
