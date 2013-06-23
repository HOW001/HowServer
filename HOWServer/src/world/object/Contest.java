package world.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import db.model.Player;
import server.cmds.ChatCP;
import server.cmds.FightCP;
import server.cmds.MapCP;
import server.cmds.UISystemCP;
import util.ByteArray;
import util.MathUtils;
import util.Util;
import util.binreader.DropData;
import util.binreader.GameParameterData;
import util.binreader.GridData;
import util.binreader.HeroData;
import util.binreader.MonsterData;
import util.binreader.PromptData;
import util.binreader.GateInfoData;
import util.binreader.SkillData;
import world.Scene;

/**
 * @author liuzg 比赛实体
 */
public class Contest {
	private static Logger logger = Logger.getLogger(Contest.class);

	// 比赛场景集合，一般只系统保留较短时间，为领取奖励及后续使用
	public static ConcurrentHashMap<Long, Contest> ContestMaps = new ConcurrentHashMap<Long, Contest>();
	public static final long MAXCREATESAVETIME = 1000 * 60 * 60 * 3;// 战斗建立但并未开始,保留时长3小时
	private static final long MAXCOMPLETSAVETIME = 1000 * 60 * 30;// 战斗完成后保留时长5分钟
	public static final long MIN_CONTEST_INTEVAL_TIME = 1000 * 60 * 3;// 比赛间隔时间
	public static final long MAXROUNDS = 100;// 最大回合数
	public long ID;// 比赛的唯一标识
	public long distoryTime = MAXCOMPLETSAVETIME;
	private static final byte FAILD_TYPE_SHIELD = 1;// 防护值失败
	private static final byte FAILD_TYPE_MAXROUNDS = 2;// 已到最大回合数

	public static final byte CONTEST_DIFFICULTY_NORMAL = 1;// 普通
	public static final byte CONTEST_DIFFICULTY_NIGHTMARE = 2;// 噩梦
	public static final byte CONTEST_DIFFICULTY_HELL = 3;// 地狱

	private int rounds = 0;// 回合数
	private GridData mapData;// 赛道信息
	private Scene currentScene;// 赛道场景
	private Creature[] teamA = new Creature[10];// 0:君主 1...9英雄
	private Creature[] teamB = new Creature[10];// 0:君主 1...9英雄

	// 队伍阵型为
	/*
	 * 7 4 1 8 5 2 ---> 9 6 3
	 */
	private static final int TEAMARRAYSLENGTH = 3;

	private int currentAttackIndex_A = 0;// A队当前攻击列数,值为0,1,2
	private int currentAttackIndex_B = 0;// B队当前攻击列数,值为0,1,2

	private int currentSkillExecuter_A = 0;// A队当前技能释放者,值为0...8
	private int currentSkillExecuter_B = 0;// B队当前技能释放者,值为0...8

	private Creature[] attackerList_A = new Creature[TEAMARRAYSLENGTH];// A队攻击者
	private Creature[] attackerList_B = new Creature[TEAMARRAYSLENGTH];// B队攻击者
	private byte[] contestData = new byte[0];// 战斗数据
	/*
	 * 战斗是否完成
	 */
	private boolean contestEnd = false;

	public long endTime = 0;
	public long createTime = 0;
	private byte faildTeam;
	private int gridID;// 关卡ID，用来记录完成

	private List<Player> winList = new ArrayList<Player>();
	private List<Player> loseList = new ArrayList<Player>();

	private static final byte BOSSSTATUS_INIT = 0;// 初始化状态
	private static final byte BOSSSTATUS_LIFE = 1;// boss活着
	private static final byte BOSSSTATUS_DEAD = 2;// boss死了

	private byte bossSatus = BOSSSTATUS_INIT;
	/*
	 * 触发完成比赛的玩家集合
	 */
	private List<Player> touchCompleteList = new ArrayList<Player>();
	/*
	 * 触发完成比赛剧情的玩家集合
	 */
	private List<Player> touchCompleteStroyList = new ArrayList<Player>();

	/*
	 * A队标识
	 */
	private static final byte TEAM_A_TAG = 1;
	/*
	 * B队标识
	 */
	private static final byte TEAM_B_TAG = 2;

