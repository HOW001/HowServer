package server.cmds;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.binreader.AreaInfoData;
import util.binreader.GameParameterData;
import util.binreader.GridData;
import util.binreader.ChapterInfoData;
import util.binreader.GateInfoData;
import world.object.AutoFightEntry;
import world.object.Contest;
import world.object.GridState;
import db.model.Player;

/**
 * 地图、可见列表
 * 
 * @author Administrator
 * 
 */
public class MapCP extends CmdParser {
	public static final int MAIN_MAP = 1001;// 主场景
	public static final int MAP_001 = 1002;// 探险场景,返回世界地图

	public static final int FIGHT_MAP = 9999;// 战斗地图
	private static Logger logger = Logger.getLogger(MapCP.class);

	private static final int MAP_DATA = 0x0001;// 请求/响应地图数据

	private static final int MAP_CHAPTER_DATA = 0X0002;// 章节数据

	private static final int MAP_GATE_DATA = 0X0003;// 关卡数据

	private static final int MAP_AREA_DATA = 0X0004;// 区域数据

	private static final int MAP_GRID_DATA = 0x0005;// 格子数据

	private static final int MAP_ROAD_RESET = 0X0099;// 赛道重置

	public final static MapCP instance = new MapCP();

	public static MapCP getInsatance() {
		return instance;
	}

	private MapCP() {
		super(TYPE_MAP);
	}

	public static int getCMD(int command) {
		return generateCMD(TYPE_MAP, command);
	}

	public void parse(NioSession session, int command, byte[] data) {
		try {
			Player player = session.getAttribute(Player.PLAYERKEY) != null
					&& session.getAttribute(Player.PLAYERKEY) instanceof Player ? (Player) session
					.getAttribute(Player.PLAYERKEY) : null;
			if (player == null) {
				return;
			}
			ByteArray ba = new ByteArray(data);
			switch (getCommand(command)) {
			case MAP_DATA:// 请求相关场景切换
				int sceneID = ba.readInt();
				logger.info(player.getName() + "请求切换场景:" + sceneID);
				requestTransMap(player, sceneID);
				break;
			case MAP_CHAPTER_DATA:// 请求章节数据
				sceneID = ba.readInt();
				int defficult = ba.readByte();
				sendChapterInfo(player, sceneID, defficult);
				break;
			case MAP_GATE_DATA:// 请求关卡数据
				int chapterID = ba.readInt();
				defficult = ba.readByte();
				sendGateInfo(player, chapterID, defficult);
				break;
			case MAP_AREA_DATA:// 请求区域数据
				int gateID = ba.readInt();
				defficult = ba.readByte();
				sendAreaInfo(player, gateID, defficult);
				break;
			case MAP_GRID_DATA:// 请求格子数据
				int areaID = ba.readInt();
				defficult = ba.readByte();
				sendGridInfo(player, areaID, defficult);
				break;
			case MAP_ROAD_RESET:// 赛道重置
				int chapterType = ba.readInt();
				defficult = Contest.CONTEST_DIFFICULTY_NORMAL;
				resetRoad(player, chapterType, defficult);
				requestTransMap(player, chapterType);
				break;
			}
		} catch (Exception e) {
			logger.error("地图命令解析异常:", e);
		}
	}

	/**
	 * @author liuzg
	 * @param player
	 *            重新加载当前地图信息
	 */
	public void reLoadMapInfo(Player player) {
		if (player.getSceneID() >= MAP_001) {
			requestTransMap(player, player.getSceneID());
		}
	}

