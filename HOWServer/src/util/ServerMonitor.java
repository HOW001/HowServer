package util;

import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author liuzg
 * 检测当前虚拟机状态
 */
public class ServerMonitor {
	private Logger logger = Logger.getLogger(ServerMonitor.class);
	
	private static final int maxDepth = 10;
	private ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	
	private static ServerMonitor instance = null;
	public static ServerMonitor getMonitor(){
		if(instance == null){
			instance = new ServerMonitor();
		}
		return instance;
	}
	private ServerMonitor(){}
	
	public static void main(String[] argv){
		String[] strs = ServerMonitor.getMonitor().CheckDeadLocks();
		for(int i=0; i<strs.length; i++){
			System.err.println(strs[i]);
		}
	}
	
	public String[] CheckDeadLocks(){
		String[] strs = new String[2];
		strs[0] = checkSyncDeadLock();
		strs[1] = checkWaitDeadLock();
		return strs;
	}	
	/**
	 * @author liuzg
	 * @return
	 * 检测线程同步死锁
	 */
	private String checkSyncDeadLock(){
		long[] syncIds = bean.findDeadlockedThreads();
		ThreadInfo[] infos = getThreadInfos(syncIds);
		logger.info("同步死锁状态:");
		StringBuffer sb = new StringBuffer();
		for(ThreadInfo info : infos){
			sb.append("Getter Thread->");
			sb.append(info.getThreadName() + " ");
			sb.append("Object->");
			sb.append(info.getLockInfo() + " ");
			sb.append("Owner Thread->");
			sb.append(info.getLockOwnerName());
			StackTraceElement[] element = info.getStackTrace();
			for(StackTraceElement e : element){
				sb.append(e);
			}
			logger.info(sb.toString());
		}
		return sb.toString();
	}
	/**
	 * @author liuzg
	 * @return
	 * 检测线程等待死锁
	 */
	private String checkWaitDeadLock(){
		long[] waitIds = bean.findMonitorDeadlockedThreads();
		ThreadInfo[] infos = getThreadInfos(waitIds);
		logger.info("等待死锁状态:");
		StringBuffer sb = new StringBuffer();
		for(ThreadInfo info : infos){
			sb.append("Getter Thread->");
			sb.append(info.getThreadName());
			StackTraceElement[] element = info.getStackTrace();
			for(StackTraceElement e : element){
				sb.append(e);
			}
			logger.info(sb.toString());
		}
		return sb.toString();
	}
	private ThreadInfo[] getThreadInfos(long[] ids){
		if(ids == null || ids.length == 0){
			return new ThreadInfo[0];
		}
		ThreadInfo[] infos = new ThreadInfo[ids.length];
		for(int i=0; i<ids.length; i++){
			infos[i] = bean.getThreadInfo(ids[i],maxDepth);
		}
		return infos;
	}
}
