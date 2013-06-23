package world;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import db.model.Player;
import util.binreader.*;
import world.object.Creature;
import world.object.Monster;

/**
 * 场景
 * 
 * @see World
 */
public class Scene {
	private static Logger logger = Logger.getLogger(Scene.class);
	/**
	 * 普通地图
	 */
	public static final byte TYPE_MAP = 1;
	/**
	 * 普通地下城
	 */
	public static final byte TYPE_DUNGEON = 2;

	/**
	 * 不活跃计数器(心跳为0.3秒)
	 * 
	 * 默认为5分钟 = 300秒 = 300/0.5 = 600 TICK
	 */
	protected static final int INACTIVE_TICKER = 600;
	/**
	 * 副本的不活跃计数器 //默认为30分钟 = 5 = 3000 TICK 改为5分钟
	 */
	protected static final int INACTIVE_TICKER_DUNGEON = 600;
	protected int inactiveTime = INACTIVE_TICKER;

	protected List<Player> players;// 玩家
	protected ConcurrentHashMap<Integer, Monster> npcs;// NPC
	protected ConcurrentHashMap<Integer, Creature> creatures;// 活物
	/**
	 * 场景类型 默认为TYPE_MAP
	 * 
	 * @see TYPE_MAP,TYPE_DUNGEON,TYPE_BATTLEGROUND,TYPE_ARENA
	 */
	protected int type = TYPE_MAP;
	protected int sceneID;// 场景ID
	protected long dungeonID;// 副本进度ID

	public int getSceneID() {
		return sceneID;
	}
	/**
	 * 构造函数
	 */
	protected Scene(int type) {
		setType(type);
		initlize(null);
	}

	protected Scene(int type, List<Player> players) {
		setType(type);
		initlize(players);
	}

	protected Scene() {
		this(TYPE_MAP);
	}

	protected void setType(int type) {
		this.type = (byte) type;
		switch (type) {
		case TYPE_DUNGEON:
			inactiveTime = INACTIVE_TICKER_DUNGEON;
			break;
		default:
			inactiveTime = INACTIVE_TICKER;
			break;
		}
	}

	/**
	 * 载入所有数据,在系统初始时调用或生成副本
	 * 
	 * @param index
	 */
	protected void loadAllData(GridData data) {
		sceneID = data.id;
		setMapData(data);
	}
	/**
	 * 初始化信息
	 */
	protected void initlize(List<Player> players) {
		if (players == null) {
			this.players = new ArrayList<Player>();
		} else {
			this.players = players;
		}
		if (npcs == null) {
			npcs = new ConcurrentHashMap<Integer, Monster>();
		}
		if (creatures == null) {
			creatures = new ConcurrentHashMap<Integer, Creature>();
		}
	}

	/**
	 * 添加活物
	 * 
	 * @param one
	 */
	public void addCreature(Creature one) {
		if (one == null) {
			return;
		}
		if (creatures.contains(one)) {
			return;
		}
		switch (one.getType()) {
		case Creature.TYPE_PLAYER:
			Player p = (Player) one;
			if (players.contains(p) == false) {
				players.add(p);
			}
			break;
		case Creature.TYPE_MOB:
			npcs.put(one.getGameInteger(), (Monster) one);
			break;
		}
		one.setSceneID(sceneID);
		one.setCurrentScene(this);
		creatures.put(one.getGameInteger(), one);
	}

	/**
	 * 移除活物
	 * 
	 * @param one
	 */
	public void removeCreature(Creature one) {
		if (one == null) {
			return;
		}
		switch (one.getType()) {
		case Creature.TYPE_PLAYER:
			removePlayer((Player) one);
			break;
		case Creature.TYPE_MOB:
			Monster npc=(Monster)one;
			npcs.remove(npc.getGameInteger(),npc);
			break;
		default:
			break;
		}
		one.resetVisibleList();
		creatures.remove(one.getGameInteger());
	}

	protected void removePlayer(Player p) {
		players.remove(p);
	}
	/**
	 * 是否包含某活物
	 * 
	 * @param one
	 * @return
	 */
	public boolean contains(Creature one) {
		return creatures.containsValue(one);
	}

	/**
	 * 根据gameID取得活物
	 * 
	 * @param gameID
	 * @return
	 */
	public Creature getCreature(int gameID) {
		if(creatures.get(Integer.valueOf(gameID))==null){
			logger.error("活物在场景中不存在:gameID="+gameID);
			return null;
		}
		return creatures.get(Integer.valueOf(gameID));
	}

	/**
	 * 是否是副本
	 * 
	 * @return
	 */
	public boolean isDungeon() {
		return (type == TYPE_DUNGEON);
	}

	/**
	 * 本地图是否为不活跃 如果没有返回true
	 * 
	 * @return
	 */
	protected boolean inActive() {
		if (players == null) {
			players = new ArrayList<Player>();
			return true;
		} else {
			return players.size() == 0;
		}
	}

	/**
	 * 普通心跳
	 */
	protected void normalTick() {
//		if (isActive) {// 如果活跃就tick
			try {
				for (Monster one : npcs.values()) {
					one.tick();
				}
			} catch (Exception e) {
				logger.error("mobs tick", e);
			}
	}

	public void tick() {
		try {
			normalTick();
		} catch (Exception e) {
			logger.error("Scene线程", e);
		}
	}
	/**
	 * 根据地图索引取得怪
	 * 
	 * @param index
	 * @return
	 */
	public Monster getNPC(int index) {
		return npcs.get(index);
	}

	/**
	 * 获取场景中怪物的数量
	 * 
	 * @return
	 */
	public int getNPCNum() {
		return npcs.size();
	}

	/**
	 * 获取场景中的怪物
	 * 
	 * @return
	 */
	public Collection<Monster> getNPCs() {
		return npcs.values();
	}

	/**
	 * 获取场景中的活物
	 * 
	 * @return
	 */
	public Collection<Creature> getCreatures() {
		return creatures.values();
	}

	public String toString() {
		return getName() + sceneID;
	}

	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * 地图的信息，包括复活点等
	 */
	protected GridData mapData;

	/**
	 * 当前场景的地图信息
	 * 
	 * @param data
	 */
	public void setMapData(GridData data) {
		mapData = data;
		type = data.type;
	}

	/**
	 * 人数已满
	 * 
	 * @return
	 */
	public boolean playerFull(Player p) {
		return false;
	}

	public GridData getMapStaticData() {
		return mapData;
	}

	public String getName() {
		return GridData.getGridData(sceneID).name;
	}

	public Scene getScene(Player p) {
		return this;
	}

	/**
	 * 销毁副本
	 * 
	 */
	public void dispose() {
		players.clear();
		npcs.clear();
		creatures.clear();
	}

	/**
	 * 取得子场景
	 * 
	 * @param index
	 * @return
	 */
	public Scene getChild(int index) {
		return null;
	}
	/**
	 * 返回副本个数
	 * 
	 * @return
	 */
	public int size() {
		return 1;
	}
	/**
	 * @return the dungeonID
	 */
	public long getDungeonID() {
		return dungeonID;
	}
}