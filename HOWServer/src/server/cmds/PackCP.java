package server.cmds;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import db.model.Item;
import db.model.Player;
import db.model.PlayerPack;
//import db.service.DBShopImp;

import server.netserver.MsgOutEntry;
//import server.netserver.SessionAttributeEntry;
import util.ByteArray;
import util.binreader.GameParameterData;
import util.binreader.PromptData;
//import world.World;
/**
 * 背包模块
 * @author fengmx
 */
public class PackCP extends CmdParser{
	private static Logger logger = Logger.getLogger(PackCP.class);
	private static final byte pack_item_list_by_type = 0x0001;//显示背包内物品列表      发送多个物品的信息
	private static final byte pack_update = 0x0002;//更新指定位置的物品信息
	private static final byte pack_sell = 0x0003;//出售物品
	private static final byte pack_move = 0x0004;//移动   
	private static final byte pack_splite = 0x0005;//拆分
	private static final byte pack_itemUse = 0x0006;//物品使用
	private static final byte pack_tidy = 0x0007;//整理背包
	private static final byte pack_isOpen = 0x0008;//背包自动补给功能是否开启
	private static final byte pack_extend = 0x0009;//扩充
	public PackCP() {
		super(TYPE_PACK);
	}
	private static PackCP instance;
	public static PackCP getInstance(){
		if(instance == null){
			instance = new PackCP();
		}
		return instance;
	}
	@Override
	public void parse(NioSession session, int command, byte[] bytes) {
		try {
			Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
			if(player==null){
				return;
			}
			PlayerPack playerPack = player.getPlayerPackEntry();
			ByteArray ba = new ByteArray(bytes);
			switch (getCommand(command)) {
			case pack_item_list_by_type:
//			byte type = ba.readByte();
				sendPackView(session,playerPack);
//				logger.error("sdfs");
				break;
			case pack_sell:
				int index = ba.readInt();
				int number = ba.readInt();
				if(playerPack.sellItem(index,number)){
					PackCP.getInstance().updateItemMessage(session, playerPack);
				} else {
					UISystemCP.openDialog(session, "出售物品失败");
				}
				break;
			case pack_move:
				int oldIndex = ba.readInt();
				int newIndex = ba.readInt();
				if(playerPack.moveItem(oldIndex, newIndex)){
					PackCP.getInstance().updateItemMessage(session, playerPack);
					if(oldIndex==playerPack.getOpenIndex()){
						playerPack.setOpenIndex(newIndex);
						sendIsOpenMessage(session, player);
					}else if(newIndex==playerPack.getOpenIndex()){
						playerPack.setOpenIndex(oldIndex);
						sendIsOpenMessage(session, player);
					}
				}
				break;
			case pack_splite:
				oldIndex = ba.readInt();
				newIndex = ba.readInt();
				number = ba.readInt();
				if(playerPack.spliteItem(oldIndex,newIndex,number)){
					PackCP.getInstance().updateItemMessage(session, playerPack);
				}
				break;
			case pack_itemUse:
				index = ba.readInt();
				if(playerPack.useItem(index)){
				}
				break;
			case pack_tidy:
				playerPack.tidyPack();
//				player.getPlayerPackEntry().addItem(70801, 1, 11);
//				playerPack.addItem(70802, 1, 11);
//				playerPack.addItem(30303, 1, 11);
//				playerPack.addItem(30401, 1, 11);
//				playerPack.addItem(30501, 1, 11);
				break;
			case pack_isOpen:
				byte isOpen = ba.readByte();
				if(isOpen!=0&&isOpen!=1){
					logger.error("角色名："+player.getName()+"，方法名：parse"+",无效的背包开关：" + player.getName());
					break;
				}
				if(playerPack.openPack(isOpen)){
					sendIsOpenMessage(session, player);
				}
				break;
			case pack_extend:
				int extendNumber = ba.readInt();
				if(playerPack.extendNumber(extendNumber)){
					PromptData promptData = PromptData.getDataById(68);
					if(promptData!=null){
						UISystemCP.sendMessageForType(session, promptData.type, promptData.msg,promptData.id,new String[]{""});
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("背包命令解析异常:",e);
		}
	}
	/**
	 * 扩充背包
	 * @param session
	 * @param player
	 */
	public void sendCurrentPackNumber(NioSession session, Player player) {
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(PackCP.pack_extend));
			PlayerPack playerPack = player.getPlayerPackEntry();
			if(playerPack==null){
				logger.error(player.getName()+"无背包实体");
				return;
			}
			ba.writeInt(playerPack.getCurrentPackNumber());
			ba.writeInt(playerPack.getMaxPackSize());
			sendData(session, ba);
		} catch (Exception e) {
			logger.error("发送背包数量信息异常:", e);
		}
	}
	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {
		
	}
	/**
	 * 初始化角色背包
	 */
	public void sendPackView(NioSession session, PlayerPack playerPack) {
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(PackCP.pack_item_list_by_type));
			ba.writeByte(playerPack.getIsOpen());
			if(playerPack.getIsOpen() == 0){
				ba.writeInt(playerPack.getOpenIndex());
			} else{
				ba.writeInt(-1);
			}
			ba.writeInt(playerPack.getCurrentPackNumber());
			ba.writeInt(playerPack.getMaxPackSize());
			List<Item> itemsList= playerPack.getPackItemsList();
			if(itemsList == null){
				ba.writeShort(0);
			} else {
				ba.writeShort(itemsList.size());
				for(Item item: itemsList){
					if(item == null){
						continue;
					}
					if(!item.hasItem()){
						continue;
					}
					ba.writeInt(item.getIndexId());
					item.writeItemMessage(ba);
				}
			}
			ba.writeInt(GameParameterData.extend_number);
			ba.writeInt((int)GameParameterData.pack_value1);
			ba.writeInt((int)GameParameterData.pack_value1);
			sendData(session, ba);
		} catch (Exception e) {
			logger.error("发送背包物品数据异常", e);
		}
	}
	public void sendIsOpenMessage(NioSession session,Player player){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(PackCP.pack_isOpen));
			int isOpen = player.getPlayerPackEntry().getIsOpen();
			ba.writeByte((byte)isOpen);
			if(isOpen == 0){
				ba.writeInt(player.getPlayerPackEntry().getOpenIndex());
			} else{
				ba.writeInt(1);
			}
			sendData(session, ba);
		} catch (Exception e) {
			logger.error("背包自动补给功能开启异常：" + player.getName(), e);
		}
	}
	/**
	 * 更新背包内多个物品信息
	 * @param session
	 * @param playerPack
	 */
	public void updateItemMessage(NioSession session, PlayerPack playerPack) {
		updateItemMessage(session, playerPack, -1);
	}
	/**
	 * 
	 * @param session
	 * @param playerPack
	 * @param itemPlace -1表示无物品来源，否则为物品来源的模块号
	 */
	public void updateItemMessage(NioSession session, PlayerPack playerPack,int itemPlace) {
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(PackCP.pack_update));
			List<Integer> updateIndexs = playerPack.getUpdateIndex();
			ba.writeShort(updateIndexs.size());
			for(int i=0;i<updateIndexs.size();i++){
				int index = updateIndexs.get(i);
				ba.writeInt(index);
				Item item = playerPack.getItemByIndex(index);
				if(item == null){
					ba.writeByte((byte)1);
					Item.writeNullMessage(ba);
				} else {
					ba.writeByte((byte)0);
					item.writeItemMessage(ba);
				}
			}
			ba.writeInt(itemPlace);
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om=null;
		} catch (Exception e) {
			logger.error("查询物品信息异常", e);
		}
	}
	/**
	 * 最终数据的发送
	 * @param session
	 * @param ba
	 */
	private void sendData(NioSession session, ByteArray ba){
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
		om=null;
	}
	
	/**
	 * @author liuzg
	 * 购买物品第2步
	 * 参见http://wiki.open.qq.com/wiki/API3.0%E6%96%87%E6%A1%A3
	 */
