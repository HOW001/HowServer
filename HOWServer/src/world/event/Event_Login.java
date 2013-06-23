/**
 * 
 */
package world.event;

import db.model.Player;

/**
 * @author liuzg
 * 登录事件
 */
public class Event_Login implements GameEvent {
	 private int eventType;
	 private Player toucher;
	public Event_Login(Player player,int eventID){
		eventType=eventID;
		toucher=player;
		SubjectManager.getInstance().addEvent(this);
	}
	/* (non-Javadoc)
	 * @see world.event.GameEvent#getCampaignAdapterID()
	 */
	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return eventType;
	}
	@Override
	public Player getToucher() {
		// TODO Auto-generated method stub
		return toucher;
	}

}
