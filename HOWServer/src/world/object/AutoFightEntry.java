/**
 * 
 */
package world.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import server.cmds.CmdParser;
import server.cmds.FightCP;
import util.ByteArray;
import util.binreader.GameParameterData;
import util.binreader.ItemData;
import util.binreader.GateInfoData;
import util.logger.ItemLogger;
import util.logger.MoneyLogger;
import world.World;
import db.model.Player;

/**
 * @author liuzg 自动战斗实体
 */
public class AutoFightEntry {
	private static Logger logger = Logger.getLogger(AutoFightEntry.class);
	public static int PERGATECDTIME = 1000 * 60;// 以毫秒为单位
	private long startTime = 0;// 开始时间
	private int holder = 0;// 持有者
	private int mapType = 0;
	public long lastTickTime = 0;// 计算最后一次心跳的时间

	public AutoFightEntry(Player p, int mapType) {
		holder = p.getId();
		startTime = System.currentTimeMillis();
		this.mapType = mapType;
		AutoFightManager.addAutoFightEntry(holder, this.mapType, this);
		PERGATECDTIME = GameParameterData.autoFightCDTime * 1000;
		lastTickTime = System.currentTimeMillis();
	}

	private long needTotalCDTime = 0;// 本次自动战斗需要冷却的时间,以毫秒为单位
	private String completeResult = "";// 完成结果,即为什么结束自动战斗
	// 当前完成的赛道，即下面所有集合的key
	private Queue<Integer> completeGateList = new java.util.concurrent.ConcurrentLinkedQueue<Integer>();
	// 每个赛道获得的金钱奖励key=gateID,value=moneyNum
	private Map<Integer, Double> rewardMoneyMaps = new ConcurrentHashMap<Integer, Double>();
	// 每个赛道获得的物品奖励key=gateID,value=ItemID1#Num1&ItemID2#Num2
	private Map<Integer, String> rewardItemMaps = new ConcurrentHashMap<Integer, String>();
	// 每个赛道获得的经验奖励key=gateID,value=exp
	private Map<Integer, Double> rewardExpMaps = new ConcurrentHashMap<Integer, Double>();
	// 每个赛道获得的助手经验奖励key=gateID,value=helperExp
	private Map<Integer, Double> rewardHelperExpMaps = new ConcurrentHashMap<Integer, Double>();
	// 每个赛道捕捉的活动信息key=gateID,value=captureID
	private Map<Integer, Integer> rewardCaptureMaps = new ConcurrentHashMap<Integer, Integer>();

	// 自动战斗之后，需要发送的奖励信息
	private Map<Integer, byte[]> rewardInfo = new TreeMap<Integer, byte[]>();// 关卡名称UTF,金钱Int,角色经验Int,助手经验Int,[物品IDInt,数量Int]
	private boolean isStopFight = false;

	/**
	 * @author liuzg 玩家已退出游戏，将未发放的奖励清空，并减少相关冷却时间
	 */
	public synchronized void playerLogout() {
		if (completeGateList.size() == 0) {
			return;
		}
		int size = completeGateList.size();
		completeGateList.clear();
		needTotalCDTime -= (size * PERGATECDTIME);
		if (needTotalCDTime <= 0) {
			needTotalCDTime = 0;
		}
		stopFight("玩家已退出游戏!");
	}

