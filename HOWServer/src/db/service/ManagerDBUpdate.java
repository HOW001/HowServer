package db.service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import world.World;
import db.model.Player;

/**
 * @author liuzg 所有数据库信息管理分为两部分: 1:玩家部分，统一有玩家作为保存对象，比如背包、装备 2:公共部分，由各自集合保存
 */
public class ManagerDBUpdate {
	private static Logger logger = Logger.getLogger(ManagerDBUpdate.class);

	public boolean isLock = false;
	private final static ManagerDBUpdate instance = new ManagerDBUpdate();
	/*
	 * 所有使用过的用户名=id，游戏启动时进行初始化和创建用户时进行添加
	 * 每用户占用:32+4=36Byte
	 * 100万用户占用约:35MB
	 */
    private static Map<String,Integer> usedNameMaps=new HashMap<String,Integer>();
    /*
     * 用户openID#服务器编号=playerID
     * 每用户占用:32+3+4=39Byte
     * 100万用户占用约:37MB
     */
    private static Map<String,Integer> openIDAndPlayerIDMaps=new HashMap<String,Integer>();
	/*
	 * 已经存在的用户Uid
	 * 每用户占用:32+4=36Byte
	 * 100万用户占用约:35MB
	 */
    private static Map<String,Integer> usedUid=new HashMap<String,Integer>();

	public static ManagerDBUpdate getInstance() {
		return instance;
	}

	private static ConcurrentHashMap<Integer, Player> buffer = new ConcurrentHashMap<Integer, Player>();

	/*
	 * 正在登录过过程中的玩家
	 */
	public static CopyOnWriteArrayList<String> ISLOGININGPLAYER=new CopyOnWriteArrayList<String>();
	
	private ManagerDBUpdate() {
	}

	/**
	 * @author liuzg 所有持久信息保存
	 * @param isShutDown
	 *            游戏运行时不保存在线玩家信息,只有玩家下线后才进行一次保存
	 */
	public synchronized void saveDBInfo(boolean isShutDown) {
		if (isLock == false) {
			buffer.clear();
			long saveTimes = System.currentTimeMillis();
			try {
				isLock = true;
//				isBug = false;
				logger.info("开始保存所有角色及相关信息!");
//				int index = 0;
				int number = 0;
//				if (isSaveOnLinePlayer) {
					number = World.players.size();
					for (Player player : World.players.values()) {	
						if(ISLOGININGPLAYER.contains(player.getId())){//这个id可以userID,表示唯一个玩家即可
							logger.info(player.getName()+"玩家登录期间不进行更新");
							continue;
						}
						logger.info("开始更新:<" + player.getName() + ">的信息!"
								+ (number));
						player.isSaveState=true;
						try {
							if(DBPlayerImp.getInstance().updatePlayer(player,"ManagerDBUpdataOnLine")==false){
								int version = DBPlayerImp.getInstance().getPlayerVersion(
										player.getId());
								if (version > 0) {
									logger.info(player.getName() + "从数据库中获取新的version="+ version + ",currentVersion="+ player.getVersion());
									player.setVersion(version);
								}
								player.isDBException++;
								if(player.isDBException>1){
									player.createDBException("saveDBInfo");
								}
							}else{
								player.isDBException=1;
							}
						} catch (Exception e) {
							logger.error(player.getName()+"例行更新异常:",e);
						}
						player.isSaveState=false;
						logger.info("完成更新:<" + player.getName() + ">的信息!"
								+ (number));
						number--;
					}
//				}
				ISLOGININGPLAYER.clear();
				for (Player player : World.bufferPlayers.values()) {
					buffer.put(player.getId(), player);
				}
				number = buffer.values().size();
				for (Player player : buffer.values()) {
					number--;
					logger.info("开始更新:<" + player.getName() + ">的buffer信息!"
							+ (number));
					DBPlayerImp.getInstance().updatePlayer(player,"ManagerDBUpdataBuffer");
					logger.info("完成更新:<" + player.getName() + ">的buffer信息!"
							+ (number));					
					logger.info(player.getName() + "保存后被移出缓冲区...");
					World.bufferPlayers.remove(player.getId());
					player=null;
				}
				
				logger.info((buffer.size()+World.players.size())+"个玩家保存所有DB信息用时:"
						+ (System.currentTimeMillis() - saveTimes) + "毫秒");
				buffer.clear();
//				logger.info("开始保存PK排行榜信息");
//				for(int index=1;index<=PKRankInfo.PKRANKLISTSIZE;index++){
//					PKRank ranker=PKRankInfo.getInstance().getPKRank(index);
//					if(ranker!=null){
//						DBPKRankImp.getInstance().update(ranker);
//					}else{
//						logger.info("获取第"+index+"名排行榜信息尚未加载!");
//					}
//				}
				logger.info("完成保存PK排行榜信息");
				
			} catch (Exception e) {
				logger.error("保存至数据库时出现异常:", e);
			} finally {
				isLock = false;
			}
		} else {
			logger.error("目前无法获取保存锁");
		}
	}

