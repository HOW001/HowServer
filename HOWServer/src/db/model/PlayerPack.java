package db.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.List;

import org.apache.log4j.Logger;

import db.service.DBPlayerItemImp;
import db.service.IDManager;
import server.cmds.PackCP;
import server.cmds.PlayerCP;
import server.cmds.UISystemCP;
import util.binreader.GameParameterData;
import util.binreader.ItemData;
import util.binreader.PromptData;
import util.logger.ItemLogger;
import world.World;
/**
 * @author fengmx
 */
public class PlayerPack implements DataBaseEntry{
	private static Logger logger = Logger.getLogger(PlayerPack.class);
	private int id;
	private int version;
	private int holder;
	private int currentPackNumber;
	private int isOpen;//背包自动补给功能是否开启 0:开启  1：关闭
	private int openIndex;//背包自动补给的物品位置
	//**********************************************
	public int maxPackSize = 90;
	private PlayerPack(){}
	public static PlayerPack create(){
		PlayerPack pack=new PlayerPack();
		pack.id=IDManager.getInstance().getCurrentPlayerPackID();
		return pack;
	}
	public int getMaxPackSize() {
		return maxPackSize;
	}
	public void setMaxPackSize(int maxPackSize) {
		this.maxPackSize = maxPackSize;
	}
	public static final int init_pack_size = 30; 
	/**
	 * 需要更新的索引
	 */
	public List<Integer> updateIndex = null;
	public void addUpdataIndex(int index){
		if(updateIndex == null){
			updateIndex = new ArrayList<Integer>();
		}
		if(!updateIndex.contains(index)){
			updateIndex.add(index);
		}
	}
	public List<Integer> getUpdateIndex(){
		List<Integer> copy = new LinkedList<Integer>();
		if(updateIndex == null){
			updateIndex = new ArrayList<Integer>();
			return copy;
		}
		copy.addAll(updateIndex);
		updateIndex.clear();
		return copy;
	}
//	Player player;
	Set<Item> itemsEntry = new HashSet<Item>();
	/**
	 * 添加物品，但是不需要指定物品来源
	 * @param code
	 * @param number
	 */
	public synchronized void addItem(int code, int number){
		addItem(code, number, -1,true);
	}
	/**
	 * 添加单类多个物品
	 * @param code
	 * @param number
	 * @param itemPlace  物品来源
	 */
	public synchronized boolean addItem(int code, int number, int itemPlace){
		return addItem(code, number, itemPlace,true);
	}
	/**
	 * 添加单类多个物品
	 * @param code
	 * @param number
	 * @param itemPlace 物品来源
	 * @param isNeed false:不需要提示
	 */
	public synchronized boolean addItem(int code, int number, int itemPlace,boolean isNeed){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.addItem(int code, int number, int itemPlace,boolean isNeed)时车手对象为空"+holder);
			return false;
		}
		long time = System.currentTimeMillis();
		if(number<=0){
			return false;
		}
		List<Item> itemsList = new ArrayList<Item>();
		ItemData itemData = ItemData.getItemData(code);
		Item item = null;
		if(itemData==null){
			logger.error("无法找到相关的物品数据:"+code+"，物品来源："+itemPlace);
			return false;
		}
		int packNumber = itemData.getPackNumber();
		int packCount = number / packNumber;
		if(number % packNumber != 0){
			packCount += 1;
		}
		for(int i=0; i<packCount; i++){
			if(number<=0){
				break;
			}
			item = Item.createItem(code);
			if(number > packNumber){
				item.setNumber(packNumber);
				number -= packNumber;
			} else{
				item.setNumber(number);
			}
			if(!addItem(item,packNumber,i)){
				if(i<=0){
					return false;
				}
			} else {
				if(item.getNumber()>0){
					item.setItemPlace(itemPlace);
					itemsList.add(item);
				}
			}
		}
		//增加物品时，先存储数据库，后添加到内存
		DBPlayerItemImp.getInstance().saveItems(itemsList);
		for(Item temp:itemsList){
			if(temp.getId()==0){
				logger.error("未存储数据库："+temp.getId());
				continue;
			}
			if(temp.getPackId()==0){
				logger.error("未存储背包id："+temp.getPackId() + "物品来源："+itemPlace);
				continue;
			}
			//后添加到内存
			this.itemsEntry.add(temp);
		}
		PackCP.getInstance().updateItemMessage(player.getIoSession(),this,itemPlace);
				
