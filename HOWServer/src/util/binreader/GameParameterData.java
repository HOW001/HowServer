/**
 * 
 */
package util.binreader;

import java.util.HashMap;
import java.util.Map;

import common.Logger;

/**
 * @author liuzg
 * 游戏当中所使用的所有参数配置
 */
public class GameParameterData implements PropertyReader {
	private static Logger logger=Logger.getLogger(GameParameterData.class);
    public int id;
    public String name;//所有配置名称必须唯一
//    public int count;//参数个数,用来与参数值个数进行验证
    public double[] params;//参数值
    
    private Map<Integer,GameParameterData> datas=new HashMap<Integer,GameParameterData>();
    
    /*配置名称定义*/
    public static final String NAME_PLAYER="player_param";//角色参数
    public static final String NAME_CONTEST="contest_param";//战斗伤害参数
    public static final String NAME_HELPER="helper_param";//助手参数
    public static final String NAME_SHOP="shop_param";//商城参数
    public static final String DEFENSECAR_MAX_PARAM="defenseCar_max_param";//防护赛车最大值参数
    public static final String BALANCECAR_MAX_PARAM="balanceCar_max_param";//经典赛车最大值参数
    public static final String ATTACKCAR_MAX_PARAM="attackCar_max_param";//时尚赛车最大值参数
    public static final String PLAYER_MAX_PARAM="player_max_param";//角色属性最大值参数
    public static final String GROW_MAX_PARAM="grow_max_param";//成长最大值参数
    public static final String INIMATE_EXP_PARAM="intimate_exp_param";//成长最大值参数
    public static final String ARENA_PARAM="arena_param";//竞技场积分相关参数
    public static final String ACTIVITY_PARAM="activity_param";//日常活动相关参数
    public static final String Time_SPEED_PARAMTIME="time_speed_param";//时间加速相关参数
    public static final String AUCTION_ITEMNUM_PARAM="auction_itemNum_param";//时间加速相关参数
    public static final String FISH_PRICE_PARAM = "fish_price_param";//钓鱼活动相关
    public static final String GOLD_PARAM = "gold_param";//金钱相关
    public static final String PACK_PARAM = "pack_param";//背包相关
    public static final String RESET_ROAD_INFO="reset_road";//赛道重置相关
    public static final String FRIENG_CD="friend_cd";//好友推荐冷却时间
    public static final String STRENGTHEN="strengthen";//免费强化CD和每天免费强化最大次数
    
    public static final String ROAD_DIFFICULTY="map_difficulty";//赛道难度
    /*参数定义*/
    //player_param
    public static double maxMarkParam=0;//最大评分系数
    public static double baseTrainLevel=0;//训练等级初始加成
    public static double trainLevelAddition=0;//训练等级加成增量
    public static double playerLevelValue=0;//角色等级修正系数
    public static double playerExpValue=0;//角色经验修正系数
    public static double liveness_sign_level=0;//签到开放等级
    public static double liveness_list_level=0;//活跃度列表开放等级

    //player_param
    
