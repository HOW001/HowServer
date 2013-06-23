package util.binreader;

import java.io.*;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

import server.ServerConfigurationNew;
import server.ServerEntrance;
import util.ByteArray;

public class BinReader {
	private static Logger logger = Logger.getLogger(BinReader.class);
	public static String RES_DIR = "res/staticdatas";//默认资源所在目录
	public static final byte CHAR_VARS_START = ';';
	public static final byte CHAR_COMMENT_START = '/';
	
	/**
	 * 解析数据
	 * @param fileName
	 * @param clazz
	 */
	public void parseFile(String fileName,Class<? extends PropertyReader> clazz) {
		try{
			Object o = clazz.newInstance();
			if(o instanceof PropertyReader){
				//清空一次static集合数据
				PropertyReader entry=(PropertyReader)o;
				entry.clearStaticData();
			}
			FileInputStream fis = new FileInputStream(new File(fileName.toLowerCase()));
			DataInputStream dis = new DataInputStream(fis);
			byte[] b = new byte[fis.available()];
			dis.readFully(b);
			ByteArray ba = new ByteArray(b);
			int rows = ba.readInt();
			int cols = ba.readInt();
			Field[] fields = null;
			for(int i=0;i<rows;i++){
				String[] strs = new String[cols];
				for(int j=0;j<cols;j++){
					strs[j] = ba.readUTF().trim();
				}
				if(strs[0].length() == 0){
					continue;
				}
				if(strs[0].charAt(0) == CHAR_COMMENT_START){
					continue;
				} else if(strs[0].charAt(0) == CHAR_VARS_START){
					fields = getFields(clazz,strs);
				} else {
					parseLine(strs,fields,clazz);
				}
			}
			fis.close();
			dis.close();
		} catch(Exception e){
			logger.error("读取文件：" + fileName + "出错",e);
			System.exit(1);
		}
	}
	
	/**
	 * 解析一行
	 * @param value
	 * @param fields
	 * @param clazz
	 * @throws Exception
	 */
	protected void parseLine(String[] value,Field[] fields,Class<? extends PropertyReader> clazz) throws Exception{		
		Object o = clazz.newInstance();
		boolean isReLoad=false;
		/**
		 * 用于动态加载数据
		 */
		if(o instanceof PropertyReader){
			PropertyReader entry=(PropertyReader)o;
			if(entry.getData(decodeInt(value[0]))!=null){//如果集合中存在该数据映射，表明是重新加载，只需替换数据无须新建对象
				o=entry.getData(decodeInt(value[0]));
				isReLoad=true;
			}
		}
		for(int i=0;i<fields.length;i++){
			Field f = fields[i];
			if(f == null){
				continue;
			} 
			if(f.getType().equals(int.class)){
				f.setInt(o,decodeInt(value[i]));
			}else if(f.getType().equals(long.class)){
			   f.setLong(o, decodeLong(value[i]));	
			} else if(f.getType().equals(double.class)) {
				f.setDouble(o,decodeDouble(value[i]));
			} else if(f.getType().equals(int[].class)){
				f.set(o,parseIntArray(value,i));
			} else if(f.getType().equals(double[].class)){
				double[] r = new double[value.length-i];
				for(int j=0;j<r.length;j++){
					r[j] = decodeDouble(value[i+j]);
				}
				f.set(o,r);
			} else if(f.getType().equals(String[].class)){
				f.set(o,parseStringArray(value,i));
			} else{
				f.set(o,value[i]);
			}
		}
		if(isReLoad){
			((PropertyReader)o).clearData();
		}
		((PropertyReader)o).addData(isReLoad);
	}
	
	/**
	 * 将字符串数组从指定索引转化成字符串数组
	 * 字符串数组将不包含指定索引
	 * @param value
	 * @param startIndex
	 * @return
	 */
	protected String[] parseStringArray(String[] value,int startIndex){
		String[] r = new String[value.length-startIndex];
		System.arraycopy(value, startIndex, r, 0, r.length);
		
		int len = 0;
		for(int i=0;i<r.length;i++){
			if(r[i].length() > 0){
				len ++;
			}
		}
		if(len == r.length){
			return r;
		} else {
			String[] d = new String[len];
			for(int i=0,j=0;i<r.length;i++){
				if(r[i].length() > 0){
					d[j] = r[i];
					j++;
				}
			}
			return d;
		}
	}
	
	
	/**
	 * 将字符串数组从指定索引转化成int数组
	 * int数组将不包含指定索引
	 * @param value
	 * @param startIndex
	 * @return
	 */
	protected int[] parseIntArray(String[] value,int startIndex){
		int[] r = new int[value.length-startIndex];
		int realLen=0;
		for(int j=0;j<r.length;j++){
			if(value[startIndex+j].length()==0){
				continue;
			}
			realLen++;
			r[j] = decodeInt(value[startIndex+j]);
		}
		int[] real=new int[realLen];
		for(int index=0;index<real.length;index++){
			real[index]=r[index];
		}
		return real;
	}
	