		return true;
	}
	/**
	 * 添加物品
	 * @param Item 物品
	 * @return 
	 */
	private boolean addItem(Item item, int packNumber,int times){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.addItem(Item item, int packNumber)时车手对象为空"+holder);
			return false;
		}
		//判断叠加
		if(packNumber == 1 || packNumber==item.getNumber()){
			return addItem(item,times);
		}
		if(itemsEntry != null && itemsEntry.size() != 0){
			for(Item it:itemsEntry){
				if(!it.hasItem()){
					continue;
				}
				if(it.getNumber()>=packNumber){
					continue;
				}
				if(item.getCode() == it.getCode()){
					if(it.getNumber()<=packNumber - item.getNumber()){
						it.setNumber(it.getNumber()+item.getNumber());
						item.setNumber(0);
						item.setIndexId(it.getIndexId());
						addUpdataIndex(it.getIndexId());
						return true;
					} else {
						//判断空闲背包
						if(getSpaceNumber()<=0){
							logger.info(player.getName()+"空闲背包不足");
							return false;
						}
						item.setNumber((it.getNumber() + item.getNumber())- packNumber);
						it.setNumber(packNumber);
						boolean isOk = addItem(item,times);
						if(isOk){
							addUpdataIndex(it.getIndexId());
							addUpdataIndex(item.getIndexId());
						}
						return isOk;
					}
				}
			}
		}
		return addItem(item,times);
//		return true;
	}
