package util.logger;

import org.apache.log4j.Logger;

import db.model.Player;
/**
 * 金钱日志
 * @author fengmx
 */
public class MoneyLogger {
	private static Logger logger = Logger.getLogger(MoneyLogger.class);
	private static final char DIV = '|';
//	private static final char NULL = ' ';
	private static final String GAME = "rekoocar";
	private static final String OPERATE_ADDMONEY = "添加金钱";//
	private static final String OPERATE_DEDUCTMONEY = "扣除金钱";//
	/**
	 * 金钱的来源
	 */
	public static int[] moneyAdd=
	{0,//出售物品获得 
	1,//使用礼包
	2,//使用成长礼包
	3,//拍卖返回
	4,//星座活动获得
	5,//疯狂淘宝活动获得
	6,//GM（系统）赠送
	7,//抽奖获得
	8,//领取开服活动奖励
	9,//更服获取赔偿
	10,//VIP翻牌
	11,//参与签到活动获得
	12,//每日奖励活动获得
	13,//赛道掉落奖励
	14,//任务奖励
	15,//战斗小游戏获得
	16,//购买银袋
	17,//封测奖励
	18,//pk获得
	19//邮件获得
	};
//	public static int[] moneyAdd=
//	{0,//购买银袋
//	1,//出售物品获得
//	2,//任务奖励
//	3,//活动奖励
//	4,//赛道掉落奖励
//	5,//GM（系统）赠送
//	6//拍卖行返还
//	};
	/**
	 * 金钱扣除
	 */
	public static int[] moneyDeduct=
	{0,//购买技能能量
	1,//雇佣助手
	2,//与助手交谈
	3,//扩充背包
	4,//商城刷新
	5,//商城购买物品
	6,//学习技能
	7,//训练扩充时间
	8,//训练加速
	9,//活动立即完成
	10,//停止探险活动
	11,//定点投掷
	12,//拍卖
	13,//世界消息和广播需要消费的钱币
	14,//疯狂淘宝
	15,//沙漠寻宝
	16,//购买鱼饵
	17,//赠送鲜花
	18,//扑捉助手
	19,//助手交谈加速
	20,//赛道解锁
	21//PK广场挑战
	};
	
	private static void info(String s){
		logger.info(s);
	}
	/**
	 * 添加金钱
	 * @param player
	 * @param moneyAdd
	 * @param isBind 获得的银币是否绑定  true绑定 false不绑定
	 * @param gold
	 */
	public static void addMoneyLog(Player player,int moneyAdd,boolean isBind,int gold){
		if(gold<=0){
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(OPERATE_ADDMONEY).append(DIV);//操作
		sb.append("车手名：").append(player.getName()).append(DIV);
		sb.append("金钱来源：").append("[").append(getMoneyFromForMoneyAdd(moneyAdd)).append("]").append(DIV);
		String s = "";
		if(isBind){
			s="绑定";
		}else{
			s="不绑定";
		}
		sb.append("[").append(s).append("]").append(DIV);
		sb.append("数额：").append(gold);
		info(sb.toString());
	}
	/**
	 * 扣除金钱
	 * @param player
	 * @param gold
	 */
	public static void deductMoneyLog(Player player,int moneyDeduct,String result){
//		if(gold<=0){
//			return;
//		}
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(OPERATE_DEDUCTMONEY).append(DIV);//操作
		sb.append("车手名：").append(player.getName()).append(DIV);
		sb.append("扣除方式：").append("[").append(getMoneyFromForMoneyDeduct(moneyDeduct)).append("]").append(DIV);
		sb.append(result);
//		sb.append(gold);
		info(sb.toString());
	}
	private static String getMoneyFromForMoneyAdd(int type){
		String msg = "rekoo_add";
		switch (type) {
		case 0:
			msg = "出售物品获得";
			break;
		case 1:
			msg = "使用礼包";
			break;
		case 2:
			msg = "使用成长礼包";
			break;
		case 3:
			msg = "拍卖返回";
			break;
		case 4:
			msg = "星座活动获得";
			break;
		case 5:
			msg = "疯狂淘宝活动获得";
			break;
		case 6:
			msg = "GM（系统）赠送";
			break;
		case 7:
			msg = "抽奖获得";
			break;
		case 8:
			msg = "领取开服活动奖励";
			break;
		case 9:
			msg = "更服获取赔偿";
			break;
		case 10:
			msg = "VIP翻牌";
			break;
		case 11:
			msg = "参与签到活动获得";
			break;
		case 12:
			msg = "每日奖励活动获得";
			break;
		case 13:
			msg = "赛道掉落奖励";
			break;
		case 14:
			msg = "任务奖励";
			break;
		case 15:
			msg = "战斗小游戏获得";
			break;
		case 16:
			msg = "购买银袋";
			break;
		case 17:
			msg = "封测奖励";
			break;
		case 18:
			msg = "pk获得";
			break;
		default:
			break;
		}
		return msg;
	}
	private static String getMoneyFromForMoneyDeduct(int type){
		String msg = "rekoo_delete";
		switch (type) {
		case 0:
			msg = "购买技能能量";
			break;
		case 1:
			msg = "雇佣助手";
			break;
		case 2:
			msg = "与助手交谈";
			break;
		case 3:
			msg = "扩充背包";
			break;
		case 4:
			msg = "商城刷新";
			break;
		case 5:
			msg = "商城购买物品";
			break;
		case 6:
			msg = "学习技能";
			break;
		case 7:
			msg = "训练扩充时间";
			break;
		case 8:
			msg = "训练加速";
			break;
		case 9:
			msg = "活动立即完成";
			break;
		case 10:
			msg = "停止探险活动";
			break;
		case 11:
			msg = "定点投掷";
			break;
		case 12:
			msg = "拍卖";
			break;
		case 13:
			msg = "世界消息和广播需要消费的钱币";
			break;
		case 14:
			msg = "疯狂淘宝";
			break;
		case 15:
			msg = "沙漠寻宝";
			break;
		case 16:
			msg = "购买鱼饵";
			break;
		case 17:
			msg = "赠送鲜花";
			break;
		case 18:
			msg = "扑捉助手";
			break;
		case 19:
			msg = "助手交谈加速";
			break;
		case 20:
			msg = "赛道解锁";
			break;
		default:
			break;
		}
		return msg;
	}
}
