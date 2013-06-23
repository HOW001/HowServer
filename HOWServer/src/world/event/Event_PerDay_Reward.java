/**
 * 
 */
package world.event;

import db.model.Player;

/**
 * @author liuzg
 *每日活动事件
 */
public class Event_PerDay_Reward implements GameEvent {

	private int eventType;
	public Event_PerDay_Reward(int eventType){
		this.eventType=eventType;
	}
	/* (non-Javadoc)
	 * @see world.event.GameEvent#getEventType()
	 */
	@Override
	public int getEventType() {
		return eventType;
	}

	/* (non-Javadoc)
	 * @see world.event.GameEvent#getToucher()
	 */
	@Override
	public Player getToucher() {
		// TODO Auto-generated method stub
		return null;
	}

}
