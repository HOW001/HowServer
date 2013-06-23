package db.model;

import org.apache.log4j.Logger;

import db.service.IDManager;
import server.cmds.PackCP;
import util.ByteArray;
import util.binreader.ItemData;
import util.logger.ItemLogger;
/**
 * 物品
 * @author fengmx
 *
 */
public class Item implements DataBaseEntry{
	//test
	private static Logger logger = Logger.getLogger(Item.class);
	private int id;//编号
	private int version;
	private int packId;//背包编号
	private int code;//物品编号(策划)
	private int indexType;//位置类型 （0：背包）  默认是背包里的物品
	private int indexId;//物品的位置编号
	//玩家装备:0:头盔  1:驾驶证  2:手套  3:衣服  4:手表  5:靴子  6:头饰  7:时装
	//赛车装备:0:车顶 1:车头 2:车尾3:底盘 4:车轮　
	//车库装备:1:盆栽  2:改装架  3:工具柜  4:沙发  5:加油箱  6:训练室 
	private int isBind; //该物品是否绑定（1：绑定  2：不绑定）
	private int currentDura;//当前耐久
	private int hasUseTimes;//可以使用的次数
	private long lastUseTime;//上次使用时间
	private long lastGetTime;//获得时间
	private long dueTime;//到期时间
	private int number;//数量
	private int itemPlace;//物品来源
	private int attributeValue;//增加属性值

	private Item() {
	}
	public static Item create(){
		Item item=new Item();
		item.id=IDManager.getInstance().getCurrentItemID();
		return item;
	}
	//*****************物品类型******************************
	public static final byte type_consume = 1;//消耗品
	public static final byte sum_type = 1; //总的物品类型数
	//******************物品所属关系*****************************
	public static final byte index_pack = 0;//属于背包

