package util.logger;

import org.apache.log4j.Logger;

/**
 * 
 * 服务器启动日志
 *
 */
public class FatalLogger {
	private static Logger logger = Logger.getLogger(FatalLogger.class);

	public static void log(String msg){
		logger.info(msg);
	}
	
	public static void error(String msg,Exception e){
		logger.error(msg,e);
	}
}
