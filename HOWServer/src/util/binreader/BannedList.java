package util.binreader;
import java.util.*;
/**
 * 
 * @author lzg
 * 非法注册用户关键字列表
 */
public class BannedList implements PropertyReader{
	public String bannedName;
	private static List<String> bannedNames = new ArrayList<String>();
	
	/**
	 * 名字是否合法
	 * 返回true说明合法
	 * 返回false说明不合法
	 * @param name
	 * @return 
	 */
	public static boolean legal(String name){
		if(name == null) {
			return false;
		}
		for(String str : bannedNames){
			if(str.equalsIgnoreCase(name)){
				return false;
			}
			if(name.contains(str.toUpperCase())){
				return false;
			}
		}
		return true;
	}

	@Override
	public void addData(boolean isReLoad) {
		if(bannedNames.contains(bannedName)){
			return;
		}
		if(isReLoad==false){
			bannedNames.add(bannedName);
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
		// TODO Auto-generated method stub
		return null;
	}

}
