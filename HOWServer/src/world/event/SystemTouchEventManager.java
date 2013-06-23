package world.event;

import org.apache.log4j.Logger;

import server.ServerEntrance;
import server.cmds.ChatCP;
import util.binreader.EventData;
import world.World;

/**
 * @author liuzg
 * 管理系统负责触发事件
 */
public class SystemTouchEventManager implements Runnable {
	 private static Logger logger=Logger.getLogger(SystemTouchEventManager.class);
		private final static SystemTouchEventManager instance=new SystemTouchEventManager();
		public static SystemTouchEventManager getInstance(){
			return instance;
		}
		private SystemTouchEventManager(){
			ServerEntrance.runThread(this);
		}
		@Override
		public void run() {
			long times = System.currentTimeMillis();
	         while(World.running()){
	        	 try {
	        		 times = System.currentTimeMillis();
	        	 for(ObserverEntry listener:SubjectManager.getInstance().listeners.values()){
	        			 EventData entry=listener.eventData;
	        			 if(listener.isRunTime()==false){
	        				 /*
	        				  * 不满足运行时间要求
	        				  */
	        				 continue;
	        			 }	
	        			 if(entry.msgParameter>0&&System.currentTimeMillis()-entry.lastMsgTime>entry.msgParameter*1000){
	        				 /*
	        				  * 进行一次广播
	        				  */
	        				 ChatCP.sendSystemMsgToAllPlayerFromGame(entry.msgContent);
	        				 entry.lastMsgTime=System.currentTimeMillis();
	        			 }
	        			 if(entry.touchType.equals("system")==false){
	        				 continue;
	        			 }	        			
	        			 if(System.currentTimeMillis()-entry.lastTouchTime<(entry.touchInterval*1000)){
	        				/*
	        				 * 不满足触发间隔
	        				 */
	        				 continue;
	        			 }
	        			 if(entry.isOpen==0){
	        				 continue;
	        			 }
	        			 logger.info(entry.id+"上次触发:"+entry.lastTouchTime+",当前时间:"+System.currentTimeMillis()+",触发间隔:"+(entry.touchInterval*1000));
	        			 entry.lastTouchTime=System.currentTimeMillis();
	        			 GameEvent event=null;
	        			 switch(entry.id){
	        			 case EventData.EVENT_TYPE_SYSTEM_MSG:
	        				 event=new Event_SystemMsg(EventData.EVENT_TYPE_SYSTEM_MSG);
	        				 break;
	        			 case EventData.EVENT_TYPE_PERDAY_REWARD://每日奖励活动
	        				 event=new Event_PerDay_Reward(EventData.EVENT_TYPE_PERDAY_REWARD);
	        				 break;
	        			 default:
	        				 logger.error("系统触发无效事件:ID="+entry.id);
	        				 event=null;
	        			 }
	        			 if(event!=null){
	        				 logger.info("系统添加一次触发活动事件:"+entry.title);
	        				 SubjectManager.getInstance().addEvent(event);
	        			 }
	        	 }
	        	 long useTimes = System.currentTimeMillis() - times;
		 			if(useTimes>=1000){
		 				logger.error("SystemTouchEventManager()线程运行时间过长" + useTimes);
		 			}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("系统触发事件异常:",e);
				}
	        	
	         }    
	}

}
