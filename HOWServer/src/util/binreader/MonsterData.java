package util.binreader;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author liuzhigang
 * 怪物类
 */
public class MonsterData implements PropertyReader {

	public int id;
	public String name;
	public String image;
	public int grade;
	public int type;
	public int level;
	public int power;
	public int agile;
	public int mp;
	public int attack;
	public int atc_pram;	
	public int hp;
	public int skill_id;
	
	public static Map<Integer,MonsterData> datas=new HashMap<Integer,MonsterData>();
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}

	}
    public static MonsterData getMonsterData(int id){
    	return datas.get(id);
    }
	@Override
	public PropertyReader getData(int id) {
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
