/**
 * 玩家比赛完成信息
 */
package db.model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import world.object.Contest;
import db.service.IDManager;

/**
 * @author liuzg
 * 
 */
public class PlayerCompetitionInfo implements DataBaseEntry {
	private static Logger logger = Logger
			.getLogger(PlayerCompetitionInfo.class);

	private static final Calendar cal = Calendar.getInstance();

	private PlayerCompetitionInfo() {
	};

	public static PlayerCompetitionInfo create() {
		PlayerCompetitionInfo info = new PlayerCompetitionInfo();
		info.id = IDManager.getInstance().getCurrentPlayerCompetitionInfoID();
		return info;
	}

	private int id;
	private int version;

	/**
	 * 持有人
	 */
	private int holder;

	/**
	 * 普通格子完成状态 1#完成时间#完成次数#天数#当天完成次数#重置次数#bossID&2#完成时间#天数#当天完成次数#重置次数#bossID　　
	 * 对应于grid_info当中的id
	 */
	private String normalGridCompleteState;

	/**
	 * 普通格子完成集合
	 */
	private Map<Integer, String> normalGridMaps = new HashMap<Integer, String>();

	/**
	 * 噩梦关卡完成状态 1#完成时间#完成次数#天数#当天完成次数#重置次数#bossID&2#完成时间#天数#当天完成次数#重置次数#bossID　　
	 * 对应于grid_info当中的id
	 */
	private String nightMareGridCompleteState;

	/**
	 * 噩梦格子完成集合
	 */
	private Map<Integer, String> nightMareGridMaps = new HashMap<Integer, String>();
	/**
	 * 地狱关卡完成状态 1#完成时间#完成次数#天数#当天完成次数#重置次数#bossID&2#完成时间#天数#当天完成次数#重置次数#bossID　　
	 * 对应于grid_info当中的id
	 */
	private String hellGridCompleteState;

	/**
	 * 地狱格子完成集合
	 */
	private Map<Integer, String> hellGridMaps = new HashMap<Integer, String>();

	/**
	 * @author liuzg 将数据库信息转换为List,在玩家登录时调用
	 */
	public void convertToList() {
		for (String gate : normalGridCompleteState.split("&")) {
			int gateID = Integer.parseInt(gate.split("#")[0]);
			normalGridMaps.put(gateID, gate);
		}
		for (String gate : nightMareGridCompleteState.split("&")) {
			int gateID = Integer.parseInt(gate.split("#")[0]);
			nightMareGridMaps.put(gateID, gate);
		}
		for (String gate : hellGridCompleteState.split("&")) {
			int gateID = Integer.parseInt(gate.split("#")[0]);
			hellGridMaps.put(gateID, gate);
		}

	}

