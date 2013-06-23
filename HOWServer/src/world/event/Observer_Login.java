package world.event;

import java.util.Calendar;

import org.apache.log4j.Logger;

import db.model.Player;
import server.cmds.UISystemCP;
import util.Util;
import util.binreader.EventData;
import util.logger.MoneyLogger;

/**
 * 
 * @author liuzg
 * 登录观察者
 */
public class Observer_Login extends ObserverEntry {
    private static Logger logger=Logger.getLogger(Observer_Login.class);
	public Observer_Login(EventData eventData){
		super(eventData);
	}

	@Override
	public void runEvent(GameEvent event) {
		if(event instanceof Event_Login){
			if(isRunTime()){
	             if(event.getToucher()!=null){
	            	 Player toucher=event.getToucher();
	            	 Calendar c=Calendar.getInstance();
	            	 if(c.get(Calendar.DAY_OF_YEAR)==toucher.getSignInDay()){
	            		 logger.info(toucher.getName()+"已经签到.....");
	            		 UISystemCP.sendFlutterMessageForOK(toucher.getIoSession(), "亲,明天才可以哦!");
	            		 return;
	            	 }
	            	 Calendar startTime=Calendar.getInstance();
	            	 String[]peroidTimes=this.eventData.times.split("\\+");
	     			for(String peroidTime:peroidTimes){
	     				String[] peroid=peroidTime.split("#");	     				
	     				startTime.setTime(Util.getNormalDataParse(peroid[0]));
	     				break;
	     			}
	     			 /*
	     			  * 目前是活动第N天
	     			  */
	     			 toucher.setSignInDay(c.get(Calendar.DAY_OF_YEAR));
	            	 int indexDay=toucher.getSignInDay()-startTime.get(Calendar.DAY_OF_YEAR);
	            	 String []values=eventData.specialValue.split("#");
	            	 if(indexDay<0 ){
	            		 logger.error(toucher.getName()+"请求的签到活动已关闭");
	            		 return;
	            	 }
	            	 if(indexDay>=values.length){//默认取最后一天
	            		 indexDay=values.length-1;
	            	 }
	            	 int gold=Integer.parseInt(values[indexDay]);	            	
//	            	 logger.info(toucher.getName()+"参与签到活动增加银币:"+gold);
//	            	 toucher.addBindGold(gold,MoneyLogger.moneyAdd[11]);
	            	 UISystemCP.openDialog(toucher.getIoSession(), "注册账号10天内，每天登录游戏可获得银币奖励！这是您今日登录游戏的奖励"+gold+"银币。");
//	            	 UISystemCP.sendFlutterMessageForOK(toucher.getIoSession(), "今日签到成功,奖励您"+exp+"经验!");
	             }
			}
		}
	}
}
