package world.event;
import java.util.Calendar;
import org.apache.log4j.Logger;

import util.Util;
import util.binreader.EventData;

/**
 * 
 * @author liuzg
 *
 * 活动事件监听
 * 观察者模式观察者
 */
public abstract class  ObserverEntry {
	private static Logger logger=Logger.getLogger(ObserverEntry.class);
	/**
	 * 活动数据实体
	 */
	protected EventData eventData;
	public ObserverEntry(EventData eventData){
		this.eventData=eventData;
	}
	/**
	 * @author liuzg
	 * @return
	 * 是否在活动期间
	 */
	public boolean isRunTime(){
		try {
			if(eventData.isOpen==0){
				return false;
			}
			String times=eventData.times;//2012-6-9 13:00:00#2013-7-9 15:00:00+2012-6-11 9:00:00#2015-6-9 17:00:00
			String[]peroidTimes=times.split("\\+");
			for(String peroidTime:peroidTimes){
				String[] peroid=peroidTime.split("#");
				Calendar startTime=Calendar.getInstance();
				startTime.setTime(Util.getNormalDataParse(peroid[0]));
				Calendar endTime=Calendar.getInstance();
				endTime.setTime(Util.getNormalDataParse(peroid[1]));
				if(System.currentTimeMillis()<startTime.getTimeInMillis()||System.currentTimeMillis()>endTime.getTimeInMillis()){
					return false;
				}
				Calendar c=Calendar.getInstance();
				startTime.set(Calendar.YEAR, c.get(Calendar.YEAR));
				startTime.set(Calendar.MONTH, c.get(Calendar.MONTH));
				startTime.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
				endTime.set(Calendar.YEAR, c.get(Calendar.YEAR));
				endTime.set(Calendar.MONTH, c.get(Calendar.MONTH));
				endTime.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
				if(System.currentTimeMillis()>=startTime.getTimeInMillis()&&System.currentTimeMillis()<=endTime.getTimeInMillis()){
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			logger.error("无法解析活动时间：ID="+eventData.id);
			return false;
		}
	}
	/**
	 * @author liuzg
	 * 运行事件，所有事件逻辑在此方法中实现
	 */
	public abstract void runEvent(GameEvent event);
	}
