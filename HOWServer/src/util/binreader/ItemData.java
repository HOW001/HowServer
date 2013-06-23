package util.binreader;

import java.util.HashMap;
import java.util.Map;
/**
 * 物品静态数据
 * @author fengmx
 */
public class ItemData implements PropertyReader{
	public int id;//物品编号
	private String name;//物品名
	private int type;//物品类型（所属背包）
	private int colorType;//物品颜色
	private int packNumber;//可叠加数量
	private int bindType;//绑定类型
	public int strengthen;//强化等级
	public int level;//物品等级
	public int strengthenItemId;
	private int leftKeyFunction;//左键功能列表
	public int needLevelType;
	private int needLevel;
	private int timeType;//有效期类型
	private int totalUseTime;//物品有效期
	private int useTimes;//可以使用次数
	public double mark;//物品评分
	public double shield;//防护
	public double power;//动力
	public double speed;//速度
	public double handleUse;//操控值
	public double attack;//攻击力，技能
	public double reaction;//反应力
	private String strengthenValues;
	private int isCanBuy;//是否可购买
	private int price;//价格
	private int getPlaceType;
	private int getPlace;//物品获得路径
	private int imageId;//背包内物品图片编号
	private int pictureIdForBoy;
	private int pictureIdForGirl;
	private String description;//物品描述信息
	public int triggerStory;//触发剧情
	private int red;//红钻折扣
	public int triggerGuide;//触发引导
	
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			data.put(id, this);
		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
	//*****************************************
	private static Map<Integer,ItemData> data = new HashMap<Integer, ItemData>();
	public static Map<Integer, ItemData> getData() {
		return data;
	}
	public static ItemData getItemData(int code){
		return data.get(code);
	}
	/**
	 * 检测是否有该编号的策划数据
	 * @param code
	 * @return
	 */
//	public boolean hasItemData(int code){
//		if(data.get(code)==null){
//			return false;
//		}
//		return true;
//	}
	//*****************************
//	public int getId() {
//		return id;
//	}
	public String getName() {
		return name;
	}
	public int getType() {
		return type;
	}
	public int getColorType() {
		return colorType;
	}
	public int getPackNumber() {
		return packNumber;
	}
	public int getBindType() {
		return bindType;
	}
	public int getLevel() {
		return level;
	}
	public int getLeftKeyFunction() {
		return leftKeyFunction;
	}
	public int getNeedLevelType() {
		return needLevelType;
	}
	public int getNeedLevel() {
		return needLevel;
	}
	public int getTimeType() {
		return timeType;
	}
	public int getTotalUseTime() {
		return totalUseTime;
	}
	public int getUseTimes() {
		return useTimes;
	}
//	public int getMark() {
//		return mark;
//	}
//	public int getShield() {
//		return shield;
//	}
//	public int getPower() {
//		return power;
//	}
//	public int getSpeed() {
//		return speed;
//	}
//	public int getHandleUse() {
//		return handleUse;
//	}
//	public int getAttack() {
//		return attack;
//	}
//	public int getReaction() {
//		return reaction;
//	}
	public int getIsCanBuy() {
		return isCanBuy;
	}
	public int getPrice() {
		return price;
	}
	public int getGetPlaceType() {
		return getPlaceType;
	}
	public int getGetPlace() {
		return getPlace;
	}
	public int getImageId() {
		return imageId;
	}
	public int getPictureIdForBoy() {
		return pictureIdForBoy;
	}
	public int getPictureIdForGirl() {
		return pictureIdForGirl;
	}
	public String getDescription() {
		return description;
	}
	public int getStrengthenItemId() {
		return strengthenItemId;
	}
	public String getStrengthenValues() {
		return strengthenValues;
	}
	public int getRed() {
		return red;
	}
	public void setRed(int red) {
		this.red = red;
	}
	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return data.get(id);
	}
	
}
