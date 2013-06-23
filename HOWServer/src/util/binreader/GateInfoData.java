/**
 * 赛道关卡信息
 */
package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author liuzg
 * 
 */
public class GateInfoData implements PropertyReader {
	public int id;
	public String name;
	public int chapter_id;
	public int need_item;
	public String desc;
	public int complete;//完成度
	
	public String condition;
	private static Map<Integer, GateInfoData> datas = new HashMap<Integer, GateInfoData>();

	// 章节信息对应的关卡列表
	private static Map<Integer, List<Integer>> gateList = new HashMap<Integer, List<Integer>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}
		List<Integer> temp = gateList.get(chapter_id);
		if (temp == null) {
			temp = new ArrayList<Integer>();
		}
		if (temp.contains(id) == false) {
			temp.add(id);
			gateList.put(chapter_id, temp);
		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		gateList.clear();
	}
	public static GateInfoData getRoadGateInfoData(int gateID) {
		return datas.get(gateID);
	}

	public static List<Integer> getGateList(int chapter_ID) {
		if (gateList.get(chapter_ID) == null) {
			return new ArrayList<Integer>();
		}
		return gateList.get(chapter_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		return datas.get(id);
	}
}
