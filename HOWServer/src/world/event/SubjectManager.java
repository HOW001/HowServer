package world.event;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import server.ServerEntrance;
import world.World;

/**
 * 
 * @author liuzg
 * 观察者模块主题
 */
public class SubjectManager implements Runnable {

	private final static SubjectManager instance=new SubjectManager();
	public static SubjectManager getInstance(){
		return instance;
	}
	private SubjectManager(){
		ServerEntrance.runThread(this);
	}
	private static Logger logger=Logger.getLogger(SubjectManager.class);
	private static final int MAX_EVENT_SIZE=100;
	//事件编号#事件监听实体
	public java.util.concurrent.ConcurrentHashMap<Integer,ObserverEntry> listeners=new java.util.concurrent.ConcurrentHashMap<Integer,ObserverEntry>();//各个事件监听
	
	private  BlockingQueue<GameEvent> events =  new ArrayBlockingQueue<GameEvent>(MAX_EVENT_SIZE);
	
	/**
	 * @author liuzg
	 * @param eventID
	 * @return
	 * 是否处于运行时间,供外部接口调用
	 */
	public boolean isRunTime(int eventID){
		if(listeners.get(eventID)==null){
			return false;
		}
		return listeners.get(eventID).isRunTime();
	}
	/**
	 * @author liuzg
	 * @param event
	 * 添加一个事件
	 */
	public void addEvent(GameEvent event){
		try {
			events.put(event);
			logger.info("添加事件类型:"+event.getEventType());
		} catch (Exception e) {
			logger.error("添加事件类型异常：",e);
		}
	}
	/**
	 * @author liuzg
	 * @param type
	 * @param listener
	 * 添加一个解析实体
	 */
	public void addListener(int id,ObserverEntry listener){
		listeners.put(id, listener);
	}
	/**
	 * @author liuzg
	 * 重载数据时清空所有监听
	 */
	public void clearListener(){
		listeners.clear();
	}
	@Override
	public void run() {
		long times = System.currentTimeMillis();
		while(World.running()){
			try{	
				GameEvent event = events.take();
				ObserverEntry listener=listeners.get(event.getEventType());
				if(listener!=null){
					listener.runEvent(event);
				}
//				else{
//					logger.error("无法解析的事件：ID="+event.getEventType());
//				}
				if(events.size()==0){
					Thread.sleep(1000);
				}
			} catch(Exception e){
				logger.error("EventManager",e);
			}
		}
		long useTimes = System.currentTimeMillis() - times;
		if(useTimes>=100){
			logger.error("SubjectManager()线程运行时间过长" + useTimes);
		}
	}

}
