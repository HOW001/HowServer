package server;

import java.net.*;
import org.apache.log4j.Logger;
//import server.netserver.ThreadPool;
import server.netserver.thread.ThreadPool;
import util.ServerMonitor;
import world.World;

/**
 * 服务器线程重启、数据库重启、保存重启
 *
 */
public class ServerExceptionProcess {
	private static Logger logger = Logger.getLogger(ServerExceptionProcess.class);
	private static int PORT_SHUTDOWN = 8912;
	private static final String LOCAL_ADDRESS = "127.0.0.1";
	ServerSocket shutdownServer;
	public static void startShutdownServer(){
		try{
			new ServerExceptionProcess().start(); 
		} catch(Exception e){
			logger.error("出现异常201208281343:",e);
		}
	}
	public ServerExceptionProcess() throws Exception{
		PORT_SHUTDOWN = ServerEntrance.port + 100;
		shutdownServer = new ServerSocket(PORT_SHUTDOWN);
	}	
	private Socket connectSocket;	
	int times;
	public void start(){
		Runnable r = new Runnable(){
			public void run(){
				while(World.running()){
					long timess = System.currentTimeMillis();
					try{
						connectSocket = shutdownServer.accept();
						if(connectSocket != null){
							logger.error(connectSocket.getLocalAddress().getHostAddress());
							if(LOCAL_ADDRESS.equalsIgnoreCase(connectSocket.getLocalAddress().getHostAddress())){
								action(times);
								times++;
							}
						}
						connectSocket.close();
						ServerMonitor.getMonitor().CheckDeadLocks();
					} catch(Exception e){
						logger.error("出现异常201208281344:",e);
					}
					long useTimes = System.currentTimeMillis() - timess;
					if(useTimes>=100){
						logger.error("ServerExceptionProcess.start()线程运行时间过长" + useTimes);
					}
				}
			}
		};
		ServerEntrance.runThread(r);
	}
	
	protected void action(int times){
		logger.fatal("ExceptionServer: level:" + times);
		switch(times){
		case 0:
			logger.fatal("restart threadPool");
			ServerEntrance.threadPool =ThreadPool.getNewInstance();// new ThreadPool();
			ServerEntrance.threadPool.start();
			break;
		case 1:
			logger.fatal("try to disconnect hibernate and relogin");
			ServerEntrance.exceptionHibernate();
			break;
		case 3:
			break;
		}
	}
}
