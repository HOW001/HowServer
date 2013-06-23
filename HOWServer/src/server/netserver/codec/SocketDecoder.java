package server.netserver.codec;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.transport.socket.nio.NioSession;
import server.netserver.DataPackEntry;
import server.netserver.MsgInThread;
import util.Bits;
import util.ByteArray;
import world.World;

/**
 * Description:解析上行报文头信息端,上行信息格式:int(验证值)+short(数据体长度)+byte[](数据体)<br>
 * 
 * @author liuzg
 */
public class SocketDecoder extends CumulativeProtocolDecoder {
	private static Logger logger = Logger.getLogger(SocketDecoder.class);
//	public static final int VALIDVALUE = 19820708;
//	public static final int VALIDVALUE_LINK = 19821230;// 无需加密
	/*
	 * 剩余区数据
	 */
	// private byte[] remainingAreaData=new byte[0];

	/*
	 * 数据包最小长度
	 */
	private static final int PACKHEADMINLENGTH = 4;

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		if (MsgInThread.isExit()) {
			logger.error("网络接收线程已关闭...");
			logger.error(session + "网络接收线程已关闭,关闭session...");
			session.close(false);
			return false;
		}
		byte[] remainingAreaData = (byte[]) session
				.getAttribute("remainingAreaData");
		if (remainingAreaData == null) {
			remainingAreaData = new byte[0];
		}
		int currentDataLength = in.remaining();
		if (currentDataLength < 10) {
			logger.info("收到数据长度:" + currentDataLength);
		}
		byte[] currentUseData = new byte[currentDataLength];
		in.get(currentUseData);
		/*
		 * 剩余区数据需要读取
		 */
		if (remainingAreaData.length > 0) {
			/*
			 * 将上次剩余数据与本次接收到的数据进行拼接
			 */
			byte[] remainingAreaAndCurrentData = new byte[currentUseData.length
					+ remainingAreaData.length];
			System.arraycopy(remainingAreaData, 0, remainingAreaAndCurrentData,
					0, remainingAreaData.length);
			System.arraycopy(currentUseData, 0, remainingAreaAndCurrentData,
					remainingAreaData.length, currentUseData.length);
			currentUseData = remainingAreaAndCurrentData;
		}