	//******************物品到期类型*********************************
	public static final byte type_time_forever = 0;//永久有效
	public static final byte type_time_get = 1;//获得开始计时
	public static final byte type_time_use = 2;//使用开始计时
	//******************物品的绑定类型********************************
	public static final byte type_bind_get = 0;
	public static final byte type_bind_use = 1;
	public static final byte type_bind_forever = 2;
	//******************物品是否绑定***************************
	public static final byte type_bind = 1;
	public static final byte type_not_bind = 2;
	//*******************使用/装备限制类型**************************
	public static final int type_use_item_no = 0;//无限制
	public static final int type_use_item_player = 1;//角色等级
//	public static final int type_use_item_car = 2;//赛车等级
//	public static final int type_use_item_carBarn = 3;//车库等级
	ItemData itemData;//物品的策划数据
	public ItemData getItemData(int code){
		return ItemData.getData().get(code);
	}
	/**
	 * 检测物品是否有效
	 * @return
	 */
	public boolean hasItem(){
		if(this.getNumber() == Item.noUseTime){
			return true;
		}
		if(this.getNumber()<=0){
			return false;
		}
		return true;
	}
	/**
	 * 检测数量是否有效
	 * @param number
	 * @param code
	 * @return
	 */
	public boolean checkNumber(int number, int code){
		if(number<=0 || number>this.getItemData(code).getPackNumber()){
			return false;
		}
		return true;
	}
	/**
	 * 无使用次数限制  -1
	 */
	public static final byte noUseTime = -1;
	/**
	 * 使用物品
	 * 设计规定有使用次数的物品，不能叠加
	 */
	public boolean use(Player player){
		if(this.getItemData(this.getCode()).getUseTimes() != noUseTime){
			this.hasUseTimes--;
			ItemLogger.updateItemLog(player, code, this.hasUseTimes, ItemLogger.itemUpdate[0], 0);
			if(this.hasUseTimes<=0){
				this.number=0;
				ItemLogger.deleteItemLog(player, code, 1, ItemLogger.itemDelete[1]);
			}
			return true;
		}
		return false;
	}
	/**
	 * 写入物品数据
	 * 
	 * @param ba
	 * @param itemData 策划数据实体
	 */
	public void writeItemMessage(ByteArray ba){
		//*************不变的数据*****************
		ba.writeInt(this.getCode());
		ItemData itemData = ItemData.getItemData(this.code);
		if(itemData==null){
			logger.error("无效的Item数据："+this.code);
			ba.writeInt(0);
		} else {
			ba.writeInt(itemData.getBindType());
		}
		//**************变化的数据**************************
		ba.writeInt(this.getId());
		ba.writeByte((byte)this.getIsBind());
		ba.writeInt(this.getHasUseTimes());
		ba.writeLong(this.getDueTime());
		ba.writeInt(this.getNumber());
	}
	/**
	 * 发空数据
	 * @param ba
	 */
	public static void writeNullMessage(ByteArray ba){
		//*************不变的数据*****************
		ba.writeInt(-1);
		ba.writeInt(-1);
		//**************变化的数据**************************
		ba.writeInt(-1);
		ba.writeByte(-1);
		ba.writeInt(-1);
		ba.writeLong(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
		ba.writeInt(-1);
	}

	//**********************************************
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getPackId() {
		return packId;
	}
	public void setPackId(int packId) {
		this.packId = packId;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public int getIndexType() {
		return indexType;
	}
	public void setIndexType(int indexType) {
		this.indexType = indexType;
	}
	public int getIndexId() {
		return indexId;
	}
	public void setIndexId(int indexId) {
		this.indexId = indexId;
	}
	public int getIsBind() {
		return isBind;
	}
	public void setIsBind(int isBind) {
		this.isBind = isBind;
	}
	public int getCurrentDura() {
		return currentDura;
	}
	public void setCurrentDura(int currentDura) {
		this.currentDura = currentDura;
	}
	public int getHasUseTimes() {
		return hasUseTimes;
	}
	public void setHasUseTimes(int hasUseTimes) {
		this.hasUseTimes = hasUseTimes;
	}
	public long getLastUseTime() {
		return lastUseTime;
	}
	public void setLastUseTime(long lastUseTime) {
		this.lastUseTime = lastUseTime;
	}
	public long getLastGetTime() {
		return lastGetTime;
	}
	public void setLastGetTime(long lastGetTime) {
		this.lastGetTime = lastGetTime;
	}
	public long getDueTime() {
		return dueTime;
	}
	public void setDueTime(long dueTime) {
		this.dueTime = dueTime;
	}
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getItemPlace() {
		return itemPlace;
	}
	public void setItemPlace(int itemPlace) {
		this.itemPlace = itemPlace;
	}
	public int getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(int attributeValue) {
		this.attributeValue = attributeValue;
	}
	@Override
	public void initDBEntry(Player player) {
		this.setPackId(player.getPlayerPackEntry().getId());
	}
	/**
	 * 生成新的物品
	 * @param code
	 */
	public static Item createItem(int code){
		Item item =  create();
		ItemData itemData = item.getItemData(code);
		if(itemData == null){
			logger.error("无效的物品编号：" + code);
			return null;
		}
		item.setCode(code);
		//是否是获取绑定
		if(itemData.getBindType() == type_bind_get){
			item.setIsBind(Item.type_bind);
		} else {
			item.setIsBind(Item.type_not_bind);
		}
//		item.setHasUseTimes(1);
		item.setHasUseTimes(itemData.getUseTimes());
		item.setLastGetTime(System.currentTimeMillis());
		//根据物品的到期类型设置到期时间
		if(itemData.getTimeType()== type_time_get){
			item.setDueTime(System.currentTimeMillis() + itemData.getTotalUseTime()*1000);
		} else {
			item.setDueTime(itemData.getTotalUseTime()*1000);
		}
		return item;
	}

	public static final int max_player_equip_level = 250;//角色装备最大等级
	public static final int max_car_equip_level = 250;//赛车挂件最大等级
	public static final int max_car_barn_equip_level = 250;//车库装饰物最大等级
	
	
	/**
	 * 装备升级
	 * @param item
	 * @return
	 */
	public boolean equipLevelUp(Player player, Item item,int itemType,int strengthenType) {
		return true;
	}
	/**
	 * 检测装备中的物品能否升级
	 * @param strengthenType
	 * @return
	 */
	public boolean checkItemLevelUp(int strengthenType){
		return false;
	}
		/**
	 * 使用消耗物品
	 * @return
	 */
	public boolean use(Player player,double needShield,double needPower,int index){
		if(attributeValue<=0){
			return false;
		}
		return true;
	}
}
