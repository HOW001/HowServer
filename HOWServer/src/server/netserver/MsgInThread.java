package server.netserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

import util.Util;
import world.World;


/**
 * 
 * @author Administrator 启动接收线程
 */
public class MsgInThread implements Runnable {
	private static Logger logger = Logger.getLogger(MsgInThread.class);
	private BlockingQueue<DataPackEntry> queue = new LinkedBlockingQueue<DataPackEntry>();
	private static boolean exit = false;

	public boolean receive(DataPackEntry dpe) {
		return queue.add(dpe);
	}
	private static long msgCount=0;
	private static long recordeTime=0;
//	private static long sendCount=0;
    private static long lastPrintTime=0;
	public void run() {
		logger.info("开始启动接收服务.....");
		while (!exit) {
			try {
				DataPackEntry dpe = queue.take();
				if (dpe == null) {
					continue;
				}
				World.getInstance().receiveCommand++;
				World.getInstance().size_receive+=dpe.getData().length;
				MsgInParse.getInstance().process(dpe);
				if(System.currentTimeMillis()-lastPrintTime>Util.ONE_MIN){
					lastPrintTime=System.currentTimeMillis();
					logger.info("信息接收线程正常运行中...");
				}
				msgCount++;
				if(System.currentTimeMillis()-recordeTime>Util.ONE_MIN){
					logger.info("服务器每分钟接收消息:"+msgCount+"条");
					msgCount=0;
					recordeTime=System.currentTimeMillis();
				}
			} catch (Exception e) {
				logger.error("信息接收线程出现异常 ", e);
			}
		}
	}

	public static void exit() {
		exit = false;
	}
	public static boolean isExit(){
		return exit;
	}
}