	/**
	 * @author liuzg 处理奖励发放数据
	 */
	public void processRewardData() {
		Player p = World.getPlayer(holder);
		if (p == null) {
			return;
		}

		int gateID = 0;
		GateInfoData gate = null;
		ByteArray ba = new ByteArray();
		gateID = completeGateList.remove();
		logger.info(p.getName() + "处理一次自动战斗奖励信息:mapType=" + mapType + ",gateID=" + gateID);
		gate = GateInfoData.getRoadGateInfoData(gateID);
		if (gate != null) {
			ba.writeUTF(gate.name);
		} else {
			ba.writeUTF("未知赛道:" + gateID);
		}
		List<String> awardInfo = new ArrayList<String>();
		// 处理赛道奖励金钱
		if (rewardMoneyMaps.get(gateID) != null) {
			double money = rewardMoneyMaps.get(gateID);
			ba.writeInt((int) money);
			if (money > 0) {
				logger.info(p.getName() + "自动战斗比赛完成奖励processRewardData玩家金钱:" + money);
				// p.addBindGold((int) money, MoneyLogger.moneyAdd[13]);
				awardInfo.add("绑定银币+" + (int) money);
			}
		} else {
			ba.writeInt(0);
		}
		// 处理赛道奖励经验
		if (rewardExpMaps.get(gateID) != null) {
			gate = GateInfoData.getRoadGateInfoData(gateID);
			double exp = rewardExpMaps.get(gateID);
			ba.writeInt((int) exp);
			if (exp > 0) {
				logger.info(p.getName() + "自动战斗比赛完成奖励processRewardData玩家经验:" + exp);
				p.addXp(Math.round(exp));
				awardInfo.add("角色经验+" + Math.round(exp));
			}
		} else {
			ba.writeInt(0);
		}
		// 处理赛道奖励助手经验
		if (rewardHelperExpMaps.get(gateID) != null) {
			gate = GateInfoData.getRoadGateInfoData(gateID);
			double exp = rewardHelperExpMaps.get(gateID);
			ba.writeInt((int) exp);
		} else {
			ba.writeInt(0);
		}
		// 处理赛道奖励物品
		int itemNum = 0;
		ByteArray itemData = new ByteArray();
		if (rewardItemMaps.get(gateID) != null) {
			gate = GateInfoData.getRoadGateInfoData(gateID);
			String info = rewardItemMaps.get(gateID);
			// 通知玩家并增加物品信息
			for (String itemAndNum : info.split("&")) {
				int itemID = Integer.parseInt(itemAndNum.split("#")[0]);
				int num = Integer.parseInt(itemAndNum.split("#")[1]);
				ItemData item = ItemData.getItemData(itemID);
				if (item != null && num > 0) {
					itemNum++;
					itemData.writeInt(itemID);
					itemData.writeInt(num);
					logger.info(p.getName() + "自动战斗得到物品processRewardData:id=" + itemID + ",num=" + num);
					p.getPlayerPackEntry().addItem(itemID, num, ItemLogger.itemAdd[2]);
					awardInfo.add(item.getName() + "+" + num);
				}
			}
		}
		ba.writeShort(itemNum);
		ba.writeByteArray(itemData.toArray());
		rewardInfo.put(gateID, ba.toArray());
		// 发送自动战斗奖励信息
		FightCP.getInstance().responseAutoFightAward(p, mapType, awardInfo);
		if (completeGateList.size() == 0) {
			// 奖励发放完毕,可以停止战斗了
			stopFight("");
		}
	}

	/**
	 * @author liuzg
	 * @param p
	 *            停止游戏
	 */
	public void stopFight(String desc) {
		isStopFight = true;
		needTotalCDTime = 0;
		Player p = World.getPlayer(holder);
		if (p == null) {
			return;
		}
		if (desc.length() > 0) {
			completeResult = desc;
		}
		logger.info(p.getName() + "自动战斗奖励已经提示发放完毕!mapType=" + mapType + "desc=" + desc);
		if (getCompleteResult().length() > 0) {
			p.sendResult("自动战斗结束原因:" + getCompleteResult());
			setCompleteResult("");
		}
		// 通知前端自动战斗停止
		sendAutoFightStop(p);
	}