    //contest_param
    public static double attackReturnParam=0;//战斗转化系数
    public static double baseEvadeRateParam=0;//基础躲闪率
    public static double evadeAdditionParam=0;//躲闪加成系统
    public static double attackCreatureTypeParam_NPC=0;//对手攻击类型NPC
    public static double attackCreatureTypeParam_PLAYER=0;//对手攻击类型Player
    public static double baseAttackIntervalParam=0;//初始攻击间隔
    public static double reactionReturnParam=0;//反应转化系数
    public static double roadLengthParam=0;//赛道长度系数
    public static double baseRoadAccidentParam_NPC=0;//赛道事故初始参数NPC
    public static double baseRoadAccidentParam_PLAYER=0;//赛道事故初始参数NPC
    public static double roadAccidentAdditionParam=0;//事故加成系数
    public static double difficultyReturnParam=0;//难度转换系数
    public static double roadTimeReturnParam=0;//赛道时间系数
    public static double minAttackIntervalParam=0;//最小攻击间隔
    public static double sheildAddtionParam=0;//防护加成系数
    public static double speedAddtionParam=0;//速度加成系数
    public static double handleUseAddtionParam=0;//操控加成系数
    public static double attackAddtionParam=0;//攻击加成系数
    public static double reactionAddtionParam=0;//反应加成系数
  //contest_param
    //helper_param
    public static double helperGrowAddition=0;//助手成长强化增量
    public static double helperResolve[]=new double[5];//助手亲密度
    public static double lowRate = 0;
    public static double highRate = 0;
    public static double helperTalkTime = 0;
    //helper_param
    //shop_item
    public static double shopRefreshTime = 0;
    public static double shopRefreshItemNumber = 0;
    public static double shopRefreshNeedGold = 0;
    public static double shopRefreshTimesPerDay = 0;
    //shop_item
    //defenseCar_max_param
    public static double defenseCarShield = 0;
    public static double defenseCarPower = 0;
    public static double defenseCarSpeed = 0;
    public static double defenseCarHandleUse = 0;
    public static double defenseCarAttack = 0;
    public static double defenseCarReaction = 0;
    //defenseCar_max_param
    //balanceCar_max_param
    public static double balanceCarShield = 0;
    public static double balanceCarPower = 0;
    public static double balanceCarSpeed = 0;
    public static double balanceCarHandleUse = 0;
    public static double balanceCarAttack = 0;
    public static double balanceCarReaction = 0;
    //balanceCar_max_param
    //attackCar_max_param
    public static double attackCarShield = 0;
    public static double attackCarPower = 0;
    public static double attackCarSpeed = 0;
    public static double attackCarHandleUse = 0;
    public static double attackCarAttack = 0;
    public static double attackCarReaction = 0;
    //attackCar_max_param
    //player_max_param
    public static double playerShield = 0;
    public static double playerPower = 0;
    public static double playerSpeed = 0;
    public static double playerHandleUse = 0;
    public static double playerAttack = 0;
    public static double playerReaction = 0;
    //player_max_param
    //grow_max_param
    public static double playerMark = 0;
    public static double helperMark = 0;
    public static double carMark = 0;
    public static double playerSelfMark = 0;
    //grow_max_param
    //intimacy_exp_param
    public static double exp1 = 0;
    public static double exp2 = 0;
    public static double exp3 = 0;
    public static double exp4 = 0;
    public static double exp5 = 0;
   //intimacy_exp_param
    //arena_param
    //单人
    public static double single_arena_win_basic=0;
    public static double single_arena_lose_basic=0;
    public static double single_arena_win_mark_param=0;
    public static double single_arena_lose_mark_param=0;
    //多人
    public static double multi_arena_win_basic=0;
    public static double multi_arena_lose_basic=0;
    public static double multi_arena_win_mark_param=0;
    public static double multi_arena_lose_mark_param=0;
    
