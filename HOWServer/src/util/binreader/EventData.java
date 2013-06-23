package util.binreader;

import java.util.HashMap;
import java.util.Map;

import common.Logger;

import world.event.ObserverEntry;
import world.event.Observer_Login;
import world.event.Observer_PerDay_Reward;
import world.event.Observer_SystemMsg;
import world.event.SubjectManager;

public class EventData implements PropertyReader{
	private static Logger logger=Logger.getLogger(EventData.class);
	public int id;
	public String title;
	public String detail;
	public String touchType;//触发类型 system=系统触发 player=角色触发
	public long touchInterval;//触发间隔，以秒为单位 
	public String times;//格式为:2011-12-9 13:00:00#2011-12-9 15:00:00&2011-12-11 9:00:00#2011-12-9 17:00:00
	public int isOpen;//1:开放 0：关闭
	public int msgParameter;//消息播放通知间隔时间
	public String msgContent;
	public String specialValue;//特殊值定义，如下:
	//1001:第一个参数奖励经验数,第二个参数奖励银币数

	
	public long lastTouchTime;//上次系统触发时间
	public long lastMsgTime;//上次系统信息公告时间
	
	public static final int EVENT_TYPE_SYSTEM_MSG=1000;//系统消息事件
	public static final int EVENT_TYPE_LOGIN=1001;//玩家登录事件
	public static final int EVENT_TYPE_PERDAY_REWARD=1002;//每日奖励活动

	private static Map<Integer, EventData> data = new HashMap<Integer, EventData>();
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(id,this);
		}
		ObserverEntry listener=null;
		switch (id) {
		case EVENT_TYPE_SYSTEM_MSG://系统
			listener = new Observer_SystemMsg(this);
			break;
		case EVENT_TYPE_LOGIN://登录事件
			listener = new Observer_Login(this);
			break;
		case EVENT_TYPE_PERDAY_REWARD://每日奖励活动
			listener = new Observer_PerDay_Reward(this);
			break;
		default:
			logger.error("无法解析的事件类型:id="+id);
		}
		if(listener!=null){
			SubjectManager.getInstance().addListener(id, listener);
		}
//		data.put(id, this);	
	}
	@Override
	public void clearData() {
//		SubjectManager.getInstance().clearListener();
	}
	@Override
	public void clearStaticData() {
		
	}
	@Override
	public PropertyReader getData(int id) {
		return data.get(id);
	}
}
