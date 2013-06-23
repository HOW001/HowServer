package server.cmds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import server.ServerEntrance;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.Util;
import world.World;
import db.model.Item;
import db.model.Player;
import util.binreader.ChatForbiden;
import util.binreader.GameParameterData;
import util.binreader.ItemData;
import util.binreader.PromptData;
import util.logger.MoneyLogger;

/**
 * @author liuzg
 * 聊天模块
 */
public class ChatCP extends CmdParser implements Runnable {
	private static Logger logger = Logger.getLogger(ChatCP.class);
	public static AtomicLong linkId = new AtomicLong(0);
	public static final byte CHANNEL_NONE = 0;// 保留
	public static final byte CHANNEL_BROADCAST= 1;// 广播
	public static final byte CHANNEL_WORLD = 2;// 世界
	public static final byte CHANNEL_PRIVATE = 3;// 私聊
	public static final byte CHANNEL_SYSTEM = 4;// 系统
	public static final byte CHANNEL_GOLD = 5;// 消费金钱提示
	public static final byte CHANNEL_CLAN = 6;// 工会
	public static final byte CHANNEL_DESIGN=7;//策划表提示信息
	
	
	/**
	 * 各个频道的发消息间隔时间
	 */
	private static final int[] CHAT_INTERVAL = { 0,// 保留
		    30000,// 广播
			5000,// 世界
			3000,// 私聊
			0,// 系统
			0,//消费金钱提示
			0,//公会
	};
	public static final int CMD_CHAT = 0x0001;// 聊天

//	public static final int SEND_WORLD_OR_BORADCAR_USE_MONEY=GameParameterData.channel_broadcast;//世界消息和广播需要消费的钱币
	
	private static final int MAX_MSG_LENGTH=500;//最大信息长度
	
	private static final int MSG_COUNT=1000;//信息列表长度
	
	public static final int MIN_ALLPLAYERINFO_TIME=1000;//最小全服信息间隔,1秒
	private static  long LAST_SEND_ALLPLAYER_INFO_TIME=0;
	private static MsgOutEntry om =null;
	private static BlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
	public static final int ANTI_SPAM_TIME=30000;//防刷屏时间30秒
	
	/*
	 * PK广场中的玩家列表
	 */
	private static final CopyOnWriteArrayList<Integer> PKHomePlayerList=new CopyOnWriteArrayList<Integer>();
	/*
	 * PK擂台中的玩家列表
	 */
	public static final CopyOnWriteArrayList<Integer> PKRankPlayerList=new CopyOnWriteArrayList<Integer>();
	private boolean flag = true;
	private final static ChatCP instance= new ChatCP();
	public static boolean isCloseSysChat=false;
	public static ChatCP getInstance() {
		return instance;
	}
	private ChatCP() {
		super(TYPE_CHAT);
		init();
	}
	public void init() {
		ServerEntrance.runThread(this);
		
	}
	public void exit() {
		this.flag = false;
	}
	/**
	 * 发送循环消息
	 * 
	 * @param p
	 * @param message
	 */
	public void sendCycle(Player p, String message,List<LinkMessage> links) {
		if (message == null){
			return;
		}	
		if (message.length() == 0){
			return;
		}
		addMessage(0, "", p, 0, CHANNEL_BROADCAST, message,links);
	}
//    private long count=0;
	public void run() {
		long times = System.currentTimeMillis();
		ServerEntrance.threadPool.setThreadName(Thread.currentThread().getName(), "ChatMessage");
		while (flag) {
			try {
				Message message = messages.take();
				if (message == null) {
//					count=0;
//					Thread.sleep(1000);
					continue;
				}
				send(message);
			} catch (Exception e) {
				logger.error(this + "send error!", e);
			}
		}
		long useTimes = System.currentTimeMillis() - times;
		if(useTimes>=100){
			logger.error("201210181846线程运行时间过长" + useTimes);
		}
	}

