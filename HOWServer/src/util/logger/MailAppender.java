package util.logger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import server.ServerConfigurationNew;
import server.ServerEntrance;
import util.SMTPSender;

/**
 * 发送邮件的日志
 *
 */
public class MailAppender extends AppenderSkeleton {
	private static String DEFAULT_NAME = "SERVER_NAME";
	private String serverName;
	
	private boolean enabled = false;
	/**
	 * 
	 */
	public MailAppender() {
		super();
		try{
			serverName  = ServerConfigurationNew.id;
		} catch(Exception e){
			serverName = DEFAULT_NAME;
		}
		if(serverName == null){
			serverName = DEFAULT_NAME;
		}
	}


	public void append(LoggingEvent event) {
		if(enabled == false){
			return;
		}
		if (layout == null) {
			errorHandler.error("No layout set for the appender named [" + name + "].");
			return;
		}
		String content = getLayout().format(event);
		String title =  serverName + " " + event.getLoggerName();
		SMTPSender.sendMail(title, content);
	}
	

	public void close() {
	}
	
	public void setEnabled(boolean b){
		enabled = b;
	}
	
	public boolean getEnabled(){
		return enabled;
	}

	/**
	 * 需要layout
	 */
	public boolean requiresLayout() {
		return true;
	}

}
