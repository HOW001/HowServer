package world;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSession;
import server.RestartThreadListener;
import server.ServerConfigurationNew;
import server.ServerEntrance;
//import server.cmds.ActivityCP;
import server.cmds.CMDThread;
import server.cmds.ChatCP;
import server.cmds.CmdDispatch;
//import server.cmds.ExerciseCP;
import server.cmds.LoginCP;
import server.cmds.MailCP;
import server.cmds.MapCP;
//import server.cmds.OpenServiceActivityCP;
import server.cmds.RegisterCP;
//import server.cmds.TaskCP;
//import server.cmds.TrainCP;
import server.netserver.GameServerPortListener;
import server.netserver.MsgOutEntry;
//import server.netserver.SessionAttributeEntry;
import util.MathUtils;
import util.Util;
import util.binreader.GridData;
import util.binreader.PReader;
import util.logger.LoginLogger;
import world.event.SystemTouchEventManager;
//import world.object.ArenaCenter;
//import world.object.AuctionHouse;
import world.object.AutoFightManager;
//import world.object.Constellation;
import world.object.Contest;
import db.model.Player;
//import db.service.DBOnlineCheck;
import db.service.DBPlayerImp;
import db.service.DBUserImp;
import db.service.HibernateUtil;
import db.service.ManagerDBUpdate;

/**
 * 游戏世界
 * 
 */
public class World implements Runnable {
	private static Logger logger = Logger.getLogger(World.class);// World的日志
	/**
	 * 服务器人数
	 */
	private static int MAX_PLAYERS = 9999;
	/**
	 * 最高在线人数
	 */
	public int maxPlayers=0;
	/**
	 * 上次记录最高在线人数
	 */
	public long lastTime = 0;
	static {
		try {
			MAX_PLAYERS = ServerConfigurationNew.players;// ServerProperties.getInt("players");
		} catch (Exception e) {
			MAX_PLAYERS = 2000;
		}
	}
	/**
	 * 服务器是否超过了规定人数
	 */
	private boolean full = false;
	/**
	 * 储存玩家的时间
	 */
	public static final long SAVE_WORLD_TIME = Util.ONE_MIN*3;
	public static String anounceMent = "欢迎来到赛车大亨世界";
	public int RANK_UPDATE_TIME = 0;//排行榜数据更新时间点
	private static final int HEART_BEAT = 500;// 心跳时间
	/**
	 * 在线玩家
	 */
	public static ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();
	/**
	 * 缓冲玩家
	 */
	public static ConcurrentHashMap<Integer, Player> bufferPlayers = new ConcurrentHashMap<Integer, Player>();

	protected static java.util.Map<Integer, Scene> scenes = new HashMap<Integer, Scene>();// 所有的场景列表

    /**
     * 玩家登录次数
     */
	public static long LOGIN_PLAYERS_COUNT=0;
	/**
     * 最高在线人数
     */
	public  long MAX_ONLINE_PLAYERS=0;
	/**
     * 最低在线人数
     */
	public  long MIN_ONLINE_PLAYERS = 0;
	private  long GAMESTARTTIME=0;
	
	public static long NEWREGISTERPLAYER=0;
	
	public static boolean isPrintDetailInfo=true;
	/**
	 * 世界
	 */
	private World() {
		try {
			anounceMent = ServerConfigurationNew.anounncement;
		} catch (Exception ex) {
			logger.error("初始化World失败", ex);
		}
	}

	/**
	 * 初始化
	 */
	public void init() {
		GAMESTARTTIME=System.currentTimeMillis();
		ServerEntrance.runThread(this);
		processDBThread();
		processPlayerTickThread();
		loadMaps();
		processSceneThread();
		processContestThread();
		GameStaticValues.readStaticValues();
		processFixTimeRunThread();
		SystemTouchEventManager.getInstance();
		AutoFightManager.getInstance().startAutoFightManagerThread();
	}

	/**
	 * 载入地图
	 * 
	 */
	private void loadMaps() {
		try {
			for (GridData data : GridData.getData().values()) {
				if (data.type == Scene.TYPE_MAP) {
					scenes.put(data.id, new Map());
				} else if (data.type == Scene.TYPE_DUNGEON) {
					scenes.put(data.id, new Dungeon(data.id, 0));
				} else {
					scenes.put(data.id, new Map());
				}
				if (scenes.get(data.id) != null) {
					scenes.get(data.id).loadAllData(data);
				}
			}
			logger.info("载入地图完毕");
		} catch (Exception e) {
			logger.error("载入地图异常:", e);
		}
	}