	public void parse(NioSession session, int packCommand, byte[] bytes) {
		try {
			ByteArray ba = new ByteArray(bytes);
			Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
			if (player == null) {
				logger.info("player is null in ChatCP");
				return;
			}
			logger.debug(player.getName()+"请求命令:"+Integer.toHexString(packCommand));
			switch (getCommand(packCommand)) {
			case CMD_CHAT:
				byte channel = ba.readByte();// 频道
				int targetId = ba.readInt();// 目标玩家ID
				String msg = ba.readUTF();// 聊天内容
				if(!player.canChat()){//
					UISystemCP.sendFlutterMessageForWarn(player.getIoSession(), "禁言中，暂时不能说话");
//					sendMessage(player, targetId, channel, "您被禁言了，暂时不能说话！");
					return;
				}
				if (msg.length() > MAX_MSG_LENGTH*10) {
					logger.error(player.getName()+"聊天内容过长，已被踢出游戏！");
					UISystemCP.openDialog(session,"你的信息长超过"+(MAX_MSG_LENGTH*10)+"个字符,已被踢出游戏!");
					World.kickoutPlayer(player, null);
					return;
				}
				if(msg.length()>MAX_MSG_LENGTH){
					PromptData prompt=PromptData.getDataById(91);
					if(prompt!=null){
						UISystemCP.sendMessageForType(player.getIoSession(), prompt.type,prompt.msg,prompt.id,new String[]{""});
					}
					return;
				}
				if(isCanSendMsg(player, channel, msg)){
					sendMessage(player, targetId, channel, msg);
				}
				break;
			default:
				logger.info(player + "unknow cmd in ChatCP:"
						+ getCommand(packCommand) + "[" + packCommand + "]");
				break;
			}
		} catch (Exception e) {
			logger.error("聊天命令解析异常:",e);
		}
	}
	/**
	 * @author liuzg
	 * @param player
	 * @param channel
	 * 是否能发送信息，需要满足相关条件
	 */
	private boolean isCanSendMsg(Player player, byte channel, String msg) {
		/*
		 * 广播时间间隔限制
		 */
		if(channel==CHANNEL_BROADCAST){
			long intervalTime=CHAT_INTERVAL[CHANNEL_BROADCAST];
			
			if(System.currentTimeMillis()-player.lastBroadCastChatTime<intervalTime){			
				UISystemCP.sendFlutterMessageForNull(player.getIoSession(), "您说话太快了,歇一会儿吧");
				return false;
			}
			player.lastBroadCastChatTime=System.currentTimeMillis();
		}
		/*
		 * 世界时间间隔限制
		 */
		if(channel==CHANNEL_WORLD){
			long intervalTime=CHAT_INTERVAL[CHANNEL_WORLD];
			
			if(System.currentTimeMillis()-player.lastWorldChatTime<intervalTime){			
				UISystemCP.sendFlutterMessageForNull(player.getIoSession(), "您说话太快了,歇一会儿吧");
				return false;
			}
			if(System.currentTimeMillis()-player.lastWorldChatTime<ANTI_SPAM_TIME){
				if(player.lastWorldChatInfo.equals(msg)){
					ChatCP.sendSystemMessage(player, "30秒内请不要重复发送相同的内容");
					return false;
				}
			}
			player.lastWorldChatTime=System.currentTimeMillis();
			player.lastWorldChatInfo=msg;
		}
		/*
		 * 广播和世界消息需要收费
		 */
		if(channel==CHANNEL_BROADCAST){
//			if(!player.checkGold(GameParameterData.channel_broadcast)){
//				UISystemCP.sendFlutterMessageForNull(player.getIoSession(), "金钱不足无法发送消息");
//				return false;
//			}
//			if(player.payGold(GameParameterData.channel_broadcast,MoneyLogger.moneyDeduct[13])){
////				MoneyLogger.deductMoneyLog(player, MoneyLogger.moneyDeduct[13],GameParameterData.channel_broadcast);
//			}
		}
		/*
		 * 玩家不能发送系统消息
		 */
		if(channel==CHANNEL_SYSTEM){
			UISystemCP.sendFlutterMessageForNull(player.getIoSession(), "玩家无法发送系统消息");
			return false;
		}
		/*
		 * 频道错误
		 */
		if (channel < CHANNEL_NONE || channel > CHANNEL_CLAN) {
			logger.error(player.getName()+"发送频道错误 " + channel);
			return false;
		}
		return true;
	}
	/**
	 * @author liuzg
	 * @param fromPlayer
	 * @param targetId
	 * @param channel
	 * @param message
	 * 发送消息
	 */
	private void sendMessage(Player fromPlayer, int targetId, byte channel,String message) {
		if ((message == null || message.trim().equals(""))) {
			return;
		}
		switch (channel) {
		case CHANNEL_NONE: // 保留频道
			break;
		case CHANNEL_PRIVATE:// 私聊频道
			sendWhisperChannel(fromPlayer, targetId, message,null);
			break;
		case CHANNEL_BROADCAST: // 广播频道
			sendBroadCastMessage(fromPlayer, message,null);
			break;	
		case CHANNEL_WORLD: // 世界频道
			sendWorldChannel(fromPlayer, message,null);
			break;
		case CHANNEL_CLAN: // 工会频道
			sendClanChannel(fromPlayer, true, message,null);
			break;
		case CHANNEL_GOLD: // 金钱提示频道
			sendGoldMessage(message);
			break;
		
		default:
			break;
		}
	}

