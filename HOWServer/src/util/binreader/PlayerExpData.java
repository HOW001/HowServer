package util.binreader;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author liuzhigang
 * 君主经验关系表
 */
public class PlayerExpData implements PropertyReader {
    public int id;//等级
    public int exp;//经验
    public int leadership;//领导力
    
    private static Map<Integer,PlayerExpData> datas=new HashMap<Integer,PlayerExpData>();
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}

	}
    public static PlayerExpData getPlayerExpData(int id){
    	return datas.get(id);
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

}