	/******** 赛道难度相关参数 ********/
	private int currentDifficuty = CONTEST_DIFFICULTY_NORMAL;// 当前比赛难度

	/**
	 * @author liuzg
	 * @param mapData
	 * @param teamA
	 * @param teamB
	 *            两个队伍申请比赛，或两个玩家申请比赛
	 */
	public Contest(Scene scene) {
		this.ID = System.currentTimeMillis();
		currentScene = scene;
		this.mapData = scene.getMapStaticData();
		this.gridID = mapData.id;
	}

	/**
	 * @author liuzg
	 * @param playerTeam
	 *            PVE
	 */
	public boolean initContest(Creature[] teamA) {
		Creature teamB[] = null;
		if (bossSatus != BOSSSTATUS_LIFE) {
			teamB = createMob(currentScene);
			if(teamB==null){
				return false;
			}
		} else {
			contestEnd = false;
		}
				initContest(teamA, teamB);
		return true;
	}

	/**
	 * @author liuzg
	 * @param teamA
	 * @param teamB
	 *            开始战斗之前必须经过的
	 */
	public void initContest(Creature[] teamA, Creature[] teamB) {

		this.teamA = teamA;
		this.teamB = teamB;
		this.teamA[0].setCurrentScene(currentScene);
		this.teamB[0].setCurrentScene(currentScene);

		for (int index = 1; index < this.teamA.length; index++) {
			if (teamA[index] != null) {
				int value = teamA[index].getPower() * teamA[index].getPower() + 100;
				this.teamA[0].addCurrentContestHP(value);
			}
		}
		for (int index = 1; index < this.teamB.length; index++) {
			if (teamA[index] != null) {
				int value = teamA[index].getPower() * teamA[index].getPower() + 100;
				this.teamB[0].addCurrentContestHP(value);
			}
		}

		ContestMaps.put(ID, this);
		createTime = System.currentTimeMillis();
	}

	/**
	 * @author liuzg
	 * @param mapData
	 * @return
	 */
	private Creature[] createMob(Scene scene) {
		//判断当前格子指定boss是否存在,如果不存在则返回null
		Monster mob = Monster.createMob(MonsterData.getMonsterData(10001));
		scene.addCreature(mob);
		Creature[] teamMob = new Creature[10];
		teamMob[0] = mob;
		teamMob[1] = mob;
		return teamMob;
	}

	/**
	 * @author liuzg 发送战斗数据
	 */
	public byte[] getContestData() {
		return contestData;
	}

	/**
	 * @author liuzg 开始比赛 通常一场比赛只能开始一次，也就是说即使多个玩家参与比赛，也只会调用一次该方法且保证所有玩家得到的数据一致
	 */
	public synchronized void startContest() {
		if (contestEnd) {// 只开始一次
			return;
		}
		long useTime = System.currentTimeMillis();
		try {
			ByteArray contestData = new ByteArray();
			contestData.writeInt(FightCP.getCMD(FightCP.FIGHT_INFO));
			ByteArray roundsData = new ByteArray();
			/*
			 * rounds 回合数，前端以依些来确定回合次数 所谓回合数，即比赛当中的一个表示单位，如Buffer、CD时间的计算等
			 */
			roundsData.writeShort(rounds);
			while (!contestEnd) {
				rounds++;
				logger.debug("**********第" + rounds + "回合************");
				
				attackerList_A = new Creature[3];
				attackerList_B = new Creature[3];
				
				createAttacker();
				/*
				 * 对可发动攻击的玩家进行具体处理
				 */
				byte[] attackData_A = startAttack(TEAM_A_TAG);
				roundsData.writeByteArray(attackData_A);
				byte[] attackData_B = startAttack(TEAM_B_TAG);
				roundsData.writeByteArray(attackData_B);
				/* 判断比赛是否完成 */
				contestEndState(roundsData);
			}
			byte[] data = roundsData.toArray();
			data = Util.getBytesForShort(rounds, data);
			contestData.writeByteArray(data);
			logger.info(ID + "本次比赛共" + rounds + "回合,mapID=" + mapData.id);
			this.contestData = contestData.toArray();
		} catch (Exception e) {
			logger.error(ID + "战斗数据异常:", e);
			ByteArray contestData = new ByteArray();
			contestData.writeInt(FightCP.getCMD(FightCP.FIGHT_START));
			contestData.writeByte(0);
			this.contestData = contestData.toArray();
		} finally {
			endTime = System.currentTimeMillis();

			logger.info(ID + "战斗数据长度:" + this.contestData.length + "Bytes");
			logger.info(ID + "战斗数据生成用时:" + (System.currentTimeMillis() - useTime) + "毫秒");
			// 清空战斗后的数据
			clear();
		}
	}

