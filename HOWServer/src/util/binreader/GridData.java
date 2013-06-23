package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import server.cmds.ChatCP;
import util.MathUtils;

import db.model.Player;

/**
 * 地图数据
 * 
 * @author lzg------2011-6-1
 */
public class GridData implements PropertyReader {
	private static Logger logger=Logger.getLogger(GridData.class);
	
	private static final int mainSceneID = 1001;// 主场景地图ID
	
	public int id;// 索引
	public String name;// 格子名称
	public int type;
	public int area_id;
	public int gold;
	public int exp;
	public int event;//产生事件类型 1.出现卡牌 2.出现随机boss 3.出现宝箱
	public String value;
	public String desc;
//	public int maptype;// 地图类型
//	public int enable;// 是否开放
//	public int type;//类型
//	public int min_level;// 赛道进入等级
//	public int level;// 赛道等级
//	public int max_level;//赛道最高等级
//	public int perday_max_times;//每日最多进入次数
//	public int player_number;// 0为不限制人数,为1是表示单人模式。其他为多人，最高5
//	public int map_cd;//地图冷却时间
//	public String npcs;// 赛道拥有的NPC名称。比赛中有可能出现NPC对你车轮战术。如果就一个，则比赛中就只出现一个NPC。如果有多个，则按照NPC出现间隔间隔出现。比赛结束，NPC将不再出现。格式：{npcID1&npcID2}。需要NPC表支持
//	public int npc_interval;// 拥有多个NPC时，每个NPC出现的时间。0为同时出现，-1为前一个NPC被杀死才出现。
//	public String image;// 赛道对应的循环播放的美术资源名称。
//	public String loading;// 比扫LOADING图素材名称
//	public String loadingDesc;
//	public String loading_text;// LOADING的时候对赛道的一种描述，可理解为剧情简介。
//	private String drop_id;// 掉落ID
//	public String description;//赛道的描述
	
	private static HashMap<Integer, GridData> data = new HashMap<Integer, GridData>();
    
	public static Map<Integer,List<Integer>> areaDatas=new HashMap<Integer,List<Integer>>();
//	private List<Integer> npcList = new ArrayList<Integer>();

	
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(Integer.valueOf(id), this);
		}
//		String npcsstr[] = npcs.split("&");
//		for (String npc : npcsstr) {
//			int npcID = Integer.parseInt(npc);
//			npcList.add(npcID);
//		}
		if(areaDatas.get(area_id)==null){
			List<Integer> list=new ArrayList<Integer>();
			list.add(id);
			areaDatas.put(area_id,list);
		}else{
			areaDatas.get(area_id).add(id);
		}
	}
	@Override
	public void clearData() {
//		npcList.clear();
	}
	@Override
	public void clearStaticData() {
		
	}
    
	public static List<Integer> getGridDataForAreaID(int areaID){
		if(areaDatas.get(areaID)==null){
			return new ArrayList<Integer>();
		}else{
			return areaDatas.get(areaID);
		}
	}
	/**
	 * 给外部类的接口
	 * 
	 * @param id
	 * @return
	 */
	public static GridData getGridData(int id) {
		return data.get(Integer.valueOf(id));
	}

	public static GridData getMainSceneData() {
		return data.get(mainSceneID);
	}

	/**
	 * @author lzg
	 * @param mapName
	 * @return
	 */
	public int getMapIndex(String mapName) {
		Set<Integer> s = data.keySet();
		int mapIndex = -1;
		for (Iterator<Integer> it = s.iterator(); it.hasNext();) {
			mapIndex = (Integer) it.next();
			GridData map = getGridData(mapIndex);
			if (map.name.equals(mapName))
				break;
		}
		return mapIndex;
	}

	public void init() {
		data.clear();
	}

//	public List<Integer> getNpcList() {
//		return npcList;
//	}

	public static HashMap<Integer, GridData> getData() {
		return data;
	}

	@Override
	public PropertyReader getData(int mapIndex) {
		return data.get(Integer.valueOf(mapIndex));
	}
    
	/**
	 * @author liuzg
	 * @return
	 * 地图可能掉落的信息
	 * 
	 */
//	public List<int[]> getPossibleDropInfo(){
//		List<int[]> dropList = new ArrayList<int[]>();
//		//int[掉落类型,类型ID]
//		for (String str : drop_id.split("&")) {
//			int dropID = Integer.parseInt(str);
//			DropData drop = DropData.getDropData(dropID);
//			if(drop!=null){
//				int[] type=drop.getType();
//				int[] typeID=drop.getType_id();
//				if(type.length!=typeID.length){
//					continue;
//				}
//				for(int index=0;index<type.length;index++){
//					int[] info=new int[2];
//					info[0]=type[index];
//					info[1]=typeID[index];
//					dropList.add(info);
//				}
//			}
//		}
//		return dropList;
//	}
	/**
	 * @author liuzg
	 * @return int[] 掉落类型 掉落ID 掉落数量
	 */
//	public List<int[]> getDropInfo(boolean isFirst,int addTimes) {
//		// int[] 掉落类型 掉落ID 掉落数量
//		List<int[]> dropList = new ArrayList<int[]>();
//		//常规比赛掉落
//			for (String str : drop_id.split("&")) {
//				int dropID = Integer.parseInt(str);
//				DropData drop = DropData.getDropData(dropID);
//				if (drop != null) {
//					int[][] item = drop.getDropInfo(addTimes);
//					for (int index = 0; index < item.length; index++) {
//						dropList.add(item[index]);
//					}
//
//				}
//			}
//		return dropList;
//	}
}