	/**
	 * 将字符串转化成数字
	 * @param str
	 * @return
	 */
	protected double decodeDouble(String str){
		try{
			return Double.parseDouble(str);
		} catch(Exception e){
			return 0;
		}
	}
	/**
	 * @author liuzg
	 * @param str
	 * @return
	 * 将字符串转化为成Long
	 */
	protected long decodeLong(String str){
		try {
			return Long.decode(str);
		} catch (Exception e) {
			return 0;
		}
	}
	/**
	 * 将字符串转化成数字
	 * @param str
	 * @return
	 */
	protected int decodeInt(String str){
		try{
			return Integer.decode(str);
		} catch(Exception e){
			return 0;
		}
	}
	
	/**
	 * 根据变量名的字符串取得变量对象
	 * @param clazz
	 * @param strs
	 * @return
	 * @throws Exception
	 */
	protected Field[] getFields(Class<? extends PropertyReader> clazz,String[] strs) throws Exception{
		Field[] fields = new Field[strs.length];
		for(int i=0;i<strs.length;i++){
			String s = strs[i];
			if(s.length() > 0){
				if(s.charAt(0) == CHAR_VARS_START){
					s = s.substring(1,s.length());
				}
				fields[i] = clazz.getDeclaredField(s);
				fields[i].setAccessible(true);
			}
			
		}

		return fields;
	}
	
	/**
	 * 读所有文件
	 */
	public void readAllData(){
		try{
			if(ServerEntrance.serverPath!=null && ServerEntrance.serverPath.length()>0){
				RES_DIR = ServerEntrance.serverPath+ServerConfigurationNew.resDir;
			}else{
				RES_DIR = ServerConfigurationNew.resDir;
			}
		} catch(Exception e){
			RES_DIR = "res/staticdatas";
		}
		try {
			parseFile(RES_DIR+"/param_player_base.bin",ParamPlayerBaseData.class);
			parseFile(RES_DIR+"/game_parameter.bin",GameParameterData.class);//数值参数配置		
			parseFile(RES_DIR + "/hero.bin",HeroData.class);//英雄数据
			parseFile(RES_DIR+"/monster.bin",MonsterData.class);//怪物数据
			parseFile(RES_DIR+"/bannedname.bin",BannedList.class);//非法关键字
			parseFile(RES_DIR+"/forbidenchat.bin",ChatForbiden.class);//聊天屏蔽字
			parseFile(RES_DIR+"/skill.bin",SkillData.class);//技能数据
			parseFile(RES_DIR+"/skill_combo.bin",SkillComboData.class);//技能组合数据
			parseFile(RES_DIR+"/buff.bin",BuffData.class);
			parseFile(RES_DIR+"/items.bin",ItemData.class);//物品数据
			parseFile(RES_DIR+"/chapter_info.bin",ChapterInfoData.class);//赛道章节信息
			parseFile(RES_DIR+"/gate_info.bin",GateInfoData.class);//赛道关卡信息
			parseFile(RES_DIR+"/area_info.bin",AreaInfoData.class);//赛道区域信息
			parseFile(RES_DIR+"/grid_info.bin",GridData.class);//地图数据
			parseFile(RES_DIR+"/drop.bin",DropData.class);//掉落数据
			parseFile(RES_DIR+"/hero_exp.bin",HeroExpData.class);//英雄经验关系数据
			parseFile(RES_DIR+"/player_exp.bin",PlayerExpData.class);//君主经验关系数据
			parseFile(RES_DIR+"/legal_name.bin",LegalNameData.class);//随机名信息
			parseFile(RES_DIR+"/event.bin",EventData.class);//事件信息
			parseFile(RES_DIR+"/prompt.bin",PromptData.class);//提示信息信息
			parseFile(RES_DIR+"/liveness_award.bin",LivenessAwardData.class);//版本更新内容
			parseFile(RES_DIR+"/liveness_list.bin",LivenessListData.class);//开服活动
			/*初始化相关数据*/
			logger.info("完成一次静态数据加载.....");
		} catch (Exception e) {
			logger.error("加载静态数据时出现异常:",e);
			System.exit(1);
		}
	}
	public static void main(String[] args){
		BinReader reader = new BinReader();
		reader.readAllData();
	}
}