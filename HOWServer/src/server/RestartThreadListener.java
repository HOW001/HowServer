package server;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import world.World;

import org.apache.log4j.Logger;

public class RestartThreadListener {
	public static final int TYPE_RESTART_CMDTHREAD=1;//重启命令执行线程
	private static Logger logger=Logger.getLogger(RestartThreadListener.class);
	public static void init(){
		logger.info("启动重启线程监听!");
		Runnable run=new Runnable(){
			public  void run(){
				start();
			}
		};
		ServerEntrance.runThread(run);
	}
	public static  void start() {
		try {
			ServerSocket serverSocket = new ServerSocket(38246);
			while (World.running()) {
				try {
					// 新建一个连接
					Socket socket = serverSocket.accept();
					logger.info("重启线程监听连接成功......");
					InputStream in=socket.getInputStream();
					int dataLen=in.available();
					byte[] data=new byte[dataLen];
					in.read(data);
					if(data.length>0){
						int type=data[0];
						World.getInstance().restartThread(type);
					}
                    socket.close();
				} catch (Exception e) {
					logger.error("重启线程监听出现异常！" + e);
				}
			}
		} catch (IOException e) {
			logger.error("重启线程监听出现异常！" + e);
		}
	}
}
