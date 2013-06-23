/**
 * 
 */
package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.MathUtils;

/**
 * @author liuzg
 *
 */
public class LegalNameData implements PropertyReader {

	private int id;
	private String family_name="aaa";
	private String male_name="bbb";
	private String female_name="ccc";
	
	private static Map<Integer,LegalNameData> datas=new HashMap<Integer,LegalNameData>();
	private static List<String> familyNameList=new ArrayList<String>();
	private static List<String> maleNameList=new ArrayList<String>();
	private static List<String> famaleNameList=new ArrayList<String>();
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){	
			datas.put(id, this);
		}
		familyNameList.add(this.family_name);
		maleNameList.add(this.male_name);
		famaleNameList.add(this.female_name);
	}
	
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		familyNameList.clear();
		maleNameList.clear();
		famaleNameList.clear();
	}
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return datas.get(id);
	}
	/**
	 * @author liuzg
	 * @return
	 * 获取随机角色名
	 */
   public static String getRandomName(int gender){
	   int ran=MathUtils.random(0, familyNameList.size());
	   String name=familyNameList.get(ran);
	   if(gender==1){
		   ran=MathUtils.random(0, maleNameList.size());
		   name+=maleNameList.get(ran);
	   }else{
		   ran=MathUtils.random(0, famaleNameList.size());
		   name+=famaleNameList.get(ran);
	   }
	  
	   return name;
   }
}
