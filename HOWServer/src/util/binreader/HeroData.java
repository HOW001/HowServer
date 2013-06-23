package util.binreader;

import java.util.HashMap;
import org.apache.log4j.Logger;
/**
 * @author lzg------2011-6-1
 *NPC数据
 */
public class HeroData implements PropertyReader{
	public int id;
	public String name;
	public int max_level;
	public int color;
	public int type;
	public double power;
	public double power_param;
	public double agile;
	public double agile_param;
	public double mp;
	public double mp_param;
	public double attack;
	public double attack_param;
	public double hp;
	public double hp_param;
	public int leadership;
	public int toughness;
	public int skill_id;
	public String icon;

	private static Logger logger=Logger.getLogger(HeroData.class);
//	public List<Integer> skillList=new ArrayList<Integer>();
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(id,this);
		}
	}
	@Override
	public void clearData() {

	}
	@Override
	public void clearStaticData() {
		
	}
	public int getSkillID(){
		return skill_id;
	}
	
	public static HeroData getHeroData(int id){
		return data.get(Integer.valueOf(id));
	}
	private static HashMap<Integer,HeroData> data = new HashMap<Integer,HeroData>();

	public static HashMap<Integer,HeroData> getData(){
		return data;
	}
	public int getID() {
		return id;
	}
	@Override
	public HeroData getData(int id){
		return data.get(Integer.valueOf(id));
	}
}
