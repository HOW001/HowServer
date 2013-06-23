package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式验证
 * @author Administrator
 *
 */
public class DataValidate {
	
	/**
	 * 验证整数
	 * @param msg
	 * @return
	 */
	public static boolean validateNum(String msg) {
		Pattern p = Pattern.compile("^[0-9]+$");
		Matcher m = p.matcher(msg);
		return m.matches();
	}
	
	/**
	 * 验证浮点数
	 * @param msg
	 * @return
	 */
	public static boolean validateFload(String msg) {
		Pattern p = Pattern.compile("^(-?\\d+)(\\.\\d+)?$");
		Matcher m = p.matcher(msg);
		return m.matches();
	}
	 /**
	    * @author lzg 2010-6-24
	    * 字符串必须为字母\数字\汉字,可能有比较特殊的汉字无法注册
	   *  \\w单词字符 0-9 a-z A-Z
    * \\W非单词字符
    * \\d数字
    * \\D非数字
    * \\p{Lower}小写字母
    * \\p{Upper}大写字母
	 */
	public static boolean isLegalUserName(String source) {
		Pattern p=null;
		Matcher m=null;
		for(int index=0;index<source.length();index++){
			String str=source.substring(index,index+1);
			p = Pattern.compile("^[A-Za-z]+$");//字母
			m = p.matcher(str);
			if(m.find()){
				continue;
			}
			p = Pattern.compile("^[0-9]+$");//数字
			m = p.matcher(str);
			if(m.find()){
				continue;
			}
			p = Pattern.compile("[^\\x00-\\xff]");//汉字及汉字字符
			m = p.matcher(str);
			if(m.find()){
				continue;
			}
			return false;
		}
		return true;
//		Pattern p = Pattern.compile("[^\\w]");
//		Matcher m = p.matcher(source);
//		List<String> list = new ArrayList<String>();
//		while (m.find()) {
//			list.add(m.group());
//		}
//		if (list.size() == 0) {
//			return true;
//		}
//		int count = source.length() - list.size();
//		for (String temp : list) {
//			if (temp.getBytes().length != temp.length()) {
//				char c[] = temp.toCharArray();
//				if ((c[0] >= 19968 && c[0] <= 40868))// 汉字
//				{
//					count++;
//				}
//			}
//		}
//		if (count == source.length()) {
//			return true;
//		} else {
//			return false;
//		}
	}
}
