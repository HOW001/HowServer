/**
 * 
 */
package server.cmds;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import server.ServerEntrance;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.Util;
import util.binreader.GridData;
import util.binreader.GateInfoData;
import world.Scene;
import world.World;
import world.object.AutoFightEntry;
import world.object.AutoFightManager;
import world.object.Contest;
import world.object.Creature;
import world.object.GridState;
import db.model.Player;

/**
 * @author liuzg
 * 
 */
public class FightCP extends CmdParser {
	private final static FightCP instance = new FightCP();
	private static Logger logger = Logger.getLogger(FightCP.class);
	public static final int SEARCH_GRID=0X0001;//探索格子
	public static final int START_FIGHT=0X0002;//产生战斗
	public static final int FIGHT_INFO=0X0003;//战斗信息
	
	private static final int SELECT_GATE = 0X0089;// 选择指定关卡,此处的响应为战前面阵页面信息
	private static final int SURE_COMEIN_FIGHT_MAP = 0X0090;// 部阵完成后,确定进入指定赛道
	public static final int FIGHT_START = 0X0091;// 开始战斗
	private static final int FIGHT_RES_INFO = 0X0092;// 战斗所需特效资源信息
	private static final int FIGHT_COMPLITE = 0X0093;// 战斗结束
	private static final int REQUEST_UNLOACK_GATE_LIST = 0X0094;// 获取可以进入的关卡列表
	private static final int REQUEST_START_AUTO_FIGHT = 0X0095;// 请求开始自动挑战
	private static final int RESPONSE_AUTO_FIGHT_AWARD = 0X0096;// 自动战斗奖励信息
	private static final int SEND_AUTO_FIGHT_STOP = 0X0097;// 自动战斗停止，可以是服务端发起，也可以客户端发起

	public static FightCP getInstance() {
		return instance;
	}