	/**
	 * @author liuzg
	 * @param p
	 *            向前端发送自动战斗停止信息
	 */
	private void sendAutoFightStop(Player p) {
		logger.info(p.getName() + "通知可以停止自动战斗:mapType=" + mapType);
		ByteArray ba = new ByteArray();
		ba.writeInt(mapType);
		ba.writeShort(rewardInfo.size());
		for (Map.Entry<Integer, byte[]> data : rewardInfo.entrySet()) {
			// logger.info(p.getName()+"完成的关卡顺序:"+data.getKey());
			ba.writeByteArray(data.getValue());
		}
		FightCP.getInstance().sendAutoFightStop(p, ba.toArray());
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param captureID
	 *            添加赛道捕捉活物
	 */
	public void addRewardCaptureMaps(int gateID, int captureID) {
		// 暂时每一次一张赛道只能捕捉一个助手
		rewardCaptureMaps.put(gateID, captureID);
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param helperExp
	 *            添加赛道助手经验信息
	 */
	public void addRewardHelperExpMaps(int gateID, double helperExp) {
		if (rewardHelperExpMaps.get(gateID) == null) {
			rewardHelperExpMaps.put(gateID, helperExp);
		} else {
			rewardHelperExpMaps.put(gateID, helperExp + rewardHelperExpMaps.get(gateID));
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param exp
	 *            添加赛道奖励经验信息
	 */
	public void addRewardExpMaps(int gateID, double exp) {
		if (rewardExpMaps.get(gateID) == null) {
			rewardExpMaps.put(gateID, exp);
		} else {
			rewardExpMaps.put(gateID, exp + rewardExpMaps.get(gateID));
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param itemID
	 * @param itemNum
	 *            添加赛道奖励的物品
	 */
	public void addRewardItem(int gateID, int itemID, int itemNum) {
		if (rewardItemMaps.get(gateID) == null) {
			rewardItemMaps.put(gateID, itemID + "#" + itemNum);
		} else {
			StringBuffer info = new StringBuffer(rewardItemMaps.get(gateID));
			Map<Integer, Integer> itemMaps = new HashMap<Integer, Integer>();
			for (String itemAndNum : info.toString().split("&")) {
				int id = Integer.parseInt(itemAndNum.split("#")[0]);
				int num = Integer.parseInt(itemAndNum.split("#")[1]);
				itemMaps.put(id, num);
			}
			if (itemMaps.get(itemID) == null) {
				itemMaps.put(itemID, itemNum);
			} else {
				itemMaps.put(itemID, itemNum + itemMaps.get(itemID));
			}
			info = new StringBuffer();
			for (Map.Entry<Integer, Integer> entry : itemMaps.entrySet()) {
				info.append("&");
				info.append(entry.getKey() + "#" + entry.getValue());
			}
			rewardItemMaps.put(gateID, info.substring(1));
		}
	}

	/**
	 * @author liuzg
	 * @param gateID
	 * @param moneyNum
	 *            增加赛道奖励的金钱
	 */
	public void addRewardMoney(int gateID, double moneyNum) {
		if (rewardMoneyMaps.get(gateID) == null) {
			rewardMoneyMaps.put(gateID, moneyNum);
		} else {
			rewardMoneyMaps.put(gateID, moneyNum + rewardMoneyMaps.get(gateID));
		}
	}

	/**
	 * @author liuzg
	 * @param times
	 *            增加CD时间
	 */
	public void addNeedTotalCDTime(long times) {
		needTotalCDTime += times;
	}

	/**
	 * @author liuzg
	 * @param gateID
	 *            添加一张完成赛道
	 */
	public void addCompleteGateID(int gateID) {
		if (completeGateList.contains(gateID) == false) {
			completeGateList.add(gateID);
		}
	}

	/**
	 * @author liuzg
	 * @return 单位秒 获取自动战斗的剩余时间 >0为冷却时间 <=0为活跃时间
	 */
	public int getAutoFightCDTime() {
		int cd = (int) (needTotalCDTime - (System.currentTimeMillis() - startTime));
		if (cd <= 0) {
			return 0;
		} else {
			return cd > 1000 ? cd / 1000 : 1;
		}
	}

	/**
	 * @author liuzg
	 * @param p
	 * @param mapType
	 * @return 返回单位秒 获取自动战斗的CD时间
	 */
	public static int getAutoFightCDTime(Player p, int mapType) {
		// 获取一个自动战斗实体
		AutoFightEntry auto = AutoFightManager.getAutoFightEntry(p, mapType);
		if (auto != null) {
			// 判断是否进行下一次自动战斗,主要是冷却时间
			if (auto.getAutoFightCDTime() > 0) {
				return auto.getAutoFightCDTime();
			}
		}
		return 0;
	}

	public String getCompleteResult() {
		return completeResult;
	}

	public void setCompleteResult(String completeResult) {
		this.completeResult = completeResult;
	}

	public int getHolder() {
		return holder;
	}

	public boolean isStopFight() {
		return isStopFight;
	}

	public void setStopFight(boolean isStopFight) {
		this.isStopFight = isStopFight;
	}

	public int getMapType() {
		return mapType;
	}
}
