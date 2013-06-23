package world.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import server.netserver.MsgOutEntry;
import world.Scene;
import world.World;

/**
 * 
 * 用于描述可见列表和活物的一些基本属性
 * 
 */
public abstract class Creature {
	private static Logger logger = Logger.getLogger(Creature.class);
	// 类型表
	public static final byte TYPE_PLAYER = 0x1;// 玩家,发送给客户端的时候是其他玩家
	public static final byte TYPE_MOB = 0x2;// 怪物
	public static final byte TYPE_HERO=0X3;//英雄
	// 状态表
	/**
	 * 还没刷新
	 */
	public static final byte STATE_RESPAWN = 1;// 还没刷新
	/**
	 * 死的
	 */
	public static final byte STATE_DEAD = 2;// 死的
	/**
	 * 活的
	 */
	public static final byte STATE_LIVE = 3;// 活的
	/**
	 * 战斗的
	 */
	public static final byte STATE_FIGHTING = 4;// 战斗的
	/**
	 * 离线
	 */
	public static final byte STATE_DISCONNECTED = 5;// 离线

	protected byte state = STATE_LIVE;

	protected int gameID;// 游戏ID
	private int id;// 数据库ID
	protected byte type;// 类型
	protected String name = "";// 名字
	protected int code;// 编号
	// 性别
	protected byte gender;// 1男2女
	protected short level;// 级别
	protected int currentExp = 0;// 经验
	// 地图相关
	protected short sceneID;// 地图ID
	protected Scene currentScene;// 地图
	

	public long currentContestID = 0;// 当前比赛场景ID

	public long lastAttendContestTime=0;//最后一次参战比赛的时间
	
	public int lastAttackRound = 0;// 最后一次攻击时间
	
	private int currentContestHP=0;//当前战斗血量,仅在战斗中有效
	public static final long CONTESTCD=1000*60*3;//比赛公共CD时间
	public static final int STEPLENGTH = 1000;//可见范围
	
	//=============英雄属性
	private int power;//力量
	private int agile;//敏捷
	private int mp;//魔法
	private int toughness;//韧性

