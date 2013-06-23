package server.netserver;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import server.cmds.CmdDispatch;
import util.Bits;
import util.EncryptUtil;

/**
 * 
 * @author lzg 命令解析
 */
public class MsgInParse {
	private static Logger logger = Logger.getLogger(MsgInParse.class);
	private static final MsgInParse parse = new MsgInParse();

	private MsgInParse() {
	};

	public static MsgInParse getInstance() {
		return parse;
	}

	/**
	 * @author lzg------2011-2-16
	 * @param session
	 * @param bytes
	 *            已经处理完毕的数据
	 */
	public void process(DataPackEntry dpe) {
		try {
			if (dpe.getConnType() == DataPackEntry.CONN_TYPE_HTTP) {
				logger.info("服务器收到http请求");
				processMsg(dpe.getSession(), dpe.command, dpe.getData(), false);
			} else {
				byte[] bytes = dpe.getData();
				processMsg(dpe.getSession(), dpe.command, bytes, true);
			}

		} catch (Exception e) {
			logger.error("process " + dpe.getSession() + " "
					+ dpe.getData().length, e);
			 logger.error(dpe.getSession()+"被服务器主动关闭201210241430");
			 logger.error(dpe.getSession()+"处理数据异常,关闭session...");
			dpe.getSession().close();
		}
	}
   

	/**
	 * 处理网络层收到的数据
	 * 
	 * @author liuzg
	 * @param session
	 * @param command
	 * @param data
	 * @param isSocket
	 */
	private void processMsg(NioSession session, int command, byte[] data,
			boolean isSocket) {
		try {
			// 流水号
//			int sid = Bits.getByte(data, 0);
//			byte[] bytes = new byte[data.length - 1];
//			System.arraycopy(data, 1, bytes, 0, bytes.length);
//			if (isSocket==false || command>=0x160001&&command<=0x160099||command==0x10098) {
//				logger.debug("未加密的命令:"+Integer.toHexString(command));
//			} else {
//				// 解密信息start
//				if (session.getAttribute("keyIndex") == null) {
//					logger.error("无法找到密钥信息!");
//					return;
//				}
//				int index = (Integer) session.getAttribute("keyIndex");
//				bytes = EncryptUtil.decrypt(bytes,
//						EncryptUtil.getEncryptKey(index), sid);
//			}
			if (isSocket) {
				CmdDispatch.getInstance().parseCMD(session, data);
			} else {
				CmdDispatch.getInstance().parseCMDForHttp(session, command, data);
			}
		} catch (Exception e) {
			logger.error("处理网络层信息出现异常:",e);
		}
	}
}