		if (processCurrentData(session, currentUseData, out)) {
			return true;
		}
//		int currentPos = 0;
//		if (currentDataLength != currentPos) {
//			logger.info(session + "出现数据包过长:currentDataLength="
//					+ currentDataLength + ",currentPos=" + currentPos);
//			if (session.getAttribute("GWT") == null) {// 表示尚未建立GWT连接
//				if (currentDataLength > 45) {// 表示GWT之后有信息存在
//					if (currentUseData[0] == 116 && currentUseData[1] == 103
//							&& currentUseData[2] == 119
//							&& currentUseData[3] == 95
//							&& currentUseData[4] == 108) {
//						// 此处确定是GWT信息,并且GWT之后有尚未处理的数据
//						logger.info(session + "出现GWT之后拥有数据");
//						remainingAreaData = new byte[currentUseData.length - 45];
//						System.arraycopy(currentUseData, 45, remainingAreaData,
//								0, remainingAreaData.length);
//						if (processCurrentData(session, remainingAreaData, out)) {
//							return true;
//						}
//					}
//				}
//			} else {
//				session.setAttribute("GWT", true);
//			}
//		}
		remainingAreaData = new byte[0];
		session.setAttribute("remainingAreaData", remainingAreaData);
		return true;
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param currentUseData
	 * @param out
	 * @return 解析数据包
	 */
	private boolean processCurrentData(IoSession session,
			byte[] currentUseData, ProtocolDecoderOutput out) {
		// logger.info("本次处理信息为:");
		// StringBuffer sb=new StringBuffer();
		// for(byte bytes:currentUseData){
		// sb.append(bytes+",");
		// }
		// logger.info(sb.toString());
		byte remainingAreaData[] = null;
		/*
		 * 拼接后实际长度
		 */
		int currentDataLength = currentUseData.length;
		ByteArray currentUseDataArray = new ByteArray(currentUseData);
		if (currentDataLength < PACKHEADMINLENGTH) {
			/*
			 * 最小长度为:int(验证码)+int(数据长度)
			 */
			remainingAreaData = currentUseData;
			session.setAttribute("remainingAreaData", remainingAreaData);
			return true;
		}
		int currentPos = 0;
		/*
		 * 本次数据包所包含的命令数量
		 */
		int cmdCount = 1;
		for (; currentPos < currentDataLength;) {
			if (cmdCount > 1) {
				logger.info("解析第" + cmdCount + "个包!");
			}
			cmdCount++;
			if (currentDataLength - currentPos < PACKHEADMINLENGTH) {
				/*
				 * 当前指针位置之后的数据长度小于4,直接放入剩余区
				 */
				remainingAreaData = new byte[currentUseData.length - currentPos];
				System.arraycopy(currentUseData, currentPos, remainingAreaData,
						0, remainingAreaData.length);
				session.setAttribute("remainingAreaData", remainingAreaData);
				return true;
			}
			/*
			 * 是否有效命令验证
			 */
//			int isValid = currentUseDataArray.readInt();// in.getInt();
//			currentPos += 4;
//			if (isValid == VALIDVALUE || isValid == VALIDVALUE_LINK) {// 验证值是验证数据有效性
				int packageLen = currentUseDataArray.readInt()-4;// in.getInt();
				if (packageLen > 1024 * 4) {
					logger.info(session + "实际数据长度太长:" + packageLen);
					logger.error(session + "收到的实际数据太长,关闭session...");
					session.close(false);
					remainingAreaData = new byte[0];
					session.setAttribute("remainingAreaData", remainingAreaData);
					return true;
				}
				if (packageLen < 0) {
					logger.info(session + "实际数据长度无效:" + packageLen);
					logger.error(session + "收到实际数据长度无效,关闭session...");
					session.close(false);
					remainingAreaData = new byte[0];
					session.setAttribute("remainingAreaData", remainingAreaData);
					return true;
				}
				
				if (World.players.size() < 10
						&& currentDataLength != (packageLen + PACKHEADMINLENGTH)) {
					logger.info(session + "收到包总长:" + currentDataLength
							+ ",需要数据长度：" + packageLen);
				}
				if (currentDataLength - currentPos < packageLen) {
					/*
					 * 当前指针之后的数据无法满足本次数据包需要，放入剩余区
					 */
					// 如果此处有问题，可以参考TextLineDecoder
					logger.debug(session + "pos=" + currentPos + "实际长度"
							+ currentDataLength + "<需求数据长度" + packageLen
							+ ",前端一个数据包没有发送完全部数据，需要重新接收！");
					// in.rewind();// 重新读取已包含的数据
					// in.position(pos-8);//回到此包的开始位置
					remainingAreaData = new byte[currentUseData.length
							- currentPos + PACKHEADMINLENGTH];
					System.arraycopy(currentUseData, currentPos
							- PACKHEADMINLENGTH, remainingAreaData, 0,
							remainingAreaData.length);
					session.setAttribute("remainingAreaData", remainingAreaData);
					return true;
				}
				currentPos += 4;
				byte dataBody[] = currentUseDataArray.readByteArray(packageLen);
				currentPos += dataBody.length;
				int command = Bits.getInt(dataBody, 0);// 可以从包装中读取命令ID
				logger.info("检测到命令:0x"+Integer.toHexString(command)+",len="+dataBody.length);
				DataPackEntry dpe = new DataPackEntry((NioSession) session,
						DataPackEntry.CONN_TYPE_SOCKET, command, dataBody);
				out.write(dpe);
				remainingAreaData = new byte[0];
				session.setAttribute("remainingAreaData", remainingAreaData);
//			} else {
//				remainingAreaData = new byte[0];
//				session.setAttribute("remainingAreaData", remainingAreaData);
//				return true;
//			}
		}
		if (currentPos == currentDataLength) {
			// 已经正确分解完所有数据包
			return true;
		}
		return false;
	}
}