	public void convertToStringForGridMaps() {
		StringBuffer sb = new StringBuffer();
		for (String gate : normalGridMaps.values()) {
			sb.append("&" + gate);
		}
		normalGridCompleteState = sb.substring(1);
		sb = new StringBuffer();
		for (String gate : nightMareGridMaps.values()) {
			sb.append("&" + gate);
		}
		nightMareGridCompleteState = sb.substring(1);
		sb = new StringBuffer();
		for (String gate : hellGridMaps.values()) {
			sb.append("&" + gate);
		}
		hellGridCompleteState = sb.substring(1);
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param difficulty指定难度
	 *            添加关卡重置次数
	 */
	public void addResetTimes(int gateID, int difficulty) {
		try {
			Map<Integer, String> gateMaps = new HashMap<Integer, String>();
			switch (difficulty) {
			case Contest.CONTEST_DIFFICULTY_NORMAL:
				gateMaps = normalGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
				gateMaps = nightMareGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_HELL:
				gateMaps = hellGridMaps;
				break;
			}
			cal.setTimeInMillis(System.currentTimeMillis());
			int days = cal.get(Calendar.DAY_OF_YEAR);
			if (gateMaps.containsKey(gateID)) {
				String info = gateMaps.get(gateID);
				if (info != null) {
					String datas[] = info.split("#");
					int temp = Integer.parseInt(datas[3]);// 当天编号
					if (days != temp) {
						datas[3] = days + "";
						datas[5] = "1";// 重置次数
					} else {
						datas[5] = (Integer.parseInt(datas[5]) + 1) + "";
					}
					datas[4] = "0";// 当天完成次数
					info = datas[0] + "#" + datas[1] + "#" + datas[2] + "#"
							+ datas[3] + "#" + datas[4] + "#" + datas[5]+"#"+datas[6];
					gateMaps.put(gateID, info);
					convertToStringForGridMaps();
				}
			}
		} catch (Exception e) {
			logger.error("添加重置关卡次数出现异常:", e);
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 *            某关卡已完成
	 */
	public void addCompeleteGrid(int gateID, int bossID,int difficulty) {
		try {
			cal.setTimeInMillis(System.currentTimeMillis());
			int days = cal.get(Calendar.DAY_OF_YEAR);
			Map<Integer, String> gridMaps = new HashMap<Integer, String>();
			switch (difficulty) {
			case Contest.CONTEST_DIFFICULTY_NORMAL:
				gridMaps = normalGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
				gridMaps = nightMareGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_HELL:
				gridMaps = hellGridMaps;
				break;
			}

			if (gridMaps.containsKey(gateID)) {
				String info = gridMaps.get(gateID);
				if (info != null) {
					String datas[] = info.split("#");
					datas[0] = datas[0];// 关卡ID
					datas[1] = System.currentTimeMillis() + "";// 完成时间
					datas[2] = (Integer.parseInt(datas[2]) + 1) + "";// 完成次数
					int temp = Integer.parseInt(datas[3]);// 当天编号
					if (days != temp) {
						datas[3] = days + "";
						datas[4] = "0";
						datas[5] = "0";// 第二天设为0
					} else {
						datas[5] = Integer.parseInt(datas[5]) + "";// 同一天重置次数不变
					}
					datas[4] = (Integer.parseInt(datas[4]) + 1) + "";// 当天完成次数
					datas[6] =bossID+"";
					info = datas[0] + "#" + datas[1] + "#" + datas[2] + "#"
							+ datas[3] + "#" + datas[4] + "#" + datas[5]+"#"+datas[6];
					gridMaps.put(gateID, info);
				}
			} else {// 未完成
					// 关卡ID#完成时间#完成次数#当天编号#当天完成次数#重置次数#bossID
				String info = gateID + "#" + System.currentTimeMillis() + "#"
						+ 1 + "#" + days + "#" + 1 + "#" + 0+"#"+0;
				gridMaps.put(gateID, info);
			}
			convertToStringForGridMaps();
		} catch (Exception e) {
			logger.error("判断某关卡是否完成时出现异常:", e);
		}
	}

	/**
	 * @author liuzg
	 * @param gridID
	 * @param difficulty
	 * @return 某关卡是否完成
	 */
	public boolean isCompeleteGrid(int gridID, int difficulty) {
		Map<Integer, String> gridDatas = new HashMap<Integer, String>();
		switch (difficulty) {
		case Contest.CONTEST_DIFFICULTY_NORMAL:
			gridDatas = normalGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
			gridDatas = nightMareGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_HELL:
			gridDatas = hellGridMaps;
			break;
		}

		if (gridDatas.containsKey(gridID)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @return 获取关卡完成时间，即冷却计时
	 */
	public long getCompleteGridTime(int gateID, int difficulty) {
		Map<Integer, String> gridMaps = new HashMap<Integer, String>();
		switch (difficulty) {
		case Contest.CONTEST_DIFFICULTY_NORMAL:
			gridMaps = normalGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
			gridMaps = nightMareGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_HELL:
			gridMaps = hellGridMaps;
			break;
		}

		if (gridMaps.containsKey(gateID) == false) {
			return 0;
		}
		String value = gridMaps.get(gateID);
		String values[] = value.split("#");
		long time;
		try {
			time = Long.parseLong(values[1]);
			return time;
		} catch (Exception e) {
			logger.error("获取关卡完成时间异常:", e);
			return 0;
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @return 获取关卡完成次数
	 */
	public int getCompleteGridNum(int gateID, int difficulty) {
		Map<Integer, String> gridMaps = new HashMap<Integer, String>();
		switch (difficulty) {
		case Contest.CONTEST_DIFFICULTY_NORMAL:
			gridMaps = normalGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
			gridMaps = nightMareGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_HELL:
			gridMaps = hellGridMaps;
			break;
		}

		if (gridMaps.containsKey(gateID) == false) {
			return 0;
		}
		String value = gridMaps.get(gateID);
		String info[] = value.split("#");
		int num = 0;
		try {
			num = Integer.parseInt(info[2]);
			return num;
		} catch (Exception e) {
			logger.error("关卡完成次数异常:" + num, e);
			return 0;
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @return 获取当天完成次数
	 */
	public int getCompleteGridNumForCurrentDay(int gateID, int difficulty) {
		try {
			Map<Integer, String> gridMaps = new HashMap<Integer, String>();
			switch (difficulty) {
			case Contest.CONTEST_DIFFICULTY_NORMAL:
				gridMaps = normalGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
				gridMaps = nightMareGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_HELL:
				gridMaps = hellGridMaps;
				break;
			}

			if (gridMaps.containsKey(gateID) == false) {
				return 0;
			}
			// synchronized(cal){
			cal.setTimeInMillis(System.currentTimeMillis());
			// }
			int days = cal.get(Calendar.DAY_OF_YEAR);
			String value = gridMaps.get(gateID);
			String info[] = value.split("#");
			if (Integer.parseInt(info[3]) == days) {
				return Integer.parseInt(info[4]);
			} else {
				return 0;
			}
		} catch (Exception e) {
			logger.error("获取当天完成次数时出现异常:", e);
			return 0;
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @return 关卡重置次数
	 */
	public int getResetTimes(int gateID, int difficulty) {
		try {// 当天尚未重置
			Map<Integer, String> gridMaps = new HashMap<Integer, String>();
			switch (difficulty) {
			case Contest.CONTEST_DIFFICULTY_NORMAL:
				gridMaps = normalGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
				gridMaps = nightMareGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_HELL:
				gridMaps = hellGridMaps;
				break;
			}

			if (gridMaps.containsKey(gateID) == false) {
				return 0;
			}
			// synchronized(cal){
			cal.setTimeInMillis(System.currentTimeMillis());
			// }
			int days = cal.get(Calendar.DAY_OF_YEAR);
			String value = gridMaps.get(gateID);
			String info[] = value.split("#");
			int num = 0;
			int temp = Integer.parseInt(info[3]);// 当天编号
			if (days != temp) {
				return 0;
			} else {
				num = Integer.parseInt(info[5]);
				return num;
			}
		} catch (Exception e) {
			logger.error("关卡重置次数异常", e);
			return 0;
		}
	}
	/**
	 * @author liuzg
	 * @param gateID
	 * @return 关卡打败的bossID
	 */
	public int getGridBossID(int gateID, int difficulty) {
		try {// 当天尚未重置
			Map<Integer, String> gridMaps = new HashMap<Integer, String>();
			switch (difficulty) {
			case Contest.CONTEST_DIFFICULTY_NORMAL:
				gridMaps = normalGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
				gridMaps = nightMareGridMaps;
				break;
			case Contest.CONTEST_DIFFICULTY_HELL:
				gridMaps = hellGridMaps;
				break;
			}

			if (gridMaps.containsKey(gateID) == false) {
				return 0;
			}
			String value = gridMaps.get(gateID);
			String info[] = value.split("#");
			int temp = Integer.parseInt(info[6]);// 当天编号
			return temp;
		} catch (Exception e) {
			logger.error("关卡bossID异常", e);
			return 0;
		}
	}

	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());
		this.setNormalGridCompleteState("0");
		this.setNightMareGridCompleteState("0");
		this.setHellGridCompleteState("0");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getHolder() {
		return holder;
	}

	public void setHolder(int holder) {
		this.holder = holder;
	}

	public Map<Integer, String> getGridMaps(int difficulty) {
		Map<Integer, String> gridMaps = new HashMap<Integer, String>();
		switch (difficulty) {
		case Contest.CONTEST_DIFFICULTY_NORMAL:
			gridMaps = normalGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_NIGHTMARE:
			gridMaps = nightMareGridMaps;
			break;
		case Contest.CONTEST_DIFFICULTY_HELL:
			gridMaps = hellGridMaps;
			break;
		}

		return gridMaps;
	}

	public String getNormalGridCompleteState() {
		return normalGridCompleteState;
	}

	public void setNormalGridCompleteState(String normalGateCompleteState) {
		this.normalGridCompleteState = normalGateCompleteState;
	}

	public String getNightMareGridCompleteState() {
		return nightMareGridCompleteState;
	}

	public void setNightMareGridCompleteState(String nightMareGateCompleteState) {
		this.nightMareGridCompleteState = nightMareGateCompleteState;
	}

	public String getHellGridCompleteState() {
		return hellGridCompleteState;
	}

	public void setHellGridCompleteState(String hellGateCompleteState) {
		this.hellGridCompleteState = hellGateCompleteState;
	}
}
