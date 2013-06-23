package server.cmds;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.Util;
import util.binreader.EventData;
import util.binreader.HeroData;
import util.binreader.PlayerExpData;
import world.event.Event_Login;
import world.event.SubjectManager;
import db.model.Hero;
import db.model.JiTan;
import db.model.Player;

public class PlayerCP extends CmdParser {
	private static Logger logger = Logger.getLogger(PlayerCP.class);
	private static final int PLAYER_INFO = 0x0001;// 玩家君主信息
	private static final int PLAYER_MESSAGE_UPDATE = 0x0002;// 更新君主的部分基本信息
	private static final int REQUEST_JITAN_INFO=0x0003;//请求祭坛信息
	private static final int REQUEST_REFRESH_JITAN=0X0004;//请求刷新祭坛
	private static final int PLAYER_SET_HERO_POSTION=0X0005;//玩家设置英雄战斗位置
	private static final int HERO_LIST=0X0006;//英雄列表
	private static final int HERO_INFO=0X0007;//英雄信息
	private static final int HERO_GIVE=0X0008;//英雄获得
	private static final int REQUEST_HERO_POSTION=0X0009;//获取英雄布阵信息
	
	
	private static final int PLAYER_PERDAY_SIGNIN=0X0098;//玩家每日签到
	private static final int PLAYER_IS_PERDAYSIGIN=0X0099;//玩家今日是否签到
	private static PlayerCP instance;
	public static PlayerCP getInstance() {
		if (instance == null) {
			instance = new PlayerCP();
		}
		return instance;
	}
	private PlayerCP() {
		super(TYPE_PLAYER);
	}
	public static int getCMD(int command) {
		return generateCMD(TYPE_PLAYER, command);
	}
	public void parse(NioSession session, int packCommand, byte[] bytes) {
		ByteArray ba = new ByteArray(bytes);
		Player p = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
		if(p==null){
			return;
		}
		try {
			switch (getCommand(packCommand)) {
				case REQUEST_JITAN_INFO://请求祭坛信息
					requestJiTanInfo(p);
					break;
				case REQUEST_REFRESH_JITAN://请求刷新祭坛
					requestRefreshJiTan(p,bytes);
					break;
				case PLAYER_SET_HERO_POSTION://玩家设置英雄布阵信息
					logger.info(p.getName()+"玩家设置英雄布阵信息!");
					requestSetHeroPostion(p,bytes);
					break;
				case HERO_LIST://英雄列表
					logger.info(p.getName()+"请求英雄列表");
					requestHeroList(p);
					break;
				case HERO_INFO://英雄信息
					int id=ba.readInt();
					logger.info(p.getName()+"请求英雄详细信息:"+id);
					requestHeroInfo(p,id);
					break;
				case REQUEST_HERO_POSTION://获取英雄布阵信息
					logger.info(p.getName()+"请求英雄布阵信息!");
					requestHeroPostion(p);
					break;
				case PLAYER_PERDAY_SIGNIN:// 每日签到
					perDaySignIn(p);
					break;
				case PLAYER_IS_PERDAYSIGIN:// 今日是否已签到
					isPerDaySignIn(p);
					break;
				default:
					break;
				}
		} catch (Exception e) {
			logger.error(p + "PlayerCP.parse() " + Integer.toHexString(packCommand), e);
		}
	}
	/**
	 * @author liuzhigang
	 * @param p
	 * 请求英雄布阵信息
	 */
	private void requestHeroPostion(Player p){
		responseHeroPostion(p);
	}
	/**
	 * @author liuzhigang
	 * @param p
	 * @param id
	 * 请求英雄信息
	 */
	private void requestHeroInfo(Player p,int id){
		Hero hero=p.getHeroEntry(id);
		if(hero==null){
			p.sendResult("不存在的英雄信息:id="+id);
			return;
		}
		ByteArray ba=new ByteArray();
		ba.writeInt(getCmd(HERO_INFO));
		byte[] heroBytes=getHeroBytes(p,hero);
		ba.writeByteArray(heroBytes);
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
 		om.flush(ba.toArray());
 		om = null;
	}
	/**
	 * @author liuzhigang
	 * @param p
	 * 请求英雄列表
	 */
	private void requestHeroList(Player p){
		ByteArray ba=new ByteArray();
		ba.writeInt(getCmd(HERO_LIST));
		Set<Hero> heroList=p.getHeroEntry();
		ba.writeShort(heroList.size());
		Iterator<Hero> it=heroList.iterator();
		while(it.hasNext()){
			Hero hero=it.next();
			byte[] heroBytes=getHeroBytes(p,hero);
			ba.writeByteArray(heroBytes);
		}
		MsgOutEntry om = new MsgOutEntry(p.getIoSession());
 		om.flush(ba.toArray());
 		om = null;
	}
	/**
	 * 英雄信息
	 * @param hero
	 * @return
	 */
	private byte[] getHeroBytes(Player p,Hero hero){
		HeroData data=HeroData.getHeroData(hero.getCode());
		if(data==null){
			data=HeroData.getHeroData(10001);
		}
		ByteArray ba=new ByteArray();
		ba.writeInt(hero.getId());
		ba.writeInt(hero.getCode());
		ba.writeInt(hero.getCurrentExp());
		ba.writeInt(hero.getLevel());
		ba.writeInt(data.color);//颜色
		ba.writeInt(1);//星级
		ba.writeInt(hero.getPower());
		ba.writeInt(hero.getAgile());
		ba.writeInt(hero.getMp());
		ba.writeInt(hero.getToughness());
		ba.writeInt(data.skill_id);//技能
		ba.writeInt(0);//攻击力
		ba.writeInt(0);//血量
		ba.writeInt(0);//暴击
		ba.writeUTF(data.icon);
		ba.writeInt(p.getHeroPostion(hero));//获取英雄上阵位置,0为未上阵
		
		return ba.toArray();
	}
	/**
	 * @author liuzhigang
	 * @param player
	 * @param bytes
	 * 请求设置卡牌战斗位置
	 */
	private void requestSetHeroPostion(Player player,byte[] bytes){
		ByteArray ba=new ByteArray(bytes);
		int size=ba.readByte();
		for(int index=1;index<=size;index++){
			int pos=ba.readByte();
			int heroID=ba.readInt();
			if(heroID<=0){
				player.setCurrentFightHero(pos, null);
			}else{
				Hero hero=player.getHeroEntry(heroID);
				player.setCurrentFightHero(pos, hero);
			}
		}
		responseHeroPostion(player);
	}
	/**
	 * @author liuzhigang
	 * @param player
	 * 响应当前战斗卡牌的位置
	 */
	public void responseHeroPostion(Player player){
		ByteArray ba=new ByteArray();
		ba.writeInt(getCmd(PLAYER_SET_HERO_POSTION));
		ba.writeByte(player.getCurrentFightHero().length-1);
		for(int index=1;index<player.getCurrentFightHero().length;index++){
			ba.writeByte(index);
			Hero hero=player.getCurrentFightHero()[index];
			if(hero!=null){
				ba.writeInt(hero.getId());
			}else{
				ba.writeInt(0);
			}
		}
		MsgOutEntry om = new MsgOutEntry(player.getIoSession());
 		om.flush(ba.toArray());
 		om = null;
	}
	/**
	 * @author liuzhigang
	 * @param player
	 * @param bytes
	 * 请求刷新祭坛
	 */
	private void requestRefreshJiTan(Player player,byte[] bytes){
		ByteArray ba=new ByteArray(bytes);
		int type=ba.readInt();//1001:白色 1002:绿色 1003:蓝色 1004:紫色
		JiTan jitan=player.getJitan();
		if(jitan.isCanFreeFresh(type)){
			//开始刷新并获得相关英雄
		}
		//发送祭坛信息
		requestJiTanInfo(player);
	}
	/**
	 * @author liuzhigang
	 * @param player
	 * 请求祭坛信息
	 */
	private void requestJiTanInfo(Player player){
		ByteArray ba =new ByteArray();
		ba.writeInt(getCmd(REQUEST_JITAN_INFO));
		JiTan jitan=player.getJitan();
		//白色上次刷新时间
		ba.writeUTF(Util.getDateFormatLong(jitan.getFreshTime(JiTan.FRESH_TYPE_WHITE)));
		//白色冷却剩余时间
		ba.writeInt(jitan.getFreshResidualTime(JiTan.FRESH_TYPE_WHITE));
		//绿色上次刷新时间
		ba.writeUTF(Util.getDateFormatLong(jitan.getFreshTime(JiTan.FRESH_TYPE_GREEN)));
		ba.writeInt(jitan.getFreshResidualTime(JiTan.FRESH_TYPE_GREEN));
		//蓝色
		ba.writeUTF(Util.getDateFormatLong(jitan.getFreshTime(JiTan.FRESH_TYPE_BLUE)));
		ba.writeInt(jitan.getFreshResidualTime(JiTan.FRESH_TYPE_BLUE));
		//紫色
		ba.writeUTF(Util.getDateFormatLong(jitan.getFreshTime(JiTan.FRESH_TYPE_PURPLE)));
		ba.writeInt(jitan.getFreshResidualTime(JiTan.FRESH_TYPE_PURPLE));
		MsgOutEntry om = new MsgOutEntry(player.getIoSession());
 		om.flush(ba.toArray());
 		om = null;
	}
	/**
	 * @author liuzg
	 * @param player
	 * 玩家今日是否已签到
	 */
	private void isPerDaySignIn(Player player){
		/*
		 * 是否已签到
		 * 0:未签到
		 * 1:已签到
		 * 2:活动已结束
		 */
		byte isSign=0;
		Calendar c=Calendar.getInstance();
    	if(c.get(Calendar.DAY_OF_YEAR)==player.getSignInDay()){
    		logger.info(player.getName()+"今日已经签到.....");
    		isSign=1;
    	}
    	if(SubjectManager.getInstance().isRunTime(EventData.EVENT_TYPE_LOGIN)==false){
    		logger.info(player.getName()+"今日签到活动已关闭");
    		isSign=2;
    	}
    	ByteArray ba=new ByteArray();
 		ba.writeInt(getCmd(PLAYER_IS_PERDAYSIGIN));
 		ba.writeByte(isSign);
 		MsgOutEntry om = new MsgOutEntry(player.getIoSession());
 		om.flush(ba.toArray());
 		om = null;
	}
	/**
	 * @author liuzg
	 * @param player
	 * 每日签到
	 */
	private void perDaySignIn(Player player){
		//触发登录事件
		if (SubjectManager.getInstance().isRunTime(EventData.EVENT_TYPE_LOGIN)) {
			new Event_Login(player, EventData.EVENT_TYPE_LOGIN);
		}
		ByteArray ba=new ByteArray();
		ba.writeInt(getCmd(PLAYER_PERDAY_SIGNIN));
		MsgOutEntry om = new MsgOutEntry(player.getIoSession());
		om.flush(ba.toArray());
		om = null;
	}
	/**
	 * @author liuzg
	 * @param p
	 * 仅登录时调用　
	 */
	public void initPlayerStatus(Player p) {
		if (p == null || p.getIoSession() == null) {
			return;
		}
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(PlayerCP.PLAYER_INFO));
		initPlayerInfo(p,ba);
		sendData(p.getIoSession(), ba);
	}
	/**
	 * @author liuzg
	 * @param player
	 * @param ba
	 */
	public void initPlayerInfo(Player player,ByteArray ba) {
		try {
			ba.writeInt(player.getGameID());//ID
			ba.writeUTF(player.getName());//名称
			ba.writeInt(player.getLevel());//等级
			ba.writeInt((int)Math.round(player.getCurrentExp()));//经验
			ba.writeInt(player.getGender());//性别
			ba.writeInt(player.getLeaderShip());//领导力
			ba.writeInt(player.getActionValue());//体力
			ba.writeInt(player.getCurrentSkill());//当前绝技
			ba.writeInt(player.getCurrentSkillValue());//当前绝技值
			int nextLevelExp=PlayerExpData.getPlayerExpData(player.getLevel()).exp;
			ba.writeInt(nextLevelExp);
			ba.writeInt(1);//vip等级
			} catch (Exception e) {
			logger.error(player + " error in send player xp", e);
		}
	}
	
		
			/**
	 * 更新角色部分基本信息
	 * @param session
	 * @param player
	 */
	public void updatePlayerMessage(Player player){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(PlayerCP.PLAYER_MESSAGE_UPDATE));
			ba.writeInt(player.getLevel());//等级
			ba.writeInt((int)Math.round(player.getCurrentExp()));//经验
			ba.writeInt(player.getGender());//性别
			ba.writeInt(player.getLeaderShip());//领导力
			ba.writeInt(player.getActionValue());//体力
			ba.writeInt(player.getCurrentSkill());//当前绝技
			ba.writeInt(player.getCurrentSkillValue());//当前绝技值
			int nextLevelExp=PlayerExpData.getPlayerExpData(player.getLevel()).exp;
			ba.writeInt(nextLevelExp);
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("更新角色基本信息异常：" + player.getName(), e);
		}
	}
	private void sendData(NioSession session, ByteArray ba){
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
		om = null;
	}
	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {

	}
}