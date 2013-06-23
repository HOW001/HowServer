package server;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.core.session.IoSession;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import server.cmds.ChatCP;
import server.cmds.CmdDispatch;
import server.netserver.GameServerPortListener;
import server.netserver.thread.ThreadPool;
import util.SMTPSender;
import util.ServerMonitor;
import util.Util;
import util.binreader.PReader;
import util.logger.FatalLogger;
import world.GameStaticValues;
import world.World;
import db.model.Player;
import db.service.IDManager;
import db.service.ManagerDBUpdate;
import db.service.HibernateUtil;

public class ServerEntrance {
	private static boolean isDebug = false;
	private static Logger logger = Logger.getLogger(ServerEntrance.class);
	public static int port;
	public static ThreadPool threadPool;// 线程池
	private static CmdDispatch cmdDispatch;
	public static long gameStartTime = 0;
	public static String HostIP;
	public ServerEntrance() {
	}
	private static final int[] TIMES = { 1000,// 0
			35000,// 1
			65000,// 2
			95000,// 3
			125000,// 4
			150000,// 5
			160000,// 6
	};
	/**
	 * 打印服务器构建时间
	 */
	private String getBuildTime(String jarname) {
		try {
			JarFile jarFile = new JarFile(jarname);
			Manifest mf = jarFile.getManifest();
			return mf.getMainAttributes().getValue("tstamp");
		} catch (Exception e) {
			return "not found";
		}
	}
	public static String serverPath="";
	public static void main(String args[]) {
		try {
			if (args != null && args.length > 0) {
				serverPath=args[0];
			}
		} catch (Exception e) {
			logger.error("Server Error 1", e);
		}
		if(serverPath.length()>0){
			  System.out.println("serverBasicPath:"+serverPath);
		}
		logger.info("读取log4j配置文件...");
		try {
			String fileName = serverPath+"res/log4j.properties";
			File file = new File(fileName);
			if (!file.exists()) {
				throw new NullPointerException("can not find file --> "
						+ fileName);
			}
			PropertyConfigurator.configure(fileName);
		} catch (Exception e) {
			logger.error("Load log4j prop file error", e);
			System.exit(0);
		}
		logger.info("log4j配置文件读取完毕！");
		logger.info("读取server.xml配置文件...");
		try {
			loadServerConfigurationNew();
		} catch (Exception e) {
			logger.error("Server Error 2", e);
			System.exit(0);
		}
		logger.info("server.xml配置文件读取完毕！");
		logger.info("服务器开始启动...");
		try {
			ServerEntrance server = new ServerEntrance();
			server.start();
			ServerExceptionProcess.startShutdownServer();
			Thread t = new Thread() {
				public void run() {
					try {
						ServerEntrance.shutdown("守护线程结束关闭游戏");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			Runtime.getRuntime().addShutdownHook(t);
			ShutDownServer.startShutdownServer();// 运行关机服务器
			
		} catch (Exception e) {
			logger.error("Server Error 3", e);
		}
	}
	public static void loadServerConfigurationNew(){
		try {
			logger.info("开始加载一次配置文件"+serverPath+"res/server.xml...");
			File file=new File(serverPath+"res/server.xml" );
			FileInputStream fis=new FileInputStream(file);
			SAXReader reader = new SAXReader();
			Document dom = reader.read(fis);
			Element data = dom.getRootElement();
			Iterator it = data.elementIterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				logger.info("配置文件数据:name="+e.getName()+",value="+e.getStringValue());
				ServerConfigurationNew.setValue(e.getName(),  e.getStringValue());
			}
			port = ServerConfigurationNew.port;
			isDebug = ServerConfigurationNew.debug;
			World.setMaxPlayer(ServerConfigurationNew.players);
			// 邮件系统配置
			if (ServerConfigurationNew.mailEnable) {
				String hostStr = "smtp.163.com";
				try {
					hostStr = ServerConfigurationNew.mailHost;
				} catch (Exception e) {
					hostStr = "smtp.163.com";
				}
				String fromStr = "rekoo002@163.com";
				try {
					fromStr = ServerConfigurationNew.subgame+ "@" + hostStr;
				} catch (Exception e) {
					fromStr = "rekoo002@163.com";
				}
				String mailuser = "";
				String mailpassword = "";
				try {
					mailuser = ServerConfigurationNew.mailUser;
				} catch (Exception e) {
					mailuser = "";
				}
				try {
					mailpassword = ServerConfigurationNew.mailPassword;
				} catch (Exception e) {
					mailpassword = "";
				}
				SMTPSender.setEnable(true);
				SMTPSender.doConfig(hostStr, fromStr);
				SMTPSender.doConfigUserPassword(mailuser, mailpassword);
				try {
					SMTPSender.addMailList(ServerConfigurationNew.mailList);
				} catch (Exception e) {
					System.err.println();
				}
			} else {
				SMTPSender.setEnable(false);
			}
			logger.info("完成加载配置文件server.xml...");
		} catch (Exception e) {
			logger.error("读取配置文件时出现异常!",e);
		}
	}
	/**
	 * 标记是否是Debug版服务器 现在配置如下：
	 * 
	 * 开发服务器返回true，包含长期更新内容 先锋服务器和正式服务器返回false，包含下周更新内容
	 * 
	 * @return
	 */
	public static boolean ISDEBUG() {
		return isDebug;
	}
	/**
	 * 启动游戏服务器
	 * 
	 */
	public final void start() {
		try {
			gameStartTime = System.currentTimeMillis();
			String time = getBuildTime("carServer.jar");
			threadPool=ThreadPool.getInstance();
			threadPool.start();// 启动线程池
			logger.info("线程池启动完成init thread pool successfully.");
			initNetDependence();
			GameServerPortListener.init(port);
			
			logger.info("启动网络服务端口net serve started successfully,port:" + port);
			FatalLogger.log( "buildTime:" + time);
			PReader.getInstance().init();
			logger.info("初始化数据库信息");
			initDB();
			ManagerDBUpdate.initPlayerDB();
			IDManager.getInstance().initID();
			World.getInstance().init();
			logger.info("初始化游戏世界init world.");
			InetAddress addres = InetAddress.getLocalHost();
			HostIP = addres.getHostAddress();
			RestartThreadListener.init();
			logger.info("初始化商品信息数据");
			logger.info("===============服务器启动完毕server start completed.===============");
		} catch (Exception e) {
			logger.error("start error" ,e);
			System.exit(1);
		}
	}
	/**
	 * 加载数据库相关
	 * 
	 */
	private void initDB() {
		/*--------------------- 加载数据库配置 -----------------------------*/
		HibernateUtil.loadHibernate();
		logger.info("数据库初始化process db Init.");	
	}
	private static int shutDonwGuage = 0;// 关闭进度
	private static boolean stopped = false;
	private static boolean compleShutDown = false;// 是否关闭完成,与stopped区别是关闭完成前30秒设为true
	public static boolean isCompleShutDown() {
		return compleShutDown;
	}
	/**
	 * 服务器是否已经处在关闭状态
	 * 
	 * @return
	 */
	public static boolean isStopped() {
		return stopped;
	}
	public static int getShutDownGuage() {
		return shutDonwGuage;
	}
	private static int currentStep = 0;
	/**
	 * 
	 * 停止游戏服务器的调用方法 该方法中会对游戏中的玩家进行退出前的保存 以及各个功能模块的清理工作，关闭网络等
	 * @param desc 关闭原因
	 * 
	 */
	public static void shutdown(final String desc) {
		if (stopped) {
			return;
		} else {
			FatalLogger.log("收到服务器关闭命令，开始尝试关闭服务器");
			stopped = true;
		}
		final long shutDownTime = System.currentTimeMillis();
		Thread t = new Thread() {
			public void run() {
				while (World.running()) {
					long time = System.currentTimeMillis() - shutDownTime;
					if (time > TIMES[currentStep] && currentStep == 5) {
						logger.info("收到关闭服务器命令,关闭原因:"+desc);
						logger.info("开始关闭服务器...");
						try {
							logger.info("关闭Socket和Http网络线程...");
							GameServerPortListener.stop();
							for(Player player:World.players.values()){
								player.getIoSession().close(false);
							}
							for(Player player:World.bufferPlayers.values()){
								player.getIoSession().close(false);
							}
						} catch (Exception e) {							
							logger.error("关闭Socket和Http网络线程出现异常，", e);
						}
					
						try {
							logger.info("游戏世界停止运行");
							World.stop();
						} catch (Exception e) {
							logger.error("关闭游戏世界出现异常,", e);
						}
						try {
							logger.info("关闭聊天系统");
							ChatCP.getInstance().exit();
						} catch (Exception e) {
							logger.error("关闭聊天系统异常,", e);
						}
						try {
							logger.info("强制发送邮件");
							SMTPSender.sendMailNow();
						} catch (Exception e) {
							logger.error("强制发送邮件", e);
						}
						shutDonwGuage = 90;
						// 等待存储结束
						try {
							while (true) {
								boolean isLock=ManagerDBUpdate.getInstance().isLock;
								if (isLock==false) {
									break;
								}
								logger.info("DB正在保存中,等待..." + isLock);
								Thread.sleep(10000);
							}
							logger.info("关机时,开始保存所有玩家信息...");
							ManagerDBUpdate.getInstance().saveDBInfo(true);
							logger.info("关机时,完成保存所有玩家信息...");
							logger.info("关机时,开始保存持久化信息...");
							GameStaticValues.writeStaticValues();
							logger.info("关机时,完成保存持久化信息...");
							logger.info("系统等待10秒...");
							Thread.sleep(10000);
						} catch (Exception e) {
							logger.error("关闭服务器异常:", e);
						}
						ServerMonitor.getMonitor().CheckDeadLocks();
						try {
							logger.info("关闭线程池");
							threadPool.shutdown();
						} catch (Exception e) {
							logger.error("关闭线程池出现异常", e);
						}
						shutDonwGuage = 100;
						FatalLogger
						.log("关闭用时:"+((System.currentTimeMillis()-shutDownTime)/1000)+"秒!");
						FatalLogger
								.log("===============服务器关闭完成===============");
						System.exit(0);
					} else if (time > TIMES[currentStep] && currentStep == 4) {
						shutDonwGuage = 75;
						World.announce("服务器即将关闭");
						currentStep++;
						compleShutDown = true;
					} else if (time > TIMES[currentStep] && currentStep == 3) {
						shutDonwGuage = 60;
						World.announce("服务器将在30秒内关闭，请及时退出游戏，避免不必要的损失");
						currentStep++;
					} else if (time > TIMES[currentStep] && currentStep == 2) {
						shutDonwGuage = 45;
						World.announce("服务器将在60秒内关闭，请及时退出游戏，避免不必要的损失");
						currentStep++;
					} else if (time > TIMES[currentStep] && currentStep == 1) {
						shutDonwGuage = 30;
						World.announce("服务器将在90秒内关闭，请及时退出游戏，避免不必要的损失");
						currentStep++;
					} else if (time > TIMES[currentStep] && currentStep == 0) {
						shutDonwGuage = 15;
						currentStep++;
						World.announce("服务器将在2分钟内关闭，请及时退出游戏，避免不必要的损失");
					}
				}
			}
		};
		currentStep = 0;
		t.start();
	}
	public static CmdDispatch getCmdDispatch() {
		return cmdDispatch;
	}
	private void initNetDependence() {
		cmdDispatch = CmdDispatch.getInstance();
	}
	public static boolean stopSaving = false;
	public static void exceptionHibernate() {
		logger.fatal("重置保存队列的数据");
		stopSaving = true;
		logger.info("resave DBManager in exceptionServer");
		ManagerDBUpdate.getInstance().saveDBInfo(true);
		logger.fatal("设置hibernate为开启状态");

	}
	public static void runThread(Runnable r) {
		if (r == null) {
			return;
		}
		if (threadPool != null) {
			logger.info("启动线程:" + r.getClass().getName());
			threadPool.run(r);
		}
	}
	/**
	 * @author liuzg
	 * @param session
	 * @param params
	 * @return
	 * 通过Http请求关闭服务器
	 */
    public static String requestShutDownServerFromHttp(IoSession session,String[] params){
    	try {
			logger.info(session.getRemoteAddress()+"通过Http请求关闭游戏服务器...");
		} catch (Exception e) {
			logger.error("获取行程地址失败:",e);
		}
    	String userName="";
    	String password="";
    	for(String value:params){
    		String[] values=value.split("=");
    		if(values.length!=2){
    			continue;
    		}
    		if(values[0].equals("userName")){
    			userName=values[1];
    		}
    		if(values[0].equals("password")){
    			password=values[1];
    		}
    	}
    	if(userName.equals("liuzg0532")==false){
    		logger.info("请求关闭服务器时用户名错误:"+userName);
    		return "请求关闭服务器时用户名错误:"+userName;
    	}
    	if(password.equals(ServerConfigurationNew.id+"liuzg"+Util.getCurrentDate())==false){
    		logger.info("请求关闭服务器时密码错误:"+password);
    		return "请求关闭服务器时密码错误:"+password;
    	}
    	logger.info("userName="+userName+",password="+password+"请求关闭服务器...");
    	logger.info("开始关闭游戏服务器...");
    	if(stopped){
    		return "操作失败,因为服务器已关闭!";
    	}else{
    		shutdown("通过Http请求关闭服务器");
    		return "操作成功,服务器正在关闭!";
    	}
    }
}