	/**
	 * 给一群玩家发消息
	 * 
	 * @param senderID
	 * @param senderName
	 * @param repClass
	 * @param channel
	 * @param receiver
	 * @param message
	 */
	private void sendMessageToPlayer(Player sender, byte channel,
			String message, List<LinkMessage> links) {
		if (messages.size() > MSG_COUNT||System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME) {
			logger.info(sender.getName() + "发送的广播消息被过滤:" + message);
			getInstance().addMessage(sender.getGameID(), sender.getName(),
					 sender, sender.getIDByGameID(),
					CHANNEL_WORLD, message, links);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		for (Player p : World.players.values()) {
			addMessage(sender.getGameID(), sender.getName(),
					 p, p.getGameID(), channel, message,
					links);
		}
	}

	/**
	 * @author lzg
	 * @param fromPlayer
	 * @param showID
	 * @param message
	 *            公会
	 */
	private void sendClanChannel(Player fromPlayer, boolean showID,String message,List<LinkMessage> links) {
		logger.info(fromPlayer.getName() + "[公会]频道喊话：" + message);
		addMessage(0, "", fromPlayer, fromPlayer.getGameID(), CHANNEL_CLAN,"你没有加入任何氏族",links);
	}

	/**
	 * @author lzg
	 * @param fromPlayer
	 * @param targetId
	 * @param message
	 *            私聊
	 */
	private void sendWhisperChannel(Player fromPlayer, int targetId, String message,List<LinkMessage> links) {
		Player targetPlayer = World.getPlayer(targetId);		
		if (targetPlayer != null) {
			if(fromPlayer==targetPlayer){
				return;
			}
			//发给from
			addMessage(fromPlayer.getGameID(), fromPlayer.getName(),
					fromPlayer, targetPlayer.getGameID(), CHANNEL_PRIVATE,
					message,links);
			logger.info(fromPlayer.getName() + "对" + targetPlayer.getName()
					+ "[私聊]" + message);
			//发给target
			addMessage(fromPlayer.getGameID(), fromPlayer.getName(),
					targetPlayer, targetPlayer.getGameID(), CHANNEL_PRIVATE,
					message,links);
		}
	}

	/**
	 * @author lzg 2010-7-19
	 * @param message
	 * @param player
	 * GM发送私聊信息
	 */
	public static void sendPrivateMessage(String message, Player player){
		getInstance().addMessage(0, "私聊", player, player.getId(), CHANNEL_PRIVATE, message, null);
	}
	/**
	 * 某玩家在世界频道发送消息
	 * 
	 * @param p
	 * @param message
	 */
	private void sendWorldChannel(Player p, String message,List<LinkMessage> links) {
		if (p == null) {
			return;
		}
		logger.info(p.getName()+"通过世界频道喊话:"+message);
		sendMessageToPlayer(p, CHANNEL_WORLD, message,links);
	}
	/**
	 * @author lzg
	 * @param fromId
	 *            发出ID
	 * @param fromName
	 *            发出昵称
	 * @author fromVipLevel
	 *            发出者VIP等级，系统或广播为-1
	 * @param targetPlayer
	 *            接收玩家
	 * @param toid
	 *            接收ID
	 * @param channel
	 *            消息类型
	 * @param message
	 *            消息内容
	 */
	private void addMessage(int fromId, String fromName, Player targetPlayer,
			int toid, byte channel, String message,List<LinkMessage> links) {
		try {
			if (targetPlayer == null) {
				return;
			}
			NioSession targetHandler = targetPlayer.getIoSession();
			if (targetHandler == null) {
				return;
			}
			
			if ((message == null || message.trim().equals(""))) {
				return;
			}
			if (channel != CHANNEL_SYSTEM
					&& channel != CHANNEL_BROADCAST ) {
				message = ChatForbiden.getCheckedString(message);
//				logger.info("--------------------message:"+message);
			}
//			logger.info(fromId+"发送信息给:"+targetPlayer.getName()+",channel="+channel+","+message);
			Message msg = new Message(targetHandler, fromId, toid, channel,
					fromName, message,links);
			messages.put(msg);
		} catch (Exception e) {
			logger.error(fromId + " send message InterruptedException", e);
		}
	}

	
	/**
	 * @author liuzg
	 * @param message
	 * @param links
	 * 给所有玩家发送交易信息
	 */
	public static void sendGoldMessage(String message) {
//		for (Player player : World.players.values()) {
//			getInstance().addMessage(0, "交易",-1, player, player.getId(), CHANNEL_GOLD, message,null);
//		}
	}
	/**
	 * @author lzg
	 * @param session
	 * @param name
	 * @param message
	 * 玩家给所有玩家发送广播信息
	 */
	public static void sendBroadCastMessage(Player fromPlayer, String message,
			List<LinkMessage> links) {
		if (fromPlayer == null) {
			return;
		}
		if (messages.size() > MSG_COUNT||System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME) {
			logger.info(fromPlayer.getName() + "发送的广播消息被过滤:" + message);
			getInstance().addMessage(fromPlayer.getGameID(),
					fromPlayer.getName(), fromPlayer,
					fromPlayer.getIDByGameID(), CHANNEL_BROADCAST, message,
					links);
			return;
		}
		logger.info(fromPlayer.getName()+"通过广播频道喊话:"+message);
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		for (Player toPlayer : World.players.values()) {
			getInstance()
					.addMessage(fromPlayer.getGameID(), fromPlayer.getName(),
						   toPlayer,
							toPlayer.getIDByGameID(), CHANNEL_BROADCAST,
							message, links);
		}
	}
	
	/**
	 * @author lzg
	 * @param session
	 * @param name
	 * @param message
	 * 系统给所有玩家发送广播信息
	 */
	public static void sendBroadCastMessageFromGM(String message,List<LinkMessage> links) {
		if(messages.size()>MSG_COUNT){
			logger.info("系统给所有玩家发送广播消息被过滤:"+message);
			return;
		}
		if(System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME){
			logger.info("未满足全服信息间隔而被过滤4:"+message);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		if (ChatCP.isCloseSysChat==false) {
			for (Player toPlayer : World.players.values()) {
				getInstance().addMessage(0, "", toPlayer,
						toPlayer.getIDByGameID(), CHANNEL_BROADCAST, message,
						links);
			}
		} else {// 超过100人后走随机发布
			List<Integer> playerIDs = World.getInstance().getSomePlayerID(0);
			for (int playerID : playerIDs) {
				Player p = World.getPlayer(playerID);
				if (p != null) {
					getInstance().addMessage(0, "", p, p.getIDByGameID(),
							CHANNEL_BROADCAST, message, links);
				}
			}
		}
	}
	
   /**
    * @author liuzg
    * @param target
    * @param items 物品信息
    * @param message "您获得了:"
    */
   public static void sendItemToPlayer(Player target,List<Item> items,String message){
	   List<LinkMessage> links=new ArrayList<LinkMessage>();
	   for(Item item:items){
		   LinkMessage link=new LinkMessage(item.getCode(),LinkMessage.TYPE_LINK_ITEM,item.getItemData(item.getCode()).getName(),item.getItemData(item.getCode()).getName());
		   links.add(link);
	   }
	   getInstance().addMessage(0, "系统",target, 0, CHANNEL_SYSTEM,
				message,links);
   }
	/**
	 * @author lzg
	 * @param session
	 * @param message
	 * 给指定玩家发送系统信息
	 */
	public static void sendSystemMessage(Player target, String message) {
		if (target == null){
			return;
		}
//		LinkMessage link=new LinkMessage(1,TYPE_LINK_PLAYER,"刘志刚","刘志刚");
//		links=new ArrayList<LinkMessage>();
//		links.add(link);
		getInstance().addMessage(0, "系统", target, 0, CHANNEL_SYSTEM,
				message,null);
	}
	/**
	 * @author lzg
	 * @param message
	 * 给所有玩家发送世界信息
	 */
	public static void sendWorldMsgToAllPlayerFromGame(String message) {
		if(message.length()>MAX_MSG_LENGTH/2){
			logger.error("世界信息超过最大信息限制被过滤:"+message);
			return;
		}
		if(messages.size()>MSG_COUNT){
			logger.info("给所有玩家发送世界消息被过滤:"+message);
			return;
		}
		if(System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME){
			logger.info("未满足全服信息间隔而被过滤3:"+message);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		if (ChatCP.isCloseSysChat==false) {
			for (Player player : World.players.values()) {
				getInstance().addMessage(0, "世界", player, player.getId(),
						CHANNEL_WORLD, message, null);
			}
		} else {// 超过100人后走随机发布
			List<Integer> playerIDs = World.getInstance().getSomePlayerID(0);
			for (int playerID : playerIDs) {
				Player p = World.getPlayer(playerID);
				if (p != null) {
					getInstance().addMessage(0, "世界", p, p.getId(),
							CHANNEL_WORLD, message, null);
				}
			}
		}
	}
	/**
	 * @author liuzg
	 * @param message
	 * 发送信息给所有玩家，来自系统
	 */
	public static void sendSystemMsgToAllPlayerFromGame(String message){
		if(message.length()>MAX_MSG_LENGTH/2){
			logger.error(message.length()+"系统信息超过最大信息限制被过滤:"+message);
			return;
		}
		if(messages.size()>MSG_COUNT){
			logger.info(message.length()+"发送全服系统广播消息被过滤:"+message);			
			return;
		}
		if(System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME){
			logger.info("未满足全服信息间隔而被过滤2:"+message);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		if (ChatCP.isCloseSysChat==false) {
			for (Player toPlayer : World.players.values()) {
				getInstance().addMessage(0, "系统", toPlayer, 0,
						CHANNEL_SYSTEM, message, null);
			}
		} else {// 超过100人后走随机发布
			List<Integer> playerIDs = World.getInstance().getSomePlayerID(0);
			for (int playerID : playerIDs) {
				Player p = World.getPlayer(playerID);
				if (p != null) {
					getInstance().addMessage(0, "系统", p, 0, CHANNEL_SYSTEM,
							message, null);
				}
			}
		}
	}
	/**
	 * @author liuzg
	 * @param message
	 * 发送策划提示信息
	 */
	public static void sendDesignMsgToAllPlayerFromGame(int type,String message){
		if(message.length()>MAX_MSG_LENGTH/2){
			logger.error("策划信息超过最大信息限制被过滤:"+message);
			return;
		}
		if(messages.size()>MSG_COUNT){
			logger.info("发送全服系统广播消息被过滤:"+message);			
			return;
		}
		if(System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME){
			logger.info("未满足全服信息间隔而被过滤1:"+message);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		if (ChatCP.isCloseSysChat==false) {
			switch(type){
			case 5://世界消息
			case 6://系统消息
			case 8://PK信息
				for (Player toPlayer : World.players.values()) {
					getInstance().addMessage(0, "策划", toPlayer, 0,
							CHANNEL_DESIGN, message, null);
				}
				break;
			}
			
		} else {// 超过100人后走随机发布
			List<Integer> playerIDs = World.getInstance().getSomePlayerID(0);
			for (int playerID : playerIDs) {
				Player p = World.getPlayer(playerID);
				if (p != null) {
					getInstance().addMessage(0, "策划", p, 0,
							CHANNEL_DESIGN, message, null);
				}
			}
		}
	}
	/**
	 * @author liuzg
	 * @param loginPlayer
	 * @param msg
	 * 发送玩家登录信息，由系统自动发送
	 */
	public void sendPlayerLoginInfoToAll(Player loginPlayer,String message){
		if(message.length()>MAX_MSG_LENGTH/2){
			logger.error("登录信息超过最大信息限制被过滤:"+message);
			return;
		}
		if(messages.size()>MSG_COUNT){
			logger.info("发送全服系统广播消息被过滤:"+message);			
			return;
		}
		if(System.currentTimeMillis()-LAST_SEND_ALLPLAYER_INFO_TIME<MIN_ALLPLAYERINFO_TIME){
			logger.info("未满足全服信息间隔而被过滤４:"+message);
			return;
		}
		LAST_SEND_ALLPLAYER_INFO_TIME=System.currentTimeMillis();
		if (ChatCP.isCloseSysChat==false) {
			for (Player toPlayer : World.players.values()) {
//				getInstance().addMessage(0, "策划", -1, toPlayer, 0,
//						CHANNEL_DESIGN, message, null);
				addMessage(loginPlayer.getGameID(), loginPlayer.getName(),
					  toPlayer, toPlayer.getGameID(), CHANNEL_WORLD, message,
						null);
				
			}
		} else {// 超过100人后走随机发布
			List<Integer> playerIDs = World.getInstance().getSomePlayerID(0);
			for (int playerID : playerIDs) {
				Player p = World.getPlayer(playerID);
				if (p != null) {
//					getInstance().addMessage(0, "策划", -1, p, 0,
//							CHANNEL_DESIGN, message, null);
					addMessage(loginPlayer.getGameID(), loginPlayer.getName(),
						   p, p.getGameID(), CHANNEL_WORLD, message,
							null);
				}
			}
		}
	}
	private static int count=0;
	private static long msgSize=0;
	private static long time=System.currentTimeMillis();
	/**
	 * @author lzg
	 * @param m
	 * @throws IOException
	 *             发送信息的最后一步
	 */
	private void send(Message m) throws IOException {		
		ByteArray ba=new ByteArray();
		ba.writeInt(getCmd(CMD_CHAT));
		ba.writeByteArray(m.getMessageData());
		om = new MsgOutEntry(m.targetsession);
		om.flush(ba.toArray());
		msgSize+=m.getMessageData().length;
		m = null;
		count++;
		if(System.currentTimeMillis()-time>Util.ONE_MIN){
			time=System.currentTimeMillis();
			logger.info("每分钟发送聊天信息:"+count+"条,"+"信息量:"+msgSize+"bytes");
			count=0;
			msgSize=0;
		}
	}

	public String toString() {
		return "Chat Thread";
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {
		
		
	}
	/**
	 * @author liuzg
	 * @param p
	 * @return
	 * 玩家名称着色
	 */
	public static String getPlayerNameForColor(Player p){
//		String herf="<u><a href=\"event:"+LinkMessage.TYPE_LINK_PLAYER+"|"+p.getGameID()+"|"+p.getName()+"\">"+p.getName()+"</a></u>  , ";
//		return "<font color='#67FFFA'>"+herf+"</font>";
		String herf="<a href=\'event:"+LinkMessage.TYPE_LINK_PLAYER+"|"+p.getGameID()+"|"+p.getName()+"\'>"+p.getName()+"</a>";
		return "<font color='#67FFFA'>"+herf+"</font>";
	}
	/**
	 * @author liuzg
	 * @param desc
	 * @return
	 * 描述信息着色
	 */
	public static String getDescForColor(String desc){
		return "<font color='#67AAAA'>"+desc+"</font>";
	}
	
	/**
	 * @author liuzg
	 * @param p
	 * @return
	 * 道具名称着色
	 */
	public static String getItemNameForColor(ItemData item){
//		String herf="<u><a href=\"event:"+LinkMessage.TYPE_LINK_PLAYER+"|"+p.getGameID()+"|"+p.getName()+"\">"+p.getName()+"</a></u>  , ";
//		return "<font color='#67FFFA'>"+herf+"</font>";
		String herf="<a href=\'event:"+LinkMessage.TYPE_LINK_ITEM+"|"+item.id+"|"+item.getName()+"\'>"+item.getName()+"</a>";
		return "<font color='#67AAAA'>"+herf+"</font>";
	}
	
	/**
	 * @author liuzg
	 * @param p
	 * @return
	 * 跳转链接
	 */
	public static String getJumpLinkForColor(PromptData prompt,Player player){
		if(prompt==null){
			return "";
		}
		if(prompt.jumpType!=0){
			if(prompt.jumpType==7){
				return "";
			}else if(prompt.jumpType==13){
				if(player==null){
					return "";
				}
				String herf="<u><a href=\'event:"+prompt.jumpType+"|"+player.getId()+"|"+prompt.jumpInfo+"\'>"+prompt.jumpInfo+"</a></u>";
				return "<font color='#908CCA'>"+herf+"</font>";
			}else{
				String herf="<u><a href=\'event:"+prompt.jumpType+"|"+prompt.jumpId+"|"+prompt.jumpInfo+"\'>"+prompt.jumpInfo+"</a></u>";
				return "<font color='#908CCA'>"+herf+"</font>";
			}
		}else{
			return "";
		}
	}
	
	/**
	 * @author liuzg
	 * @param p
	 * @return
	 * 跳转链接
	 */
	public static String getJumpLinkForColor(PromptData prompt,int playerID){
		if(prompt==null){
			return "";
		}
		if(prompt.jumpType!=0){
			if(prompt.jumpType==4){
				String herf="<u><a href=\'event:"+prompt.jumpType+"|"+playerID+"|"+prompt.jumpInfo+"\'>"+prompt.jumpInfo+"</a></u>";
				return "<font color='#908CCA'>"+herf+"</font>";
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	
	/**
	 * @author liuzg
	 * @param p
	 * @return
	 * 跳转链接(加入团队赛)
	 */
	public static String getJumpLinkForMap(PromptData prompt,int teamID){
		if(prompt==null){
			return "";
		}
		if(prompt.jumpType!=0){
			if(prompt.jumpType==14){
				String herf="<u><a href=\'event:"+prompt.jumpType+"|"+teamID+"|"+prompt.jumpInfo+"\'>"+prompt.jumpInfo+"</a></u>";
				return "<font color='#908CCA'>"+herf+"</font>";
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	
	/**
	 * @author liuzg
	 * @param chapterType 赛道类型
	 * @return
	 * 赛道跳转链接
	 */
	public static String getJumpLinkForMap(int chapterType){
		if(chapterType==0){
			return "";
		}
//		if(chapterType == MapCP.DRIVER_PRACTICE_MAP){
//			String herf="<u><a href=\'event:"+7+"|"+MapCP.DRIVER_PRACTICE_MAP+"|"+"去比赛"+"\'>"+"去比赛"+"</a></u>";
//			return "<font color='#908CCA'>"+herf+"</font>";
//		}else if(chapterType == MapCP.WORLD_TOUR_MAP){
//			String herf="<u><a href=\'event:"+7+"|"+MapCP.WORLD_TOUR_MAP+"|"+"去比赛"+"\'>"+"去比赛"+"</a></u>";
//			return "<font color='#908CCA'>"+herf+"</font>";
//		}else if(chapterType == MapCP.HELL_CHALLENGE_MAP){
//			String herf="<u><a href=\'event:"+7+"|"+MapCP.HELL_CHALLENGE_MAP+"|"+"去比赛"+"\'>"+"去比赛"+"</a></u>";
//			return "<font color='#908CCA'>"+herf+"</font>";
//		}else if(chapterType == MapCP.GROUP_CHALLENGE_MAP){
//			String herf="<u><a href=\'event:"+7+"|"+MapCP.GROUP_CHALLENGE_MAP+"|"+"去比赛"+"\'>"+"去比赛"+"</a></u>";
//			return "<font color='#908CCA'>"+herf+"</font>";
//		}
		return "";
	}
	
	/**
	 * @author liuzg
	 * @param p
	 * 将玩家添加至PK广场
	 */
    public void addPKHome(Player p){
    	if(p==null){
			return;
		}
    	if(PKHomePlayerList.contains(p.getId())==false){
    		PKHomePlayerList.add(Integer.valueOf(p.getId()));
    		logger.info(p.getName()+"进入PK广场");
    	}
    }
    /**
     * @author liuzg
     * @param p
     * 将玩家添加至PK擂台
     */
    public void addPKRank(Player p){
    	if(p==null){
			return;
		}
    	if(PKRankPlayerList.contains(p.getId())==false){
    		PKRankPlayerList.add(Integer.valueOf(p.getId()));
    		logger.info(p.getName()+"进入PK擂台");
    	}
    }
	/**
	 * @author liuzg
	 * @param p
	 * 将玩家移除PK广场
	 */
	public void removePKHome(Player p){
		if(p==null){
			return;
		}		
		PKHomePlayerList.remove(Integer.valueOf(p.getId()));
		logger.info(p.getName()+"请求退出PK广场");
	}
	/**
	 * @author liuzg
	 * @param p
	 * 请求PK擂台信息
	 */
	public void removePKRank(Player p){
		if(p==null){
			return;
		}		
		PKRankPlayerList.remove(Integer.valueOf(p.getId()));
		logger.info(p.getName()+"请求退出PK擂台");
	}
}

/**
 * 消息封装类
 * 
 * @author lzg
 * 
 */
class Message {
//    private static Logger logger=Logger.getLogger(Message.class);
	public NioSession targetsession;
	public int fromid;
	public int toid;
	public byte channel;
	public String name;
	public String message;
	
	public List<LinkMessage> links;
	// 发给多个人时候的缓冲
	private byte[] datas;

	public Message(NioSession targetsession, int fromid, int toid,
			byte channel, String name, String message,List<LinkMessage> links) {
		this.targetsession = targetsession;
		this.fromid = fromid;
		this.toid = toid;
		this.channel = channel;
		this.name = name;
		this.message = message;
		this.links=links;
		if (this.name == null) {
			this.name = "";
		}
	}

	public byte[] getMessageData() {
		if (datas == null) {
			initMessageData();
		}
		return datas;
	}

	private void initMessageData() {
		ByteArray ba = new ByteArray();
		ba.writeByte(channel);
		ba.writeInt(fromid);
		ba.writeInt(toid);
		ba.writeUTF(name);
		if(links!=null && links.size()>0){
		   StringBuffer sb=new StringBuffer();
		   sb.append(message);
		   for(LinkMessage link:links){
			   sb.append(link.toString()+"  , ");	   
		   }
		   ba.writeUTF(sb.substring(0,sb.length()-2).toString());
//		   logger.info(name+"向前端发送信息:"+sb.substring(0,sb.length()-2).toString());
		}else{
		   ba.writeUTF(message);
//		   logger.info(name+"向前端发送信息2:"+message);
		}
		datas = ba.toArray();
	}
	
}
/**
 * @author liuzg
 * 超链信息
 */
class LinkMessage{
	public static final int TYPE_LINK_PLAYER=1;//人物超链
	public static final int TYPE_LINK_ITEM=2;//物品连接
	public static final int TYPE_LINK_JUMP=3;//界面跳转连接
	public int id;//连接索引，如玩家ID,物品ID,任务ID
	public int type;//超链类型
	public String name;//名称
	public String desc;//超链信息
	public LinkMessage(int id,int type,String name,String desc){
		this.id=id;
		if(name==null){
			name="";
		}
		this.name=name;
		this.type=type;
		this.desc=desc;
	}
	public String toString(){
		return "<u><a href=\"event:"+type+"|"+id+"|"+name+"\">"+desc+"</a></u>";
	}
	
}