	/**
	 * @author liuzg 完成比赛后清空各项数据
	 */
	public void clear() {
		try {
			logger.info(this.ID + "开始清理战斗遗留数据...");
			boolean isDispose = true;
			if (teamB[0] instanceof Monster) {
				isDispose = teamB[0].isDead();// boss尚未死亡
			}
			// Boss赛boss未死亡时，不销毁副本
			if (isDispose) {
				currentScene.dispose();// 销毁副本
			} else {
				bossSatus = BOSSSTATUS_LIFE;
				for (Creature one : teamA) {
					logger.debug(one.getName() + "移除副本!");
					currentScene.removeCreature(one);
				}
			}
			logger.info(this.ID + "完成清理战斗遗留数据...");
		} catch (Exception e) {
			logger.error(this.ID + "清理战斗遗留数据时出现异常:", e);
		}
	}

	/**
	 * @author liuzg 销毁战斗实体
	 */
	public void dispose() {
		try {
			logger.info(this.ID + "开始销毁战斗实体...");
			if (teamA != null) {
				// teamA.clear();
				teamA = null;
			}
			if (teamB != null) {
				// teamB.clear();
				teamB = null;
			}

			if (contestData != null) {
				contestData = null;
				// attackerData.clear();
			}
			attackerList_A = null;
			attackerList_B = null;
			if (winList != null) {
				winList.clear();
				winList = null;
			}
			if (loseList != null) {
				loseList.clear();
				loseList = null;
			}
			if (touchCompleteList != null) {
				touchCompleteList.clear();
				touchCompleteList = null;
			}
			if (touchCompleteStroyList != null) {
				touchCompleteStroyList.clear();
				touchCompleteStroyList = null;
			}
			logger.info(this.ID + "完成销毁战斗实体...");
		} catch (Exception e) {
			logger.error(this.ID + "销毁战斗实体时出现异常:", e);
		}
	}

	byte faildType = FAILD_TYPE_SHIELD;

	/**
	 * @author liuzg
	 * @return 判断比赛是否处于结束状态
	 */
	private boolean contestEndState(ByteArray ba) {

		boolean endTmp = teamA[0].isDead();
		faildTeam = TEAM_A_TAG;
		if (endTmp) {
			logger.debug("A队玩家已死绝....");
			faildTeam = TEAM_A_TAG;
			faildType = FAILD_TYPE_SHIELD;
		} else {
			endTmp = teamB[0].isDead();
			if (endTmp) {
				logger.debug("B队玩家已死绝....");
				faildTeam = TEAM_B_TAG;
				faildType = FAILD_TYPE_SHIELD;
			}
		}
		/*
		 * 判断回合数是否达到最大回合数
		 */
		if (endTmp == false) {
			if (rounds >= MAXROUNDS) {
				logger.debug("已达到最大回合数");
				endTmp = true;
				faildType = FAILD_TYPE_MAXROUNDS;
				if (teamA[0].getCurrentContestHP() >= teamB[0].getCurrentContestHP()) {
					faildTeam = TEAM_B_TAG;
				} else {
					faildTeam = TEAM_A_TAG;
				}
			}
		}
		/*
		 * 最后确定比赛是否已完成
		 */
		if (endTmp) {
			/*
			 * 已完成
			 */
			contestEnd = endTmp;
			ba.writeBoolean(endTmp);// 完成状态
			ba.writeByte(faildType);// 完成类型
			if (faildTeam == TEAM_B_TAG) {// A队胜利
				for (Creature win : teamA) {
					if (win instanceof Player) {
						winList.add((Player) win);
					}
				}
				for (Creature lose : teamB) {
					if (lose instanceof Player) {
						loseList.add((Player) lose);
					}
				}

			} else {
				for (Creature win : teamB) {
					if (win instanceof Player) {
						winList.add((Player) win);
					}
				}
				for (Creature lose : teamA) {
					if (lose instanceof Player) {
						loseList.add((Player) lose);
					}
				}
			}
			ba.writeUTF(ID + "");
		} else {
			ba.writeBoolean(endTmp);
			ba.writeByte(1);// 以下是结构性数据，无效
			ba.writeUTF("0");
		}
		return endTmp;
	}