	/**
	 * @author liuzg
	 * 初始化数据库中的玩家数据
	 */
	public static void initPlayerDB(){
		DBPlayerImp.getInstance().initPlayerName(usedNameMaps);
		DBPlayerImp.getInstance().initPlayerIDForOpenIDAndServerName(openIDAndPlayerIDMaps);
//		DBUserImp.getInstance().initUID(usedUid);
	}
	/**
	 * @author liuzg
	 * @param name
	 * @return
	 * 昵称是否使用过
	 */
	public static boolean isUsedName(String name){
		name=name.toLowerCase();
		return usedNameMaps.containsKey(name);
	}
	/**
	 * @author liuzg
	 * @param id
	 * @return
	 * 玩家ID是否使用过
	 */
	public static boolean isUsedPlayerID(Integer id){
		for(Map.Entry<String, Integer> entry:usedNameMaps.entrySet()){
			if(entry.getValue()==id){
				return true;
			}
		}
		return false;
	}
	/**
	 * @author liuzg
	 * @param name
	 * 添加已使用过的昵称，在确定新建角色之前
	 */
	public static void addUsedName(String name){
		name=name.toLowerCase();
		usedNameMaps.put(name, -1);
	}
	/**
	 * @author liuzg
	 * @param name
	 * @return
	 * 取得用户名对应的玩家ID
	 */
	public static int getPlayerID(String name){
		name=name.toLowerCase();
		if(usedNameMaps.get(name)==null){
			return -1;
		}else{
			return usedNameMaps.get(name);
		}
	}
	/**
	 * @author liuzg
	 * @param id
	 * @return
	 * 根据角色ID获取角色昵称
	 */
	public static String getPlayerName(int id){
		for(Map.Entry<String, Integer> entry:usedNameMaps.entrySet()){
			if(entry.getValue()==id){
				return entry.getKey();
			}
		}
		return null;
	}
	/**
	 * @author liuzg
	 * @param name
	 * @param id
	 * 添加已使用过的昵称，在确定新建角色之后调用
	 */
	public static void addUsedName(String name,Integer id){
		name=name.toLowerCase();
		usedNameMaps.put(name, id);
	}
	/**
	 * @author liuzg
	 * @param userName
	 * @return
	 * 获取玩家ID
	 */
	public static int getPlayerIDForUserName(String userName){
		if(openIDAndPlayerIDMaps.get(userName)==null){
			return -1;
		}else{
			return openIDAndPlayerIDMaps.get(userName);
		}
	}
	/**
	 * @author liuzg
	 * @param userName
	 * @param playerID
	 * 添加playerID对应关系
	 */
	public static void addPlayerID(String userName,int playerID){
		openIDAndPlayerIDMaps.put(userName, playerID);
	}
	/**
	 * @author fengmx
	 * 添加UID
	 * @param uid
	 * @param id
	 */
	public static void addUid(String uid,int id){
		if(!usedUid.containsKey(uid)){
			usedUid.put(uid, id);
		}
	}
	/**
	 * @author fengmx
	 * 检测是否存在uid
	 * @param uid
	 * @return
	 */
	public static boolean isUsedUid(String uid){
		return usedUid.containsKey(uid);
	}
}
