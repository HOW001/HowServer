package server.netserver.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


public class ThreadPool {
	
	private static Logger logger=Logger.getLogger(ThreadPool.class);
	
	public static final int MAX_THREADS = 400;// maxThreads的最大值
//	public static final int MAX_THREADS_MIN = 100;// 10;// maxThreads的最小值
//	public static final int MAX_SPARE_THREADS = 200;// 50;// 最大空闲线程
	public static final int MIN_SPARE_THREADS = 20;// 4;//池中所保存的线程数
													// 最小空闲线程（当线程池初始化时就启动这么多线程）
													// 当要追加开启线程时，追加开启这么多线程
	public static final int WORK_WAIT_TIMEOUT = 60 * 1000;// 最大等待时间（1分钟），Monitor每隔这么长时间检查一次空闲线程
    private static ThreadPool instance=null;
    ThreadPoolExec executor;
    TaskQueue taskqueue;
    public static ThreadPool getInstance(){
    	if(instance==null){
    		instance=new ThreadPool();
    	}
    	return instance;
    }
    public static ThreadPool getNewInstance(){
    	if(instance!=null){
    		instance.shutdown();
    	}
    	instance=new ThreadPool();
    	return instance;
    }
    private ThreadPool(){};
    public void start(){
    	   taskqueue = new TaskQueue();
           TaskThreadFactory tf = new TaskThreadFactory("car_",false,Thread.NORM_PRIORITY);
           executor = new ThreadPoolExec(MIN_SPARE_THREADS, MAX_THREADS, WORK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS,taskqueue, tf);
           taskqueue.setParent( (ThreadPoolExec) executor);
    }
    /**
     * @author liuzg
     * @param run
     */
    public void run(Runnable run){
    	if(executor!=null){
    		try {
				executor.execute(run);
			} catch (Exception e) {
				logger.error("线程执行异常:",e);
			}
    	}
    }
    /**
     * @author liuzg
     * 关闭线程池
     */
    public void shutdown(){
       if(executor!=null){
    	   logger.error("关闭线程池.....");
    	   executor.shutdown();
       }
    }
    /**
	    * @author liuzg
	    * 查看当前正在运行的线程
	    */
	   public void listRunThread(){
		   logger.info("主动执行任务的近似线程数:"+executor.getActiveCount());
		   logger.info("池中的当前线程数:"+executor.getPoolSize());
		   logger.info("曾经同时位于池中的最大线程数:"+executor.getLargestPoolSize());
		   logger.info("允许的最大线程数:"+executor.getMaximumPoolSize());
		   logger.info("已完成执行的近似任务总数:"+executor.getCompletedTaskCount());
		   
	   }
    private static Map<String,String> threadNameMaps=new HashMap<String,String>();
    public void setThreadName(String threadKey,String threadName){
		   threadNameMaps.put(threadKey, threadName);
	   }
}