	/**
	 * @author liuzg
	 * @param one
	 * @param isWin
	 * @param ba
	 *            比赛完成后的奖励信息
	 */
	private void sendCompletePrice(Creature one, boolean isWin, ByteArray ba) {

	}

	/**
	 * @author liuzg
	 * @param dropInfo
	 * @return 获取其他奖励信息的描述
	 */
	public String getOtherPriceInfo(List<int[]> dropInfo) {
		StringBuffer sb = new StringBuffer();
		List<int[]> remove = new ArrayList<int[]>();
		for (int index = 0; index < dropInfo.size(); index++) {
			int[] info = dropInfo.get(index);
			if (info[0] == DropData.DROP_TYPE_GOD) {
				sb.append("额外奖金:" + info[2] + "\n");
				remove.add(info);
			}
			if (info[0] == DropData.DROP_TYPE_SKILL) {
				sb.append("奖励技能:" + info[1] + "\n");
				remove.add(info);
			}
		}
		for (int info[] : remove) {
			dropInfo.remove(info);
		}
		return sb.toString();
	}

	/**
	 * @author liuzg 攻击流程
	 *  1.A方发起攻击,检测A方是否有战前技能 
	 *  2.有战前技能  2.1,无战前技能3
	 *    2.1:该技能是增益buff转入普攻进入3,该技能是伤害技能直接攻击4
	 *  3.A进入物攻状态,判断B方是否闪避,如果无法闪避进入4,否则进入5 
	 *  4.B方掉血 
	 *  5.战斗完成
	 */
	private byte[] startAttack(int teamTag) {
		ByteArray ba = new ByteArray();
		ba.writeByte(teamTag);// 攻击方标识
		Creature attackExecuter = getSkillExecuter(teamTag);//攻击技能执行者
		Creature beAttackExecter = getSkillExecuter(teamTag == TEAM_A_TAG?TEAM_B_TAG:TEAM_A_TAG);//被攻击技能执行者
		Creature[] attacker = null;
		Creature[] beAttacker = null;
		if (teamTag == TEAM_A_TAG) {
			attacker = attackerList_A;
			beAttacker = attackerList_B;
		} else {
			attacker = attackerList_B;
			beAttacker = attackerList_A;
		}
		boolean isHaveAttackSkill=false;//自己是否拥有攻击技能
		boolean isHaveShieldBuffer=false;//对方是否拥有防护buffer
		boolean isCalcFending = false;// 对方是否计算闪避
		int attackSkillID = attackExecuter.isHaveAttackSkill();
		SkillData attackeSkill = SkillData.getSkill(attackSkillID);
		int shieldSkillID=beAttackExecter.isHaveShieldSkill();
		SkillData beAttackSkill =SkillData.getSkill(shieldSkillID);
		if (attackeSkill != null) {
			//自己拥有攻击技能
			isHaveAttackSkill=true;			
			if (beAttackSkill!=null &&  beAttackSkill.getSkillSpecialType() == SkillData.ATTACKBUFFER) {
				//对方是否拥有防护技能
				isHaveShieldBuffer=true;
			} 
		} 
		//计算对方是否能够闪避start
		isCalcFending=true;
		//计算对方是否能够闪避end
		ba.writeBoolean(isHaveAttackSkill);
		ba.writeInt(attackSkillID);
		ba.writeBoolean(isHaveShieldBuffer);
		ba.writeInt(shieldSkillID);
		ba.writeBoolean(isCalcFending);
		if (isCalcFending==false) {
			//躲闪失败
			ba.writeInt(10);//掉血
		} 
		return ba.toArray();
	}

