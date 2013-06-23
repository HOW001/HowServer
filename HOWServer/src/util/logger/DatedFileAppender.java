package util.logger;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lzg------2010-11-9
 * 日志文件配置
 */
public class DatedFileAppender extends RollingFileAppender {
    private String m_directory = "logs";
    private String m_prefix = "gameserver.";
    private String m_suffix = ".log";
    private File m_path = null;
    private Calendar m_calendar = null;
    private long m_tomorrow = 0L;
    


    public DatedFileAppender() {
    }

    public DatedFileAppender(String directory, String prefix, String suffix) {
        m_directory = directory;
        m_prefix = prefix;
        m_suffix = suffix;
        activateOptions();
    }

    //===============Properties==========================//
    public String getDirectory() {
        return m_directory;
    }

    public void setDirectory(String directory) {
        m_directory = directory;
    }

    public String getPrefix() {
        return m_prefix;
    }


    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    public String getSuffix() {
        return m_suffix;
    }

    public void setSuffix(String suffix) {
        m_suffix = suffix;
    }

    //=======================Public Methods=====================//
    public void activateOptions() {
        if (m_prefix == null) {
            m_prefix = "";
        }
        if (m_suffix == null) {
            m_suffix = "";
        }
        if ((m_directory == null) || (m_directory.length() == 0)) {
            m_directory = ".";
        }
        m_path = new File(m_directory);
        if (!m_path.isAbsolute()) {
            String base = System.getProperty("catalina.base");
            if (base != null) {
                m_path = new File(base, m_directory);
            }
        }
        m_path.mkdirs();
        if (m_path.canWrite()) {
            m_calendar = Calendar.getInstance(); // initialized
        }
    }
    
    public boolean validLevel(){
    	return false;
    }

    public void append(LoggingEvent event) {
        if (this.layout == null) {
            errorHandler.error("No layout set for the appender named [" + name +
                               "].");
            return;
        }
        if (this.m_calendar == null) {
            errorHandler.error(
                    "Improper initialization for the appender named [" + name +
                    "].");
            return;
        }

        long n = System.currentTimeMillis();
        if (n >= m_tomorrow) {
            m_calendar.setTime(new Date(n));
            String datestamp = datestamp(m_calendar); 
            tomorrow(m_calendar); 
            m_tomorrow = m_calendar.getTime().getTime();
            File newFile = new File(m_path, m_prefix + datestamp + m_suffix);
            this.fileName = newFile.getAbsolutePath();
            super.activateOptions(); 
        }
        if (this.qw == null) { // should never happen
            errorHandler.error(
                    "No output stream or file set for the appender named [" +
                    name + "].");
            return;
        }
        subAppend(event);

    }

    /**
     * Formats a datestamp as yyyy-mm-dd using a Calendar source object.
     *
     * @param calendar a Calendar containing the date to format.
     *
     * @return a String in the form "yyyy-yy-dd".
     */
    public static String datestamp(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuffer sb = new StringBuffer();
        if (year < 1000) {
            sb.append('0');
            if (year < 100) {
                sb.append('0');
                if (year < 10) {
                    sb.append('0');
                }
            }
        }
        sb.append(Integer.toString(year));
        sb.append('-');
        if (month < 10) {
            sb.append('0');
        }
        sb.append(Integer.toString(month));
        sb.append('-');
        if (day < 10) {
            sb.append('0');
        }
        sb.append(Integer.toString(day));
        return sb.toString();
    }

    /**
     * Sets a calendar to the start of tomorrow, with all time values reset to
     * zero.
     *
     * <p>Takes advantage of the fact that the Java Calendar implementations are
     * mercifully accommodating in handling non-existent dates. For example,
     * June 31 is understood to mean July 1. This allows you to simply add one
     * to today's day of the month to generate tomorrow's date. It also works
     * for years, so that December 32, 2004 is converted into January 1,
     * 2005.</p>
     *
     * @param calendar Calendar
     */
    public static void tomorrow(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
        calendar.clear(); // clear all fields
        calendar.set(year, month, day); // set tomorrow's date
    }
    
    public static void main(String[] args){
    	Logger logger = LoggerFactory.getLogger("TimeTest");
    	logger.info("start");
    	final int TIMES = 100000;
    	int i = TIMES;
    	long start = System.currentTimeMillis();
    	for(;i>0;i--){
    		logger.info("test");
    	}
    	long end = System.currentTimeMillis();
    	logger.info("test times = " + TIMES );
    	logger.info("used time  = " + (end - start));
    }



}