	/**
	 * 默认当作是不生成新副本
	 * 
	 * @param index
	 * @param p
	 * @return
	 */
	public Scene getScene(int index, Player p) {
		return getScene(index, p, false);
	}

	/**
	 * @author liuzg
	 * @param index
	 * @param p
	 * @return 获取副本
	 */
	public Scene getDungeon(int index, Player p) {
		return getScene(index, p, true);
	}

	/**
	 * 根据场景索引和玩家获取地图 需要传入Player的原因是 需要根据Player的组队信息才能找到对应地下城副本
	 * 
	 * @param index
	 * @param p
	 * @param create
	 *            是否生成新副本
	 * @return
	 */
	public Scene getScene(int index, Player p, boolean create) {
		if (scenes == null) {
			return null;
		}
		if (scenes.get(index) == null) {
			logger.info("World.getMap错误的索引：" + index);
			return null;
		}
		if (p == null) {
			return scenes.get(index);
		}
		return getScene(GridData.getGridData(index), index, p, create);
	}

	protected static Scene getScene(GridData mapData, int index, Player p,
			boolean create) {
		if (mapData == null) {
			return null;
		}
		Scene scene = null;
		switch (mapData.type) {
		case Scene.TYPE_DUNGEON:// 普通副本
			if (scenes.get(index) == null) {// 此情况不应该出现
				logger.info("无法找到副本。" + index);
			} else {
				scene = scenes.get(index).getScene(p);
			}
			if (scene == null) {
				// 如果是入口
				if (create) {// 生成新副本
					long contestID=System.currentTimeMillis();
					scene = Dungeon.createDungeon(index, contestID);
					if (scene == null) {
						p.sendResult("无法生成更多的副本，请稍候尝试。");
						return null;
					}
				}
			}
			break;
		case Scene.TYPE_MAP:// 普通地图
		default:// 默认为普通地图
			if (scenes.get(index) != null) {
				return scenes.get(index);
			}

			scene = new Map();
			scene.loadAllData(mapData);
			break;
		}
		return scene;
	}

	/**
	 * 单例
	 */
	public final static World world = new World();

	public static World getInstance() {
		return world;
	}


	private long timeTaken;// 用于对心跳时间进行计算
	private static int olTickNum = 60000 / HEART_BEAT;
	private int onlineNumberTick = olTickNum;
	public long sendCommand = 0;// 每分钟发送的命令数
	public long receiveCommand = 0;// 每分钟接受的命令数
	public long size_send = 0;// 每分钟发送数据长度
	public long size_receive = 0;// 每分钟接收数据长度
	private long lastClearTime = 0;// 最后一次清除统计信息时间
	public long currentTime;// 计时
	private long lastSendMessageTime = 0;
	String msg = "";
	long time = 0;
	int times = 0;
	/**
	 * 设置循环消息
	 * @param msg
	 * @param time间隔时间
	 * @param times 次数
	 */
	public void setCycleMessage(String msg,long time,int times){
		this.msg = msg;
		this.time = time;
		this.times = times;
	}
	public long getCurrentTime() {
		return currentTime;
	}

	private static boolean running = true;

	public static boolean running() {
		return running;
	}

	private long lastSaveTime = 0;
	private int numberPlayers;
	private static long lastChatTime=System.currentTimeMillis();
	/**
	 * @author liuzg 数据库保存线程
	 */
	private void processDBThread() {
		Runnable r = new Runnable() {
			public void run() {
				ServerEntrance.threadPool.setThreadName(Thread.currentThread()
						.getName(), "processBuffThread");
				while (true) {//不可用World.running
					long times = System.currentTimeMillis();
					if (currentTime - lastSaveTime > SAVE_WORLD_TIME) {
						try {
							logger.info("players中的数量:" + players.size());
							logger.info("buffer中的数量:" + bufferPlayers.size());
							ManagerDBUpdate.getInstance().saveDBInfo(false);
							lastSaveTime = currentTime;							
						} catch (Exception e) {
							logger.error("processDB thread", e);
						}
					}
					long useTimes = System.currentTimeMillis() - times;
					if(useTimes>=1000){
						logger.error("20121018184619线程运行时间过长" + useTimes);
					}
					if(System.currentTimeMillis()-lastChatTime>1000*30){
						sendOnLinePlayerInfo();
						lastChatTime=System.currentTimeMillis();
					}
					try {
						Thread.sleep(HEART_BEAT);
					} catch (InterruptedException e) {
						logger.error("processDB thread<1>", e);
					}
				}
			}
		};
		ServerEntrance.runThread(r);
	}

