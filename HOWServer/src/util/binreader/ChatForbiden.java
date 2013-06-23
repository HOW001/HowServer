package util.binreader;
import java.util.ArrayList;
import java.util.List;
/**
 *@author lzg------2011-6-1
 *聊天时需要屏蔽的关键字
 */
public class ChatForbiden implements PropertyReader{
	public String forbidenwords;
	private static final String REPLACE_STR = "xxx";
	private static List<String> strs = new ArrayList<String>();
	
	public static String getCheckedString(String str) {
		for (String s : strs) {
			int index = str.indexOf(s);
			if (index == -1) {
				continue;
			} else {
				str = str.replace(s, REPLACE_STR);
			}
		}
		return str;
	}
	public void addData(boolean isReLoad) {
		if(strs.contains(forbidenwords)){
			return;
		}
		if(isReLoad==false){
			strs.add(forbidenwords);
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