	/**
	 * @author liuzhigang
	 * @param teamTag
	 * @return 获取技能一个技能执行者
	 */
	private Creature getSkillExecuter(int teamTag) {
		if (teamTag == TEAM_A_TAG) {
			for (int index = 1; index <= TEAMARRAYSLENGTH * 3; index++) {
				currentSkillExecuter_A = (currentSkillExecuter_A + 1) % TEAMARRAYSLENGTH * 3;// 0...8
				if (teamA[currentSkillExecuter_A + 1] != null) {
					return teamA[currentSkillExecuter_A + 1];
				}
			}
		} else {
			for (int index = 1; index <= TEAMARRAYSLENGTH * 3; index++) {
				currentSkillExecuter_B = (currentSkillExecuter_B + 1) % TEAMARRAYSLENGTH * 3;// 0...8
				if (teamA[currentSkillExecuter_B + 1] != null) {
					return teamA[currentSkillExecuter_B + 1];
				}
			}
		}
		logger.error("不应该出现找不到释放者的情况");
		return null;
	}

	/**
	 * @author liuzg
	 * @return 获取攻击者
	 */
	private void createAttacker() {
		// 产生A队攻击者
		int col = 1;// 攻击矩阵列数,最大为3列
		boolean isFindAttacker = false;// 是否已找到攻击者,即每一列有一个攻击者即为找到
		while (isFindAttacker == false && col <= TEAMARRAYSLENGTH) {
			int startIndex = currentAttackIndex_A * TEAMARRAYSLENGTH + 1;// 本次攻击的起始序号
			int attackerPostion = 0;// 当前攻击者所在攻击队列中的位置
			for (int index = startIndex; index <= startIndex + TEAMARRAYSLENGTH; index++) {
				if (teamA[index] != null) {
					attackerList_A[attackerPostion] = teamA[index];
					isFindAttacker = true;
				}
				attackerPostion++;
			}
			col++;
			currentAttackIndex_A = currentAttackIndex_A + 1 % TEAMARRAYSLENGTH;
		}
		// 产生B队攻击者
		col = 1;// 攻击矩阵列数,最大为3列
		isFindAttacker = false;// 是否已找到攻击者,即每一列有一个攻击者即为找到
		while (isFindAttacker == false && col <= TEAMARRAYSLENGTH) {
			int startIndex = currentAttackIndex_B * TEAMARRAYSLENGTH + 1;// 本次攻击的起始序号
			int attackerPostion = 0;// 当前攻击者所在攻击队列中的位置
			for (int index = startIndex; index <= startIndex + TEAMARRAYSLENGTH; index++) {
				if (teamB[index] != null) {
					attackerList_B[attackerPostion] = teamB[index];
					isFindAttacker = true;
				}
				attackerPostion++;
			}
			col++;
			currentAttackIndex_B = currentAttackIndex_B + 1 % TEAMARRAYSLENGTH;
		}

	}

	/**
	 * @author liuzg
	 * @param player
	 * @return 玩家是否在本场比赛中胜利
	 */
	public boolean isWinPlayer(Player player) {
		// if (faildTeam==TEAM_A_TAG) {
		// return teamB.contains(player);
		// } else {
		// return teamA.contains(player);
		// }
		return false;
	}

	public boolean isContestEnd() {
		return contestEnd;
	}

	public static void main(String str[]) {

	}

	/**
	 * @author liuzg 赛场心跳
	 */
	public void tick() {

	}

	/**
	 * @author liuzg
	 * @param one
	 * @return 获取玩家的队伍标识
	 */
	public byte getTeamTag(Creature one) {
		if (teamA[0] == one) {
			return TEAM_A_TAG;
		}
		return TEAM_B_TAG;
	}