//	private List<Item> addItems = new ArrayList<Item>();
	private boolean addItem(Item item, int times){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.addItem(Item item)时车手对象为空"+holder);
			return false;
		}
		if(getSpaceNumber()-times<=0){
			UISystemCP.openDialog(player.getIoSession(), "空闲背包不足");
			logger.info(player.getName()+"空闲背包不足");
			return false;
		}
		if(item.hasItem()){
			for(int i=0;i<currentPackNumber;i++){
				if(checkIsHaveItem(i)){
					continue;
				}
				item.initDBEntry(player);
				item.setIndexType(Item.index_pack);
				item.setIndexId(i+times);
				if(addItemsEntry(item)){
//					addItems.add(item);
//					itemsEntry.add(item);
					addUpdataIndex(i+times);
				} else {
					logger.info("该位置已有物品，无法添加");
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 检测该位置是否有物品
	 * @param index
	 * @return false表示 该位置没有物品
	 */
	private boolean checkIsHaveItem(int index){
		Item item = getItemByIndex(index);
		if(item == null){
			return false;
		}
		return true;
	}
	/**
	 * 删除指定位置的物品
	 * @param index 位置
	 * @param dropFrom 删除的原因
	 */
	public synchronized boolean deleteItemByIndex(int index,int dropFrom){
//		long time = System.currentTimeMillis();
		Item item = getItemByIndex(index);
		if(item == null){
			logger.info("该位置没有物品，无法删除，提示前端");
			return false;
		}
		if(!item.hasItem()){
			return false;
		}
		//必须判断条件
		return deleteItemByIndex(index, item.getNumber(), dropFrom);
	}
	/**
	 * 出售1件物品
	 * @param index
	 */
	public boolean sellItem(int index){
		return sellItem(index, 1);
	}
	/**
	 * 出售指定位置指定数量的物品
	 * @param index
	 * @param number
	 * @return
	 */
	public synchronized boolean sellItem(int index,int number){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.sellItem()时车手对象为空"+holder);
			return false;
		}
		long time = System.currentTimeMillis();
		Item item = getItemByIndex(index);
		if(item == null){
			UISystemCP.openDialog(player.getIoSession(), "该位置没有物品，无法出售");
			return false;
		}
		if(!item.hasItem()){
			UISystemCP.openDialog(player.getIoSession(), "该位置没有物品，无法出售");
			return false;
		}
		ItemData itemData =  item.getItemData(item.getCode());
		if(itemData == null){
			logger.error("角色名："+player.getName()+"，方法名：sellItem"+",无效的策划数据，" + player.getName());
			return false;
		}
		if(itemData.getPrice()<=0){
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "不可出售！");
			return false;
		}
		logger.info("出售物品：" + item.getCode());
		if(item.getNumber() - number < 0){
			UISystemCP.openDialog(player.getIoSession(), "出售的数量有误");
			return false;
		} 
		if (deleteItemByIndex(index, number, ItemLogger.itemDelete[2])) {
			int price = itemData.getPrice() * number;
//			player.addBindGold(price, MoneyLogger.moneyAdd[0]);
			// MoneyLogger.addMoneyLog(player, MoneyLogger.moneyAdd[1], true,
			// price);
			logger.info(player.getName() + "出售物品：" + itemData.getName()+ number + "，获得金钱：" + price);
			logger.info("sellItem(int index)用时："+ (System.currentTimeMillis() - time));
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 拆分物品
	 * @param index
	 * @param number
	 */
	public synchronized boolean spliteItem(int oldIndex, int newIndex, int number) {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.spliteItem()时车手对象为空"+holder);
			return false;
		}
		long time = System.currentTimeMillis();
		List<Item> itemsList = new ArrayList<Item>();
		Item oldItem = getItemByIndex(oldIndex);
		Item newItem = getItemByIndex(newIndex);
		if(oldItem == null){
			logger.error("角色名："+player.getName()+"，方法名：spliteItem"+",无效的物品，无法拆分，提示前端");
			return false;
		}
		ItemData oldItemData = oldItem.getItemData(oldItem.getCode());
		if(oldItemData == null){
			logger.error("角色名："+player.getName()+"，方法名：spliteItem"+",无效的ItemData数据："+oldItem.getCode());
			return false;
		}
		int packNumber = oldItemData.getPackNumber();
		int oldItemNumber = oldItem.getNumber();
		if(number<1 || number>=packNumber){
			logger.info("角色名："+player.getName()+"，方法名：spliteItem"+",无效的拆分数量，提示前端");
			return false;
		}
		if(oldItemNumber<=number){
			logger.info("角色名："+player.getName()+"，方法名：spliteItem"+",无效的拆分数量，提示前端");
			return false;
		}
		if(newItem == null){
			oldItem.setNumber(oldItemNumber - number);
			newItem = Item.createItem(oldItem.getCode());
			newItem.setNumber(number);
			newItem.initDBEntry(player);
			newItem.setIndexType(Item.index_pack);
			newItem.setIndexId(newIndex);
			//若拆分移动后数额不等，强制返回原来数量
			if((oldItemNumber-number+newItem.getNumber())!=(oldItem.getNumber()+newItem.getNumber())){
				oldItem.setNumber(oldItemNumber);
				return false;
			}
			itemsList.add(newItem);
		} else {
			if(oldItem.getCode()!=newItem.getCode()){
				return false;
			}
			ItemData newItemData = newItem.getItemData(newItem.getCode());
			if(newItemData == null){
				logger.error("角色名："+player.getName()+"，方法名：spliteItem"+",无效的策划数据，无法拆分，" + newItem.getCode());
				return false;
			}
			int newItemNumber = newItem.getNumber();
			if(number + newItemNumber>=2*packNumber){
				logger.info("角色名："+player.getName()+"，方法名：spliteItem"+",无效的拆分，提示前端");
				return false;
			}else if(number + newItemNumber <= packNumber){
				oldItem.setNumber(oldItemNumber - number);
				newItem.setNumber(newItemNumber + number);
//				itemsList.add(newItem);
//				itemsList.add(oldItem);
			} else {
				newItem.setNumber(packNumber);
				oldItem.setNumber(oldItemNumber + newItemNumber  - packNumber);
//				itemsList.add(newItem);
//				itemsList.add(oldItem);
			}
			//若拆分合并后数额不等，强制返回原来数量
			if((oldItemNumber+newItemNumber)!=(oldItem.getNumber()+newItem.getNumber())){
				oldItem.setNumber(oldItemNumber);
				newItem.setNumber(newItemNumber);
				return false;
			}
		}
		addUpdataIndex(oldIndex);
		addUpdataIndex(newIndex);
		if(itemsList.size()>0){
			//先存储数据库后添加到内存
			DBPlayerItemImp.getInstance().saveItems(itemsList);
			for(Item temp:itemsList){
				itemsEntry.add(temp);
				ItemLogger.addItemLog(player, temp.getCode(), temp.getNumber(), ItemLogger.itemAdd[20]);
			}
		}
		logger.info("spliteItem(int oldIndex, int newIndex, int number)用时：" + (System.currentTimeMillis() - time));
		return true;
	}
	/**
	 * 移动物品
	 * @param oldIndex
	 * @param newIndex
	 */
	public synchronized boolean moveItem(int oldIndex, int newIndex) {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.moveItem()时车手对象为空"+holder);
			return false;
		}
		logger.info(player.getName()+"移动物品");
		long time = System.currentTimeMillis();
		if(oldIndex == newIndex){
			return false;
		}
		Item oldItem = getItemByIndex(oldIndex);
		Item newItem = getItemByIndex(newIndex);
		if(oldItem == null){
			logger.info("角色名："+player.getName()+"，方法名：moveItem"+",无效的物品，无法移动，提示前端");
			return false;
		}
		ItemData oldItemData = oldItem.getItemData(oldItem.getCode());
		if(oldItemData == null){
			logger.error("角色名："+player.getName()+"，方法名：moveItem"+",无效的策划数据，无法移动，" + oldItem.getCode());
			return false;
		}
		int oldItemNumber = oldItem.getNumber();
		int newItemNumber = 0;
		List<Item> deleteItemList = new ArrayList<Item>();
		if(newItem != null){
			ItemData newItemData = newItem.getItemData(newItem.getCode());
			if(newItemData == null){
				logger.error("角色名："+player.getName()+"，方法名：moveItem"+",无效的策划数据，无法移动，" + newItem.getCode());
				return false;
			}
			if(player.isSaveState){
				logger.error(player.getName()+"数据库保存状态移动物品");
				UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "移动物品失败,请重新尝试...");
				return false;
			}
			newItemNumber = newItem.getNumber();
			if(oldItem.getCode() == newItem.getCode()){
				int packNumber = oldItemData.getPackNumber();
				if(packNumber>1){
//					logger.info("移动叠加物品");
					//移动叠加物品
					int sumNumber = oldItemNumber + newItemNumber;
					if(sumNumber <= packNumber){
//						itemsEntry.remove(oldItem);
//						deleteItemByIndex(oldIndex, ItemLogger.itemDelete[13]);
						deleteItemList.add(oldItem);
						newItem.setNumber(sumNumber);
						oldItem.setNumber(0);
					} else {
						oldItem.setNumber(sumNumber - packNumber);
						newItem.setNumber(packNumber);
					}
				} else if(packNumber==1){
					//移动交换物品
					oldItem.setIndexId(newIndex);
					newItem.setIndexId(oldIndex);
				}
			} else {
				//移动交换物品
				oldItem.setIndexId(newIndex);
				newItem.setIndexId(oldIndex);
			}
			//移动后两物品的总数量不相等，强制返回到原来状态
			int oldItemNumber2 = oldItem.getNumber();
			int newItemNumber2 = newItem.getNumber();
			int sumNumber1 = oldItemNumber + newItemNumber;
			int sumNumber2 = oldItemNumber2 + newItemNumber2;
			if(sumNumber1!=sumNumber2){
				oldItem.setIndexId(oldIndex);
				oldItem.setNumber(oldItemNumber);
				newItem.setIndexId(newIndex);
				newItem.setNumber(newItemNumber);
				logger.error(player.getName()+"移动物品时异常，原来的物品："+oldItem.getCode()+",新的物品："+newItem.getCode());
			}
		} else {
			logger.info("移动物品到空闲位置");
			oldItem.setIndexId(newIndex);
		}
		addUpdataIndex(oldIndex);
		addUpdataIndex(newIndex);
		//移动物品时，先删除内存后删除数据库
		if(deleteItemList.size()>0){
			for(Item temp:deleteItemList){
				this.itemsEntry.remove(temp);
				ItemLogger.deleteItemLog(player, temp.getCode(), temp.getNumber(), ItemLogger.itemDelete[13]);
			}
			DBPlayerItemImp.getInstance().deleteItems(deleteItemList);
		}
		logger.info("moveItem(int oldIndex, int newIndex)用时：" + (System.currentTimeMillis() - time));
		return true;
	}
	/**
	 * 使用物品
	 * @param index
	 */
	public synchronized boolean useItem(int index) {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.useItem()时车手对象为空"+holder);
			return false;
		}
		Item item = getItemByIndex(index);
		if(item == null){
			return false;
		}
		if(!item.hasItem()){
			return false;
		}
		ItemData itemData = ItemData.getItemData(item.getCode());
		if(itemData == null){
			logger.error(player.getName() + "使用物品时出现无效的item数据：" + item.getCode());
			return false;
		}
		if(checkCanUse(itemData)==false){
			return false;
		}
		int type = itemData.getType();
		int fatherType = type/1000;
		int subType = type%1000;
		boolean useSuccess = false;
		
		if(useSuccess){
			PackCP.getInstance().updateItemMessage(player.getIoSession(), player.getPlayerPackEntry());
			PlayerCP.getInstance().updatePlayerMessage(player);
			logger.info(player.getName()+"使用了物品"+item.getCode());
		}
		return useSuccess;
	}
	/**
	 * 整理背包
	 * 顺序：消耗品→赛车类→赛车装备类→角色装备类→合同类→装饰类
	 */
	public synchronized void tidyPack() {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.tidyPack()时车手对象为空"+holder);
			return;
		}
