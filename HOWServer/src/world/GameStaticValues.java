package world;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;

import server.ServerEntrance;
import server.cmds.RegisterCP;

/**
 * @author liuzg
 * 此实体内所有数值均为系统需要持久化的数据，将在系统启动时读取和定时存入
 */
public class GameStaticValues {
	private static Logger logger=Logger.getLogger(GameStaticValues.class);
	public static int TestValue=0;//测试
	public static int CurrentDayValue=0;//当前天数在本年度的表示方式，由Calendar.get(Calendar.DayOfYear)获得,适用于每天一次的记录;
	public static String WriteName="test001";//在白名单中的玩家
	/**
	 * @author liuzg
	 * 写入游戏持久化数据
	 */
	public static void writeStaticValues(){
		try {
			TestValue++;
			File execFile=new File(ServerEntrance.serverPath+"res/GameStaticValues.txt");
			FileOutputStream fw = new FileOutputStream(execFile);
			Properties property = new Properties();
			property.setProperty("TestValue", TestValue+"");
			property.setProperty("CurrentDayValue", CurrentDayValue+"");		
			property.setProperty("WriteName", WriteName);		
			logger.info("写入游戏持久常量....");
			logger.info("CurrentDayValue="+CurrentDayValue);
			logger.info("WriteName="+WriteName);
			property.store(fw, null);
			property.clear();
			fw.close();	
		} catch (Exception e) {
			logger.error("写入游戏持久常量时出现异常:",e);
		}
	}
	/**
	 * @author liuzg
	 * 开机时运行执行日志文件
	 */
	public static void readStaticValues(){
		try {
			File execFile=new File(ServerEntrance.serverPath+"res/GameStaticValues.txt");
			Calendar now=Calendar.getInstance();
			if(execFile.exists()==false){//文件不存在，设置默认值
				initValues();
				logger.info("读取执行游戏持久数值时，文件不存在");
			}else{
				FileInputStream fis = new FileInputStream(execFile);
				Properties property = new Properties();
				property.load(fis);
				//最后一次记录值
				String strValues=property.getProperty("TestValue");
				if(strValues!=null){
					TestValue=Integer.parseInt(strValues);
				}
				strValues=property.getProperty("CurrentDayValue");
				if(strValues!=null){
					CurrentDayValue=Integer.parseInt(strValues);
				}else{
					CurrentDayValue=now.get(Calendar.DAY_OF_YEAR);
				}
				strValues=property.getProperty("WriteName");
				if(strValues!=null){
					WriteName=strValues;
					for(String name:WriteName.split("#")){
						RegisterCP.WriteNameList.add(name);
					}
				}
				fis.close();
			}
		} catch (Exception e) {
			logger.error("读取游戏持久常量时出现异常:",e);
			System.exit(1);
		}
	}
	/**
	 * @author liuzg
	 * 初始化各项默认值
	 */
	public static void initValues(){
		Calendar now=Calendar.getInstance();
		TestValue=0;
		CurrentDayValue=0;
		CurrentDayValue=now.get(Calendar.DAY_OF_YEAR);
	}
}
