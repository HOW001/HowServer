/**
 * 
 */
package world.event;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import db.model.Player;
import db.service.DBPlayerImp;

import util.binreader.EventData;
import util.logger.MoneyLogger;
import world.GameStaticValues;
import world.World;

/**
 * @author liuzg
 * 
 */
public class Observer_PerDay_Reward extends ObserverEntry {
	private static Logger logger = Logger
			.getLogger(Observer_PerDay_Reward.class);

	public Observer_PerDay_Reward(EventData eventData) {
		super(eventData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.event.ObserverEntry#runEvent(world.event.GameEvent)
	 */
	@Override
	public void runEvent(GameEvent event) {
		if (event instanceof Event_PerDay_Reward) {
			if (isRunTime()) {
				Calendar c = Calendar.getInstance();
				if (c.get(Calendar.DAY_OF_YEAR) + 1 == GameStaticValues.CurrentDayValue||GameStaticValues.CurrentDayValue==0) {
					/*
					 * 当天第一次活动
					 */
					GameStaticValues.CurrentDayValue = c
							.get(Calendar.DAY_OF_YEAR);
					logger.info("开始处理每日奖励活动逻辑!");
					process();
				} else {
					logger.info("当天已处理过一次活动，不再进行处理！");
				}

			}
		}

	}
    /**
     * @author liuzg
     * 活动具体处理流程
     */
	private void process() {
         /*
          * 1.获取所有注册用户
          * 2.在线玩家在线增加
          * 3.非在线玩家数据库增加
          */
		try {
			List<Integer> list=DBPlayerImp.getInstance().getPlayerListID();
			String []values=eventData.specialValue.split("#");
			if(values.length!=2){
				logger.error("每日活动的特殊数据配置错误："+eventData.specialValue);
			}
//			int exp=Integer.parseInt(values[0]);
			int exp=0;
			int money=Integer.parseInt(values[1]);
			Player player;
			for(int playerID:list){
				player=World.getPlayer(playerID);
				if(player!=null){
					logger.info("每日奖励活动奖励玩家:"+player.getName()+",经验:"+exp+",银币:"+money);
					player.addXp(exp);
//					player.addBindGold(money,MoneyLogger.moneyAdd[12]);
				}else{
					logger.info("每日奖励活动奖励不在线玩家id="+playerID+",经验:"+exp+",银币:"+money);
					DBPlayerImp.getInstance().updatePlayerExpAndGold(playerID, exp, money);
					player = World.getInstance().getBufferPlayer(playerID);
					 if(player!=null){
						 logger.error(player.getName()+"更新的同时玩家上线了7");
						 player.updateVersion();
					 }
				}
			}
		} catch (Exception e) {
			logger.error("每日活动处理异常:",e);
		}
	}
}
