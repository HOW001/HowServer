package server.netserver;



import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import org.apache.log4j.Logger;


/**
 * 
 * @文件 SocketIoHandler.java
 * @描述 ：处理HTTP请求
 */

public class HttpIoHandler extends IoHandlerAdapter {
	private static Logger logger=Logger.getLogger(HttpIoHandler.class);
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoHandler#exceptionCaught(org.apache.mina.common.IoSession,
     *      java.lang.Throwable)
     */
    public void exceptionCaught (IoSession session, Throwable ex) throws Exception {
    	logger.error("请求关闭session连接201208161321:",ex);
    	logger.error(session+"Http出现异常,关闭session...");
        session.close(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoHandler#messageReceived(org.apache.mina.common.IoSession,
     *      java.lang.Object)
     */
    public void messageReceived (IoSession _ioSession, Object message) throws Exception {
    	DataPackEntry request = (DataPackEntry) message;
    	if(request!=null){
			logger.info("HTTP收到信息长度:"+request.getData().length+"字节!");
			MsgInAndOutThread.getInstance().receive(request);
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoHandler#sessionCreated(org.apache.mina.common.IoSession)
     */
    public void sessionCreated (IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 180);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoHandler#sessionIdle(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.IdleStatus)
     */
    public void sessionIdle (IoSession session, IdleStatus arg1) throws Exception {
    	logger.error("因空闲请求关闭session连接20120816132101");
    	logger.error(session+"Http空闲超时,关闭session...");
        session.close(false);
    }

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		super.messageSent(session, message);
		session.close(false);
	}
    
    
}