    //日常活动相关
    public static double numerator;//经验指数系数（分子）
    public static double denominator;//经验分数系数（分母）
    public static double baseExp;//基础经验值
    //时间加速相关参数time_speed_param
    public static int goldPerHourForTrainSpeedUp;//训练加速每小时银币
    public static int goldPerHourForCircleActivitySpeedUp;//闯关加速每小时银币
    public static int autoFightCDTime=60;//自动战斗单个赛道冷却时间，单位秒
    //拍卖行刷新数量相关参数
    public static int minNum;//最小值
    public static int maxNum;//最大值   
    //钓鱼相关参数
    public static int baseBaits;//鱼饵基础
    public static int baitsRed;//鱼饵红钻加成
    public static int constellationBase;//星座基础
    public static int constellationVip1;//星座vip1加成
    public static int constellationVip2;//星座vip2加成
    public static int constellationVip3;//星座vip3加成
    public static int treasureHuntBase;//寻宝基础
    public static int treasureHuntRed;//寻宝红钻加成
    public static int crazyPickGoldBase;//淘宝基础
    public static int crazyPickGoldVip1;//淘宝vip1加成
    public static int crazyPickGoldVip2;//淘宝vip2加成
    public static int crazyPickGoldVip3;//淘宝vip3加成
    public static int fishingCoe;//钓鱼相关系数
    public static int oilCoe;//寻宝油量系数
    public static int bloodCoe;//寻宝血量系数
    public static int crazyGoldPickTimeCoe;//淘宝时间系数
    public static int crazyGoldBaseTime;//淘宝基础时间
    public static int crazyGoldTimeVip1;//vip1加成时间
    public static int crazyGlodTimeVip2;//vip2加成时间
    public static int crazyGlodTimeVip3;//vip3加成时间
    public static int crazyGlodCommonPrice;//淘宝普通价格
    public static int crazyGlodHighPrice;//淘宝高级价格
    public static int crazyGlodMaxTimes;//普通淘宝最大 次数
    //金钱相关参数
    public static int channel_broadcast;
    public static int zhouyou;
    //背包相关参数
    public static int extend_number;
    public static double pack_value1;
    public static double pack_value2;
    
    //赛道重置相关参数
    public static int hell_challenge_basic=40;//地狱基础值
    public static int hell_challenge_param=60;//地狱参数值
    public static int world_tour_basic=40;//世界基础值
    public static int world_tour_param=60;//世界参数值
    public static int reset_vip2_times=3;//vip2可重置的次数
    public static int reset_vip3_times=5;//vip3可重置的次数
    
    //好友推荐相关参数
    public static double friendCD;//好友推荐冷却时间
    
    //赛道难度相关系数 map_difficulty
    public static double road_hero_npc_addition=0;//精英NPC属性加成
    public static double road_hero_length_addition=0;//精英赛道长度加成
    public static double road_hero_level_addition=0;//精英赛道等级加成
    public static double raod_hero_exp_award_addition=0;//精英经验奖励加成
    public static int 	 road_hero_drop_times_addition=0;//精英掉落执行次数
    
    public static double road_limit_npc_addition=0;//极限NPC属性加成
    public static double road_limit_length_addition=0;//极限赛道长度加成
    public static double road_limit_level_addition=0;//极限赛道等级加成
    public static double raod_limit_exp_award_addition=0;//极限经验奖励加成
    public static int 	 road_limit_drop_times_addition=0;//极限掉落执行次数
    
