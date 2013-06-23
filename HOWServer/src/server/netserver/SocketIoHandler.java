package server.netserver;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSession;

import server.ServerEntrance;
import world.World;

/**
 * @author liuzg
 * @描述 ：处理TCP请求
 */

public class SocketIoHandler extends IoHandlerAdapter {
	/**
	 * 日志句柄
	 */
	private final Logger logger = Logger.getLogger(SocketIoHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.common.IoHandler#exceptionCaught(org.apache.mina.common
	 * .IoSession, java.lang.Throwable)
	 */
	public void exceptionCaught(IoSession session, Throwable ex) throws Exception {
		if(ex instanceof Exception){
			if(ex instanceof IOException){
//				if(World.players.size()<400){
//					//测试使用
//					logger.error(session+"session句柄异常:ID"+session.getId(),ex);
//				}else{
//					logger.error(session+"session句柄异常:ID"+session.getId());
//				}
				if(session instanceof NioSession){
					World.getInstance().logout((NioSession)session,session+"session句柄IO异常:ID"+session.getId());
				}
				logger.error(session+"句柄异常,关闭session...");
				session.close(false);
			}
//			logger.error("session句柄IO异常:ID"+session.getId(),ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.common.IoHandler#messageReceived(org.apache.mina.common
	 * .IoSession, java.lang.Object)
	 */
	public void messageReceived(IoSession _ioSession, Object message) throws Exception {
		try {
			DataPackEntry dpe = (DataPackEntry) message;
			if(dpe!=null){
//				logger.info(_ioSession+"收到一条处理命令");
				MsgInAndOutThread.getInstance().receive(dpe);
			}
		} catch (Exception e) {
			logger.error(_ioSession+"接收信息出现异常:",e);
			logger.error(_ioSession+"接收信息出现异常,关闭session...");
			_ioSession.close(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.common.IoHandler#sessionClosed(org.apache.mina.common
	 * .IoSession)
	 */
	public void sessionClosed(IoSession session) throws Exception {
		try {
			logger.info("将session的所有清理操作放在此处执行...");
			if(session instanceof NioSession){
				World.getInstance().logout((NioSession)session,session+"客户端主动关闭session");
			}
			logger.error(session+"客户端关闭,关闭session...");
			session.close(false);
		} catch (Exception e) {
			logger.error("关闭连接时出现异常:",e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.common.IoHandler#sessionCreated(org.apache.mina.common
	 * .IoSession)
	 */
	public void sessionCreated(IoSession session) throws Exception {
		try {
			logger.info(session+"创建一个session连接ID="+session.getId());
			session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 180*10);
			session.setAttribute("sessionIndex",MsgInAndOutThread.getInstance().getSessionIndex());
			session.setAttribute("sendSID",0);
			if(ServerEntrance.isStopped()){
				logger.error(session+"服务器停止,关闭session...");
				session.close(false);
			}
//			session.setAttribute("isSendKey", false);
			//移至发送第一条数据之前
//			if(session instanceof NioSession){
//				RegisterCP.getInstance().changeEncryptKey((NioSession)session);
//			}
		} catch (Exception e) {
			logger.error("创建连接时触发异常:",e);
			logger.error(session+"创建连接出现异常,关闭session...");
			session.close(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.common.IoHandler#sessionIdle(org.apache.mina.common.IoSession
	 * , org.apache.mina.common.IdleStatus)
	 */
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		try {
			logger.info("请求关闭一个连接ID="+session.getId());
			if(session instanceof NioSession){
				World.getInstance().logout((NioSession)session,"session空闲超时");
			}
			logger.error(session+"空闲超时,关闭session...");
			session.close(false);
		} catch (Exception e) {
		    logger.error("请求关闭一个连接出现异常:",e);
		}	
	}
	
	/**
	 * @author liuzg
	 * 当消息被远程接收时触发
	 */
	public void messageSent(IoSession session, Object message) throws Exception {
		 try {
//			if(World.players.size()<10){
//			byte[] bytes=(byte[])message;
//			byte[] data=new byte[bytes.length-1];
//			System.arraycopy(bytes,1,data,0,data.length);
//			if(session.getAttribute("keyIndex")!=null){
//				int index=(Integer)session.getAttribute("keyIndex");
//				data=EncryptUtil.decrypt(data,EncryptUtil.getEncryptKey(index),bytes[0]);
//			}
//			int cmd=Bits.getInt(data,0);			
//			logger.info("服务器信息已被接收:CMD=0x"+Integer.toHexString(cmd)+",ID="+session.getId());
//			}
		} catch (Exception e) {
			logger.error("远程信息被接收时触发异常:",e);
		}
    }
}