//		logger.info(player.getName()+"整理背包");
		if(player.isSaveState){
			logger.error(player.getName()+"数据库保存状态整理背包");
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "整理背包失败,请重新尝试...");
			return;
		}
		long time = System.currentTimeMillis();
		//合并
		List<Item> deleteItemList = new ArrayList<Item>();
		outer:
		for(int i=0;i<currentPackNumber;i++){
			Item item1 = getItemByIndex(i);
			if(item1 == null){
				continue;
			}
			if(!item1.hasItem()){
				continue;
			}
			ItemData itemData = item1.getItemData(item1.getCode());
			if(itemData == null){
				logger.error("角色名："+player.getName()+"，方法名：tidyPack"+",无效的策划数据，严重错误" + item1.getCode() + ",playerName:" + player.getName());
				return;
			}
			for(int j=i+1;j<currentPackNumber;j++){
				Item item2 = getItemByIndex(j);
				if(item2==null){
					continue;
				}
				if(!item2.hasItem()){
					continue;
				}
				if(item1.getCode() == item2.getCode()){
					int packNumber = itemData.getPackNumber();
					int number1 = item1.getNumber();
					int number2 = item2.getNumber();
					if(number1>packNumber){
						logger.error("物品叠加数量超过叠加上限，严重错误1:" + item1.getCode() + ",playerName:" + player.getName());
						return;
					}
					if(number2>packNumber){
						logger.error("物品叠加数量超过叠加上限，严重错误2:" + item2.getCode() + ",playerName:" + player.getName());
						return;
					}
					if(number1==packNumber){
						continue outer;
					}
					if(number1+number2<=packNumber){
						item1.setNumber(number1 + number2);
						item2.setNumber(0);
						deleteItemList.add(item2);
						addUpdataIndex(i);
						addUpdataIndex(j);
					} else {
						item1.setNumber(packNumber);
						item2.setNumber(number1 + number2 - packNumber);
						addUpdataIndex(i);
						addUpdataIndex(j);
					}
				}
 			}
		}
		//排序
		List<Item> tidyItemsList = new ArrayList<Item>();
		for(int i=1;i<=Item.sum_type;i++){ //按类型排序
			for(Item item : itemsEntry){
				if(item == null){
					continue;
				}
				if(!item.hasItem()){
					continue;
				}
				if(item.getIndexType() != Item.index_pack){
					continue;
				}
				ItemData itemData= item.getItemData(item.getCode());
				if(itemData == null){
					logger.error("整理背包时发现无效的策划数据," + item.getCode() + ",playerName" + player.getName());
					continue;
				}
				addUpdataIndex(item.getIndexId());
				int type = itemData.getType()/1000;
				if(type == i){
					tidyItemsList.add(item);
				}
			}
		}
		//整理背包时，先删除内存后删除数据库
		if(deleteItemList.size()>0){
			for(Item temp:deleteItemList){
				this.itemsEntry.remove(temp);
			}
			DBPlayerItemImp.getInstance().deleteItems(deleteItemList);
		}
		for(int i=0;i<tidyItemsList.size();i++){
			Item tidyItem = tidyItemsList.get(i);
			tidyItem.setIndexId(i);
			addUpdataIndex(i);
		}
		PackCP.getInstance().updateItemMessage(player.getIoSession(), this);
		if(this.isOpen==0){
			this.openIndex=getIsOpenIndex();
			PackCP.getInstance().sendIsOpenMessage(player.getIoSession(), player);
		}
		logger.info(player.getName()+"整理背包");
		if(System.currentTimeMillis() - time>100){
			logger.info("tidyPack()用时：" + (System.currentTimeMillis() - time));
		}
	}
	/**
	 * 通过位置获得背包中的物品
	 * @param index
	 * @return
	 */
	public Item getItemByIndex(int index){
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getIndexType() != Item.index_pack){
				continue;
			}
			if(item.getIndexId() == index){
				return item;
			}
		}
		return null;
	}
	/**
	 * 通过物品编号获得物品
	 * @return
	 */
	public Item getItemById(int itemId){
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getId() == itemId){
				return item;
			}
		}
		return null;
	}
	/**
	 * 判断是否有空闲背包
	 * @return
	 */
	public boolean hasSpace(){
		if(currentPackNumber - getPackItemsList().size()<=0){
			return false;
		}
		return true;
	}
	/**
	 * 获取空闲的背包数量
	 * @return
	 */
	public int getSpaceNumber(){
		return currentPackNumber - getPackItemsList().size();
	}
	/**
	 * 获取背包中的物品列表
	 * @return
	 */
	public List<Item> getPackItemsList(){
		List<Item> packItemsList = new ArrayList<Item>();
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getIndexType() != Item.index_pack){
				continue;
			}
			packItemsList.add(item);
		}
		return packItemsList;
	}
	/**
	 * 获取背包中第一个空闲格子
	 * @return
	 */
	public int getFirstSpacePack(){
		for(int i=0;i<currentPackNumber;i++){
			if(!checkIsHaveItem(i)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 上线赠送物品
	 */
	public void sendItem(){
		for(int i=0;i<6;i++){
			this.addItem(10001,1,-1);
		}
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
	public int getHolder() {
		return holder;
	}
	public void setHolder(int holder) {
		this.holder = holder;
	}
	public int getCurrentPackNumber() {
		return currentPackNumber;
	}
	public void setCurrentPackNumber(int currentPackNumber) {
		this.currentPackNumber = currentPackNumber;
	}
	public int getIsOpen() {
		return isOpen;
	}
	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}
	public int getOpenIndex() {
		return openIndex;
	}
	public void setOpenIndex(int openIndex) {
		this.openIndex = openIndex;
	}
//	public Player getPlayer() {
//		return player;
//	}
//	public void setPlayer(Player player) {
//		this.player = player;
//	}
	public Set<Item> getItemsEntry() {
		return itemsEntry;
	}
	public void setItemsEntry(Set<Item> itemsEntry) {
		this.itemsEntry = itemsEntry;
	}
	public boolean addItemsEntry(Item item){
		if(itemsEntry == null){
			itemsEntry = new HashSet<Item>();
		}
		for(Item temp : itemsEntry){
			if(temp.equals(item)){
				return false;
			}
		}
		return true;
	}
	@Override
	public void initDBEntry(Player p) {
		//初始化背包
		this.setHolder(p.getId());
//		this.setPlayer(p);
		this.setCurrentPackNumber(init_pack_size);
		this.setIsOpen(-1);
		this.setOpenIndex(-1);
//		try {
//			DBPlayerPackImp.getInstance().save(this);
//		} catch (Exception e) {
//			logger.error("添加玩家物品信息异常:",e);
//		}
	}
	/**
	 * 扩充背包
	 * @param extendNumber 扩充背包的数量
	 * @return
	 */
	public boolean extendNumber(int extendNumber) {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.extendNumber()时车手对象为空"+holder);
			return false;
		}
		extendNumber=GameParameterData.extend_number;
		if(this.getCurrentPackNumber() + extendNumber>this.maxPackSize){
			PromptData promptData=PromptData.getDataById(25);
			if(promptData!=null){
				UISystemCP.sendMessageForType(player.getIoSession(), promptData.type, promptData.msg,promptData.id,new String[]{""});
			}
			return false;
		}
//		int needGold = (int)(GameParameterData.pack_value1+(this.currentPackNumber-init_pack_size)*GameParameterData.pack_value2);
//		if(!player.checkGold(needGold)){
//			PromptData promptData=PromptData.getDataById(24);
//			if(promptData!=null){
//				UISystemCP.sendMessageForType(player.getIoSession(), promptData.type, promptData.msg,promptData.id,new String[]{});
//			}
//			return false;
//		}
//		this.setCurrentPackNumber(this.getCurrentPackNumber() + extendNumber);
//		if(player.payGold(needGold,MoneyLogger.moneyDeduct[3])){
////			MoneyLogger.deductMoneyLog(player, MoneyLogger.moneyDeduct[3],needGold);
//		}
		PackCP.getInstance().sendCurrentPackNumber(player.getIoSession(), player);
//		logger.info(player.getName()+"扩充背包："+extendNumber+"格"+"，扣除金钱："+needGold);
		return true;
	}
	/**
	 * 检测指定编号的物品是否充足
	 * @param itemCode 指定的物品编号
	 * @param number 需要的数量
	 * @return
	 */
	public boolean checkItem(int itemCode, int number) {
		int count = 0;
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getIndexType() != Item.index_pack){
				continue;
			}
			if(item.getCode() == itemCode){
				count+=item.getNumber();
				if(count>=number){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 删除指定编号指定数量的物品(后端智能寻找)
	 * @param itemCode
	 * @param number
	 * @return
	 */
	public synchronized boolean deleteItem(int itemCode, int number, int deleteFrom) {
		
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.deleteItem()时车手对象为空"+holder);
			return false;
		}
		logger.info(player.getName()+"删除指定物品"+itemCode+",number="+number);
		
		long time = System.currentTimeMillis();
		int oldNumber = number;
		List<Item> removeItems = new ArrayList<Item>();
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getIndexType()  != Item.index_pack){
				continue;
			}
			if(item.getCode() == itemCode){
				if(item.getNumber()>number){
					item.setNumber(item.getNumber() - number);
					number=0;
					addUpdataIndex(item.getIndexId());
					break;
				} else if(item.getNumber()==number){
					number = number - item.getNumber();
					addUpdataIndex(item.getIndexId());
					removeItems.add(item);
					break;
				} else {
					number = number - item.getNumber();
					addUpdataIndex(item.getIndexId());
					removeItems.add(item);
					continue;
				}
			}
		}
		if(number>0){
			return false;
		}
		if(player.isSaveState){
			logger.error(player.getName()+"数据库保存状态删除物品");
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "删除物品失败,请重新尝试...");
			return false;
		}
		//先删除内存，后删除数据库
		if(removeItems.size()>0){
			for(Item item:removeItems){
				addUpdataIndex(item.getIndexId());
				itemsEntry.remove(item);
				item = null;
			}
			DBPlayerItemImp.getInstance().deleteItems(removeItems);
		}
		ItemLogger.deleteItemLog(player, itemCode, oldNumber, deleteFrom);
		long useTime = System.currentTimeMillis() - time;
		if(useTime>50){
			logger.info("deleteItem()用时：" + useTime);
		}
		return true;
	}
	/**
	 * 清空背包
	 * @return
	 */
	public boolean deleteItemAll(){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.deleteItemAll()时车手对象为空"+holder);
			return false;
		}
		logger.info(player.getName()+"清空背包");
		if(player.isSaveState){
			logger.error(player.getName()+"数据库保存状态清空背包");
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "清空背包失败,请重新尝试...");
			return false;
		}
		List<Item> removeItemsAll =new ArrayList<Item>();
		removeItemsAll.addAll(this.getPackItemsList());
		//先删除内存后删除数据库
		if(removeItemsAll.size()>0){
			for(Item item:removeItemsAll){
				itemsEntry.remove(item);
				ItemLogger.deleteItemLog(player, item.getCode(), item.getNumber(), ItemLogger.itemDelete[12]);
				player.getPlayerPackEntry().addUpdataIndex(item.getIndexId());
			}
			DBPlayerItemImp.getInstance().deleteItems(removeItemsAll);
		}
