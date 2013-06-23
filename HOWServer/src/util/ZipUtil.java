package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
/**
 * @author lzg------2010-11-10
 * 压缩工具
 *
 */
public final class ZipUtil {
	private static Logger logger=Logger.getLogger(ZipUtil.class);
	/**
	 * @author lzg------2010-11-10
	 * @param data
	 * @return
	 * @throws IOException
	 * 默认解压格式入口
	 */
	public static byte[] decompress( byte[] data ){
		try {
			return decompress(data,GZipInputStream.TYPE_DEFLATE);
		} catch (Exception e) {
			logger.error("解压缩异常:",e);
			return null;
		}
	}
	/**
	 * @author lzg------2010-11-10
	 * @param data
	 * @param compressionType
	 * @return
	 * @throws IOException
	 * 解压
	 */
	private static byte[] decompress( byte[] data , int compressionType) throws IOException{
		byte[] tmp=new byte[1024];
		int read;
		GZipInputStream zipInputStream = new GZipInputStream(new ByteArrayInputStream( data ) ,1024 ,compressionType,true);
		ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
		while ( (read=zipInputStream.read(tmp, 0, 1024))>0 ){
			bout.write(tmp,0,read);
		}
//		tmp=bout.toByteArray();
//		bout.close();
		zipInputStream.close();
//		bout=null;
		zipInputStream=null;
		return bout.toByteArray();
	}
	/**
	 * @author lzg------2010-11-10
	 * @param data
	 * @return
	 * @throws IOException
	 * 默认压缩格式入口
	 */
	public static byte[] compress( byte[] data ){
		try {
			return compress( data, GZipOutputStream.TYPE_GZIP );
		} catch (Exception e) {
			logger.error("压缩异常:",e);
			return null;
		}
	}
	/**
	 * @author lzg------2010-11-10
	 * @param data
	 * @param compressionType
	 * @return
	 * @throws IOException
	 * 压缩，最大压缩缓存空间为32K
	 */
	private static byte[] compress( byte[] data, int compressionType ) throws IOException{
		if (data.length > 32768){
			return compress(data,compressionType, 32768, 32768);			
		} else{
			return compress(data,compressionType, data.length, data.length);
			
		}
	}
	/**
	 * @author lzg------2010-11-10
	 * @param data
	 * @param compressionType
	 * @param plainWindowSize
	 * @param huffmanWindowSize
	 * @return
	 * @throws IOException
	 * 压缩
	 */
	private static byte[] compress( byte[] data , int compressionType,int plainWindowSize, int huffmanWindowSize) throws IOException{
		ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
		GZipOutputStream zipOutputStream = new GZipOutputStream(bout, 1024, compressionType, plainWindowSize, huffmanWindowSize);	
		zipOutputStream.write(data);
//		byte[] temp=bout.toByteArray();
		zipOutputStream.close();
//		bout.close();
		zipOutputStream=null;
//		bout=null;
		return bout.toByteArray();
	}
}
