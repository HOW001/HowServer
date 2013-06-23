package world;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import util.binreader.GridData;
import world.object.Creature;
import db.model.Player;

/**
 * 普通地下城
 * 
 * @author lzg
 * @see Scene
 * @see Map
 */
public class Dungeon extends Scene {
	private static final Logger logger = Logger.getLogger(Dungeon.class);
	public static final int MAX_DUNGEON_MEMBER = 100000000;// 一个地下城最多有50个副本

	/**
	 * 只有ID为0的副本会初始化此集合
	 */
	protected ConcurrentHashMap<Long, Dungeon> scenes; // 副本集合
	/**
	 * 副本的原本
	 */
	protected Dungeon parent;
	/**
	 * 副本入口，即副本的第一个场景
	 */
//	protected Scene entrance;

	/**
	 * 返回副本个数
	 * 
	 * @return
	 */
	public int size() {
		if (scenes == null) {
			return 0;
		}
		int size = scenes.size();
		return size;
	}
	/**
	 * 生成副本
	 * 
	 * @param index
	 * @param playerID
	 * @return
	 */
	protected static Dungeon createDungeon(int index, long dungeonID) {
		// 查询个数
		if (World.scenes.get(index).size() > Dungeon.MAX_DUNGEON_MEMBER) {
			return null;
		}
		logger.info("当前副本数量:"+World.scenes.get(index).size());
		return new Dungeon(index, dungeonID);
	}

	/**
	 * 生成地下城
	 * 
	 * @param index
	 * @param playerID
	 *            玩家的GAMEID
	 */
	protected Dungeon(int index, long dungeonID) {
		this(index, dungeonID, null);
	}

	/**
	 * 生成地下城
	 * 
	 * @param index
	 * @param dungeonID
	 *            玩家的GAMEID
	 */
	protected Dungeon(int index, long dungeonID, List<Player> players) {
		setType(TYPE_DUNGEON);
		initlize(players);
		this.dungeonID = dungeonID;// 副本的ID
		if (dungeonID != 0) {
			logger.info("生成ID0x" + Long.toHexString(dungeonID) + "地图编号为"
					+ index + "的副本");
		}
		GridData mapData = GridData.getGridData(index);
		loadAllData(mapData);
		parent = getCopyFrom();
		if (dungeonID == 0) {
			scenes = new ConcurrentHashMap<Long, Dungeon>();
		}
		if (dungeonID != 0) {
			parent.scenes.put(dungeonID, this);
		}
	}

	/**
	 * 取得存储此副本的原本
	 * 
	 * @return
	 */
	public Dungeon getCopyFrom() {
		if (dungeonID == 0){
			return null;
		}
		Scene s = World.world.getScene(sceneID, null);
		if (s == null){
			return null;
		}
		return (Dungeon) s;
	}

	protected void removePlayer(Player p) {
		if (p == null) {
			return;
		}
		players.remove(p);
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
		default:
			break;
		}
		one.resetVisibleList();
		creatures.remove(one.getGameInteger());
	}

	protected Dungeon getDungeon(long dungeonID) {
		return scenes.get(dungeonID);
	}

	public Scene getScene(Player p) {
		return getScene(p.bossContestID, p.getGameID());
	}

	/**
	 * 
	 * @param teamID
	 * @param playerID
	 * @param playerIDs
	 * @return 所有子类应该复写此类
	 */
	private Scene getScene(long dungeonID, int playerID) {
		Dungeon dungeon = getDungeon(dungeonID);
		// 参与的比赛场次是否存在
		if (dungeon != null) {
			if (dungeon.dungeonID == dungeonID) {
				return dungeon;
			}
		}
		// 返回null 抛给上层处理
		return dungeon;
	}

	/**
	 * 正常心跳
	 */
	public void tick() {
    
	}

	/**
	 * 销毁副本
	 */
	public void dispose() {
		parent.scenes.remove(dungeonID);
//		isActive = false;
		logger.info(this + "---disppose(),size="+parent.scenes.size());
	}

	public String toString() {
		return "副本：" + getName() +",mapID:"+ sceneID + "dungeonID:"
				+ dungeonID;
	}
}
