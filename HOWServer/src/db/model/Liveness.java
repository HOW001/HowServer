package db.model;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import db.service.IDManager;
import server.cmds.LivenessCP;
import server.cmds.UISystemCP;
import util.binreader.ItemData;
import util.binreader.LivenessAwardData;
import util.binreader.LivenessListData;
import util.logger.ItemLogger;

/**
 * 活跃度
 * @author fengmx
 */
public class Liveness implements DataBaseEntry{
	private static Logger logger = Logger.getLogger(Liveness.class);
	
	private Liveness(){};
	public static Liveness create(){
		Liveness liveness=new Liveness();
		liveness.id=IDManager.getInstance().getCurrentLivenessID();
		return liveness;
	}
	private int id;
	private int version;
	private int holder;
	/**
	 * 当前活跃度
	 */
	private int currentLiveness;
	/**
	 * 活跃编号和该编号完成的次数
	 */
	private String codeAndCompleteTimes;
	/**
	 * 签到领奖的7天状态
	 */
	private String isAwardForSign;
	/**
	 * 一天中活跃度领取的4个状态
	 */
	private String isAwardForList;
	/**
	 * 分享领奖
	 */
	private String share_award;//格式：领奖1,是否领取#领奖2,是否领取#领奖3,是否领取
	
	public Map<Integer, Integer> livenessMap = new TreeMap<Integer, Integer>();
	public Map<Integer, Integer> isAwardForSignMap = new TreeMap<Integer, Integer>();
	public Map<Integer, Integer> isAwardForListMap = new TreeMap<Integer, Integer>();
	public Map<Integer, Integer> share_award_map = new TreeMap<Integer, Integer>();
	public Map<Integer, Integer> getLivenessMap() {
		return livenessMap;
	}
	public static final int state_has = 1;//已领取状态
	public static final int state_can = 2;//领取高亮状态
	public static final int state_not_can = 3;//领取灰色状态
	/**
	 * 初始化活跃度
	 */
	public void initLiveness(){
		//活跃度
		String[] arr = this.codeAndCompleteTimes.split(",");
		if(arr!=null){
			for(String subString:arr){
				String[] subArr = subString.split("#");
				if(subArr.length!=2){
					continue;
				}
				livenessMap.put(Integer.parseInt(subArr[0]), Integer.parseInt(subArr[1]));
			}
		}
		//签到
		arr = this.isAwardForSign.split(",");
		if(arr!=null){
			for(String subString:arr){
				String[] subArr = subString.split("#");
				if(subArr.length!=2){
					continue;
				}
				isAwardForSignMap.put(Integer.parseInt(subArr[0]), Integer.parseInt(subArr[1]));
			}
		}
//		isAwardForSignMap.put(1, state_can);
//		for(int i=2;i<=7;i++){
//			isAwardForSignMap.put(i, state_not_can);
//		}
//		isAwardForSign=mapToString(isAwardForSignMap);
		//活跃度列表
		arr = this.isAwardForList.split(",");
		if(arr!=null){
			for(String subString:arr){
				String[] subArr = subString.split("#");
				if(subArr.length!=2){
					continue;
				}
				isAwardForListMap.put(Integer.parseInt(subArr[0]), Integer.parseInt(subArr[1]));
			}
		}
		//分享
		if(share_award!=null&&share_award.length()>=3){
			arr = this.share_award.split(",");
			if(arr!=null){
				for(String subString:arr){
					String[] subArr = subString.split("#");
					if(subArr.length!=2){
						continue;
					}
					share_award_map.put(Integer.parseInt(subArr[0]), Integer.parseInt(subArr[1]));
				}
			}
		} 
	}
	/**
	 * 根据活跃度编号获取完成次数
	 * @param code
	 * @return
	 */
	public int getCompleteTimesByCode(int code){
		for(Map.Entry<Integer, Integer> entry:livenessMap.entrySet()){
			if(entry.getKey()==code){
				return entry.getValue();
			}
		}
		return 0;
	}
	public static final int bronze_medal = 20;
	public static final int silver_medal = 50;
	public static final int gold_medal = 80;
	public static final int diamond_medal = 100;

