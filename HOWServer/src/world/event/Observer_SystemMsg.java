package world.event;


import org.apache.log4j.Logger;

import util.binreader.EventData;
import world.World;

public class Observer_SystemMsg extends ObserverEntry {
    private static Logger logger=Logger.getLogger(Observer_SystemMsg.class);
	public Observer_SystemMsg(EventData eventData){
		super(eventData);
	}

	@Override
	public void runEvent(GameEvent event) {
		if(event instanceof Event_SystemMsg){
			if(isRunTime()){
                //这里处理系统消息事件逻辑
				World.sendWorldMsg(eventData.msgContent);
			    logger.info("触发测试事件");
			}else{
				logger.info("触发测试事件已关闭");
			}
		}else{
			logger.info("无效触发事件:ID="+event.getEventType());
		}
			
	}	
}