	/**
	 * @author liuzg
	 * @param player
	 * @param chapterID
	 *            重置赛道
	 */
	private void resetRoad(Player player, int chapterID, int defficult) {
		logger.info(player.getName() + "重置地狱挑战赛:" + chapterID);
		/*
		 * 已重置的次数
		 */
		int resetTimes = getResetTimes(player, chapterID, defficult);
		if (resetTimes < 0) {
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "你没有完成任何一个赛道,不能重置!");
			return;
		}
		logger.info(player.getName() + "重置赛道扣除银币:" + getResetNeedMoney(resetTimes, chapterID) + ",已重置:" + resetTimes
				+ "次");
		long times = System.currentTimeMillis();
		List<Integer> gateList = GateInfoData.getGateList(chapterID);
		for (int gateID : gateList) {
			player.getPlayerCompetitionInfoEntry().addResetTimes(gateID, defficult);
		}
		long useTimes = System.currentTimeMillis() - times;
		if (useTimes >= 100) {
			logger.error("addResetTimes()加锁运行时间过长：" + useTimes);
		}
		logger.info(player.getName() + "重置赛道完成!chapterType=" + chapterID);
		UISystemCP.sendFlutterMessageForNull(player.getIoSession(), "恭喜您重置完成!");
	}

	/**
	 * @author liuzg
	 * @param player
	 * @param mapID
	 *            请求进入某一场景
	 */
	public synchronized void requestTransMap(Player player, int sceneID) {
		int cdTime = AutoFightEntry.getAutoFightCDTime(player, sceneID);
		if (cdTime > 0) {
			player.sendResult(cdTime + "秒内自动战斗冷却中...");
			return;
		}
		player.setSceneID(sceneID);
		boolean isTrans = true;
		GridData map = GridData.getGridData(sceneID);
		if (map != null) {
			logger.info("判断场景是否可进:mapID=" + sceneID);
		}
		// 判断是否能够进入指定场景
		if (isTrans) {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(MAP_DATA));
			ba.writeShort(sceneID);// 写入地图ID
			MsgOutEntry om = new MsgOutEntry(player.getIoSession());
			om.flush(ba.toArray());
			om = null;
			logger.info("向" + player.getName() + "发送场景:" + sceneID);
		}
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param p
	 * @throws IOException
	 *             场景地图信息
	 */
	public void sendMapData(Player p) {
		if (p == null) {
			return;
		}
		if (p.getIoSession() == null) {
			return;
		}
		logger.info(p.getName() + "开始发送主场景地图");
		requestTransMap(p, MAIN_MAP);
	}

	/**
	 * @author liuzg 发送赛道章节信息
	 * @param p
	 * @param chapterType
	 *            1002探险
	 */
	private void sendChapterInfo(Player p, int chapterType, int difficulty) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(MAP_CHAPTER_DATA));
		getChapterInfo(p, ba, chapterType, difficulty);
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
		om.flush(ba.toArray());
		om = null;
		logger.info("发送章节关卡信息完毕:chapterType=" + chapterType);
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param ba
	 * @param chapterType
	 *            章节关卡信息 格式: 章节数量short 章节ID int 是否开启 boolean 开启描述 string
	 */
	private void getChapterInfo(Player p, ByteArray ba, int chapterType, int difficulty) {
		List<Integer> chapterList = ChapterInfoData.getChapterList(chapterType);
		ba.writeShort(chapterList.size());
		for (int chapterID : chapterList) {
			ChapterInfoData chapter = ChapterInfoData.getChapterInfo(chapterID);
			if (chapter == null) {
				logger.error("策划的章节数据表有误:chapterID:" + chapterID);
				ba.writeInt(-1);
				ba.writeUTF("未知");
				ba.writeByte(1);
				ba.writeUTF("不存在的章节信息");
				continue;
			}
			String[] chapterLockInfo = isUnLockForChapter(p, chapter);
			ba.writeInt(chapter.id);
			ba.writeUTF(chapter.title);
			ba.writeByte(Integer.parseInt(chapterLockInfo[0]));
			ba.writeUTF(chapterLockInfo[1]);
		}
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param chapterID
	 * @param difficulty
	 *            发送指定章节的关卡信息
	 */
	private void sendGateInfo(Player p, int chapterID, int difficulty) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(MAP_GATE_DATA));
		getGateInfoForChapter(p, ba, chapterID, difficulty);
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
		om.flush(ba.toArray());
		om = null;
		logger.info("发送关卡信息完毕:chapterID=" + chapterID);
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param ba
	 * @param difficulty
	 * @param chapterID
	 *            获取指定章节的关卡信息 关卡数量 short 关卡ID int 是否开启 boolean 开启描述 string
	 */
	private void getGateInfoForChapter(Player p, ByteArray ba, int chapterID, int difficulty) {
		// 关卡信息
		List<Integer> gateList = GateInfoData.getGateList(chapterID);
		ba.writeShort(gateList.size());
		for (int gateID : gateList) {
			GateInfoData gate = GateInfoData.getRoadGateInfoData(gateID);
			if (gate == null) {
				logger.error("策划的关卡数据表有误:gateID:" + gateID);
				ba.writeInt(-1);
				ba.writeUTF("未知");
				ba.writeByte(0);
				ba.writeUTF("不存在的关卡信息");
				continue;
			}
			GridState grid = isUnLockForGate(p, gateID, difficulty);
			ba.writeInt(gateID);
			ba.writeUTF(gate.name);
			ba.writeByte(grid.getState());
			ba.writeUTF(grid.getDesc());
			logger.info(p.getName() + "关卡解锁信息:" + grid.toString());
		}
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param gateID
	 * @param defficult
	 *            请求区域信息
	 */
	private void sendAreaInfo(Player p, int gateID, int difficulty) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(MAP_AREA_DATA));
		getAreaInfoForGateID(p, ba, gateID, difficulty);
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
		om.flush(ba.toArray());
		om = null;
		logger.info("发送区域信息完毕:gateID=" + gateID);
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param ba
	 * @param gateID
	 * @param defficult
	 *            获取区域信息
	 */
	private void getAreaInfoForGateID(Player p, ByteArray ba, int gateID, int difficulty) {
		List<Integer> list = AreaInfoData.getAreaInfoDataForGateID(gateID);
		ba.writeShort(list.size());

		for (int areaID : list) {
			AreaInfoData area = AreaInfoData.getAreaInfoData(areaID);
			if (area == null) {
				ba.writeInt(0);
				ba.writeUTF("城区" + areaID);
				ba.writeByte(0);// 通过状态
				ba.writeUTF("通过状态");
			} else {
				ba.writeInt(area.id);
				ba.writeUTF(area.name);
				GridState state = isUnLockForArea(p, areaID, difficulty);
				ba.writeByte(state.getState());
				ba.writeUTF(state.getDesc());
			}
		}
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param areaID
	 * @param defficult
	 *            请求地图信息
	 */
	private void sendGridInfo(Player p, int areaID, int difficulty) {
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(MAP_GRID_DATA));
		getGridInfoForAreaID(p, ba, areaID, difficulty);
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
		om.flush(ba.toArray());
		om = null;
		logger.info("发送地图信息完毕:aredID=" + areaID);
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param ba
	 * @param areaID
	 * @param difficulty
	 *            获取地图信息
	 */
	private void getGridInfoForAreaID(Player p, ByteArray ba, int areaID, int difficulty) {
		List<Integer> list = GridData.getGridDataForAreaID(areaID);
		ba.writeShort(list.size());
		for (int gridID : list) {
			GridData data = GridData.getGridData(gridID);
			if (data == null) {
				ba.writeInt(0);
				ba.writeUTF("未知");
				ba.writeByte(0);// 通过状态
				ba.writeUTF("状态");
			} else {
				ba.writeInt(data.id);
				ba.writeUTF(data.name);
				GridState state = isUnLockForGrid(p, gridID, difficulty);
				ba.writeByte(state.getState());
				ba.writeUTF(state.getDesc());
			}
		}
	}

	/**
	 * @author liuzg
	 * @param resetTimes
	 *            已重置过的次数
	 * @return 重置需要的金钱
	 */
	private int getResetNeedMoney(int resetTimes, int chapterType) {
		if (chapterType == MAP_001) {
			return GameParameterData.world_tour_basic + GameParameterData.world_tour_param * resetTimes;
		}
		// 40+60*（额外攻击次数-1）
		return 0;
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param gateID
	 * @return 获取赛道的重置次数
	 */
	private int getResetTimes(Player p, int chapterID, int defficult) {
		int times = -1;
		List<Integer> gateList = GateInfoData.getGateList(chapterID);
		for (int gateID : gateList) {
			if (p.getPlayerCompetitionInfoEntry().isCompeleteGrid(gateID, defficult)) {
				if (p.getPlayerCompetitionInfoEntry().getResetTimes(gateID, defficult) > times) {
					times = p.getPlayerCompetitionInfoEntry().getResetTimes(gateID, defficult);
				}
			} else {
				continue;
			}
		}
		return times;
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param gate
	 * @return 关卡是否处于锁定状态
	 */
	public boolean isLockGrid(Player p, GridData grid) {
		GridState state = isUnLockForGrid(p, grid.id, Contest.CONTEST_DIFFICULTY_NORMAL);
		logger.info(p.getName() + "关卡解锁信息2:" + grid);
		if (state.getState() == GridState.GRID_STATE_OK) {
			return false;// 未锁
		} else {
			return true;// 已锁
		}
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param gate
	 * @return 指定关卡是否可扫荡，只有通过的赛道可扫荡
	 */
	public boolean isLockGateForAutoFight(Player p, GateInfoData gate, int difficulty) {
		GridState grid = isUnLockForGate(p, gate.id, difficulty);
		logger.info(p.getName() + "关卡解锁信息2:" + grid);
		if (grid.getState() == GridState.GRID_STATE_OK) {
			return false;// 未锁
		} else {
			return true;// 已锁
		}
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param area
	 * @param diffcluty
	 * @return 当前关卡是否解锁
	 */
	public GridState isUnLockForGate(Player p, int gateID, int diffculty) {
		// 循环当前关卡的所有区域
		GridState state = new GridState();
		state.setState(GridState.GRID_STATE_OK);
		state.setDesc("全部通过");
		List<Integer> list = AreaInfoData.gateDatas.get(gateID);
		GateInfoData gate=GateInfoData.getRoadGateInfoData(gateID);
		if (list == null||gate==null) {
			return state;
		}
		int complete=0;
		for (int areaID : list) {
			state = isUnLockForArea(p, areaID, diffculty);
			if (state.getState()== GridState.GRID_STATE_OK) {
				complete++;
			}
		}
		double completeRate = complete / list.size();
		if (completeRate * 100 > gate.complete) {
			state.setState(GridState.GRID_STATE_OK);
			state.setDesc("完成度达到!");
		} else {
			state.setState(GridState.GRID_STATE_NOT_PASS);
			state.setDesc("完成度不够:" + gate.complete);
		}
		return state;
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param area
	 * @return 当前区域是否解锁,主要检测地区的完成度
	 */
	public GridState isUnLockForArea(Player p, int areaID, int difficulty) {
		AreaInfoData area = AreaInfoData.getAreaInfoData(areaID);
		GridState state = new GridState();
		state.setState(GridState.GRID_STATE_OK);
		state.setDesc("全部通过");
		if (area == null) {
			state.setState(GridState.GRID_STATE_NOT_PASS);
			state.setDesc("对象实体不存在!");
			return state;
		}
		//上一个区域的所有格子
		List<Integer> list = GridData.areaDatas.get(area.pri_area_id);
		if (list == null) {
			return state;
		}
		for (Integer gridID : list) {
			logger.info("查找这个格子是否有boss死掉:"+gridID);
		}
		return state;

	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @return[0]是否解锁，[1]原因
	 */
	private String[] isUnLockForChapter(Player p, ChapterInfoData chapter) {
		// boolean isUnLock = false;
		int isUnLock = 0;
		String desc = "";
		if (chapter.unlock_condition.equals("0")) {
			isUnLock = 0;
			desc = "此章节没有解锁条件";
			return new String[] { String.valueOf(isUnLock), desc };
		}
		if (chapter.lock_type == ChapterInfoData.LOCKTYPEGATE) {// 关卡是否完成
			isUnLock = 0;
			for (int gateID : chapter.conditionList) {
				isUnLock = p.getPlayerCompetitionInfoEntry().isCompeleteGrid(gateID, Contest.CONTEST_DIFFICULTY_NORMAL) ? 0
						: 1;
				if (isUnLock > 0) {
					GateInfoData gate = GateInfoData.getRoadGateInfoData(gateID);
					if (gate != null) {
						desc = "完成上一章节全部比赛后开启";// "关卡{" + gate.name + "}尚未完成";
					} else {
						desc = "完成上一章节全部比赛后开启";// "关卡" +
												// chapter.unlock_condition +
												// "尚未完成";
					}
					break;
				}
			}
			if (isUnLock == 0) {
				desc = "已解锁";
			}
		} else if (chapter.lock_type == ChapterInfoData.LOCKTYPETASK) {// 任务是否完成

		} else if (chapter.lock_type == ChapterInfoData.LOCKTYPEITEM) {// 道具是否可以使用

		}
		// 判断完毕
		return new String[] { String.valueOf(isUnLock), desc };
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param chapterID
	 * @param gateID
	 * @return 判断指定章节和指定关卡是否可以开始比赛
	 */
	public GridState isUnLock(Player p, int gridID, int difficulty) {
		GridState grid = new GridState();
		grid.setState(GridState.GRID_STATE_NOT_PASS);
		grid.setDesc("格子未通过");
		grid = isUnLockForGrid(p, gridID, difficulty);
		return grid;
	}

	/**
	 * @author liuzhigang
	 * @param p
	 * @param gridID
	 * @return 指定格子完成状态
	 */
	private GridState isUnLockForGrid(Player p, int gridID, int difficulty) {
		GridState grid = new GridState();
		if (p.getPlayerCompetitionInfoEntry().isCompeleteGrid(gridID, difficulty)) {
			grid.setState(GridState.GRID_STATE_OK);
			grid.setDesc("格子已完成");
		} else {
			grid.setState(GridState.GRID_STATE_NOT_PASS);
			grid.setDesc("格子未完成");
		}
		grid.setGridId(gridID);
		return grid;
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {

	}
}