    //免费强化冷却时间和每天免费强化最高次数   CD单位毫秒
    public static int strength_playerEquip_cd=0;//车手装备免费强化CD   
    public static int strength_playerEquip_times=0;//车手装备每天免费强化最大次数
    public static int strength_carEquip_cd=0;//赛车挂件免费强化CD
    public static int strength_carEquip_times=0;//赛车挂件每天免费强化最大次数
    public static int strength_carBarnEquip_cd=0;//车库装饰免费强化CD
    public static int strength_carBarnEquip_times=0;//车库装饰每天免费强化最大次数
    public static int strength_car_performance_cd=0;//赛车性能免费强化CD
    public static int strength_car_performance_times=0;//赛车性能每天免费强化最大次数
    public static int strength_car_appearance_cd=0;//赛车款型免费强化CD
    public static int strength_car_appearance_times=0;//赛车款型每天免费强化最大次数
    public static int strength_carBarn_cd=0;//车库免费强化CD
    public static int strength_carBarn_times=0;//车库每天免费强化最大次数
    public static int strength_helperGrowUp_cd=0;//助手成长免费强化CD
    public static int strength_helperGrowUp_times=0;//助手成长每天免费强化最大次数
    //赛道难度相关系数 map_difficulty
    //arena_param
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
        if(isReLoad==false){
        	datas.put(id,this);
        }
		try {
			if(name.equals(NAME_PLAYER)){//角色参数
				maxMarkParam=params[0];
				baseTrainLevel=params[1];
				trainLevelAddition=params[2];
				playerLevelValue=params[3];
				playerExpValue=params[4];
				liveness_sign_level=params[5];
				liveness_list_level=params[6];
				return;
			}
			if(name.equals(NAME_CONTEST)){
				attackReturnParam=params[0];
				baseEvadeRateParam=params[1];
				evadeAdditionParam=params[2];
				attackCreatureTypeParam_NPC=params[3];
				attackCreatureTypeParam_PLAYER=params[4];
				baseAttackIntervalParam=params[5];
				reactionReturnParam=params[6];
				roadLengthParam=params[7];
				baseRoadAccidentParam_NPC=params[8];
				baseRoadAccidentParam_PLAYER=params[9];
				roadAccidentAdditionParam=params[10];
				difficultyReturnParam=params[11];
				roadTimeReturnParam=params[12];
				minAttackIntervalParam=params[13];
				sheildAddtionParam=params[14];
				speedAddtionParam=params[15];
				handleUseAddtionParam=params[16];
				attackAddtionParam=params[17];
				reactionAddtionParam=params[18];
				return;
			}
			if(name.equals(NAME_HELPER)){
				helperGrowAddition=params[0];
				helperResolve[0]=params[1];
				helperResolve[1]=params[2];
				helperResolve[2]=params[3];
				helperResolve[3]=params[4];
				helperResolve[4]=params[5];
				lowRate=params[6];
				highRate=params[7];
				helperTalkTime=params[8];
				return;
			}
			if(name.equals(NAME_SHOP)){
				shopRefreshTime=params[0];
				shopRefreshItemNumber=params[1];
				shopRefreshNeedGold=params[2];
				shopRefreshTimesPerDay=params[3];
			}
			if(name.equals(DEFENSECAR_MAX_PARAM)){
				defenseCarShield=params[0];
				defenseCarPower=params[1];
				defenseCarSpeed=params[2];
				defenseCarHandleUse=params[3];
				defenseCarAttack=params[4];
				defenseCarReaction=params[5];
			}
			if(name.equals(BALANCECAR_MAX_PARAM)){
				balanceCarShield=params[0];
				balanceCarPower=params[1];
				balanceCarSpeed=params[2];
				balanceCarHandleUse=params[3];
				balanceCarAttack=params[4];
				balanceCarReaction=params[5];
			}
			if(name.equals(ATTACKCAR_MAX_PARAM)){
				attackCarShield=params[0];
				attackCarPower=params[1];
				attackCarSpeed=params[2];
				attackCarHandleUse=params[3];
				attackCarAttack=params[4];
				attackCarReaction=params[5];
			}
			if(name.equals(PLAYER_MAX_PARAM)){
				playerShield=params[0];
				playerPower=params[1];
				playerSpeed=params[2];
				playerHandleUse=params[3];
				playerAttack=params[4];
				playerReaction=params[5];
			}
			if(name.equals(GROW_MAX_PARAM)){
				playerMark=params[0];
				helperMark=params[1];
				carMark=params[2];
				playerSelfMark=params[3];
			}
			if(name.equals(INIMATE_EXP_PARAM)){
				exp1=params[0];
				exp2=params[1];
				exp3=params[2];
				exp4=params[3];
				exp5=params[4];
			}
			if(name.equals(ARENA_PARAM)){
				single_arena_win_basic=params[0];
				single_arena_lose_basic=params[1];
				single_arena_win_mark_param=params[2];
				single_arena_lose_mark_param=params[3];
				multi_arena_win_basic=params[4];
				multi_arena_lose_basic=params[5];
				multi_arena_win_mark_param=params[6];
				multi_arena_lose_mark_param=params[7];
			}
			if(name.equals(ACTIVITY_PARAM)){
				numerator = params[0];
				denominator = params[1];
				baseExp = params[2];
			}
			if(name.equals(Time_SPEED_PARAMTIME)){
				goldPerHourForTrainSpeedUp = (int) params[0];
				goldPerHourForCircleActivitySpeedUp = (int) params[1];
				autoFightCDTime=(int)params[2];
			}
			if(name.equals(AUCTION_ITEMNUM_PARAM)){
				minNum = (int) params[0];
				maxNum = (int) params[1];
			}
			if(name.equals(FISH_PRICE_PARAM)){
				baseBaits = (int)params[0];
				baitsRed = (int)params[1];
				constellationBase = (int)params[2];
				constellationVip1 = (int)params[3];
				constellationVip2 = (int)params[4];
				constellationVip3 = (int)params[5];
				treasureHuntBase = (int)params[6];
				treasureHuntRed = (int)params[7];
				crazyPickGoldBase = (int)params[8];
				crazyPickGoldVip1 = (int)params[9];
				crazyPickGoldVip2 = (int)params[10];
				crazyPickGoldVip3 = (int)params[11];
				fishingCoe = (int)params[12];
				oilCoe = (int)params[13];
				bloodCoe = (int)params[14];
				crazyGoldPickTimeCoe = (int)params[15];
				crazyGoldBaseTime = (int)params[16];
				crazyGoldTimeVip1 = (int)params[17];
				crazyGlodTimeVip2 = (int)params[18];
				crazyGlodTimeVip3 = (int)params[19];
				crazyGlodCommonPrice = (int)params[20];
				crazyGlodHighPrice = (int)params[21];
				crazyGlodMaxTimes = (int)params[22];
			}
			if(name.equals(GOLD_PARAM)){
				channel_broadcast = (int)params[0];
				zhouyou = (int)params[1];
			}
			if(name.equals(PACK_PARAM)){
				extend_number = (int)params[0];
				pack_value1 = (int)params[1];
				pack_value2 = (int)params[2];
			}
			if (name.equals(RESET_ROAD_INFO)) {
				hell_challenge_basic = (int) params[0];// 地狱基础值
				hell_challenge_param = (int) params[1];// 地狱参数值
				world_tour_basic = (int) params[2];// 世界基础值
				world_tour_param = (int) params[3];// 世界参数值
			}
			if (name.equals(FRIENG_CD)) {
				friendCD = params[0];// 冷却时间
			}
			if(name.equals(ROAD_DIFFICULTY)){
				road_hero_npc_addition=params[0];//精英NPC属性加成
			    road_hero_length_addition=params[1];//精英赛道长度加成
			    road_hero_level_addition=params[2];//精英赛道等级加成
			    raod_hero_exp_award_addition=params[3];//精英经验奖励加成
			    road_hero_drop_times_addition=(int)params[4];//精英掉落执行次数
			    road_limit_npc_addition=params[5];//极限NPC属性加成
			    road_limit_length_addition=params[6];//极限赛道长度加成
			    road_limit_level_addition=params[7];//极限赛道等级加成
			    raod_limit_exp_award_addition=params[8];//极限经验奖励加成
			    road_limit_drop_times_addition=(int)params[9];//极限掉落执行次数
			}
			if(name.equals(STRENGTHEN)){
				strength_playerEquip_cd=(int)params[0];
				strength_playerEquip_times=(int)params[1];
				strength_carEquip_cd=(int)params[2];
				strength_carEquip_times=(int)params[3];
				strength_carBarnEquip_cd=(int)params[4];
				strength_carBarnEquip_times=(int)params[5];
				strength_car_performance_cd=(int)params[6];
				strength_car_performance_times=(int)params[7];
				strength_car_appearance_cd=(int)params[8];
				strength_car_appearance_times=(int)params[9];
				strength_carBarn_cd=(int)params[10];
				strength_carBarn_times=(int)params[11];
				strength_helperGrowUp_cd=(int)params[12];
				strength_helperGrowUp_times=(int)params[13];
			}
		} catch (Exception e) {
			logger.error("加载游戏相关配置参数时出现异常:",e);
			System.exit(1);
		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		return datas.get(id);
	}

}
