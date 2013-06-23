package util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;

import world.World;

/**
 * 日期时间处理 
 * @author Administrator
 *
 */
public class Util {
	private static Locale locale=new Locale("zh","CN");//定义一个中国地区
//	private static DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG,locale);
	private static Logger logger = Logger.getLogger(Util.class);
	private static Calendar c = Calendar.getInstance();
	public  static String getCurrentDate() {
		String linuxTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(
				System.currentTimeMillis()));
		return linuxTime;
	}
	/**
	 * @author liuzg
	 * @param time
	 * @return 2012-04-05 03:00:48
	 * 获取默认日期的描述
	 */
	public static String getNormalDataFormat(long time){
		SimpleDateFormat DATE_FORMATTER = (SimpleDateFormat) SimpleDateFormat
				.getDateTimeInstance();
		DATE_FORMATTER.applyPattern("yyyy-MM-dd HH:mm:ss");
		return DATE_FORMATTER.format(new Date(time));
	}
	/**
	 * @author liuzg
	 * @param parse 2012-04-05 03:00:48
	 * @return
	 * 返回默认日期描述的时间值
	 */
	public static Date getNormalDataParse(String parse){
		try {
			SimpleDateFormat DATE_FORMATTER = (SimpleDateFormat) SimpleDateFormat
					.getDateTimeInstance();
			DATE_FORMATTER.applyPattern("yyyy-MM-dd HH:mm:ss");
			Date time= DATE_FORMATTER.parse(parse);
			return time;
		} catch (ParseException e) {
			logger.error("无法解析日期描述："+parse,e);
			return null;
		}
	}
	
	/**
	 * @author liuzg
	 * @param parse 2012-04-05 03:00
	 * @return
	 * 返回默认日期描述的时间值（不含秒）
	 */
	public static Date getNormalDataParseForMin(String parse){
		try {
			SimpleDateFormat DATE_FORMATTER = (SimpleDateFormat) SimpleDateFormat
					.getDateTimeInstance();
			DATE_FORMATTER.applyPattern("yyyy-MM-dd HH:mm");
			Date time= DATE_FORMATTER.parse(parse);
			return time;
		} catch (ParseException e) {
			logger.error("无法解析日期描述："+parse,e);
			return null;
		}
	}
	
	/**
	 * 获取当前日期
	 * @return
	 */
	public static String getCurrentAllTime() {
		try {
			synchronized(c){
			c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			now.append('_');
			now.append(c.get(Calendar.HOUR_OF_DAY)<10?"0"+c.get(Calendar.HOUR_OF_DAY):c.get(Calendar.HOUR_OF_DAY));
			now.append(':');
			now.append(c.get(Calendar.MINUTE)<10?"0"+c.get(Calendar.MINUTE):c.get(Calendar.MINUTE));
			now.append(':');
			now.append(c.get(Calendar.SECOND)<10?"0"+c.get(Calendar.SECOND):c.get(Calendar.SECOND));
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807152201",e);
			return "1970-01-01 00:00:01";
		}
	}
	
	/**
	 * 获取当前日期
	 * @return
	 */
	public static String getCurrentTime() {
		try {
			synchronized(c){
			c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('月');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			now.append('日');
			now.append(c.get(Calendar.HOUR_OF_DAY));
			now.append('时');
			now.append(c.get(Calendar.MINUTE));
			now.append('分');
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807152301",e);
			return "7月8日16时45分";
		}		
	}

	/**
	 * 获得当前日期数
	 * @return
	 */
	public static int getCurrentDay() {
		try {
			synchronized (c) {
				c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			return c.get(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			logger.error("异常20120807152301",e);
			return 0;
		}		
	}
	
	/**
	 * 获取当前小时数
	 * @return
	 */
	public static int getCurrentHour() {	
		try {
			synchronized(c){
			c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			return c.get(Calendar.HOUR_OF_DAY);
		} catch (Exception e) {
			logger.error("异常20120807152401",e);
			return 0;
		}
	}
	
	/**
	 * 获得当前分钟数
	 * @return
	 */
	public static int getCurrentMinute() {
		try {
			synchronized(c){
			c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			return c.get(Calendar.MINUTE);
		} catch (Exception e) {
			logger.error("异常20120807152501",e);
			return 0;
		}
	}
	
	/***
	 * 时间
	 * @param d
	 * @param format
	 * @return
	 */
	public static String format(Date d, String format) {
        if (d == null){
            return "";
        }
        SimpleDateFormat myFormatter = new SimpleDateFormat(format);
        return myFormatter.format(d);
    }

  /**
   * @author liuzg
   * @param time
   * @param format
   * @return
   * 将指定格式的日期解析
   */
    public static Date parse(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = null;
        try {
        	d = sdf.parse(time);
        } catch (Exception e) {
        	logger.error("DateUtil.parse异常", e);
        }
        return d;
    }
/**
 * @author liuzg
 * @param cal
 * @param format MM/dd/yyyy HH:mm:ss
 * @return
 * 返回指定格式的日期日期
 */
    public static String format(Calendar cal, String format) {
        if (cal == null)
            return "";
        SimpleDateFormat myFormatter = new SimpleDateFormat(format);
        return myFormatter.format(cal.getTime());
    }

   /**
    * @author liuzg 
    * @param d
    * @param day
    * @return
    * 增加指定天数
    */
    public static Calendar add(java.util.Date d, int day) {
        try {
			if (d == null){
			    return null;
			}
			synchronized(c){
			c.setTime(d);
			c.add(Calendar.DATE, day);
			}
			return c;
		} catch (Exception e) {
			logger.error("异常201208071516:",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param d
 * @param day
 * @return
 * 增加指定天数
 */
    public static Date addDate(java.util.Date d, int day) {
        try {
			if (d == null){
			    return null;
			}
			synchronized(c){
			c.setTime(d);
			c.add(Calendar.DATE, day);
			}
			return c.getTime();
		} catch (Exception e) {
			logger.error("异常:20120807151701",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param d
 * @param h
 * @return
 * 增加指定小时
 */
    public static Date addHourDate(java.util.Date d, int h) {
        try {
        	synchronized(c){
			if (d == null){
			    return null;
			}
			c.setTime(d);
			c.add(Calendar.HOUR, h);
        	}
			return c.getTime();
		} catch (Exception e) {
			logger.error("异常20120807151802",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param d
 * @param h
 * @return
 * 增加指定小时
 */
    public static Calendar addHour(java.util.Date d, int h) {
        try {
			if (d == null){
			    return null;
			}
			synchronized(c){
			c.setTime(d);
			c.add(Calendar.HOUR, h);
			}
			return c;
		} catch (Exception e) {
			logger.error("异常20120807151801",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param d
 * @param m
 * @return
 * 增加指定分钟数
 */
    public static Date addMinuteDate(java.util.Date d, int m) {
        try {
			if (d == null){
			    return null;
			}
			synchronized(c){
			c.setTime(d);
			c.add(Calendar.MINUTE, m);
			}
			return c.getTime();
		} catch (Exception e) {
			logger.error("异常20120807152001",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param d
 * @param m
 * @return
 * 增加指定分钟数
 */
    public static Calendar addMinute(java.util.Date d, int m) {
        try {
			if (d == null){
			    return null;
			}
			synchronized(c){
			c.setTime(d);
			c.add(Calendar.MINUTE, m);
			}
			return c;
		} catch (Exception e) {
			logger.error("异常20120807151901",e);
			return null;
		}
    }
/**
 * @author liuzg
 * @param c1
 * @param c2
 * @return
 * 比较两个日期的大小
 */
    public static int compare(Calendar c1, Calendar c2) {
        if (c1 == null || c2 == null)
            return -1;
        long r = c1.getTimeInMillis() - c2.getTimeInMillis();
        if (r > 0)
            return 1;
        else if (r == 0)
            return 0;
        else
            return 2;
    }
/**
 * @author liuzg
 * @param c1
 * @param c2
 * @return
 * 比较两个日期的大小
 */
    public static int compare(Date c1, Date c2) {
        if (c1 == null || c2 == null)
            return -1;
        long r = c1.getTime() - c2.getTime();
        
        if (r > 0)
            return 1;
        else if (r == 0)
            return 0;
        else
            return 2;
    }
    /**
     * @author liuzg
     * @param c1
     * @param c2
     * @return
     * 两个日期的相差天数
     */
    public static int datediff(Calendar c1, Calendar c2) {
        if (c1 == null || c2 == null)
            return -1;
        long r = c1.getTimeInMillis() - c2.getTimeInMillis();
        r = r / (24 * 60 * 60 * 1000);
        return (int) r;
    }
   /**
    * @author liuzg
    * @param c1
    * @param c2
    * @return
    * 两个日期的相差天数
    */
    public static int datediff(Date c1, Date c2) {
        if (c1 == null || c2 == null)
            return -1;
        long r = c1.getTime() - c2.getTime();
        r = r / (24 * 60 * 60 * 1000);
        return (int) r;
    }
    /**
     * @author liuzg
     * @param c1
     * @param c2
     * @return
     * 两个时间的相差分钟数
     */
    public static int datediffMinute(Date c1, Date c2) {
        if (c1 == null || c2 == null)
            return 0;
        long r = c1.getTime() - c2.getTime();
        r = r / (60 * 1000);
        return (int) r;
    }
    /**
     * @author liuzg
     * @param c1
     * @param c2
     * @return
     * 两个时间的相差分钟数
     */
    public static int datediffMinute(Calendar c1, Calendar c2) {
        if (c1 == null || c2 == null)
            return 0;
        long r = c1.getTimeInMillis() - c2.getTimeInMillis();
        r = r / (60 * 1000);
        return (int) r;
    }
    /**
     * @author liuzg
     * @param year
     * @param month
     * @return
     * 获取每个月的天数
     */
    public static int getDayCount(int year, int month) {
        int daysInMonth[] = {
                            31, 28, 31, 30, 31, 30, 31, 31,
                            30, 31, 30, 31};
        
        if (1 == month)
            return ((0 == year % 4) && (0 != (year % 100))) ||
                    (0 == year % 400) ? 29 : 28;
        else
            return daysInMonth[month];
    }
    /**
     * @author liuzg
     * @param d
     * @return
     * 获取一个日期的简短描述
     */
	public static String getYMDDate(Date d) {
		try {
			synchronized(c){
			c.setTime(d);
			}
			return getChinessNumber(c.get(Calendar.YEAR)) + "年"
					+ getChinessNumber((c.get(Calendar.MONTH) + 1)) + "月"
					+ getChinessNumber(c.get(Calendar.DAY_OF_MONTH)) + "日";
		} catch (Exception e) {
			logger.error("异常20120807154101",e);
			return "1983年7月8日";
		}
	}
    /**
     * @author liuzg
     * @param number
     * @return
     * 获得一个数字的中文描述
     */
    public static String getChinessNumber(int number) {
        StringBuffer chiness = new StringBuffer();
        String temp = String.valueOf(number);
        for (int index = 0; index < temp.length(); index++) {
            String str = "";
            switch (temp.charAt(index)) {
            case '0':
                str = "零";
                break;
            case '1':
                str = "一";
                break;
            case '2':
                str = "二";
                break;
            case '3':
                str = "三";
                break;
            case '4':
                str = "四";
                break;
            case '5':
                str = "五";
                break;
            case '6':
                str = "六";
                break;
            case '7':
                str = "七";
                break;
            case '8':
                str = "八";
                break;
            case '9':
                str = "九";
                break;
            }
            chiness.append(str);
        }
        if (number <= 31) {
            if (number % 10 == 0 && number!=10) {
                chiness.delete(1, 2);
                chiness.replace(1, 1, "十");
            } else if (number > 20) {     
                chiness.insert(1, "十");
            } else if (number > 10) {
                chiness.replace(0, 1, "十");
            } else if (number == 10) {
                chiness.delete(0, 2);
                chiness.insert(0, "十");
            }
        }
        return chiness.toString();

    }
    /**
     * @author liuzg
     * @param d
     * @return
     * 获得当前时间的一个完整描述
     */
	public static String getFormatDate(Date d) {
		try {
			synchronized(c){
				c.setTime(d);
			}
			return c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月"
					+ c.get(Calendar.DAY_OF_MONTH) + "日" + "  "
					+ c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":"
					+ c.get(Calendar.SECOND) + ":" + c.get(Calendar.MILLISECOND);
		} catch (Exception e) {
			logger.error("异常20120807153401",e);
			return "1983年7月8日 16:45:30";
		}
	}
    /**
     * @author liuzg
     * @param d
     * @return
     * 获取当前时间的一个短描述
     */
	public static String getFormatDataByShort(Date d) {
		try {
			synchronized (c) {
				c.setTime(d);
			}
			return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
					+ "-" + c.get(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			logger.error("异常20120807153501", e);
			return "1983-07-08";
		}

	}
	/**
	 * 获取当前时间的一个短描述 
	 * @author fengmx
	 * @param d
	 * @return
	 */
	public static String getFormatDataToString(Date d) {
		try {
			synchronized (c) {
				c.setTime(d);
			}
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			StringBuffer sb = new StringBuffer();
			sb.append(year).append("-");
			if(month<10){
				sb.append("0");
			}
			sb.append(month).append("-");
			if(day<10){
				sb.append("0");
			}
			sb.append(day);
			return sb.toString();
		} catch (Exception e) {
			logger.error("异常20120807153501", e);
			return "1983-07-08";
		}

	}
    /**
     * @author liuzg
     * @param date
     * @return
     * 获得当前时间的一个完整描述
     */
    public static String getFormatDate(Calendar date)
    {
//        Calendar c=date;
        return date.get(Calendar.YEAR)+"年"+(date.get(Calendar.MONTH)+1)+"月" + date.get(Calendar.DAY_OF_MONTH)+"日"+"  " +date.get(Calendar.HOUR)+":"+date.get(Calendar.MINUTE)+":"+date.get(Calendar.SECOND)+":"+date.get(Calendar.MILLISECOND);
    }
    /**
     * @author liuzg
     * @param l
     * @return
     * 获得一个当前时间的描述
     */
	public static String getFormatDate(long l) {
		try {
			synchronized(c){
				c.setTime(new Date(l));
			}
			return c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月"
					+ c.get(Calendar.DAY_OF_MONTH) + "日" + "  "
					+ c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":"
					+ c.get(Calendar.SECOND) + ":" + c.get(Calendar.MILLISECOND);
		} catch (Exception e) {
			logger.error("异常20120807153701",e);
			return "1983年7月8日 16:45:31";
		}
	}
    /**
     * @author liuzg
     * @param date
     * @return
     * 下一天的描述
     */
	public static String getNextDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, 1); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取前一天的时间
	 */
	public static String getBeforeDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, -1); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取当前的时间（带小时）
	 */
	public static String getDayHourDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, 0); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
//			if(c.get(Calendar.HOUR_OF_DAY)<10){
//				now.append(' '+"0");
//				now.append(c.get(Calendar.HOUR_OF_DAY));
//			}else{
			now.append(' ');
			now.append(c.get(Calendar.HOUR_OF_DAY));
			
			
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取当前的时间
	 */
	public static String getDayDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, 0); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取Day2的时间
	 */
	public static String getDay2Date(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, -2); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取Day3的时间
	 */
	public static String getDay3Date(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, -3); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取Day7的时间
	 */
	public static String getDay7Date(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, -7); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取当前的小时 时间
	 */
	public static String getHourDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, 0); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
//			if(c.get(Calendar.HOUR_OF_DAY)<10){
//				now.append(' '+"0");
//				now.append(c.get(Calendar.HOUR_OF_DAY));
//			}else{
			now.append(' ');
			now.append(c.get(Calendar.HOUR_OF_DAY));
//			}
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author zhangqiang
	 * @param date
	 * @return 获取当前的小时  分钟
	 */
	public static String getHourAndMinuteDate(Date date) {
		try {
			synchronized(c){
				c.setTime(date);
				c.add(Calendar.DAY_OF_MONTH, 0); 
			}
			StringBuffer now = new StringBuffer();
			now.append(c.get(Calendar.YEAR));
			now.append('-');
			now.append(c.get(Calendar.MONTH) + 1);
			now.append('-');
			now.append(c.get(Calendar.DAY_OF_MONTH));
			if(c.get(Calendar.HOUR_OF_DAY)<10){
				now.append(' '+"0");
				now.append(c.get(Calendar.HOUR_OF_DAY));
			}else{
			now.append(" ");
			now.append(c.get(Calendar.HOUR_OF_DAY));
			}
			if(c.get(Calendar.MINUTE)<10){
				now.append(':'+"0");
				now.append(c.get(Calendar.MINUTE));
			}else{
			now.append(":");
			now.append(c.get(Calendar.MINUTE));
			}
			return now.toString();
		} catch (Exception e) {
			logger.error("异常20120807153901",e);
			return "1983-07-08";
		}		
	}
	/**
	 * @author liuzg
	 * @return
	 * 今天是否周日
	 */
	public static boolean checkSunday() {
		try {
			synchronized(c){
			c.setTime(new Date(World.getInstance().getCurrentTime()));
			}
			return (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
		} catch (Exception e) {
			logger.error("异常20120807152101",e);
			return false;
		}		 
	}
	/**
	 * @author lzg------2010-9-27
	 * @param time
	 * @return
	 * 返回本地化日期2010年9月27日 上午11时22分20秒
	 */
	public static String getDateFormatLong(long time){
		DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG,locale);
		Date date=new Date(time);
		String s1=dateFormat.format(date);
		return s1;
	}
	/**
	 * @author lzg------2010-9-27
	 * @param time
	 * @return
	 * 返回本地化日期2010年9月27日 上午11时22分20秒
	 */
	public static String getDateFormatLong(Date date){
		DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG,locale);
		String s1=dateFormat.format(date);
		return s1;
	}
	/**
	 * @author lzg------2010-9-27
	 * @param time
	 * @return
	 * 返回本地化日期标准格式2010-9-27 11:17:02
	 */
	public static String getDateFormateMedium(long time){
		Date date=new Date(time);
		String s1=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,locale).format(date);
		return s1;
	}
	/**
	 * @author lzg------2010-9-27
	 * @param time
	 * @return
	 * 返回本地化日期标准格式 2010-9-27 11:17:02
	 */
	public static String getDateFormateMedium(Date date){
		String s1=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,locale).format(date);
		return s1;
	}
	/**
	 * @author liuzg
	 * @param times
	 * @return
	 * 转换时间
	 */
	public static Calendar getCalendarFromTimestamp(Timestamp times) {
		try {
			long time = times.getTime();
			synchronized (c) {
				c.setTimeInMillis(time);
			}
			return c;
		} catch (Exception e) {
			logger.error("异常20120807152201", e);
			return null;
		}
	}

   public static Timestamp getTimestampFromCalendar(Calendar c){	   
	   Timestamp tmp=new Timestamp(c.getTimeInMillis());
	   return tmp;
   }
   /**
    * @author liuzg
    * @param tmp
    * @param length
    * @return
    * 返回指定宽度的字符串
    */
   public static String getLengthString(String tmp,int length){
	   if(tmp.length()>=length){
		   return tmp;
	   }
	   int count=length-tmp.length();
	   StringBuffer sb=new StringBuffer();
	   for(int index=1;index<=count;index++){
		   sb.append("  ");
	   }
	   return tmp+sb.toString();
   }
   /**
    * @author liuzg
    * @param len
    * @return
    * 获取指定长度的随机汉字
    */
	public static String getChinesseWords(int len) {
		try {
			StringBuffer sb = new StringBuffer();
			StringBuffer sbLog=new StringBuffer();
			for (int index = 1; index <= len; index++) {
				// int i=Random.getNumber(0x5000, 0x9000);
				int i = Random.getNumber(1601, 5589);
//				System.out.println("随机值:" + i);
				if (i % 10 == 0) {//空汉字位
					index--;
					continue;
				}
				if (i % 100 == 95) {//不识别汉字位
					index--;
					continue;
				}
				byte[] bytes = new byte[2];
				bytes[0] = (byte) ((i / 100) + 160);
				bytes[1] = (byte) ((i % 100) + 160);
				if (bytes[1] >= 0) {//非汉字
					index--;
					continue;
				}
				String str = new String(bytes,"GBK");
				if (str.length() <= 0) {//错误汉字
					index--;
					continue;
				}
				sb.append(str);
                sbLog.append(i+"="+str);
			}
//			 byte[] bytes=sb.toString().getBytes("GBK");  
//			   return new String(bytes);
			logger.info("生成随机名称:"+sbLog.toString());
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "rekoo" + Random.getNumber(0x5000, 0x9000);
		}
	}
   public static void main(String str[]){
	   System.out.println("时间："+getFormatDate(1355799682359L));
   }
   
   /**
	 * 1天的毫秒数
	 */
	public static final long ONE_DAY = 1000 * 60 * 60 * 24;
	/**
	 * 1小时的毫秒数
	 */
	public static final long ONE_HOUR = 1000 * 60 * 60;
	/**
	 * 1分钟的毫秒数
	 */
	public static final long ONE_MIN = 1000 * 60;
	
	/**
	 * @author liuzg
	 * 返回指定集合以指定分隔符分隔的字符串
	 */
	public static String getResplit(List list,String split){
		StringBuffer sb=new StringBuffer();
		for(Object obj:list){
			sb.append(obj+split);
		}
		return sb.substring(0, sb.length()-1);
	}
	/**
	 * @author liuzg
	 * @param num
	 * @param data
	 * @return
	 * 获取数据前的short长度
	 */
	public static byte[] getBytesForShort(int num,byte[] data){
		byte[] times = BaseDataConvertor.short2Bytes((byte)num);
		data[0] = times[0];
		data[1] = times[1];
		return data;
	}
	/**
	 * 判断两个日期是否是同一天
	 * @param date1
	 * @param date2
	 * @return 0：错误的日期    1:是同一天  2：是连续的日期  3：非连续的日期
	 */
	public static byte isSameDay(Date date1,Date date2){
		if (date1 == null || date2 == null){
			return 0;
		}
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);
		if(Math.abs(c1.get(Calendar.DAY_OF_YEAR)-c2.get(Calendar.DAY_OF_YEAR))==1){
			return 2;
		}else if(Math.abs(c1.get(Calendar.DAY_OF_YEAR)-c2.get(Calendar.DAY_OF_YEAR))==0){
			return 1;
		}else{
			if(c1.get(Calendar.YEAR)!=c2.get(Calendar.YEAR)){
				return 2;
			}else{
				return 3;
			}
		}
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.HOUR_OF_DAY, 0);
//		calendar.set(Calendar.MINUTE, 0);
//		calendar.set(Calendar.SECOND, 0);
//		// 用来比较的时间
//		long compareTime = calendar.getTimeInMillis();
//		date1.getTime();
//		if (date1.getTime() < compareTime && date2.getTime() > compareTime) {
//			return true;
//		}
//		return false;
//		if (date1 == null || date2 == null){
//			return 0;
//		}
//        Calendar calendar=getCalendarFromDate(date1);
//        int year1=calendar.get(Calendar.YEAR);
//        int month1=calendar.get(Calendar.MONTH);
//        int day1= calendar.get(Calendar.DAY_OF_MONTH);
//        calendar=getCalendarFromDate(date2);
//        int year2=calendar.get(Calendar.YEAR);
//        int month2=calendar.get(Calendar.MONTH);
//        int day2= calendar.get(Calendar.DAY_OF_MONTH);
//        if(year1==year2 && month1==month2){
//        	if(day1==day2){
//        		return 1;
//        	} else if(Math.abs(day2-day1)==1){
//        		return 2;
//        	}
//        } else if(year1==year2 && Math.abs(month1-month2)==1){
//        	
//        } else if(Math.abs(year1-year2)==1){
//        	
//        }
//		return 3;
	}
	/**
	 * 将Date类型的日期格式转换为Calendar类型的日期格式
	 * @param date
	 * @return
	 */
	public static Calendar getCalendarFromDate(Date date){
		synchronized (c) {
			c.setTimeInMillis(date.getTime());
		}
		return c;
	}
	/*
	 * 非常用SQL语句
	 两日期/时间之间相差的天数：     
	SELECT  To_Days(end_time) - To_Days(start_time);
	两日期/时间之间相差的时分秒数：     
	SELECT  SEC_TO_TIME(UNIX_TIMESTAMP(end_time) - UNIX_TIMESTAMP(start_time));
	两日期/时间之间相差的秒数：     
	SELECT  UNIX_TIMESTAMP(end_time) - UNIX_TIMESTAMP(start_time);
	 */
}
