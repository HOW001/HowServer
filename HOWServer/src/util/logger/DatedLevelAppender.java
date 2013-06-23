package util.logger;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;


/**
 * 在日志界别为ERROR的时候，会自动以System.err.println的形式打印出来
 *
 */
public class DatedLevelAppender extends DatedFileAppender{
    private String level;
    private Level currentLevel;
    
	public String getLevel() {
		return level;
	}
	
    public void append(LoggingEvent event) {
        if(!event.getLevel().isGreaterOrEqual(currentLevel)){
        	return;
        }
        if(event.getLevel().isGreaterOrEqual(Level.ERROR)){
        	System.err.println(event.getRenderedMessage());
        }
        super.append(event);
    }

	public void setLevel(String level) {
		this.level = level;
		this.currentLevel = Level.toLevel(level);
	}
}