	private FightCP() {
		super(TYPE_FIGHT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * server.cmds.CmdParser#parse(org.apache.mina.transport.socket.nio.NioSession
	 * , int, byte[])
	 */
	@Override
	public void parse(NioSession session, int command, byte[] bytes) {

		try {
			ByteArray ba = new ByteArray(bytes);
			Player p = session.getAttribute(Player.PLAYERKEY) != null
					&& session.getAttribute(Player.PLAYERKEY) instanceof Player ? (Player) session
					.getAttribute(Player.PLAYERKEY) : null;
			if (p == null) {
				return;
				// p.createDBException("测试使用");
			}
			switch (getCommand(command)) {
			case SEARCH_GRID://探索格子
				int gridID=ba.readInt();
				byte diff=ba.readByte();
				requestSearchGrid(p,gridID,diff);
				break;
			case START_FIGHT://开始战斗
				diff = ba.readByte();// 赛道难度
				logger.info(p.getName() + "可以开始战斗,赛道难度:" + diff);
				startContest(p, diff);
				break;
			case SELECT_GATE:// 选择某一关卡进行排兵布阵
				int gateID = ba.readInt();
				requestSelectGate(p, gateID);
				break;
			case SURE_COMEIN_FIGHT_MAP:// 确定进入战斗地图,在排兵布阵之后调用
				gateID = ba.readInt();
				// 确定每个位置的英雄
				Creature[] teamA = new Creature[10];
				teamA[0] = p;
				logger.info(p.getName() + "选择指定赛道:" + gateID);
				requestSureComeInFightMap(p, teamA, gateID);
				break;
			case FIGHT_START:// 开始战斗
				diff = ba.readByte();// 赛道难度
				logger.info(p.getName() + "可以开始战斗,赛道难度:" + diff);
				startContest(p, diff);
				break;
			case FIGHT_COMPLITE:// 战斗结束
				p.lastAttendContestTime = -1;
				String str = ba.readUTF();// 前端不支持Long
				short times = ba.readShort();
				List<String> smallGameWin = new ArrayList<String>();
				for (int index = 1; index <= times; index++) {
					String tag = ba.readUTF();// 完成小游戏的标识
					smallGameWin.add(tag);
				}
				long contestID = Long.parseLong(str);
				Contest cont = Contest.ContestMaps.get(contestID);
				if (cont != null) {
					cont.touchCompleteContest(p, false);
					// cont.getSmallGameAward(p,smallGameWin);
				}
				p.changeInfo();
				break;
			case REQUEST_UNLOACK_GATE_LIST:// 请求可以挑战的关卡列表
				int mapType = ba.readInt();
				// 增加场景类型限制
				if (mapType == MapCP.MAP_001) {
					requestUnLockGateList(p, mapType);
				} else {
					p.sendResult("无效的操作:001");
				}
				break;
			case REQUEST_START_AUTO_FIGHT:// 请求开始自动挑战
				boolean isAutoFeed = ba.readBoolean();// 自动使用补给包
				boolean isAutoEmployHelper = ba.readBoolean();// 自动雇佣助手
				boolean isAutoFeedEnergy = ba.readBoolean();// 自动补充能量
				int mayType = ba.readInt();// 场景编号,与MapCP中定义一致
				diff = ba.readByte();// 赛道难度
				int cdTime = requestStartAutoFight(p, isAutoFeed,
						isAutoEmployHelper, isAutoFeedEnergy, mayType, diff);
				ba = new ByteArray();
				ba.writeInt(getCmd(REQUEST_START_AUTO_FIGHT));
				ba.writeInt(mayType);
				ba.writeInt(cdTime);
				MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
				moe.flush(ba.toArray());
				if (cdTime > 0) {// 已经自动开始战斗，切换为主场景
					MapCP.getInsatance().requestTransMap(p, MapCP.MAIN_MAP);
				}
				break;
			case SEND_AUTO_FIGHT_STOP:// 客户端请求停止自动战斗
				mapType = ba.readInt();
				AutoFightEntry auto = AutoFightManager.getAutoFightEntry(p,
						mapType);
				if (auto != null) {
					logger.info(p.getName() + "请求结束自动战斗");
					auto.stopFight("主动结束战斗");
				}
				break;
			}
		} catch (Exception e) {
			logger.error("战斗命令解析异常:", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * server.cmds.CmdParser#parseForHttp(org.apache.mina.transport.socket.nio
	 * .NioSession, int, byte[])
	 */
	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {
		// TODO Auto-generated method stub

	}
	/**
	 * @author liuzhigang
	 * @param p
	 * @param gridID
	 * @param diff
	 * 请求探索格子
	 */
    private void requestSearchGrid(Player p,int gridID,byte diff){
    	int event=0;
    	/*
    	 * 判断当前格子可能产生的事件
    	 */
    	ByteArray ba=new ByteArray();
    	ba.writeInt(getCmd(SEARCH_GRID));
    	ba.writeInt(gridID);
    	ba.writeInt(event);
    	MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
		moe.flush(ba.toArray());
		moe = null;
		
		/*
		 * 确定产生boss战斗
		 */
		// 确定每个位置的英雄
		Creature[] teamA = new Creature[10];
		for(int index=1;index<=p.getCurrentFightHero().length;index++){
			teamA[index]=p.getCurrentFightHero()[index];
		}
		teamA[0] = p;
		logger.info(p.getName() + "选择指定格子:" + gridID);
		requestSureComeInFightMap(p, teamA, gridID);
    }
	/**
	 * @author liuzhigang
	 * @param p
	 * @param gateID
	 *            选择指定关卡
	 */
	private void requestSelectGate(Player p, int gateID) {
		// 返回个人布阵信息和该关卡的boss信息
	}

	// List<Player> teamList=new ArrayList<Player>();//参赛队伍
	/**
	 * @author liuzg
	 * @param p
	 * @param carID
	 * @param roadID
	 *            确定进入比赛
	 */
	private void responseComeInContest(Creature[] listA, Creature[] listB,
			GridData grid) {
		try {
//			if (listA[0] instanceof Player) {
//				Player p = (Player) listA[0];
//				logger.info(p.getName() + "发送确定进入比赛地图:" + grid.name);
//				MapCP.getInsatance().requestTransMap(p, MapCP.FIGHT_MAP);// 发送战场信息
//			}
//			if (listB != null && listB[0] instanceof Player) {
//				Player p = (Player) listB[0];
//				logger.info(p.getName() + "发送确定进入比赛地图:" + grid.name);
//				MapCP.getInsatance().requestTransMap(p, MapCP.FIGHT_MAP);// 发送战场信息
//			}
			startComeInContest(listA, listB, grid);// teamList改为Player自身队伍信息
			logger.info("开始" + grid.name + "的比赛...");
		} catch (Exception e) {
			logger.error("确定比赛时出现异常:", e);
		}
	}

	/**
	 * @author liuzhigang
	 * @param teamA
	 * @param teamB
	 * @param map
	 * 开始比赛
	 */
	private void startComeInContest(Creature[] teamA, Creature[] teamB,
			GridData map) {
		Contest cont = null;
		Scene scene = null;
		try {
			Player p = null;
			if (teamA[0].getType() == Creature.TYPE_PLAYER) {
				p = (Player) teamA[0];
			}
			if (p == null) {
				logger.error("多个人开始比赛时没有一个是Player");
				return;
			}
			scene = World.getInstance().getDungeon(map.id, p);
			if (teamA[0].getCurrentScene() != null) {
				teamA[0].getCurrentScene().removeCreature(teamA[0]);
			}
			teamA[0].setCurrentScene(scene);
			scene.addCreature(teamA[0]);
			if (teamB != null) {
				if (teamB[0].getCurrentScene() != null) {
					teamB[0].getCurrentScene().removeCreature(teamB[0]);
				}
				teamB[0].setCurrentScene(scene);
				scene.addCreature(teamB[0]);
			}

			cont = new Contest(scene);
			boolean isCanStart=true;
			if (teamB == null) {
				isCanStart=cont.initContest(teamA);
			} else {
				logger.info("初始化PVP队伍信息");
				cont.initContest(teamA, teamB);
			}
			if(isCanStart==false){
				logger.info(p.getName()+"进入战斗地图失败通知玩家");
			}
			for (Creature one : teamA) {
				if (one.getType() == Creature.TYPE_PLAYER) {
					p = (Player) one;
					p.currentContestID = cont.ID;
				}
			}
			p = (Player) teamA[0];
			logger.info(p.getName() + "初始化玩家信息");
			sendFightInitInfo(p, cont, map);
			if (teamB != null && teamB[0] instanceof Player) {
				p = (Player) teamB[0];
				logger.info(p.getName() + "初始化玩家信息");
				sendFightInitInfo(p, cont, map);
			}
		} catch (Exception e) {
			logger.error("开始比赛进入赛场时出现异常:", e);
			if (cont != null) {
				/*
				 * 如果战场已初始化完毕，则进行清理
				 */
				cont.clear();
			} else {
				if (scene != null) {
					/*
					 * 如果副本已初始化完毕，则销毁
					 */
					scene.dispose();
					scene = null;
				}
			}
		}
	}

	/**
	 * @author liuzg
	 * @param p
	 *            确定选择战斗地图
	 */
	public void requestSureComeInFightMap(Player p, Creature[] teamA, int gridID) {
		GridState state = MapCP.getInsatance().isUnLock(p,
				gridID, Contest.CONTEST_DIFFICULTY_NORMAL);
		if (state.getState()!=GridState.GRID_STATE_OK) {
			p.sendResult("进入赛道失败,原因:" + state.getDesc());
			logger.info(p.getName() + "进入赛道失败,原因:" + state.getDesc());
		} else {
			GridData grid = GridData.getGridData(gridID);
			if (grid == null) {
				p.sendResult("指定赛道在地图数据表中不存在!gridID=" +gridID);
				return;
			}
			responseComeInContest(teamA, null, grid);

		}
	}

	/**
	 * @author liuzg
	 * @param 开始战斗
	 * @param diff
	 *            赛道难度
	 */
	public void startContest(final Player p, final byte diff) {
		final Contest cont = Contest.ContestMaps.get(p.currentContestID);

		if (cont != null) {
			cont.setCurrentDifficuty(diff);
			GridData grid = GridData.getGridData(cont.getGridID());
			if (grid != null) {
				if (MapCP.getInsatance().isLockGrid(p, grid)) {
					logger.error(p.getName() + "尝试非法进入格子:" + grid.id);
					p.sendResult("战斗开始失败!");
					return;
				}
			}
			Runnable contestTask = new Runnable() {
				public void run() {
//					sendResourceInfo(p, cont);
					logger.info(p.getName() + "开始发送战斗数据:" + cont.getMapID());
//					long times = System.currentTimeMillis();
					cont.startContest();
					p.setSceneID(cont.getMapID());
					byte[] data = cont.getContestData();
					MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
					moe.flush(data);
					p.lastAttendContestTime = System.currentTimeMillis();
					p.startFighting();
					p.currentContestID = -1;
					p.bossContestID = -1;

				}
			};
			ServerEntrance.runThread(contestTask);
		} else {
			logger.error(p.getName() + "请求的赛道不存在:" + p.currentContestID);
		}
	}

	/**
	 * @author liuzg
	 * @param player
	 * @param cont
	 *            向玩家发送赛车资源信息
	 */
	private void sendResourceInfo(Player player, Contest cont) {
		logger.info(player.getName() + "开始接收战斗数据...." + cont.ID + ",mapID="
				+ cont.getMapID());
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(FIGHT_RES_INFO));
		ba.writeByteArray(cont.getResourceInfo());
		MsgOutEntry moe = new MsgOutEntry(player.getIoSession());
		moe.flush(ba.toArray());
		moe = null;
	}

	public static int getCMD(int command) {
		return generateCMD(TYPE_FIGHT, command);
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param cont
	 * @param grid
	 *            向玩家发送战斗初始化数据
	 */
	private void sendFightInitInfo(Player p, Contest cont, GridData grid) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(START_FIGHT));
		ba.writeInt(grid.id);// 格子ID
		MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
		moe.flush(ba.toArray());
	}

	/**
	 * @author liuzg
	 * @param isAutoFeed
	 *            是否自动使用补给包
	 * @param isAutoEmployHelper
	 *            是否自动雇佣助手
	 * @param isAutoFeedEnergy
	 *            是否自动补充能量
	 * @param diff
	 *            赛道难度
	 * @return
	 */
	private int requestStartAutoFight(Player p, boolean isAutoFeed,
			boolean isAutoEmployHelper, boolean isAutoFeedEnergy, int mapType,
			int diff) {
		// 判断是否在冷却时间内
		int cdTime = AutoFightEntry.getAutoFightCDTime(p, mapType);
		if (cdTime > 0) {
			p.sendResult("您还有大约:" + cdTime + "秒的冷却时间!");
			return -1;
		}
		if (mapType != MapCP.MAP_001) {
			// 进入不能自动挑战的场景
			p.sendResult("进入不能自动战斗的场景!");
			return -1;
		}
		if (p.getPlayerPackEntry().getSpaceNumber() <= 0) {
			// 背包空间不足不能开战
			p.sendResult("您的背包空间不足!");
			return -1;
		}
		// if(p.getPower()<MINPOWERVALUE){
		// //动力值不足不能开战
		// p.sendResult("动力值不足不能开战!");
		// return -1;
		// }
		if (isAutoFeed) {
			if (p.getPlayerPackEntry().getIsOpenIndex() < 0) {
				// 背包中没有补给包不能开战
				p.sendResult("背包中没有补给包不能开战!");
				return -1;
			}
		}
		return startAutoFight(p, isAutoFeed, isAutoEmployHelper,
				isAutoFeedEnergy, mapType, diff);
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param isAutoFeed
	 *            是否自动使用补给包
	 * @param isAutoEmployHelper
	 *            是否自动雇佣助手
	 * @param isAutoFeedEnergy
	 *            是否自动补充能量
	 * @param mapType
	 *            1002世界巡回赛 1003地狱挑战赛 1004新手练级赛
	 * @param diff赛道难度
	 *            开始自动战斗 return 冷却时间
	 */
	private int startAutoFight(Player p, boolean isAutoFeed,
			boolean isAutoEmployHelper, boolean isAutoFeedEnergy, int mapType,
			int diff) {
		// 获取一个自动战斗实体
		AutoFightEntry auto = AutoFightManager.getAutoFightEntry(p, mapType);
		if (auto != null) {
			// 判断是否进行下一次自动战斗,主要是冷却时间
			if (auto.getAutoFightCDTime() > 0) {
				p.sendResult("您还有大约:" + auto.getAutoFightCDTime() + "秒的冷却时间!");
				return -1;
			}
		}
		auto = new AutoFightEntry(p, mapType);
		String desc = "";
		// 获取当前场景的所有关卡ID
		List<Integer> gateList = getGateListForUnLock(p, mapType,diff);
		// 开始逐个关卡进行比赛
		for (int gateID : gateList) {

			// 判断是关卡是否可以比赛
			GateInfoData gate = GateInfoData
					.getRoadGateInfoData(gateID);
			GridData map = null;
			if (gate != null) {
//				map = GridData.getGridData(gate.map_id);
//				if (map == null) {
//					desc = "无法找到指定赛道:name=" + gate.name;
//					break;
//				}
//				if (MapCP.getInsatance().isLockGateForAutoFight(p, gate,diff)) {
//					logger.error(p.getName() + "自动战斗时关卡条件不满足:" + gate);
//					GridState grid = MapCP.getInsatance().isUnLockForGate(p,
//							gate, Contest.CONTEST_DIFFICULTY_NORMAL);
//					desc = map.name + grid.getDesc();
//					break;
//				}
			} else {
				desc = "出现非法关卡信息:ID=" + gateID;
				break;
			}
			auto.addNeedTotalCDTime(AutoFightEntry.PERGATECDTIME);// 暂定60秒跟据配置需要
			Creature teamA[] = new Creature[10];
			teamA[0] = p;
			requestSureComeInFightMap(p, teamA, gateID);
			Contest cont = Contest.ContestMaps.get(p.currentContestID);
			if (cont != null) {
				cont.setCurrentDifficuty(diff);
				logger.info(p.getName() + "开始进行自动战斗,挑战地图为:" + cont.getMapID());
				cont.startContest();
				p.currentContestID = -1;
				// 判断战斗是否失败
				if (cont.isWinPlayer(p) == false) {
					desc = "挑战" + cont.getGirdName()+ "时被击败!";
					auto.setCompleteResult(desc);
					break;
				}
				// 处理战斗后的奖励
				cont.touchCompleteContest(p, true);
				// 处理捕捉活物信息
				// if(isAutoEmployHelper){
				// HelperCP.getInstance().getAutoFightCapture(p, mapType);
				// }
				desc = "完成所有关卡";
				auto.addCompleteGateID(gateID);
			} else {
				logger.error(p.getName() + "请求的赛道不存在:" + p.currentContestID);
			}
		}
		if (desc.length() > 0) {
			auto.setCompleteResult(desc);
		}
		return auto.getAutoFightCDTime();
	}

	/**
	 * @author liuzg
	 * @param mapType
	 * @return 获取当前场景的所有可参加的关卡ID
	 */
	private List<Integer> getGateListForUnLock(Player p, int chapterID,int difficult) {
		// 获取当前场景的章节信息
		// List<Integer>
		// chapterList=RoadChapterInfoData.getChapterList(mapType);
		// 获取当前场景的所有赛道列表
		List<Integer> gateList = new ArrayList<Integer>();
		// for(int chapterID:chapterList){
		gateList.addAll(GateInfoData.getGateList(chapterID));
		// }
		List<Integer> unLockGateList = new ArrayList<Integer>();
		for (int gateID : gateList) {
			GateInfoData gate = GateInfoData
					.getRoadGateInfoData(gateID);
			if (gate != null) {
				if (MapCP.getInsatance().isLockGateForAutoFight(p, gate,difficult)) {
					continue;
				}
				unLockGateList.add(gateID);
			}
		}
		return unLockGateList;
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param mapType
	 *            获取可以进入的关卡
	 */
	private void requestUnLockGateList(Player p, int mapType) {
		List<Integer> gateList = getGateListForUnLock(p, mapType,Contest.CONTEST_DIFFICULTY_NORMAL);
		int num = 0;
		ByteArray ba = new ByteArray();
		ba.writeShort(num);
		for (int gateID : gateList) {
			GateInfoData gate = GateInfoData
					.getRoadGateInfoData(gateID);
			if (gate != null) {
				// if(MapCP.getInsatance().isLockGateForAutoFight(p, gate)){
				// continue;
				// }
				ba.writeUTF(gate.name);
				num++;
			}
		}
		byte data[] = ba.toArray();
		data = Util.getBytesForShort(num, data);
		ba = new ByteArray();
		ba.writeInt(getCmd(REQUEST_UNLOACK_GATE_LIST));
		ba.writeByteArray(data);
		MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
		moe.flush(ba.toArray());
	}

	/**
	 * @author liuzg 自动战斗奖励信息
	 */
	public void responseAutoFightAward(Player p, int mapType,
			List<String> infoList) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(RESPONSE_AUTO_FIGHT_AWARD));
		ba.writeInt(mapType);
		ba.writeShort(infoList.size());
		for (String info : infoList) {
			ba.writeUTF(info);
		}
		MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
		moe.flush(ba.toArray());
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param mapType
	 *            自动战斗停止
	 */
	public void sendAutoFightStop(Player p, byte[] data) {
		logger.info(p.getName() + "通知前端可以停止自动战斗!");
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(SEND_AUTO_FIGHT_STOP));
		ba.writeByteArray(data);
		MsgOutEntry moe = new MsgOutEntry(p.getIoSession());
		moe.flush(ba.toArray());
	}
}
