/**
 * 
 */
package world.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import server.ServerEntrance;
import server.cmds.MapCP;

import world.World;


import db.model.Player;

/**
 * @author liuzg
 * 自动战斗管理
 */
public class AutoFightManager {
	private static Logger logger=Logger.getLogger(AutoFightManager.class);
	private static AutoFightManager instance;
	private AutoFightManager(){}
	public static AutoFightManager getInstance(){
		if(instance==null){
			instance=new AutoFightManager();
		}
		return instance;
	}
	// 自动战斗实体集合 key=holder#mapType,value=AutoFightEntry;
	private static ConcurrentHashMap<String, AutoFightEntry> autoFightEntryMaps = new ConcurrentHashMap<String, AutoFightEntry>();

	/**
	 * @author liuzg
	 * @param p
	 * @param mapType
	 * @return 玩家调用检测是否存在
	 */
	public static AutoFightEntry getAutoFightEntry(Player p, int mapType) {
		return autoFightEntryMaps.get(p.getId() + "#" + mapType);
	}
	/**
	 * @author liuzg
	 * @param p
	 * @param mapType
	 * @param entry
	 * 添加自动战斗实体进行管理,仅在实体建立时调用
	 */
	public static void addAutoFightEntry(int holder,int mapType,AutoFightEntry entry){
		autoFightEntryMaps.put(holder+"#"+mapType, entry);
	}
	
	/**
	 * @author liuzg
	 * 启动自动战斗管理线程
	 */
	public void startAutoFightManagerThread(){
		logger.info("启动自动战斗管理线程...");
		Runnable run=new Runnable(){
			public void run(){
				long lastExecTime=0;
				while(World.running()){
					try {
						if(System.currentTimeMillis()-lastExecTime>10*1000){
							lastExecTime=System.currentTimeMillis();
							tick();
						}
						Thread.sleep(1000*3);
					} catch (Exception e) {
						logger.error("线程中断异常:",e);
					}
				}
			}
		};
		ServerEntrance.runThread(run);
	}
	/**
	 * @author liuzg
	 * 自动战斗
	 */
	public void tick(){
		List<String> removeEntry=new ArrayList<String>();
		for(Map.Entry<String, AutoFightEntry> entry:autoFightEntryMaps.entrySet()){
			AutoFightEntry auto=entry.getValue();
			if(System.currentTimeMillis()-auto.lastTickTime<AutoFightEntry.PERGATECDTIME){
				continue;
			}
			auto.lastTickTime=System.currentTimeMillis();
			Player p=World.getPlayer(auto.getHolder());
			if(auto.isStopFight()){
				logger.info(p.getName()+"所在自动战斗已停止:mapType="+auto.getMapType());
				removeEntry.add(entry.getKey());
			}else{
				if(p==null || p.getIoSession()==null){
					logger.info(auto.getHolder()+"已退出游戏,将自动战斗实体停止!");
					//玩家已退出游戏
					auto.playerLogout();
				}else{
					auto.processRewardData();
				}
			}
		}
		//清除完成的自动战斗实体
		for(String move:removeEntry){
			logger.info("从内存中清除自动战斗实体:"+move);
			autoFightEntryMaps.remove(move);
		}
	}
	/**
	 * @author liuzg
	 * @param player
	 * 玩家登录时退出游戏
	 */
	public static void logoutAutoFight(Player player) {
		synchronized (player) {
			logger.info(player.getName()+"上线退出自动战斗!");
			AutoFightEntry auto = autoFightEntryMaps.get(player.getId() + "#"+ MapCP.MAP_001);
			if (auto != null) {
				logger.info(player.getName()+"上线退出自动战斗!"+auto.getMapType());
				auto.playerLogout();
			}
		}
	}
}
