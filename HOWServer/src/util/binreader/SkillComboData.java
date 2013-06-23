package util.binreader;

import java.util.HashMap;
import java.util.Map;

public class SkillComboData implements PropertyReader {

	public int id;
	public int type;
    public String icon;
    public double effect;
    public int att_combo;
    public int count_combo;	
    public int []heros;
    
    private static Map<Integer,SkillComboData> datas=new HashMap<Integer,SkillComboData>();
	@Override
	public void addData(boolean isReLoad) {
       if(isReLoad==false){
    	   datas.put(id, this);
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

}
