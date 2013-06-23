package server.netserver;


import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.transport.socket.SocketAcceptor;
/**
 * 
 * @author liuzg
 *
 *  启动接收和发送线程的接口
 */
public abstract class GameServerPortListener {
    public static Logger logger=Logger.getLogger(GameServerPortListener.class);
    /**
     * 监听端口
     */
    protected int port;

    /**
     * 继承于mina的处理器
     */
    public SocketAcceptor acceptor;
    
    /*
     * Socket端口服务
     */
    public static final GameServerPortListener socket = new SocketServer();
    /*
     * Http端口服务
     */
    public static final GameServerPortListener http = new HttpServer();
    /**
     * 继承于mina的逻辑处理接口
     */
    protected IoHandler handler;
    
	public static void init(int port) {
		try {
			logger.info("启动游戏服务器监听,port=" + port);		
			socket.start(port);
			logger.info("启动http服务器监听,port=" + (9001));
			http.start(9001);
			MsgInAndOutThread.getInstance().start();
		} catch (Exception e) {
			logger.error("初始化游戏监听时出现异常:",e);
			System.exit(1);
		}
	}
	public abstract void start(int port) throws Exception;
	public static void stop(){
		MsgOutThread.exit();
		MsgInThread.exit();
	}
}
