package util.logger;

import org.apache.log4j.Logger;
import util.binreader.ItemData;
import db.model.Player;
/**
 * 物品日志
 * @author fengmx
 */
public class ItemLogger {
	private static Logger logger = Logger.getLogger(ItemLogger.class);
	private static final char DIV = '|';
//	private static final char NULL = ' ';
	private static final String GAME = "rekoocar";
	private static final String OPERATE_ADDITEM = "获得物品";//添加物品
	private static final String OPERATE_DELETEITEM = "删除物品";//删除物品
	private static final String OPERATE_UPDATEITEM = "更新物品";//更新物品
	/**
	 * 物品来源
	 */
	public static int[] itemAdd=
	{0,//商城购买
	1,//任务奖励
	2,//战斗奖励
	3,//拍卖获取
	4,//抽奖获取
	5,//GM（系统）赠送
	6,//开礼包获得
	7,//Q点购买获得
	8,//剧情奖励的物品
	9,//周游获得的物品
	10,//在线礼包领取
	11,//星座抽取
	12,//疯狂淘宝活动结算
	13,//探宝完成之后的结算
	14,//购买鱼饵
	15,//钓鱼的动作
	16,//领取红钻日奖励
	17,//领取红钻周礼包奖励
	18,//领取红钻月礼包奖励
	19,//领取VIP每日动力
	20,//拆分获得新物品
	21,//开服活动领取
	22,//更新赔偿获得物品
	23,//竞技场兑换
	24,//封测奖励获得
	25,//签到领取
	26,//邮件领取
	27,//分享领取
	28//五大活动领取
	};
	/**
	 * 物品删除的方式
	 */
	public static int[] itemDelete=
	{0,//雇佣助手时
	1,//使用物品时
	2,//出售指定位置指定数量的物品
	3,//赛车款型强化时
	4,//赛车性能强化时
	5,//车库强化时
	6,//助手成长强化
	7,//助手灵魂强化
	8,//车手装备强化
	9,//赛车挂件强化
	10,//车库装饰强化
	11,//稀有打造
	12,//GM删除
	13,//移动删除物品
	};
	/**
	 * 更新物品的方式
	 */
	public static int[] itemUpdate=
	{0//使用道具时
	};
	private static void info(String s){
		logger.info(s);
	}
	/**
	 * 添加物品日志
	 * @param playerName 角色名
	 * @param itemName 物品名
	 * @param itemFrom 物品来源
	 * @param gold 金钱
	 */
	public static void addItemLog(Player player, int code, int number, int itemAdd){
		ItemData itemData  = ItemData.getItemData(code);
		if(itemData==null){
			logger.error("角色名："+player.getName()+"，方法名：addItemLog"+",无效的ItemData数据："+code);
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(OPERATE_ADDITEM).append(DIV);//操作
		sb.append("车手名：").append(player.getName()).append(DIV);
		sb.append("物品名：").append(itemData.getName()).append(DIV);
		sb.append("物品数量：").append(number).append(DIV);
		sb.append("物品来源：").append("[").append(getItemFromForItemAdd(itemAdd)).append("]").append(DIV);
//		sb..append(gold);
		info(sb.toString());
	}
	/**
	 * 删除物品日志
	 */
	public static void deleteItemLog(Player player, int code, int number, int itemDelete){
		ItemData itemData  = ItemData.getItemData(code);
		if(itemData==null){
			logger.error("角色名："+player.getName()+"，方法名：deleteItemLog"+",无效的ItemData数据："+code);
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(OPERATE_DELETEITEM).append(DIV);//操作
		sb.append("车手名：").append(player.getName()).append(DIV);
		sb.append("物品名：").append(itemData.getName()).append(DIV);
		sb.append("物品数量：").append(number).append(DIV);
		sb.append("删除方式：").append("[").append(getItemFromForItemDelete(itemDelete)).append("]").append(DIV);
		info(sb.toString());
	}
	/**
	 * 更新物品日志
	 * @param player
	 * @param code
	 * @param useTimes 剩余使用次数
	 * @param itemUpdate
	 * @param gold
	 */
	public static void updateItemLog(Player player, int code, int useTimes, int itemUpdate, int gold){
		ItemData itemData  = ItemData.getItemData(code);
		if(itemData==null){
			logger.error("角色名："+player.getName()+"，方法名：updateItemLog"+",无效的ItemData数据："+code);
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(OPERATE_UPDATEITEM).append(DIV);//操作
		sb.append(player.getName()).append(DIV);
		sb.append(itemData.getName()).append(DIV);
		sb.append(useTimes).append(DIV);
		sb.append("[").append(itemUpdate).append("]").append(DIV);
		sb.append(gold);
		info(sb.toString());
	}
	/**
	 * 添加物品时的类型转换
	 * @param type
	 * @return
	 */
	public static String getItemFromForItemAdd(int type){
		String msg = "rekoo_add";
		switch (type) {
		case 0:
			msg = "商城购买";
			break;
		case 1:
			msg = "任务奖励";
			break;
		case 2:
			msg = "战斗奖励";
			break;
		case 3:
			msg = "拍卖获取";
			break;
		case 4:
			msg = "抽奖获取";
			break;
		case 5:
			msg = "GM（系统）赠送";
			break;
		case 6:
			msg = "开礼包获得";
			break;
		case 7:
			msg = "Q点购买获得";
			break;
		case 8:
			msg = "剧情奖励的物品";
			break;
		case 9:
			msg = "周游活动获得的物品";
			break;
		case 10:
			msg = "在线礼包领取";
			break;
		case 11:
			msg = "星座抽取";
			break;
		case 12:
			msg = "疯狂淘宝活动结算";
			break;
		case 13:
			msg = "探宝完成之后的结算";
			break;
		case 14:
			msg = "购买鱼饵";
			break;
		case 15:
			msg = "钓鱼的获得";
			break;
		case 16:
			msg = "领取红钻日礼包奖励";
			break;
		case 17:
			msg = "领取红钻周礼包奖励";
			break;
		case 18:
			msg = "领取红钻月礼包奖励";
			break;
		case 19:
			msg = "领取VIP每日动力";
			break;
		case 20:
			msg = "拆分获得新物品";
			break;
		case 21:
			msg = "开服活动领取";
			break;
		case 22:
			msg = "更新赔偿获得物品";
			break;
		case 23:
			msg = "竞技场积分兑换";
			break;
		case 24:
			msg = "封测奖励获得";
			break;
		case 25:
			msg = "签到领取";
			break;
		case 26:
			msg="邮件领取";
			break;
		case 27:
			msg="分享领取";
			break;
		case 28:
			msg="五大活动领取";
			break;
		default:
			break;
		}
		return msg;
	}
	/**
	 * 删除物品时的类型转换
	 * @param type
	 * @return
	 */
	private static String getItemFromForItemDelete(int type){
		String msg = "rekoo_delete";
		switch (type) {
		case 0:
			msg = "雇佣助手时";
			break;
		case 1:
			msg = "使用物品时";
			break;
		case 2:
			msg = "出售指定位置指定数量的物品";
			break;
		case 3:
			msg = "赛车款型强化时";
			break;
		case 4:
			msg = "赛车性能强化时";
			break;
		case 5:
			msg = "车库强化时";
			break;
		case 6:
			msg = "助手成长强化";
			break;
		case 7:
			msg = "助手灵魂强化";
			break;
		case 8:
			msg = "车手装备强化";
			break;
		case 9:
			msg = "赛车挂件强化";
			break;
		case 10:
			msg = "车库装饰强化";
			break;
		case 11:
			msg = "稀有打造";
			break;
		case 12:
			msg = "GM删除";
			break;
		case 13:
			msg = "移动删除物品";
			break;
		default:
			break;
		}
		return msg;
	}
}
