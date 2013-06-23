package server.netserver;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import server.ServerEntrance;

/**
 * 
 * @author liuzg 启动接收和发送线程
 */
public class MsgInAndOutThread {
	private static final Logger logger = Logger
			.getLogger(MsgInAndOutThread.class);
	private static final int THREADCOUNT=8;
	private static AtomicInteger sessionIndex=new AtomicInteger(-1);
    private MsgInAndOutThread(){}
	private  MsgOutThread threadOutProcess[] = null;
	private  MsgInThread threadInProcess []= null;
    private static MsgInAndOutThread instance=new MsgInAndOutThread();
    public static MsgInAndOutThread getInstance(){
    	return instance;
    }
    /**
     * @author liuzg
     * @return
     * 产生一个socket线程编号
     */
    public int getSessionIndex(){
//    	sessionIndex++;
//    	if(sessionIndex>=THREADCOUNT){
//    		sessionIndex=0;
//    	}
//    	return sessionIndex;
    	if(sessionIndex.get()+100>=Integer.MAX_VALUE){
    		sessionIndex.set(0);
    	}
    	int value= sessionIndex.incrementAndGet()%THREADCOUNT;
    	return value;
    }
	public  void start() {
		long times = System.currentTimeMillis();
		if (threadOutProcess == null) {
			threadOutProcess = new MsgOutThread[THREADCOUNT];
			for(int index=0;index<THREADCOUNT;index++){
				threadOutProcess[index]=new MsgOutThread();
			    ServerEntrance.runThread(threadOutProcess[index]);
			}
		}
		if (threadInProcess == null) {
			threadInProcess = new MsgInThread[THREADCOUNT];
			for(int index=0;index<THREADCOUNT;index++){
				threadInProcess[index]=new MsgInThread();
				ServerEntrance.runThread(threadInProcess[index]);
			}
		}

	}

	/**
	 * @author lzg------2011-2-24
	 * @param datas
	 * @return 发送
	 */
	public synchronized boolean send(DataPackEntry datas) {
		if (threadOutProcess == null) {
			return false;
		}
		if(datas==null || datas.getSession()==null||datas.getSession().getAttribute("sessionIndex")==null){
			logger.error("出现异常...");
			return false;
		}
		int index=(Integer)datas.getSession().getAttribute("sessionIndex");
		if(index>THREADCOUNT){
			logger.error(datas.getSession()+"发送出现异常...");
			return false;
		}
//		logger.info(datas.getSession()+"使用线程编号为:"+index);
		return threadOutProcess[index].send(datas);
	}

	/**
	 * @author lzg------2011-2-24
	 * @param datas
	 * @return 接收
	 */
	public synchronized boolean receive(DataPackEntry datas) {
		if(datas==null){
			return false;
		}
		if (datas.getData().length > 0xFFFFFFF) {
			logger.error("严重错误，发送数据包过大,严重错误！CDM:0x"+Integer.toHexString(datas.command)+",长度为" + datas.getData().length);
		}
		if (threadInProcess == null) {
			return false;
		}
		if(datas==null || datas.getSession()==null || datas.getSession().getAttribute("sessionIndex")==null){
			return false;
		}
		int index=(Integer)datas.getSession().getAttribute("sessionIndex");
		if(index>THREADCOUNT){
			logger.error(datas.getSession()+"接收出现异常...");
			return false;
		}
		return threadInProcess[index].receive(datas);	
	}
}
