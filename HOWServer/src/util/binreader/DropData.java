/**
 * 掉落表
 */
package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.MathUtils;
import org.apache.log4j.Logger;

/**
 * @author liuzg
 * 
 */
public class DropData implements PropertyReader {

	public static int DROP_TYPE_GOD = 0;// 金钱掉落
	public static int DROP_TYPE_ITEM = 1;// 物品掉落
	public static int DROP_TYPE_SKILL = 2;// 技能掉落
	private int id;
	private int dropTimes;
	private int atts[];// 属性数组
	private int type[];
	private int type_id[];
	private int is_random[];
	private int min[];
	private int max[];
	private int rate[];

	private static Logger logger = Logger.getLogger(DropData.class);
	private static Map<Integer, DropData> datas = new HashMap<Integer, DropData>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}
		int baseLen = 6;
		if (atts.length % baseLen != 0) {
			logger.error("掉落表数据出现异常!:id=" + id);
			System.exit(1);
			return;
		}
		if (dropTimes < 1) {
			logger.error("掉落表的掉落次数最少为1:id=" + id);
			System.exit(1);
		}
		int len = atts.length / 6;
		type = new int[len];
		type_id = new int[len];
		is_random = new int[len];
		min = new int[len];
		max = new int[len];
		rate = new int[len];
		for (int index = 0; index < len; index++) {
			type[index] = atts[index * baseLen];
			type_id[index] = atts[index * baseLen + 1];
			is_random[index] = atts[index * baseLen + 2];
			min[index] = atts[index * baseLen + 3];
			max[index] = atts[index * baseLen + 4];
			rate[index] = atts[index * baseLen + 5];
		}
		int rateCount = 0;
		for (int value : rate) {
			rateCount += value;
		}
		if (rateCount != 10000) {
			logger.error("掉落表掉落概率异常:id=" + id);
			System.exit(1);
//			return;
		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
	/**
	 * @author liuzg
	 * @param index
	 * @return 获取掉落数量
	 */
	public int getDropNum(int index) {
		if (index < 0 || index >= is_random.length) {
			return 0;
		}
		if (is_random[index] < 1) {// 非随机物品
			return min[index];
		}
		int min_v = min[index];
		int max_v = max[index];
		int random = MathUtils.random(min_v, max_v);
		return random;
	}

	/**
	 * @author liuzg
	 * @param index
	 * @return 获取掉落类型ID，即物品ID
	 */
	public int getTypeID(int index) {
		if (index < 0 || index >= type_id.length) {
			return -1;
		}
		return type_id[index];
	}
	
	/**
	 * @author liuzg
	 * @param index
	 * @return 获取掉落类型ID，即物品ID
	 */
	public int[] getAllTypeID() {
		return type_id;
	}

	/**
	 * @author liuzg
	 * @param index
	 * @return 获取掉落类型
	 */
	public int getType(int index) {
		if (index < 0 || index >= type.length) {
			return -1;
		}
		return type[index];
	}

	/**
	 * @author liuzg
	 * @return 确定掉落序号,是每个掉落的第一步
	 */
	private int getRateIndex(int seed) {
		int random = new Random(seed+System.currentTimeMillis()).nextInt(10000)+1;
//		int random = MathUtils.random(1, 10000);
//		System.out.println("random:"+random);
		int value = 0;
		for (int index = 0; index < rate.length; index++) {
			value += rate[index];
			if (random <= value) {
//				System.out.println("index:"+index);
				return index;
			}
		}
		return -1;// 不会出现这种情况
	}

	public static DropData getDropData(int id) {
		return datas.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		return datas.get(id);
	}
    
	/**
	 * @author liuzg
	 * @return 掉落信息 {掉落次数{掉落类型 掉落ID 掉落数量}}
	 */
	public int[][] getDropInfo() {
		return getDropInfo(0);
	}
	/**
	 * @author liuzg
	 * @return 掉落信息 {掉落次数{掉落类型 掉落ID 掉落数量}}
	 * @param addTimes 额外增加的掉落次数
	 */
	public int[][] getDropInfo(int addTimes) {
		List<int[]> list = new ArrayList<int[]>();
		for (int times = 0; times < dropTimes+addTimes;) {
			int index = getRateIndex(times*3333);
			if (index < 0) {
				continue;
			}
			times++;
			int[] info = new int[3];
			info[0] = getType(index);
			info[1] = getTypeID(index);
			info[2] = getDropNum(index);
			list.add(info);
		}
		int[][] datas = new int[list.size()][3];
		for (int index = 0; index < list.size(); index++) {
			datas[index] = list.get(index);
		}
		return datas;
	}
	public int getDropTimes() {
		return dropTimes;
	}

	public int[] getType() {
		return type;
	}

	public int[] getType_id() {
		return type_id;
	}
}
