package util.binreader;

import java.util.HashMap;
import java.util.Map;

public class LivenessAwardData implements PropertyReader {
	public int code;
	public int type;
	public int liveness;
	public int continueDays;
	public String award;
	private static Map<Integer, LivenessAwardData> data = new HashMap<Integer, LivenessAwardData>();

	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(code, this);
		}
	}
	@Override
	public void clearData() {
	}
	@Override
	public void clearStaticData() {
	}
	@Override
	public PropertyReader getData(int id) {
		return null;
	}
	public static LivenessAwardData getLivenessAwardData(int fatherType, int subType){
		if(fatherType==0){
			for(LivenessAwardData livenessAwardData:data.values()){
				if(livenessAwardData.type==fatherType){
					if(livenessAwardData.continueDays==subType){
						return livenessAwardData;
					}
				}
			}
		} else if(fatherType==1){
			for(LivenessAwardData livenessAwardData:data.values()){
				if(livenessAwardData.type==fatherType){
					if(livenessAwardData.liveness==subType){
						return livenessAwardData;
					}
				}
			}
		}
		return null;
	} 
	/**
	 * 获取奖励的物品
	 * @param award
	 * @return
	 */
	public int[][] getAwardItem(String award){
		String[] awardItems = award.split("#");
		if(awardItems==null || awardItems.length==0){
			return null;
		}
		int[][] result = new int[awardItems.length][2];
		for(int i=0;i<awardItems.length;i++){
			String[] items = awardItems[i].split(":");
			int[] item = new int[2];
			item[0] = Integer.parseInt(items[0]);
			item[1] = Integer.parseInt(items[1]);
			result[i] = item;
		}
		return result;
	}
}
