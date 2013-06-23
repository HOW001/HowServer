package server.netserver;

/**
 * @author lzg
 * 启动发送线程
 */
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import server.cmds.CmdParser;
import server.cmds.FightCP;
import server.cmds.RegisterCP;
import util.EncryptUtil;
import util.Util;
import world.World;

public class MsgOutThread implements Runnable {
	private static Logger logger = Logger.getLogger(MsgOutThread.class);
	private BlockingQueue<DataPackEntry> queue = new LinkedBlockingQueue<DataPackEntry>();
	private static boolean exit = false;

	public boolean send(DataPackEntry dpe) {
		return queue.add(dpe);
	}
	private static long msgCount=0;
	private static long recordeTime=0;
//    private static long count=0;
	 private static long lastPrintTime=0;
	public void run() {
		long useTime=0;
		logger.info("开始启动发送服务.....");
		while (!exit) {
			// 发送阶段会出现堵塞状况，加锁可以避免数据混乱
//			synchronized (queue) {
				try {
					DataPackEntry dpe = queue.take();
					if (dpe == null) {
						continue;
					}
					NioSession session = dpe.getSession();
					if (session == null) {
						logger.error("严重错误，session==null");
						continue;
					}
					byte[] data = dpe.getData();				
					if (data != null) {
						if(dpe.connectionType==DataPackEntry.CONN_TYPE_SOCKET){
//						int sid = 0;
//						if (session.getAttribute("sendSID") != null) {
//							sid = (Integer) session.getAttribute("sendSID");
//						}
//						sid = (sid + 1) % 100;
//						session.setAttribute("sendSID", sid);
						// 加密信息start
//						if (dpe.command != RegisterCP.getInstance().getCmd(RegisterCP.CHANGE_ENCRYPT_KEY)&&dpe.command!=FightCP.getInstance().getCmd(FightCP.FIGHT_START)) {// 传递密钥信息时无需加密
//							if(CmdParser.getType(dpe.command)!=CmdParser.TYPE_GM){
//							if (session.getAttribute("keyIndex") == null) {
//								logger.error("发送时无法找到密钥信息!");
//								return;
//							}
//							int index = (Integer) session.getAttribute("keyIndex");
//							useTime=System.currentTimeMillis();
//							data = EncryptUtil.encrypt(data,
//									EncryptUtil.getEncryptKey(index), sid);
//							useTime=System.currentTimeMillis()-useTime;
//							if(useTime>1000){
//								logger.error("加密用时:"+useTime);
//							}
//							}
//						}else{
//							if(dpe.command!=0x50002){
//								logger.info("未加密的命令:"+Integer.toHexString(dpe.command));
//							}
//						}
						// 加密信息end
//						byte[] bytes = new byte[data.length + 1];
//						bytes[0] = (byte) sid;
//						System.arraycopy(data, 0, bytes, 1, data.length);
						
						World.getInstance().sendCommand++;
						World.getInstance().size_send+=data.length;
						session.write(data);
						}else{
							//Http连接
							session.write(dpe.getData());
						}
						
					}
					if(System.currentTimeMillis()-lastPrintTime>Util.ONE_MIN){
						lastPrintTime=System.currentTimeMillis();
						logger.info("信息接收线程正常运行中...");
					}
					msgCount++;
					if(System.currentTimeMillis()-recordeTime>Util.ONE_MIN){
						logger.info("服务器每分钟发送命令:"+msgCount+"条");
						msgCount=0;
						recordeTime=System.currentTimeMillis();
					}
				} catch (Exception e) {
					logger.error("信息线程发送时出现异常 ", e);
				}
			}
	}

	public static void exit() {
		exit = true;
	}
	public static boolean isExit(){
		return exit;
	}
}