//	public void buyGoodsStep2(final Player p,final List<ItemData> buyItems){
//		Runnable buyGoodThread=new Runnable(){
//			public void run(){
//				QQPlateOperation qqpo=new QQPlateOperation(p);
//				Map<String,String> params=qqpo.getPayBuyGoods(buyItems,true);
//				if(params==null){
//					return;
//				}
//				if(params.get("ret")!=null && params.get("ret").equals("0")){
//					//TODO 根据返回结果，向前端发送token值
//					String token=params.get("token");
//					QQShopCP.getInstance().sendSetp4Message(p.getIoSession(), p, token);
//				}
//			}
//		};
//		ServerEntrance.runThread(buyGoodThread);
//	}
	/**
	 * @author liuzg
	 * @param p 确定买主
	 * @param params 详细信息
	 * 购买物品第7步，由腾讯平台回调
	 * 详情参见
	 *  http://wiki.open.qq.com/wiki/%E5%9B%9E%E8%B0%83%E5%8F%91%E8%B4%A7URL%E7%9A%84%E5%8D%8F%E8%AE%AE%E8%AF%B4%E6%98%8E_V3
	 */
//	public void buyGoodsStep7(final Player p,final Map<String,String> params){
//		if(params==null){
//			return;
//		}
//		Runnable buyGoodsThread=new Runnable(){
//			public void run(){
//				if(params.get("ret")!=null && params.get("ret").equals("0")){
//					String openID=p.openID;//从玩家身上取得
////					String openKey=p.openKey;//从玩家身上取得
////					String pf=p.platFrom;//从玩家身上取得
////					String pfkey=p.pfKey;//从玩家身上取得
////					String openID="aaa";//从玩家身上取得
////					String openKey="bbb";//从玩家身上取得
////					String pf="ccc";//从玩家身上取得
////					String pfkey="ddd";//从玩家身上取得
//					if(openID.equals(params.get("openid"))==false){
//						//平台返回openID与玩家自身ID不一致
//						return;
//					}
//					String payitem=params.get("payitem");
//					String token=params.get("token");
//					String billno=params.get("billno");
//					String providetype=params.get("providetype");
//					String amt=params.get("amt");
//					String payamt_coins=params.get("payamt_coins");
//					String pubacct_payamt_coins=params.get("pubacct_payamt_coins");
//					//购买物品第10步
//					QQPlateOperation qqpo=new QQPlateOperation(p);
//					Map<String,String> confirmParams=qqpo.getPayConfirmDelivery(payitem, token, billno, providetype, amt, payamt_coins, pubacct_payamt_coins);
//					if(confirmParams.get("ret")!=null && confirmParams.get("ret").equals("0")){
//						if(confirmParams.get("msg")!=null && confirmParams.get("msg").equals("OK")){
//							//TODO 购买成功，开始向玩家背包中添加购买物品
//							String[] items = payitem.split("\\*");
//							int itemCode = Integer.parseInt(items[0]);
//							int number = Integer.parseInt(items[2]);
//							PlayerPack playerPack = p.getPlayerPackEntry();
//							playerPack.addItem(itemCode, number,CmdParser.TYPE_QQ_SHOP);
//							ItemLogger.addItemLog(p,itemCode,number,ItemLogger.itemAdd[7],0);
//						}
//					}
//				}
//			}
//		};
//		ServerEntrance.runThread(buyGoodsThread);
//	}
}