	private static long LASTSCENETHREADRUNNINGTIME = 0;
	

	/**
	 * @author liuzg 处理比赛实体线程
	 */
	private void processContestThread() {
		Runnable r = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				ServerEntrance.threadPool.setThreadName(Thread.currentThread()
						.getName(), "processContestThread");
				logger.info("启动比赛线程实体.....");
				while (running) {
					try {
						times = System.currentTimeMillis();
						List<Long> deleteContestEntry = new ArrayList<Long>();
						for (Contest contest : Contest.ContestMaps.values()) {
							try {
								if(contest==null){
									continue;
								}
								contest.tick();
								if (contest.isContestEnd()
										&& System.currentTimeMillis()
												- contest.endTime > contest.distoryTime && contest.endTime>0) {// 战斗已完成或已达到销毁时间,并且可以从内存中清除
									deleteContestEntry.add(contest.ID);
									contest.dispose();
									contest=null;
								}
								if (contest != null) {
									if (System.currentTimeMillis()- contest.createTime > Contest.MAXCREATESAVETIME&& contest.endTime > 0) {// 最大保留时间
										deleteContestEntry.add(contest.ID);
										contest.dispose();
										contest = null;
									}
								}
							} catch (Exception e) {
								logger.error("某一比赛实体心跳异常:",e);
							}
						}
						for (long id : deleteContestEntry) {
							Contest.ContestMaps.remove(id);
							logger.info("移除比赛实体:"+id);
						}
						// logger.info("检测比赛线程实体.....");
					} catch (Exception e) {
						logger.error("比赛线程实体心跳异常:",e);
					}
					long useTimes = System.currentTimeMillis() - times;
					if(useTimes>=100){
						logger.error("processContestThread()线程运行时间过长" + useTimes);
					}
					try {
						Thread.sleep(HEART_BEAT * 10);
					} catch (Exception e) {
						logger.error("比赛线程实体心跳睡眠异常",e);
					}
					
				}			
			}
		};
		ServerEntrance.runThread(r);
	}

	/**
	 * @author liuzg 处理场景线程
	 */
	private void processSceneThread() {
		Runnable r = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				ServerEntrance.threadPool.setThreadName(Thread.currentThread()
						.getName(), "processSceneThread");
				while (running) {
					try {
						times = System.currentTimeMillis();
						if (System.currentTimeMillis()
								- LASTSCENETHREADRUNNINGTIME > Util.ONE_MIN) {
							LASTSCENETHREADRUNNINGTIME = System.currentTimeMillis();
							for (Scene s : scenes.values()) {
								if (s != null) {
									try {
										s.tick();
									} catch (Exception e) {
										logger.error(s + "tick", e);
									}
								}
							}
						}
						long useTimes = System.currentTimeMillis() - times;
						if(useTimes>=100){
							logger.error("processSceneThread()线程运行时间过长" + useTimes);
						}
					Thread.sleep(HEART_BEAT*10);// 修改暂停时间
					} catch (Exception e) {
						logger.error("场景线程处理异常:",e);
					}
					if (System.currentTimeMillis() - LASTSCENETHREADRUNNINGTIME > 1000 * 60) {
						break;
					}
				}
				
			}
		};
		ServerEntrance.runThread(r);
	}

	private static long lastPlayerTickTime = 0;
	private static long perTickTime = 30000;

	/**
	 * @author liuzg 玩家线程处理
	 */
	private void processPlayerTickThread() {
		Runnable r = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				ServerEntrance.threadPool.setThreadName(Thread.currentThread()
						.getName(), "processPlayerThread");
				while (running) {
					times = System.currentTimeMillis();
					long used = System.currentTimeMillis();
					try {
						if (System.currentTimeMillis() - lastPlayerTickTime > perTickTime) {
							for (Player p : players.values()) {	
								if(p.isCompleteLogin){
									p.tick();
									p.sendResult(p.getName()+"心跳正在进行"+times);
								}
							}
							lastPlayerTickTime = System.currentTimeMillis();
							used = System.currentTimeMillis() - used;
							if (used > perTickTime/3) {
								logger.error(players.size()+"个玩家心跳用时:" + used+"毫秒!");
							}

							used=System.currentTimeMillis();
						}
						long useTimes = System.currentTimeMillis() - times;
						if(useTimes>=1000){
							logger.error("processPlayerTickThread()线程运行时间过长" + useTimes);
						}
						Thread.sleep(perTickTime);
					} catch (Exception e) {
						logger.error("players heart beat ", e);
					}
				
				}	
			}
		};
		ServerEntrance.runThread(r);
	}

	/**
	 * 时间系统
	 */
	private static int hourTime = 0;

	public static int getHourTime() {
		return hourTime;
	}

	private void setTimes() {
		Calendar c = GregorianCalendar.getInstance();
		hourTime = c.get(Calendar.HOUR_OF_DAY);
		hourTime += 1000000 * (c.get(Calendar.YEAR));
		hourTime += 10000 * (c.get(Calendar.MONTH) + 1);
		hourTime += 100 * c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 主线程
	 */
	public void run() {
		ServerEntrance.threadPool.setThreadName(Thread.currentThread()
				.getName(), "WorldMainThread");
		while (running) {
			try {
				setTimes();
				currentTime = System.currentTimeMillis();
				if (onlineNumberTick-- < 0) {
					onlineNumberTick = olTickNum;
					// 服务器状态： 每分钟接受命令数 / 每分钟发送命令数 在线玩家数量
					StringBuffer sb = new StringBuffer();
					sb.append("系统负担: 每分钟接受");
					sb.append(receiveCommand).append("条[")
							.append((size_receive >> 10)).append("k]/发送");
					sb.append(sendCommand).append("条[")
							.append(( size_send>> 10)).append("k]共");
					sb.append(players.size()).append("玩家在线");
					logger.info(sb.toString());
//					int LINKNUMBER=0;
//					if(players.size()>0){
//						for(Player p:players.values()){
//							LINKNUMBER=p.getIoSession().getService().getManagedSessionCount();
//						}
//					}
					if(players.size()>MAX_ONLINE_PLAYERS){
						MAX_ONLINE_PLAYERS=players.size();
					}
					if(players.size()<MIN_ONLINE_PLAYERS){
						MIN_ONLINE_PLAYERS=players.size();
					}
					logger.info("当前玩家：" + players.size() + ",当前缓冲玩家:"
							+ bufferPlayers.size() + ",当前连接数量:" +GameServerPortListener.socket.acceptor.getManagedSessionCount());
					logger.info("最高在线玩家人数:"+MAX_ONLINE_PLAYERS+"最低在线玩家人数:"+MIN_ONLINE_PLAYERS+",登录游戏总人数:"+LOGIN_PLAYERS_COUNT);
					logger.info("内存共有空间："
							+ (Runtime.getRuntime().totalMemory() / 1000000)
							+ "MB,已用："
							+ ((Runtime.getRuntime().totalMemory() - Runtime
									.getRuntime().freeMemory()) / 1000000)
							+ "MB,剩余："
							+ (Runtime.getRuntime().freeMemory() / 1000000)
							+ "MB");
//					System.gc();
					numberPlayers = players.size();
//					DBOnlineCheck.getInstance().saveOnline(numberPlayers);
					logger.info("最大数据长度:len="+MsgOutEntry.MAX_MSG_LENGTH+"byte,最大数据命令:cmd="+Integer.toHexString(MsgOutEntry.MAX_MSG_CMD));
					logger.info("游戏世界已运行:"+(System.currentTimeMillis()-GAMESTARTTIME)/Util.ONE_MIN+"分钟!");
					logger.info("登录游戏人数次数为:"+LoginCP.loginNumber+"人次,平均用时:"+(LoginCP.loginUseTime/LoginCP.loginNumber)+"毫秒,新注册玩家为:"+NEWREGISTERPLAYER+"人");
					HibernateUtil.printCacheUseInfo();
					if (System.currentTimeMillis() - lastClearTime > Util.ONE_MIN) {
						logger.info("*******************清空一次数据********************");
						lastClearTime = System.currentTimeMillis();
						sendCommand = 0;
						receiveCommand = 0;
						size_send = 0;
						size_receive = 0;
					}
					if(CmdDispatch.LastReceiveTime-CMDThread.LastProcessTime>=Util.ONE_MIN*3){
						/*
						 * 当最后接收信息的时间与最后处理信息的时间相差超过一分钟时,即被认定为接收线程死亡,需要重重启
						 */
						logger.error("由于出现线程问题,游戏主动关闭!");
						ServerEntrance.shutdown("由于出现线程问题,游戏主动关闭");
//						CmdDispatch.getInstance().restartThread();
					}
//					ServerMonitor.getMonitor().CheckDeadLocks();
				}
//				//5秒后将被封号的玩家踢出游戏
//				if (System.currentTimeMillis() - GameManageCP.stopName >= 5000) {
//					GameManageCP.getInstance().stopName();
//				}
				timeTaken = System.currentTimeMillis() - currentTime;
				if (timeTaken < HEART_BEAT) {
					synchronized (this) {
						wait(HEART_BEAT - timeTaken); // 使每个周期大致相等
					}
				} else {
					logger.info("world.run timeused:" + timeTaken);
					Thread.yield(); // 让出控制权,防止饿死其它线程
				}
				//发送循环消息
				try {
					if(lastSendMessageTime == 0){
						lastSendMessageTime = currentTime;
					}
					if (time > 0) {
						if (currentTime - lastSendMessageTime > time * 1000) {
							if (times > 0) {
								ChatCP.sendSystemMsgToAllPlayerFromGame(msg);
								lastSendMessageTime = currentTime;
								times--;
								logger.error("发送循环消息时出现异常:"+msg);
							}
						}
					}
				} catch (Exception e) {
					lastSendMessageTime = currentTime;
					logger.error("发送循环消息时出现异常:",e);
				}
//				Thread.sleep(HEART_BEAT);
			} catch (Exception ex) {
				logger.error("World线程出错", ex);
			}
		}
	}

	public static void setAnnounceMent(String msg) {
		anounceMent = msg;
	}
    
	/**
	 * @author liuzg
	 * @param msg
	 * 发布一次系统消息
	 */
	public static void sendWorldMsg(String msg){
		ChatCP.sendWorldMsgToAllPlayerFromGame(msg);
	}
	/**
	 * 给所有玩家发消息
	 * 
	 * @param msg
	 */
	public static void announce(String msg) {
		logger.info("向所有玩家发送公告信息:=======================>"+msg);
		ChatCP.sendSystemMsgToAllPlayerFromGame(msg);
	}

	/**
	 * 玩家掉线或者主动退出游戏
	 */
	public void logout(Player p,String msg) {
		if (p != null) {
			p.setLastLogoutTime(World.getInstance().getCurrentTime());// 设置离线时间
			p.logout(msg);
		}
	}

	public void logout(IoSession session,String msg) {
		if (session != null) {
			Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
			if(player!=null){
				player.setLastLogoutTime(World.getInstance().getCurrentTime());// 设置离线时间
				player.logout(player.getName()+":"+msg);
			}
		}
	}

	/**
	 * 根据GAMEID取player
	 * 
	 * @param gameID
	 * @return
	 */
	public Player getPlayerByGameID(int gameID) {
		return getPlayerByID(gameID & 0x0FFFFFFF);
	}

	/**
	 * 根据ID取player
	 * 
	 * @param id
	 * @return
	 */
	public Player getPlayerByID(int id) {
		return players.get(id );
	}

	/**
	 * @author liuzg
	 * @param name
	 * @return
	 */
	public Player getPlayerByName(String name) {
		for (Player player : players.values()) {
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}

	public static void addToPlayer(Player role) {
		bufferPlayers.remove(role.getId());
		players.put(role.getId(), role);
		logger.info("当前玩家数量:" + players.size());
	}

	/**
	 * 玩家登陆到服务器 完成添加到players的操作
	 * 
	 * @param role
	 */
	public static void login(Player role) {
		if (players.containsKey(role.getId())) {
			Scene m = world.getScene(role.getSceneID(), role);
			if (m == null) {// 这种情况一般是副本内登录
				// 移动到复活点
				logger.error("找不到相关地图:" + role.getSceneID());
				if (summonRezMap(role) == false) {
					role.sendResult("无法取得地图，请联系GM解决。");
					return;
				}
			} else {
				role.setCurrentScene(m);
				MapCP.getInsatance().sendMapData(role);
				m.addCreature(role);
			}
		}
	}

	/**
	 * 根据ID寻找玩家 既可以传入gameID也可以是 roleID
	 * 
	 * @param id
	 * @return
	 */
	public static Player getPlayer(int id) {
		Player p = players.get(id & 0x0FFFFFFF);
		if (p != null) {
			return p;
		}
		return null;
	}
	public static Player getPlayerByOpenID(String openID) {
		for(Player player:players.values()){
			if(player.getUserName().equalsIgnoreCase(openID)){
				return player;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param p
	 * @param mapIndex
	 * @param x
	 * @param y
	 * @param trans
	 *            是否是传送
	 * @return
	 */
	public static boolean summonPlayer(Player p, int mapIndex) {
		GridData data = GridData.getGridData(mapIndex);
		if (adjustMapData(p, data)) {
			Scene toMap = world.getScene(mapIndex, p);
			if (toMap != null) {
				return p.transforMap(toMap);
			}
		}
		return false;
	}

	/**
	 * 是否可传送
	 * 
	 * @param p
	 * @param data
	 * @param isTrans
	 * @return
	 */
	private static boolean adjustMapData(Player p, GridData data) {
		if (p == null)
			return false;
		if (data == null) {
			p.sendResult("无法找到地图");
			return false;
		}
		return true;
	}

	/**
	 * 召唤至复活地图
	 * 
	 * @param p
	 * @param mapIndex
	 * @return
	 */
	public static boolean summonRezMap(Player p) {
		GridData data = GridData.getMainSceneData();
		if (data == null) {
			return false;
		}
		return summonPlayer(p, data.id);
	}

	/**
	 * 服务器主动关闭连接
	 * 
	 * @param NioSession
	 *            session
	 * @param String
	 *            msg
	 */
	public static void closeHandler(NioSession session, String msg) {
		if (session == null) {
			return;
		}
		if (session.isClosing()) {
			return;
		}		
		session.removeAttribute(Player.PLAYERKEY);
		session.removeAttribute(Player.USERKEY);
		logger.error(session+"请求关闭session连接201208161328");
		logger.error(session+"正常下线,关闭session...");
		session.close();
	}

	/**
	 * 踢出某玩家 同时发送消息
	 * 
	 * @param Player
	 *            p 玩家
	 * @param String
	 *            msg 消息
	 */
	public static void kickoutPlayer(Player p, String msg) {
		if (p == null) {
			return;
		}
		if (msg == null) {
			msg = "您被系统踢出游戏";
		}
		logger.error(p.getName()+"被踢出游戏,原因:"+msg);
		closeHandler(p.getIoSession(), msg);
		world.logout(p,msg);
	}

	/**
	 * 服务器在线人数
	 * 
	 * @return
	 */
	public static int getPlayerSize() {
		return players.size();
	}

	public static void stop() {
		running = false;
	}


	/**
	 * 移除玩家并放在缓冲区中
	 * 
	 * @param player
	 */
	public static void addToBuffer(Player player) {
		if (player == null){
			return;
		}
		players.remove(player.getId());
		bufferPlayers.put(player.getId(), player);
	}
	/**
	 * @author liuzg
	 * @param id
	 * @return
	 * 登录时查找缓存中是否有玩家,如果有则把该玩家从缓存中取出，然后放入player的内存中，
	 * 此行为的出现是由于玩家下线时间小于DB保存间隔
	 */
    public Player loginFindBufferPlayer(int id){
    	return bufferPlayers.remove(id);
    }
	/**
	 * 优先取得游戏世界玩家，或者取得缓冲区玩家
	 * 
	 * @param id
	 * @return
	 */
	public Player getBufferPlayer(int id) {
		Player p = players.get(id & 0x0FFFFFFF);
		if (p != null) {
			return p;
		}
		p = bufferPlayers.get(id & 0x0FFFFFFF);
		if (p != null) {
			return p;
		}
		if (p == null) {
			p = bufferPlayers.get(id);
		}
		if (p == null) {
			return null;
		}
		return p;
	}

	/**
	 * @author liuzg
	 * @param name
	 * @return 获取缓存中的所有玩家
	 */
	public Player getBufferPlayerByName(String name) {
		for (Player player : bufferPlayers.values()) {
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}

	/**
	 * 获取指定ID的场景
	 * 
	 * @param sceneID
	 * @return
	 */
	public static Scene getScene(int sceneID) {
		return scenes.get(sceneID);
	}

	public static void setMaxPlayer(int max) {
		MAX_PLAYERS = max;
		logger.info("服务器人数上限设置为" + max);
	}

	public static int getMaxPlayer() {
		return MAX_PLAYERS;
	}

	/**
	 * 服务器是否已满
	 * 
	 * @return
	 */
	public boolean isFull() {
		full = players.size() >= MAX_PLAYERS;
		if(GameServerPortListener.socket.acceptor.getManagedSessionCount()>=MAX_PLAYERS+100){
			full=true;	
		}
		return full;
	}

	private long lastSaveRunTime = 0;
	public static boolean stopSaveThread = false;

	/*
	 *保存BI所需数据 
	 */
	private long lastSaveBIData=0;
	/*
	 * 最后一次保存的天数,一天只保存一次
	 */
	private int  lastSaveDay=0;
	
	/*
	 * 最后一次加载server.xml配置表的时间
	 */
	private long lastLoadConfigFileTime=0;
	
	/*
	 *进行一次openKey续期 
	 */
	private long lastCheckIsLoginTime=0;
	/**
	 * @author liuzg 每固定时间执行一次
	 */
	private void processFixTimeRunThread() {
		Runnable run = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				ServerEntrance.threadPool.setThreadName(Thread.currentThread()
						.getName(), "processSaveThread");
				Calendar calendar = Calendar.getInstance();
				int lastDay=0;
				while (running && stopSaveThread == false) {
					try {
						if (System.currentTimeMillis() - lastSaveRunTime >= Util.ONE_MIN * 15) {
							PReader.getInstance().reLoad();
							GameStaticValues.writeStaticValues();
							lastSaveRunTime = System.currentTimeMillis();
							checkThreadRunState();		
						}
						//邮件相关
						MailCP.getInstance().deleteTimeOut(calendar);
						if(lastSaveDay!=calendar.get(Calendar.DAY_OF_YEAR)&&System.currentTimeMillis()-lastSaveBIData>Util.ONE_MIN*15){
							logger.info("记录一次BI信息...");
							sendBIData();
						}
						List<String> playerNumber = new ArrayList<String>();
						String str = "playerNumber";
						String txtName = "monitor";
						String result=null;
						/**0:未到达最大人数的80%，1:已到达*/
						if(System.currentTimeMillis()-lastLoadConfigFileTime>Util.ONE_MIN*15){
							if(players.size()>=(MAX_PLAYERS+1)*4/5){
								result = str+"="+1;
								playerNumber.add(result);
								saveMaxPlayerData(txtName, playerNumber);
								playerNumber.clear();
								logger.info("已达到最大人数的80%，写入文件"+txtName+".txt"+"时间"+new Date());
							}else{
								result = str+"="+0;
								playerNumber.add(result);
								saveMaxPlayerData(txtName, playerNumber);
								playerNumber.clear();
								logger.info("未达到最大人数的80%，写入文件"+txtName+".txt"+"时间"+new Date());
							}
//							ChatCP.sendSystemMsgToAllPlayerFromGame("距离下次PK擂台开奖时间还有:"+PKRankInfo.getNextAwardIntervalDesc()+"!");
							lastLoadConfigFileTime=System.currentTimeMillis();
							ServerEntrance.loadServerConfigurationNew();
							RegisterCP.isCanRegister=ServerConfigurationNew.canRegister;
							logger.info("当前服务器可容纳人数:"+ServerConfigurationNew.players);
						}
						calcPlayerActionValue();
						Thread.sleep(Util.ONE_MIN);
					} catch (Exception e) {
						logger.error("定时保存线程异常:", e);
					}
				}
				logger.error("processSaveThread线程停止");
				long useTimes = System.currentTimeMillis() - times;
				if(useTimes>=100){
					logger.error("processFixTimeRunThread()线程运行时间过长" + useTimes);
				}
			}
		};
		ServerEntrance.runThread(run);
	}
	/**
	 * 发送BI数据
	 */
	public void sendBIData(){
		logger.info("记录一次BI信息...");
		Calendar calendar = Calendar.getInstance();
		lastSaveDay=calendar.get(Calendar.DAY_OF_YEAR);
		lastSaveBIData=System.currentTimeMillis();
//		List<String> dataInfo=DBUserImp.getInstance().getUserInfoForYesterday();
//		saveBIData("USER",dataInfo);
//		List<String> dataInfo=null;
//		dataInfo=DBPlayerImp.getInstance().getPlayerInfoForBI();
//		saveBIData("PLAYER",dataInfo);
//		dataInfo=null;
	}
	/**
	 * @author liuzg 检测线程运行状态
	 */
	public void checkThreadRunState() {
		ServerEntrance.threadPool.listRunThread();
	}

	/**
	 * @author liuzg 重启相关线程
	 */
	public void restartThread(int type) {
		switch (type) {
		case RestartThreadListener.TYPE_RESTART_CMDTHREAD:
			logger.info("收到重启命令执行线程");
			CmdDispatch.getInstance().restartThread();
			break;
		default:
			logger.error("收到无法解析的重启线程类型");
		}
	}
	/**
	 * @author liuzg
	 * @param type
	 * @param data
	 * 保存BI数据
	 */
	private void saveBIData(String type,List<String> data){
		String path=ServerConfigurationNew.absolutePathRef;	
		try {
			File file=new File(path);
			if(file.exists()==false){
				file.mkdirs();
			}
			String fileName=ServerConfigurationNew.absolutePathRef+"/"+type+"_"+Util.getFormatDataToString(new Date())+".txt";
			file=new File(fileName);
//			FileWriter fw=new FileWriter(file);
			FileOutputStream fos=new FileOutputStream(file);
			OutputStreamWriter osw=new OutputStreamWriter(fos,Charset.forName("utf-8"));
			BufferedWriter bw=new BufferedWriter(osw);
			for(String str:data){
				bw.write(str);
				bw.newLine();
			}
			bw.close();
			osw.close();
		} catch (Exception e) {
			logger.error("保存BI数据时出现异常:",e);
		}
	}
	/**
	 * @author zhangqiang
	 * @param type
	 * @param data
	 * 保存最大人数监控数据
	 */
	@SuppressWarnings("unused")
	private void saveMaxPlayerData(String type,List<String> data){
		String path=ServerEntrance.serverPath+"res";
		try {
			if(path==null){
				return;
			}else{
				File file=new File(path);
				if(file.exists()==false){
					file.mkdirs();
				}
				String fileName=path+"/"+type+".txt";
				file=new File(fileName);
				FileOutputStream fos=new FileOutputStream(file);
				OutputStreamWriter osw=new OutputStreamWriter(fos,Charset.forName("utf-8"));
				BufferedWriter bw=new BufferedWriter(osw);
				for(String str:data){
					bw.write(str);
					bw.newLine();
				}
				bw.close();
				osw.close();
			}
		} catch (Exception e) {
			logger.error("保存最大人数监控数据时出现异常:",e);
		}
	}
	/**
	 * 获取比例
	 */
	private final static int perRate=5;
	/**
	 * 获取一些玩家
	 * @param exceptID 指定玩家除外
	 * @return
	 */
	public List<Integer> getSomePlayerID(int exceptID){
		List<Integer> playerList = new ArrayList<Integer>();
		for(Player player :World.players.values()) {
			if(player.getId()==exceptID){
				continue;
			}
			int random=MathUtils.random(1, 100);
			if(random>perRate){
				continue;
			}
			if(playerList.size()>World.players.size()*perRate/100){
				break;
			}
			playerList.add(player.getId());
		}
		return playerList;
	}
	
	/*
	 *当前在线玩家列表 
	 */
	private ConcurrentHashMap<Integer,Integer> onLinePlayerIDMaps=new ConcurrentHashMap<Integer,Integer>();
	/*
	 * 最后一次更新在线玩家ID时间
	 */
	private long lastUpdatePlayerIDTime=0;
	private final static int MAXPKSIZE=13;
	
	/**
	 * @author liuzg
	 * 发送在线玩家信息
	 */
	private void sendOnLinePlayerInfo(){
		try {
			if(running==false){
				return;
			}		
			if (System.currentTimeMillis() - lastUpdatePlayerIDTime > Util.ONE_MIN * 1) {
				lastUpdatePlayerIDTime = System.currentTimeMillis();
				onLinePlayerIDMaps.clear();
				int i = 0;// 玩家索引顺序
				for (Player p : World.players.values()) {
					onLinePlayerIDMaps.put(i, p.getGameID());
					i++;
				}
			}
		} catch (Exception e) {
			logger.error("发送在线玩家信息时出现异常:",e);
		}
	}
	private long lastCalcTime=0;//最后一次计算体力恢复的时间
	/**
	 * @author liuzhigang
	 * 计算玩家体力恢复
	 */
	private void calcPlayerActionValue(){
		if(System.currentTimeMillis()-lastCalcTime>Util.ONE_MIN*10){
			Calendar now=Calendar.getInstance();
			boolean isCalc=false;
			if((now.get(Calendar.HOUR_OF_DAY)==12 || now.get(Calendar.HOUR_OF_DAY)==18)&& now.get(Calendar.MINUTE)<=5){
				isCalc=true;
				lastCalcTime=System.currentTimeMillis();
			}
			if(isCalc){
				//开始给玩家增加体力点数,目前只给在线玩家增加
				for(Player player:players.values()){
					player.addActionValue(50);
				}
			}
		}
	}
}
