
package server.netserver.codec;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import util.ByteArray;
/**
 * 
 * @author liuzg
 * 服务器响应http的封装
 */
public class HttpEncoder extends ProtocolEncoderAdapter {
	private static Logger logger=Logger.getLogger(HttpEncoder.class);
	public static boolean isRunning=true;
	private static final String CRLF = "\r\n";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.common
	 * .IoSession, java.lang.Object,
	 * org.apache.mina.filter.codec.ProtocolEncoderOutput)
	 */
	public void encode(IoSession _session, Object _message,
			ProtocolEncoderOutput _out) throws Exception {
		if(isRunning==false){
			logger.error("Http已关闭!");
			return;
		}
		byte[] data=(byte[])_message;
		int capacity=0;
		Object obj=_session.getAttribute("isGet");
		if(obj!=null && obj.toString().equals("true")){
			//Get方式没有数据长度属性
			data=sendHttpData(data);
			capacity = data.length;
			IoBuffer buffer = IoBuffer.allocate(capacity, false);
			buffer.put(data);
			buffer.flip();
			_out.write(buffer);
		}else{
			capacity = data.length+4;
			IoBuffer buffer = IoBuffer.allocate(capacity, false);
			buffer.putInt(data.length);
			buffer.put(data);
			buffer.flip();
			_out.write(buffer);
		}
	}
    /**
     * @author liuzg
     * @param data
     * @return
     * 返回get请求,需要进行相应封装
     */
	private  byte[] sendHttpData(byte []data){
		String statusLine = "HTTP/1.1 200 OK" + CRLF;
		String serverLine = "Server: Liuzg Game Server" + CRLF;
		String contentTypeLine = "Content-type: text/xml;charset=utf-8" + CRLF;
		String contentLengthLine = "Content-Length: " + data.length + CRLF;
		ByteArray ba = new ByteArray();
		ba.writeByteArray(statusLine.getBytes());
		ba.writeByteArray(serverLine.getBytes());
		ba.writeByteArray(contentTypeLine.getBytes());
		ba.writeByteArray(contentLengthLine.getBytes());
		ba.writeByteArray(CRLF.getBytes());
		ba.writeByteArray(data);
		return ba.toArray();
	}
}
