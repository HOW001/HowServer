package world.event;

import db.model.Player;

public interface GameEvent {
	/**
	 * @author liuzg
	 * @return
	 * 事件类型
	 */
    public int getEventType();
    /**
     * @author liuzg
     * @return
     * 触发者
     * 系统事件可不必实现
     */
    public Player getToucher();
}
