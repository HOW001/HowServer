
package server.netserver.codec;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import server.cmds.FightCP;
import server.cmds.RegisterCP;
import server.netserver.MsgOutThread;
import util.Bits;

public class SocketEncoder extends ProtocolEncoderAdapter {
	private static Logger logger=Logger.getLogger(SocketEncoder.class);
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.common.IoSession,
     *      java.lang.Object,
     *      org.apache.mina.filter.codec.ProtocolEncoderOutput)
     */
    public void encode (IoSession session, Object message,
            ProtocolEncoderOutput out) throws Exception {
    	if(MsgOutThread.isExit()){
    		logger.error("网络发送线程已关闭...");
    		logger.error(session+"网络发送线程已关闭,关闭session...");
    		session.close(false);
    		return;
    	}
        byte [] sendData=(byte[])message;
        int cmd=Bits.getInt(sendData, 0);
        int capacity = sendData.length+4;
        IoBuffer buffer = IoBuffer.allocate(capacity, false);
//        if (cmd==FightCP.getInstance().getCmd(FightCP.FIGHT_START)||cmd == RegisterCP.getInstance().getCmd(
//				RegisterCP.CHANGE_ENCRYPT_KEY)
//				|| (cmd >= 0x160001 && cmd <= 160099)) {// 获取密钥和GM工具命令无需加密
//        	 buffer.putInt(SocketDecoder.VALIDVALUE_LINK);
//        	 logger.debug("无需加密的命令:0x"+Integer.toHexString(cmd));
//        	 if(cmd!=RegisterCP.getInstance().getCmd(RegisterCP.CHANGE_ENCRYPT_KEY)&&cmd!=FightCP.getInstance().getCmd(FightCP.FIGHT_START)){
//        		 logger.error("错误的命令或能是因为加密产生与GM命令相同的命令:"+Integer.toHexString(cmd));
//        	 }
//        }else{
//        	 buffer.putInt(SocketDecoder.VALIDVALUE);       	
//        }
        logger.info("发送数据长度:len="+capacity+",cmd=0x"+Integer.toHexString(cmd));
        buffer.putInt(capacity);//数据长度包括int自身
        buffer.put(sendData);
        buffer.flip();
        out.write(buffer);
    }
}
