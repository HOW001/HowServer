package util.binreader;

import java.util.HashMap;
import java.util.Map;
/**
 *提示数据 
 * @author fengmx
 */
public class PromptData implements PropertyReader {
	public int id;
	public String condition;
	public int type;
	public String msg;
	public String other;
	public int jumpType;
	public int jumpId;
	public String jumpInfo;
	private static Map<Integer, PromptData> data = new HashMap<Integer, PromptData>();
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
		return data.get(id);
	}
	public static PromptData getDataById(int id){
		return data.get(id);
//		PromptData promptData = data.get(id);
//		return promptData;
	}
}