//		itemsEntry.clear();
		return false;
	}
	/**
	 * 开启背包自动补给功能，若开启则使用补给包
	 * @param isOpen
	 * @return
	 */
	public boolean openPack(byte isOpen){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.openPack()时车手对象为空"+holder);
			return false;
		}
		this.isOpen=isOpen;
		this.openIndex=getIsOpenIndex();
		if(openIndex>=0){
			if(this.isOpen==0){
					useItem(openIndex);
			}
		}
		return true;
	}
	/**
	 * 获取被动自动补给的物品位置
	 * @return
	 */
	public int getIsOpenIndex(){
		List<Item> consumeItems = new ArrayList<Item>();
		Item itemTemp = null;
		int index = -1;
		int fatherType = 0;
		ItemData itemData = null;
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			itemData = ItemData.getItemData(item.getCode());
			fatherType = itemData.getType()/1000;
			if(item.getIndexType()!=Item.index_pack){
				continue;
			}
			if(fatherType == Item.type_consume){
				consumeItems.add(item);
			}
		}
		if(consumeItems!=null&&consumeItems.size()!=0){
			//按位置排序
			for(int i=0;i<consumeItems.size();i++){
				if(itemTemp == null){
					itemTemp = consumeItems.get(i);
					index = itemTemp.getIndexId();
					continue;
				}
				//剩余的次数最少的自动使用，当有2个或更多补给包剩余的次数相同时 则按照其在背包的顺序（从上至下 同一行则按照从左到右)靠前的率先使用。
				if(consumeItems.get(i).getAttributeValue()<itemTemp.getAttributeValue()){
					itemTemp = consumeItems.get(i);
					index = consumeItems.get(i).getIndexId();
				} else if(consumeItems.get(i).getAttributeValue()==itemTemp.getAttributeValue()){
					if(consumeItems.get(i).getIndexId()<itemTemp.getIndexId()){
						itemTemp = consumeItems.get(i);
						index = consumeItems.get(i).getIndexId();
					}
				}
			}
		}
		return index;
	}
	/**
	 * 根据物品的id，获得该物品的数量
	 * @param code
	 * @return
	 */
	public int getItemNumberById(int code){
		int count = 0;
		for(Item item:itemsEntry){
			if(item == null){
				continue;
			}
			if(!item.hasItem()){
				continue;
			}
			if(item.getCode() == code){
				count += item.getNumber();
			}
		}
		return count;
	}
	
	/**
	 * 删除指定位置指定数量的物品
	 * @param index
	 * @param number
	 * @return
	 */
	public synchronized boolean deleteItemByIndex(int index, int number,int dropFrom) {
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.deleteItemByIndex(int index, int number,int dropFrom)时车手对象为空"+holder);
			return false;
		}
		long time = System.currentTimeMillis();
		Item item = this.getItemByIndex(index);
		if(item.getNumber()<number){
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "剩余数量不足");
			return false;
		}
		if(player.isSaveState){
			logger.error(player.getName()+"数据库保存状态删除物品");
			UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "删除或使用物品失败,请重新尝试...");
			return false;
		}
		item.setNumber(item.getNumber() - number);
		try {
			if(item.getNumber()==0){
				itemsEntry.remove(item);
				DBPlayerItemImp.getInstance().delete(item);
				ItemLogger.deleteItemLog(player, item.getCode(), number, dropFrom);
				item = null;
			}
		} catch (Exception e) {
			logger.error("删除玩家物品信息异常:",e);
		}
		addUpdataIndex(index);
		logger.info("deleteItemByIndex()用时：" + (System.currentTimeMillis() - time));
		return true;
	}
	private boolean checkCanUse(ItemData itemData){
		Player player = World.getPlayer(holder);
		if(player==null){
			logger.error("PlayerPack.checkCanUse()时车手对象为空"+holder);
			return false;
		}
		int needLevelType = itemData.getNeedLevelType();
		int needLevel = itemData.getNeedLevel();
		if(needLevelType==1){
			if(player.getLevel()<needLevel){
				UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "角色等级需到达:"+needLevel+"级");
				return false;
			}
		} 
		return true;
	}
}
