package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestZip {
     public static void main(String str[]){
    	 try {
    	    System.out.println("内存使用前:"+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)+"KB");
			write();
			System.out.println("内存使用后"+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)+"KB");
//			read();
//			System.out.println("解压后内存使用后"+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)+"KB");
			System.gc();
			Thread.sleep(1000*60);
			System.out.println("1分钟后内存使用后"+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)+"KB");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
     }
     public static void read(){
    	 try {
			     File file=new File("F:/test.zip");
				 FileInputStream fis=new FileInputStream(file);
				 DataInputStream dis=new DataInputStream(fis);
				 byte [] bytes=new byte[dis.available()];
				 System.out.println("解压之前长度:"+bytes.length+"字节!");
				 dis.read(bytes);
				 byte [] unzipBytes=ZipUtil.decompress(bytes);
				 System.out.println("解压之后长度:"+unzipBytes.length+"字节!压缩率"+(100-(bytes.length*100/unzipBytes.length))+"%");
				 FileOutputStream fos=new FileOutputStream("F:/test1.doc");
	 			 DataOutputStream dos=new DataOutputStream(fos);
	 			 dos.write(unzipBytes);
	 			 dos.flush();
	 			 dos.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
     }
     public static void write(){
    	 try {
 			File file=new File("F:/test.doc");
 			 FileInputStream fis=new FileInputStream(file);
 			 DataInputStream dis=new DataInputStream(fis);
 			 byte [] bytes=new byte[dis.available()];
 			 dis.read(bytes);
 			 dis.close();
 			 fis.close();
 			 System.out.println("压缩之前长度："+bytes.length+"字节!");
 			 byte [] zipBytes=ZipUtil.compress(bytes);
 			 System.out.println("压缩之后长度:"+zipBytes.length+"字节!压缩率"+(100-(zipBytes.length*100/bytes.length))+"%");
 			 FileOutputStream fos=new FileOutputStream("F:/test.zip");
 			 DataOutputStream dos=new DataOutputStream(fos);
 			 dos.write(zipBytes);
 			 dos.flush();
 			 dos.close();
 		} catch (FileNotFoundException e) {
 			
 			e.printStackTrace();
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		}
     }
}