	/**
	 * @author liuzg
	 * @return 获取当前赛道所有玩家的资源信息
	 */
	public byte[] getResourceInfo() {
		ByteArray ba = new ByteArray();
		return ba.toArray();
	}

	/**
	 * @author liuzg 玩家触发完成比赛
	 */
	public synchronized void touchCompleteContest(Player player, boolean isAutoFight) {
		player.tickFighting();
		if (touchCompleteList.contains(player)) {
			logger.debug(player.getName() + "已触发完成比赛,无需再次触发...");
			return;
		}
		int mapType = 0;
		GridData grid = GridData.getGridData(getGridID());
		if (grid != null) {
			if (MapCP.getInsatance().isLockGrid(player, grid)) {
				logger.error(player.getName() + "尝试非法进入地图:" + grid);
				player.sendResult(mapData.name + "比赛数据异常,请联系GM解决!");
				return;
			}
		}
		AutoFightEntry auto = null;
		if (isAutoFight) {
			auto = AutoFightManager.getAutoFightEntry(player, mapType);
			if (auto == null) {
				logger.info(player.getName() + "自动战斗时无法获得战斗实体!");
				return;
			}
		}

		// 比赛完成为触发场景
		if (winList.contains(player) || loseList.contains(player)) {
			// 竞技场处理,升级之前
			if (winList.contains(player)) {// 胜利玩家相关操作
				double exp = 100;
				double gold = 200;
				// 奖励玩家经验和金钱
				if (isAutoFight) {
					logger.info(player.getName() + "自动战斗比赛完成奖励玩家经验:" + exp);
					auto.addRewardExpMaps(gridID, exp);
				} else {
					logger.info(player.getName() + "比赛完成奖励玩家经验:" + exp);
					player.addXp(Math.round(exp));//
				}
				gold = Math.round(gold);
				if (isAutoFight) {
					logger.info(player.getName() + "自动战斗比赛完成奖励玩家金钱:" + gold);
					auto.addRewardMoney(gridID, gold);
				} else {
					logger.info(player.getName() + "比赛完成奖励玩家金钱:" + gold);
					// player.addBindGold((int)gold,MoneyLogger.moneyAdd[13]);
				}
			}
			/*
			 * 胜利玩家触发任务、添加赛道记录
			 */
			if (winList.contains(player)) {
				logger.info(player.getName() + "开始调用活跃度:" + gridID);
				// 添加完成赛道记录
				if (gridID > 0) {
					logger.info(player.getName() + "添加完成赛道记录:" + gridID);
					int bossID=0;//死亡的bossID
					player.getPlayerCompetitionInfoEntry().addCompeleteGrid(gridID, bossID,
							currentDifficuty);
				}
			}
		}

		// 胜利玩家相关操作
		if (winList.contains(player)) {
			if (isAutoFight == false) {

			}
		}
		// 失败玩家相关操作
		if (loseList.contains(player)) {
			if (isAutoFight == false) {

			}
		}
		/*
		 * 胜利后奖励的物品
		 */
		if (winList.contains(player)) {
		}
		touchCompleteList.add(player);
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("比赛ID:" + ID + "当前回合:" + rounds + ",参与比赛的玩家有:");
		// for (Creature one : teamSort) {
		// sb.append(one.getName() + ",");
		// }
		return sb.toString();
	}

	/**
	 * @author liuzg
	 * @return 获取MapID
	 */
	public int getMapID() {
		return mapData.id;
	}

	/**
	 * @author liuzg
	 * @return 获取赛道名称
	 */
	public String getGirdName() {
		return mapData.name;
	}

	public int getGridID() {
		return gridID;
	}

	public int getCurrentDifficuty() {
		return currentDifficuty;
	}

	public void setCurrentDifficuty(int currentDifficuty) {
		logger.info("设置赛道难度为:" + currentDifficuty);
		if (currentDifficuty < CONTEST_DIFFICULTY_NORMAL || currentDifficuty > CONTEST_DIFFICULTY_HELL) {
			currentDifficuty = CONTEST_DIFFICULTY_NORMAL;
		}
		this.currentDifficuty = currentDifficuty;
	}
}
