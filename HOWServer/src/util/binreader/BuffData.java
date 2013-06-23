/**
 * 
 */
package util.binreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzg buff数据
 */
public class BuffData implements PropertyReader {
	
	public final static int ATTR_TYPE_NONE=0;// 无
	public final static int ATTR_TYPE_REDUCE_SHIELD=1;// 降低防御
	public final static int ATTR_TYPE_DNF=2;// 暴击伤害（服务器特殊处理）
	public final static int ATTR_TYPE_FIRE=3;// 燃烧伤害
	public final static int ATTR_TYPE_DIZZ=4;//眩晕
	public final static int ATTR_TYPE_MISS=5;// 免伤
	public final static int ATTR_TYPE_REDUCE_HP=6;// 流血
	public final static int ATTR_TYPE_FROST=7;// 寒霜
	public final static int ATTR_TYPE_ICE=8;// 冰冻
	public final static int ATTR_TYPE_PLAGUE=9;// 瘟疫
	public final static int ATTR_TYPE_SLEEP=10;//睡眠
	public final static int ATTR_TYPE_REDUCE_POWER=11;//降低力量属性
	public final static int ATTR_TYPE_ADD_HP=12;//增加一定百分比血量
	public final static int ATTR_TYPE_ADD_POWER=13;//增加一定数值力量
	
	
//	public final static int ATTR_TYPE_SHIELD = 1;// 影响防护
//	public final static int ATTR_TYPE_POWER=2;//影响动力
//	public final static int ATTR_TYPE_SPEED = 3;// 影响速度
//	public final static int ATTR_TYPE_HANDLEUSE = 4;// 影响操控
//	public final static int ATTR_TYPE_ATTACK=5;//影响攻击
//	public final static int ATTR_TYPE_REACTION=6;//影响反应
//	public final static int ATTR_TYPE_BEATTACK = 7;// 影响抵挡
	
//	public final static int ATTR_ROUNDS_TIME_LEN=1;//指定时间内有效
//	public final static int ATTR_ROUNDS_CONTEST = 2;// 当前比赛有效
//	public final static int ATTR_ROUNDS_TIMES = 3;// 指定次数有效

	public int id;
	public String name;
	public int level;
	public int target;
	public int touch_rate;
	public int type;
	public int special;
	public int times;
	public String source;
	
//	public int id;
//	public String name;
//	public int targetAttrType;// 玩家承受技能后会发生变化的属性类型：1:防护2:动力3：速度4：操控5：攻击力6：反应力7：抵挡
//	public int targetAttrValue;// 改变相关属性值
//	public double targetAttrPara;//用于参与最终变更量计算公式的系数
//	public int targetAttrRounds;// 用以确定技能对玩家属性改变的生效时间（1当前比赛、2指定回合数）
//	public int targetRoundsOut;// 技能生效类型为指定回合数
//	public int attackMusic;// 释放技能时播放的声效，填写相关音频资源ID
//	public int targetMusic;// 承受技能时播放的声效，填写相关音频资源ID
//	public int attackSpecial;// 释放技能时播放的特效，填写相关美术资源ID
//	public int targetSpecial;// 承受技能时播放的特效，填写相关美术资源ID
//	public int image;// 技能图标，填写相关美术资源ID
//	public String replaceBuff;
//	public int[] replace;
	private static Map<Integer, BuffData> datas = new HashMap<Integer, BuffData>();

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
//		String rep[] = replaceBuff.split("&");
//		replace = new int[rep.length];
//		for (int index = 0; index < rep.length; index++) {
//			int skillID = Integer.parseInt(rep[index]);
//			replace[index] = skillID;
//		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
	public  BuffData getData(int id) {
		return datas.get(id);
	}
	
	public static BuffData getBuffData(int id) {
		return datas.get(id);
	}
}