	/*
	 * 各个buffer的改变值
	 * 1.防护
	 * 2.动力
	 * 3.速度
	 * 4.操控
	 * 5.攻击
	 * 6.反应
	 */
    private double [] bufferValues=new double[7];
	public Creature(byte type) {
		this.type = type;
		init();
	}

	
	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = (byte) gender;
	}

	/**
	 * 回复满生命和法力
	 */
	public void restoreLife() {
		
	}

	/**
	 * 刷新
	 */
	public void respawn() {
		// 子类覆盖写
	}

	/**
	 * 设置活物当前场景
	 * 
	 * @param scene
	 */
	public void setCurrentScene(Scene scene) {
		currentScene = scene;
	}

	/**
	 * 获取当前场景
	 * 
	 * @return
	 */
	public Scene getCurrentScene() {
		return currentScene;
	}

	public int getGameID() {
		if (gameID == 0) {
			generateGameID();
		}
		return gameID;
	}

	public Integer getGameInteger() {
		return Integer.valueOf(getGameID());
	}

	/**
	 * 生成游戏ID 由子类覆写
	 */
	protected abstract void generateGameID();

	public static int generated = 1;

	/**
	 * 如果是玩家则返回数据库ID 如果是其他，则返回generateNumber
	 * 
	 * @return
	 */
	public int getIDByGameID() {
		return gameID & 0x0FFFFFFF;
	}

	/**
	 * 发送更新信息
	 * 
	 * @param om
	 * @throws IOException
	 */
	public void writeUpdate(MsgOutEntry om) throws Exception {

	}



	/**
	 * 判断是否在可见列表范围内
	 * 
	 * @param one
	 * @return
	 */
	public boolean inVisibleRange(Creature one) {
		if (one == null) {
			return false;
		}
		return false;
	}

	/**
	 * 添加可见列表
	 * 
	 * @param one
	 * @return
	 */
	public boolean addVisibleObject(Creature one) {
		if (one == this) {
			return false;
		}
		if (one == null) {
			return false;
		} else {
			if (inVisibleRange(one) == false) {
				return false;
			}

			if (one.isDead()) {
				return false;
			}
			
			return true;
		}
	}

	// ======================GETTER AND SETTERS ===================//
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getType() {
		return type;
	}

	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = (short) level;
	}

	public int getCurrentExp() {
		return this.currentExp;
	}

	private void setCurrentExp(int exp) {
		this.currentExp = exp;
	}

	
	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}

	public int getSceneID() {
		return sceneID;
	}

	public void setSceneID(int sceneID) {
		this.sceneID = (short) sceneID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 重置可见列表
	 */
	public void resetVisibleList() {
//		region.resetVisible();
	}
    /**
     * 初始化一些信息
     */
	protected void init() {
	}
    
	
	public byte ticker = 0;
	protected long lastTickTime = 0;

	/**
	 * 活物的心跳
	 */
	public void tick() {
		lastTickTime = World.getInstance().getCurrentTime();
		ticker++;
		switch (state) {
		case STATE_RESPAWN:
			tickRespawn();// 请求刷新
			break;
		case STATE_LIVE:// 正常状态
			tickLive();
			break;
		case STATE_DEAD:// 死亡状态
			tickDead();
			break;
		}
	}

	/**
	 * 死亡处理
	 * 
	 */
	protected void die() {
		if (state != STATE_DEAD) {
			setState(STATE_DEAD);
			announceDead();// 宣布死亡奖励信息
			logger.info(this + "死亡。");
		}
	}

	protected void tickDead() {
		setState(STATE_RESPAWN);
	}

	/**
	 * 战斗结束后置为普通状态
	 * 
	 */
	protected void tickFighting() {
		setState(STATE_LIVE);
	}

	/**
	 * 正常状态下的tick
	 * 
	 */
	protected void tickLive() {

	}

	/**
	 * 开始切磋
	 */
	public void startFighting() {
		setState(STATE_FIGHTING);
	}

	protected void tickRespawn() {
	}

	public NioSession getIoSession() {
		return null;
	}

	
	/**
	 * 对某目标使用技能
	 * 
	 * @param skill
	 * @param target
	 *            0:失败 <0:减血 >0:加血
	 */
	protected int useSkill( Creature target, int currentRounds,Contest cont) {
		if (target == null) {
			return 0;
		}
		return 0;
	}


	/**
	 * 通知其他活物此活物死亡 子类会覆写此函数
	 */
	protected void announceDead() {
	}

	/**
	 * 是否已经死亡
	 * 
	 * @return
	 */
	public boolean isDead() {
		if (this.getState() == STATE_DEAD) {
			return true;
		}
		return false;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * 警戒等级
	 */
	protected int warnLevel = 0;

	public int npc_level;

	
	/**
	 * @author liuzg
	 * @return 是否有攻击技能
	 */
	public abstract int isHaveAttackSkill();

	/**
	 * @author liuzhigang
	 * @return
	 * 是否拥有防护技能
	 */
	public abstract int isHaveShieldSkill();
	// 活物身上存在的buffer
	private Map<Integer, BufferObject> buffer = new HashMap<Integer, BufferObject>();
	// 活物身上存在的debuffer
	private Map<Integer, BufferObject> debuffer = new HashMap<Integer, BufferObject>();

	// 新加的buff需要发送
	private Map<Integer, BufferObject> needSendBuffer = new HashMap<Integer, BufferObject>();

	// 新删除的buff需要发送
	private Map<Integer, BufferObject> needSendRemoveBuffer = new HashMap<Integer, BufferObject>();

	public Map<Integer, BufferObject> getNeedSendRemoveBuffer() {
		return needSendRemoveBuffer;
	}

	public void clearNeedSendRemoveBuffer() {
		needSendRemoveBuffer.clear();
	}

	public Map<Integer, BufferObject> getNeedSendBuffer() {
		return needSendBuffer;
	}

	public void clearNeedSendBuffer() {
		needSendBuffer.clear();
	}

	public Map<Integer, BufferObject> getBuffer() {
		return buffer;
	}

	public Map<Integer, BufferObject> getDebuffer() {
		return debuffer;
	}

	public int getBufferSize() {
		return buffer.size();
	}

	public int getDebufferSize() {
		return debuffer.size();
	}

	/**
	 * @author liuzg 计算玩家当前的各项属性,每个回合调用一次
	 */
	public void calculateCurrentAttr(int currentRounds) {
		// 去除已过时的buffer和debuffer
		List<Integer> tmp = new ArrayList<Integer>();
		for (BufferObject buff : buffer.values()) {
			if (buff.isValid(currentRounds) == false) {
				tmp.add(buff.getBuffData().id);
				needSendRemoveBuffer.put(buff.getBuffData().id, buff);
			}
		}
		for (int buffID : tmp) {
			buffer.remove(buffID);
		}
		tmp.clear();
		for (BufferObject debuff : debuffer.values()) {
			if (debuff.isValid(currentRounds) == false) {
				tmp.add(debuff.getBuffData().id);
				needSendRemoveBuffer.put(debuff.getBuffData().id, debuff);
			}
		}
		for (int debuffID : tmp) {
			debuffer.remove(debuffID);
		}
		/*
		 * 清除所有Buffer产生的影响
		 * shiled不清除
		 */
		for(int index=2;index<bufferValues.length;index++){
			bufferValues[index]=0;
		}
		for (BufferObject buff : buffer.values()) {
			buff.execute(this, currentRounds);
		}
		for (BufferObject debuff : debuffer.values()) {
			debuff.execute(this, currentRounds);
		}
		logger.debug("执行一次Buffer状态更新");
	}

	/**
	 * @author liuzg 完成比赛,清空各项状态
	 */
	public void completeContest() {
		setState(Creature.STATE_LIVE);
		buffer.clear();
		debuffer.clear();
		lastAttackRound = 0;
	}

	public String toString() {
		return name + "(id=" + id + ",gameID=" + Integer.toHexString(gameID)
				+ ")"
			    + "@" + sceneID;
	}

	
	public void addBufferValue(int index,double value){
		if(index<1&&index>6){
			return;
		}
		bufferValues[index]+=value;
	}
	public boolean isFighting(){
		return System.currentTimeMillis()-this.lastAttendContestTime<Contest.MIN_CONTEST_INTEVAL_TIME;
	}


	public int getCurrentContestHP() {
		return currentContestHP;
	}

    public void addCurrentContestHP(int value){
    	currentContestHP+=value;
    	if(currentContestHP<=0){
    		this.die();
    	}
    }
    public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	public int getAgile() {
		return agile;
	}
	public void setAgile(int agile) {
		this.agile = agile;
	}
	public int getMp() {
		return mp;
	}
	public void setMp(int mp) {
		this.mp = mp;
	}
	public int getToughness() {
		return toughness;
	}
	public void setToughness(int toughness) {
		this.toughness = toughness;
	}

}