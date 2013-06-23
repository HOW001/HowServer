package server;

import java.net.*;

import org.apache.log4j.Logger;

import util.ServerMonitor;
import world.World;

/**
 * 关闭服务器
 * 
 * 更新关闭服务器的方法
 * 除了GM工具之外
 * 还可以通过在服务器本地telnet localhost 游戏端口+200来关闭服务器
 *
 */
public class ShutDownServer {
	private static Logger logger=Logger.getLogger(ShutDownServer.class);
	private static int PORT_SHUTDOWN = 8001;
	private static final String LOCAL_ADDRESS = "127.0.0.1";
	private static final String SHUTDOWN_ADDRESS = "192.168.2.18";
	
	ServerSocket shutdownServer;
	public static void startShutdownServer(){
		try{
			new ShutDownServer().start(); 
		} catch(Exception e){
			logger.error("启动关服监听异常:",e);
		}
	}
	public ShutDownServer() throws Exception{
		PORT_SHUTDOWN = ServerEntrance.port + 200;
		shutdownServer = new ServerSocket(PORT_SHUTDOWN);
	}
	
	private Socket connectSocket;
	public void start(){
		while(World.running()){
			try{
				connectSocket = shutdownServer.accept();
				if(connectSocket != null){
					String ip = connectSocket.getLocalAddress().getHostAddress();
					if(LOCAL_ADDRESS.equalsIgnoreCase(ip)){
						shutdown();//关闭服务器
					} else if(SHUTDOWN_ADDRESS.equalsIgnoreCase(ip)){
						shutdown();
					}
				}
				connectSocket.close();
				ServerMonitor.getMonitor().CheckDeadLocks();
			} catch(Exception e){
				logger.error("关闭服务器异常:",e);
			}
		}
	}
	
	private void shutdown(){
		ServerEntrance.shutdown("管理员主动关闭游戏");
	}
}
