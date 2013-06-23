package world.event;

import db.model.Player;


public class Event_SystemMsg implements GameEvent {
    private int eventType;
	public Event_SystemMsg(int eventID){
		this.eventType=eventID;
	}

	@Override
	public int getEventType() {
		return eventType;
	}

	@Override
	public Player getToucher() {
		// TODO Auto-generated method stub
		return null;
	}
}
