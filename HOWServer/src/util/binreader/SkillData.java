package util.binreader;

import java.util.HashMap;

/**
 * @author lzg
       活物技能数据
 */
public class SkillData implements PropertyReader {
	
	//技能效果
	public final static int SKILL_SPECIAL_NONE=0;//无
	public final static int SKILL_SPECIAL_EXTRA_AP=1;//额外伤害
	public final static int SKILL_SPECIAL_ADD_HIT=2;//降低攻击被闪避几率,增加命中
	public final static int SKILL_SPECIAL_REDUCE_AP=3;//格挡伤害（降低伤害）
	public final static int SKILL_SPECIAL_ADD_POWER=4;//增加所有英雄力量属性
	public final static int SKILL_SPECIAL_REDUCE_HP=5;//血量上限一定百分比的伤害
	
	
	public final static int ATTACKBUFFER=1;//攻击类buffer
	public final static int ASSISTBUFFER=2;//辅助类buffer
	
	public final static int TARGET_SELF=0;//自己
	public final static int TARGET_SINGLE_ARM=1;//目标敌人
	public final static int TARGET_ALL_FRIEND=2;//全体友军
    
	public int id;
	public String name;
	public String desc;
	public int grade;
	public int type;
	public int fix_value;
	public int skill_value;
	public int touch;
	public int touch_rate;
	public int target_type;
	public int max_times;
	public int buff_id;
	public String skill_file;
	public String skill_index;
//	  public int id;
//	  public String name;
//	  public int type;//1为攻击类、2为辅助类
//	  public int belong;//技能归属 1:人物技能  2:NPC技能
//    public int giveType;//技能的获得方式分为：0无消耗获得、1消耗银币
//    public int needPayGold;//填写相关的比赛、任务或商城商品id
//    public int consumeType;//填写释放技能时所需的消耗：0为无消耗、1-9对应技能能量表相应id
//    public int consumeValue;//如释放技能有所消耗，则再此读取消耗的个数
//    public int targetType;//用以配合技能类型最终确定技能的起效对象（0为全体、1为单人）
//    public double mountPara;//技能的伤害加成系数技能伤害 =（己方总攻击力 * 伤害转化系数 ） * （0.9~1.1）+ 技能附加固定伤害
//    public double mountFix;//减少的具体的当前防护值
//    public int buffID;//索引到对应的buff表id，支持填写多项，分隔符隔开,0为无附加效果
//    
//    public int attackMusic;//释放技能时播放的声效，填写相关音频资源ID
//    public int targetMusic;//承受技能时播放的声效，填写相关音频资源ID
//    public int attackSpecial;//释放技能时播放的特效，填写相关美术资源ID
//    public int targetSpecial;//承受技能时播放的特效，填写相关美术资源ID
//    public int image;//技能图标，填写相关美术资源ID
//    public String desc;//显示用的技能描述，此处填写相关描述信息表的ID
   
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(Integer.valueOf(id), this);
		}
		
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
	public static SkillData getSkill(int id) {
		return data.get(id);
	}
	protected static HashMap<Integer, SkillData> data = new HashMap<Integer, SkillData>();

//	public int getType() {
//		return type;
//	}
//	public int getConsumeType() {
//		return consumeType;
//	}
//	public int getConsumeValue() {
//		return consumeValue;
//	}
	/**
	 * @author liuzhigang
	 * @return
	 * 技能效果
	 */
    public int getSkillSpecialType(){
    	if(type==SKILL_SPECIAL_EXTRA_AP||type==SKILL_SPECIAL_ADD_HIT|| type==SKILL_SPECIAL_REDUCE_HP){
    		return ATTACKBUFFER;//攻击类技能
    	}else{
    		return ASSISTBUFFER;//增益类技能
    	}
    }
	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return data.get(id);
	}
	
}
