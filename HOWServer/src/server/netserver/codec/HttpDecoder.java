package server.netserver.codec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.transport.socket.nio.NioSession;

import server.netserver.DataPackEntry;
import util.Bits;
import util.ByteArray;
/*
 * (non-Javadoc)
 * 
 * @see
 * org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache
 * .mina.common.IoSession, org.apache.mina.common.IoBuffer,
 * org.apache.mina.filter.codec.ProtocolDecoderOutput)
 */

/*
 * (non-Javadoc)
 * 
 * @see
 * org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache
 * .mina.common.IoSession, org.apache.mina.common.IoBuffer,
 * org.apache.mina.filter.codec.ProtocolDecoderOutput)
 */

/**
 * @author liuzg HTTP上行报文解析
 * http://192.168.2.18:9001/game.jsp?cmd=10001
 */
public class HttpDecoder extends CumulativeProtocolDecoder {
	private static Logger logger = Logger.getLogger(HttpDecoder.class);
	private static final CharsetDecoder decoder = Charset.forName("utf-8")
			.newDecoder();
	private static final String LASTDATA="HttpDecoderLastData";
	private static int count=0;
	private static int receiveCount=0;
	private static int notHeadCount=0;
	protected boolean doDecode(IoSession session, IoBuffer buffer,
			ProtocolDecoderOutput out) throws Exception {
		receiveCount++;
		logger.info(session+"请求一次连接HttpDecoder.doDecode()...");
		int length=buffer.remaining();
		
		byte[] requestBytes=new byte[length];
		buffer.get(requestBytes);
		
		byte[] lastLoseData=null;
		if(session.getAttribute(LASTDATA)!=null){
			lastLoseData=(byte[])session.getAttribute(LASTDATA);
			if(lastLoseData.length>0){// 说明上次有遗留数据
				logger.info(session+"检测到有上一次遗留数据未处理!");
				length+=lastLoseData.length;
				byte temp[] =new byte[length];
				System.arraycopy(lastLoseData, 0, temp, 0, lastLoseData.length);
				System.arraycopy(requestBytes, 0, temp, lastLoseData.length, requestBytes.length);
				requestBytes=temp;
				lastLoseData=new byte[0];
				session.setAttribute(LASTDATA, lastLoseData);
			}
		}
		
		
  		IoBuffer in=IoBuffer.allocate(length);
		in.put(requestBytes);
		in.flip();
		String request = in.getString(decoder);	
		logger.info("收到数据长度:"+length);
		if(request.length()<=0){
			notHeadCount++;
			logger.info("收到不包含文件头的数据...");
			return processData(session,out,requestBytes);
		}
		//http://127.0.0.1:9001/test.lzg?command=0x10001&msg=Hello,World
		final String CRLF = "\r\n";
		int index = request.indexOf(CRLF + CRLF);
		try {
			if (index >= 0) {
				String strHead = request.substring(0, index);
				// GET /test?a=1&b=2 HTTP/1.1
				// Accept: */*
				// Accept-Language: zh-cn
				// Accept-Encoding: gzip, deflate
				// User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT
				// 5.1; SV1; InfoPath.2; CIBA; .NET CLR 2.0.50727)
				// Host: 127.0.0.1:51000
				// Connection: Keep-Alive
				int getPos = strHead.lastIndexOf("GET");// GET的位置
				String page = "Find Not";
				if (getPos >= 0) {// 处理GET方式
					count++;
					String tempStr = strHead.substring(getPos + 4);
					int pagramesPos = tempStr.indexOf(" ");
					tempStr = tempStr.substring(0, pagramesPos);
					tempStr = tempStr.replaceAll("/", "");
					page=tempStr;
					int questionPos = tempStr.indexOf("?");
					Map<String,String> requestMaps=new HashMap<String,String>();
					if (questionPos >= 0) {
						page = tempStr.substring(0, questionPos);
						tempStr = tempStr.substring(questionPos + 1);
						String params[] = tempStr.split("&");
						
						for (String param : params) {
							if (param.split("=").length < 2) {
									continue;
								}
								requestMaps.put( param.split("=")[0],  param.split("=")[1]);
							}
					}
					
//					WebOptionCP.getInstance().parse(session, page, requestMaps);
					logger.info(session+"本服务不对Get方式进行处理...");
					session.close(true);
					return false;
				} else {// 处理POST方式
					getPos = strHead.lastIndexOf("POST");// POST的位置
					page = "Find Not";
					logger.info(session+"请求一次POST连接");
					if (getPos >= 0) {
						String tempStr = strHead.substring(getPos + 4).trim();
						int pagramesPos = tempStr.indexOf(" ");
						tempStr = tempStr.substring(0, pagramesPos);
						page = tempStr.replaceAll("/", "");
						// POST /testxml.wjol HTTP/1.1
						// Cache-Control: no-cache
						// Pragma: no-cache
						// User-Agent: Java/1.6.0_01
						// Host: 172.16.1.31:6566
						// Accept: text/html, image/gif, image/jpeg, *; q=.2,
						// */*; q=.2
						// Connection: keep-alive
						// Content-type: application/x-www-form-urlencoded
						// Content-Length: 64
						//
						int postHeadIndex = request.lastIndexOf(CRLF)+2;//最后一个换行符之后的数据才是data部分
						byte[] postData=new byte[requestBytes.length-postHeadIndex];
						if(postData.length==0){
							//说明此条信息只包含文件头
							return false;
						}
						System.arraycopy(requestBytes, postHeadIndex, postData, 0, postData.length);
						return processData(session, out, postData);				
					}
				}
			}
		} catch (Exception e) {
			logger.error("http请求出现异常:", e);
			return false;
		}
		return false;
	}
    /**
     * @author liuzhigang
     * @param session
     * @param out
     * @param postData
     * @return
     * 处理有效数据
     */
	private boolean processData(IoSession session, ProtocolDecoderOutput out,
			byte[] postData) {
		try {
			ByteArray ba=new ByteArray(postData);
//			int validValue=ba.readInt();
//			if(validValue!=SocketDecoder.VALIDVALUE){
//				logger.info("收到验证证数据非法");
//				return true;
//			}
			int len=ba.readInt();
			logger.info("收到数据长度:"+postData.length+",需要数据长度:"+len);
			if(len<=0){
				logger.info(session+"发送的数据长度不符合最低长度要求,len="+len);
				return true;
			}
			if(len>=1024*1024*3){
				logger.error(session+"请求数据过长,len="+len);
				return true;
			}
            if(len+4!=postData.length){
				session.setAttribute(LASTDATA, postData);
            	logger.error(session+"数据尚接收完整,需要放入缓存!len="+len+",postData="+postData.length);
            	return false;
            }else{
            	//测试使用
            	byte[] lastLoseData=new byte[]{1,2,3,4};
				session.setAttribute(LASTDATA, lastLoseData);
            }
			int cmd=ba.readInt();//命令
			byte[] realData=new byte[len-4];//除去长度+命令cmd之外的包数据
			System.arraycopy(postData, 8, realData, 0, realData.length);
			logger.info(session+"Http Post模式收到命令请求:cmd=0x"+Integer.toHexString(cmd));
			DataPackEntry cds = new DataPackEntry((NioSession) session,DataPackEntry.CONN_TYPE_HTTP,cmd, realData);
			out.write(cds);
			count++;
			logger.info("成功处理第"+count+"条命令,共收到请求:"+receiveCount+",没有协议头的信息:"+notHeadCount);
			return false;
		} catch (Exception e) {
			logger.error("HttpDecoder在处理接收数据时出现异常:",e);
			return true;
		}
	}	
}