	/**
	 * 根据活跃度编号更改完成次数
	 */
	public synchronized void changeCompleteTimesByCode(Player player, int code,int value){
		if(value<=0){
			return;
		}
		if(code<=0){
			return;
		}
		for(Map.Entry<Integer, Integer> entry:livenessMap.entrySet()){
			if(entry.getKey()==code){
				LivenessListData livenessListData = LivenessListData.getLivenessListDataById(code);
				if(livenessListData==null){
					logger.error("无效的LivenessListData数据："+code);
					break;
				}
				int oldValue = entry.getValue();
				int needTimes = livenessListData.needTimes;
				if(oldValue==needTimes){
					break;
				} else if(oldValue>needTimes){
					oldValue = needTimes;
					value = 0;
					logger.error(player.getName()+"当前活跃度次数超过最大次数："+code);
				} else if(oldValue+value>needTimes){
					value = needTimes-oldValue;
				}
				entry.setValue(oldValue+value);
				if(oldValue+value>=needTimes){
					//该活跃度已完成
					int oldCurrentLiveness = this.currentLiveness;
					this.currentLiveness += livenessListData.award;
					int type = 0;
					if(oldCurrentLiveness<bronze_medal){
						if(currentLiveness>=bronze_medal){
							type = 1;
						}
					} else if(oldCurrentLiveness<silver_medal){
						if(currentLiveness>=silver_medal){
							type = 2;
						}
					} else if(oldCurrentLiveness<gold_medal){
						if(currentLiveness>=gold_medal){
							type = 3;
						}
					} else if(oldCurrentLiveness<diamond_medal){
						if(currentLiveness>=diamond_medal){
							type = 4;
						}
					}
					if(type>=1&&type<=4){
						changeStateByType(player, 1, type, state_can);
					}
				}
				LivenessCP.getInstance().updateLivenessListMessage(player, code);
				this.codeAndCompleteTimes = mapToString(livenessMap);
				logger.info(player.getName()+",活跃度："+code+",改变："+value);
				break;
			}
		}
	}
	public String mapToString(Map<Integer, Integer> map){
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<Integer, Integer> entry:map.entrySet()){
			sb.append(entry.getKey()).append("#").append(entry.getValue()).append(",");
		}
		return sb.substring(0, sb.length()-1);
	}
	/**
	 * 零点重置 或是登陆重置活跃度
	 */
	public void resetLiveness() {
		//活跃度列表
		this.currentLiveness=0;
		for(Map.Entry<Integer, Integer> entry:livenessMap.entrySet()){
			entry.setValue(0);
		}
		this.codeAndCompleteTimes = mapToString(livenessMap);
		//活跃度奖励状态
		isAwardForListMap.clear();
		for(int i=1;i<=4;i++){
			isAwardForListMap.put(i, state_not_can);
		}
		this.isAwardForList = mapToString(isAwardForListMap);
	}
	/**
	 * 零点重置 签到
	 */
	public void resetLivenessSign(Player player) {
//		player.setContinueDays(player.getContinueDays()+1);
		player.addContinueDays(1);
		player.setLastLoginTime(new Date());
		int continueDays = player.getContinueDays();
		changeSignState(player, continueDays);
//		if(continueDays==1){
//			player.getLivenessEntry().resetSign();
//		}else if(continueDays/7>=1&&continueDays%7==1){
//			player.getLivenessEntry().resetSign();
//		} else {
//			int days = continueDays%7;
////			changeStateByType(player, 0, days, Liveness.state_can);
//			for(Map.Entry<Integer, Integer> entry:isAwardForSignMap.entrySet()){
//				if(entry.getKey()<=days && entry.getValue()==Liveness.state_not_can){
//					entry.setValue(Liveness.state_can);
//					logger.info(player.getName()+",签到领奖状态："+entry.getKey()+",改变为："+Liveness.state_can);
//				}
//			}
//			LivenessCP.getInstance().updateLivenessMessage(player, days);
//		}
		isAwardForSign=mapToString(isAwardForSignMap);
	}
	/**
	 * 根据连续登陆天数，改变签到状态
	 * @param player
	 * @param continueDays
	 */
	public void changeSignState(Player player, int continueDays){
		int days=continueDays%7;
		switch(days){
		case 0:
			days = 7;
//			changeStateByType(player, 0, 7, Liveness.state_can);
			break;
		case 1:
			player.getLivenessEntry().resetSign();
			break;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
//			changeStateByType(player, 0, days, Liveness.state_can);
			break;
		}
		//若连续多天登陆，但是前些天没置会可领取状态的，再次置到可领取状态
		for(Map.Entry<Integer, Integer> entry:isAwardForSignMap.entrySet()){
			if(entry.getKey()<=days && entry.getValue()==Liveness.state_not_can){
				entry.setValue(Liveness.state_can);
				logger.info(player.getName()+",签到领奖状态："+entry.getKey()+",改变为："+Liveness.state_can);
			}
		}
		this.isAwardForSign = mapToString(isAwardForSignMap);
	}
	/**
	 * 连续登陆7天的倍数,签到领奖状态
	 */
	public void resetSign(){
		isAwardForSignMap.clear();
		isAwardForSignMap.put(1, state_can);
		for(int i=2;i<=7;i++){
			isAwardForSignMap.put(i, state_not_can);
		}
		this.isAwardForSign = mapToString(isAwardForSignMap);
	}
	/**
	 * 更改活跃度领取状态
	 * @param player
	 * @param type 1：铜牌    2：银牌    3：金牌    4：钻石
	 * @param state 新的状态
	 */
	public void changeStateByType(Player player, int fatherType, int type,int state){
		if(state!=1&&state!=2&&state!=3){
			return;
		}
		if(fatherType==0){
			if(type<1||type>7){
				return;
			}
			for(Map.Entry<Integer, Integer> entry:isAwardForSignMap.entrySet()){
				if(entry.getKey()==type){
					entry.setValue(state);
					LivenessCP.getInstance().updateLivenessMessage(player, type);
					logger.info(player.getName()+",签到领奖状态："+type+",改变为："+state);
					break;
				}
			}
			isAwardForSign=mapToString(isAwardForSignMap);
		} else if(fatherType==1){
			if(type<1||type>4){
				return;
			}
			for(Map.Entry<Integer, Integer> entry:isAwardForListMap.entrySet()){
				if(entry.getKey()==type){
					entry.setValue(state);
					LivenessCP.getInstance().updateLivenessListState(player, type);
					logger.info(player.getName()+",活跃度领奖状态："+type+",改变为："+state);
					break;
				}
			}
			isAwardForList=mapToString(isAwardForListMap);
		}else{
			for(Map.Entry<Integer, Integer> entry:share_award_map.entrySet()){
				if(entry.getKey()==type){
					entry.setValue(state);
					LivenessCP.getInstance().updateLivenessListState(player, type);
					logger.info(player.getName()+",活跃度领奖状态："+type+",改变为："+state);
					break;
				}
			}
		}
	}
	public int getStateByType(int fatherType, int subType){
		if(fatherType==0){
			for(Map.Entry<Integer, Integer> entry:isAwardForSignMap.entrySet()){
				if(entry.getKey()==subType){
					return entry.getValue();
				}
			}
		} else if(fatherType == 1){
			for(Map.Entry<Integer, Integer> entry:isAwardForListMap.entrySet()){
				if(entry.getKey()==subType){
					return entry.getValue();
				}
			}
		} else if(fatherType == 2){
			for(Map.Entry<Integer, Integer> entry:share_award_map.entrySet()){
				if(entry.getKey()==subType){
					logger.info("取出id为 "+subType+" ----领取状态=="+entry.getValue());
					return entry.getValue();
				}
			}
		}
		return 0;
	}
	/**
	 * 给玩家活跃度奖励
	 * @param fatherType 0:签到奖励   1：活跃度奖励  2：分享奖励
	 * @param subType
	 */
	public void addLivenessAward(Player player, int fatherType, int subType) {
		if(fatherType!=0&&fatherType!=1&&fatherType!=2){
			return;
		}
		if(!check(fatherType, subType)){
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "不能领取！");
			return;
		}
		//领取奖励
		LivenessAwardData livenessAwardData = LivenessAwardData.getLivenessAwardData(fatherType, subType);
		if(livenessAwardData==null){
			logger.error("无效的LivenessAwardData数据："+subType);
			return;
		}
		int[][] giftItems = livenessAwardData.getAwardItem(livenessAwardData.award);
		if(player.getPlayerPackEntry().getSpaceNumber()<giftItems.length){
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "背包空间不足");
			return;
		}
		changeStateByType(player, fatherType, subType, state_has);
		for(int i=0;i<giftItems.length;i++){
			int[] drops=giftItems[i];
			if(drops[1]>0){
				player.getPlayerPackEntry().addItem(drops[0], drops[1],ItemLogger.itemAdd[25]);
				UISystemCP.sendFlutterMessageForOK(player.getIoSession(), "获得"+ItemData.getItemData(drops[0]).getName());
			}
		}
	}
	public boolean check(int fatherType, int subType){
		if(getStateByType(fatherType,subType)==state_can){
			return true;
		}
		return false;
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
	public int getCurrentLiveness() {
		return currentLiveness;
	}
	public void setCurrentLiveness(int currentLiveness) {
		this.currentLiveness = currentLiveness;
	}
	public String getCodeAndCompleteTimes() {
		return codeAndCompleteTimes;
	}
	public void setCodeAndCompleteTimes(String codeAndCompleteTimes) {
		this.codeAndCompleteTimes = codeAndCompleteTimes;
	}
	public String getIsAwardForSign() {
		return isAwardForSign;
	}
	public void setIsAwardForSign(String isAwardForSign) {
		this.isAwardForSign = isAwardForSign;
	}
	public String getIsAwardForList() {
		return isAwardForList;
	}
	public void setIsAwardForList(String isAwardForList) {
		this.isAwardForList = isAwardForList;
	}
	public String getShare_award() {
		return share_award;
	}
	public void setShare_award(String share_award) {
		this.share_award = share_award;
	}
	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());
	}
	public void initDBEntry(Player p,String codeAndCompleteTimes, boolean isInit) {
		initDBEntry(p);
		this.codeAndCompleteTimes=codeAndCompleteTimes;
		//签到
		isAwardForSignMap.put(1, state_can);
		for(int i=2;i<=7;i++){
			isAwardForSignMap.put(i, state_not_can);
		}
		isAwardForSign=mapToString(isAwardForSignMap);
		//活跃度
		for(int i=1;i<=4;i++){
			isAwardForListMap.put(i, state_not_can);
		}
		isAwardForList=mapToString(isAwardForListMap);
	}
	
}
