package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaInfoData implements PropertyReader {

	public int id;
	public String name;
	public int gate_id;
	public int pri_area_id;//上一个区域的id
	public String desc;

	private static Map<Integer, AreaInfoData> datas = new HashMap<Integer, AreaInfoData>();

	// 关卡信息对应的区域列表
	public static Map<Integer, List<Integer>> gateDatas = new HashMap<Integer, List<Integer>>();

	@Override
	public void addData(boolean isReLoad) {
		if (isReLoad == false) {
			datas.put(id, this);
		}
		List<Integer> temp = gateDatas.get(gate_id);
		if (temp == null) {
			temp = new ArrayList<Integer>();
		}
		if (temp.contains(id) == false) {
			temp.add(id);
			gateDatas.put(gate_id, temp);
		}

	}

	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearStaticData() {
		// TODO Auto-generated method stub

	}
	public static List<Integer> getAreaInfoDataForGateID(int gateID){
		if(gateDatas.get(gateID)==null){
			return new ArrayList<Integer>();
		}else{
			return gateDatas.get(gateID);
		}
	}
    public static AreaInfoData getAreaInfoData(int id){
    	return datas.get(id);
    }
}
