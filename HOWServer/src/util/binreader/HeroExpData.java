package util.binreader;

import java.util.HashMap;
import java.util.Map;

public class HeroExpData implements PropertyReader {
   
	public int id;//等级 
	public int exp;//经验
	
	private static Map<Integer,HeroExpData> datas=new HashMap<Integer,HeroExpData>();
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}

	}
    public static HeroExpData getHeroExpData(int id){
    	return datas.get(id);
    }
	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return datas.get(id);
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
