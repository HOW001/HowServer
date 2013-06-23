package util.binreader;

import java.util.HashMap;
import java.util.Map;

public class LivenessListData implements PropertyReader {
	public int id;
	public String name;
	public int type;
	public int level;
	public int needPlayerLevel;
	public int needTimes;
	public int isGiveUp;
	public int award;
	public String desc;
	private static Map<Integer, LivenessListData> data = new HashMap<Integer, LivenessListData>();
	public static Map<Integer, LivenessListData> getData() {
		return data;
	}
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(id, this);
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
	public static LivenessListData getLivenessListDataById(int code){
		return data.get(code);
	}
	public static int getLivenessListCodeBytype(int type){
		for(LivenessListData entry:data.values()){
			if(entry.type==type){
				return entry.id;
			}
		}
		return -1;
	}

}
