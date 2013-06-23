package db.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import server.ServerConfigurationNew;
import server.cmds.ChatCP;
import server.cmds.MapCP;
import server.cmds.PlayerCP;
import server.cmds.UISystemCP;
import util.MathUtils;
//import util.QQPlateOperation;
import util.SMTPSender;
import util.Util;
import util.binreader.LivenessListData;
import util.binreader.ParamPlayerBaseData;
import util.binreader.PromptData;
import util.logger.LoginLogger;
import util.logger.MoneyLogger;
import world.Scene;
import world.World;
import world.object.AutoFightManager;
import world.object.Creature;
import db.service.DBPlayerImp;
import db.service.IDManager;
import db.service.ManagerDBUpdate;

/**
 * 
 * @author lzg 玩家实体
 */
public class Player extends Creature implements DataBaseEntry {
	public static final String PLAYERKEY = "PLAYER";
	public static final String USERKEY = "USER";
	/*
	 * 体力恢复间隔
	 */
	public static final long ACTIONRECOVERYINTERVAL = Util.ONE_MIN * 30;
	/*
	 * 体力恢复数量
	 */
	public static final int ACTIONRECOVERYNUM = 5;
	/*
	 * 最大体力点数
	 */
	public static final int MAXACTIONVALUE = 200;
	private int version;
	/**
	 * 领导力
	 */
	private int leaderShip = 0;

	/**
	 * 体力
	 */
	private int actionValue = 0;

	/**
	 * 最后体力的恢复时间
	 */
	private long lastActionRecoveryTime = 0;// 最后体力的恢复时间

	/**
	 * 当前使用的绝技
	 */
	private int currentSkill = 0;

	/**
	 * 当前技能值
	 */
	private int currentSkillValue = 0;

	/**
	 * 玩家总在线时长
	 */
	private long onLineTime = 0;
	/**
	 * 记录注册此玩家的时间，方便统计
	 */
	private Date registerTime;

	/**
	 * 玩家权限等级
	 */
	protected byte playerLevel = LEVEL_PLAYER;
	/**
	 * 用户名
	 */
	public String userName;
	/**
	 * 注册IP
	 */
	public String registerIP;
	/**
	 * 玩家上次离线时间
	 */
	private long lastLogoutTime;
	/**
	 * 签到日期
	 */
	private int signInDay;
	/**
	 * 安装渠道
	 */
	public String install;

	/**
	 * 当日在线时间
	 */
	private long dailyOnLineTime;

	/**
	 * 上次登录时间
	 */
	private Date lastLoginTime;
	/**
	 * 连续登陆天数
	 */
	private int continueDays;

	/**
	 * 当前拥有的绝技,以逗号分隔
	 */
	private String skills;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public long getLastLogoutTime() {
		return lastLogoutTime;
	}

	public void setLastLogoutTime(long lastLogoutTime) {
		this.lastLogoutTime = lastLogoutTime;
	}

	public long getDailyOnLineTime() {
		return dailyOnLineTime;
	}

	public void setDailyOnLineTime(long dailyOnLineTime) {
		this.dailyOnLineTime = dailyOnLineTime;
	}

	public long getOnLineTime() {
		return onLineTime;
	}

	public void setOnLineTime(long onLineTime) {
		this.onLineTime = onLineTime;
	}

	public Date getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public void setPlayerLevel(byte level) {
		playerLevel = level;
	}

	public byte getPlayerLevel() {
		return playerLevel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getInstall() {
		return install;
	}

	public void setInstall(String install) {
		this.install = install;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public int getContinueDays() {
		return continueDays;
	}

	public void setContinueDays(int continueDays) {
		this.continueDays = continueDays;
	}

	public void addContinueDays(int addContinueDays) {
		this.continueDays += addContinueDays;
		logger.info(this.getName() + "连续登陆：" + this.continueDays + "天");
	}

	/**
	 *************************** 非数据库属性信息********************************
	 */
	private boolean isChangeInfo = false;
	// public boolean isYouKe=false;//是否是游客状态
	private static Logger logger = Logger.getLogger(Player.class);// 日志
	public static byte LEVEL_BANNED = 0;// 被封的号
	public static byte LEVEL_PLAYER = 2;// 普通玩家

	public static final int MAXLEVEL = 50;
	// boss赛ID
	public long bossContestID = 0;

	public boolean isLogout = false;
	public long lastSaveDBTime = 0;

	public long DBExceptionTime = 0;
	public byte isDBException = 0;// 0无状态
									// 1第一次发生状态，不通知玩家下线　2第二次发生状态通知玩家下线　3已通知玩家下线

	public boolean isSaveState = false;

	private Hero[] currentFightHero = new Hero[10];// 当前战斗中卡牌的位置,1...9
	/**
	 * 玩家关联实体信息
	 */
	private Set<Hero> heroEntry = new HashSet<Hero>();// 英雄实体
	private PlayerPack playerPackEntry = null;// 背包
	private PlayerNonBasicInfo playerNonBasicInfoEntry = null;// 玩家非基本信息实体
	private PlayerCompetitionInfo playerCompetitionInfoEntry = null;// 玩家比赛完成信息实体
	private Liveness livenessEntry = null;// 开服活动实体
	private List<Mail> mails = null;// 玩家的邮件
	private JiTan jitan = null;// 祭坛实体

	public List<Mail> getMails() {
		return mails;
	}

	public void setMails(List<Mail> mails) {
		this.mails = mails;
	}

	public PlayerCompetitionInfo getPlayerCompetitionInfoEntry() {
		return playerCompetitionInfoEntry;
	}

	private void setPlayerCompetitionInfoEntry(PlayerCompetitionInfo playerCompetitionInfoEntry) {
		this.playerCompetitionInfoEntry = playerCompetitionInfoEntry;
	}

	/**
	 * 关联玩家非基本信息实体
	 */
	public PlayerNonBasicInfo getPlayerNonBasicInfoEntry() {
		return playerNonBasicInfoEntry;
	}

	private void setPlayerNonBasicInfoEntry(PlayerNonBasicInfo playerNonBasicInfoEntry) {
		this.playerNonBasicInfoEntry = playerNonBasicInfoEntry;
	}

	public PlayerPack getPlayerPackEntry() {
		return playerPackEntry;
	}

	/**
	 * @author liuzg 仅Hibernate允许访问
	 */
	private void setPlayerPackEntry(PlayerPack playerPackEntry) {
		this.playerPackEntry = playerPackEntry;
	}

	public Liveness getLivenessEntry() {
		return livenessEntry;
	}

	public void setLivenessEntry(Liveness livenessEntry) {
		this.livenessEntry = livenessEntry;
	}

	/**
	 * 玩家关联实体信息
	 */
	/**
	 * 允许掉线的时间，
	 */
	public static final long DISCONNECT_LOGOUT = 1000 * 60 * 1;// 3分钟
	/**
	 * 上次登录时间
	 */
	public long loginTime = 0;

	public long lastBroadCastChatTime = 0;
	public long lastWorldChatTime = 0;
	public long closeChatLastTime = 0;
	public long closeChatTime = 0;
	public String lastWorldChatInfo = "";
	public long lastAuctionTime = 0;
	public boolean isOpenActivity = false;// 玩家是否有新的活动(默认为没有)

	public long getCloseChatLastTime() {
		return closeChatLastTime;
	}

	public void setCloseChatLastTime(long closeChatLastTime) {
		this.closeChatLastTime = closeChatLastTime;
	}

	public long getCloseChatTime() {
		return closeChatTime;
	}

	public void setCloseChatTime(long closeChatTime) {
		this.closeChatTime = closeChatTime;
	}

	public boolean canChat() {
		if (this.closeChatTime + this.closeChatLastTime <= System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	private NioSession session;
	/**
	 * 是否需要更新角色基础信息
	 */
	private boolean needSendStatusChange = true;

	public void setNeedSendStatusChange(boolean needSendStatusChange) {
		this.needSendStatusChange = needSendStatusChange;
	}

	protected int loginLevel;
	protected long loginXP;
	private int teamID;
	/*
	 * 是否已经完成登录流程
	 */
	public boolean isCompleteLogin = false;

	public int getTeamID() {
		return teamID;
	}

	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}

	public void setState(byte state) {
		super.setState(state);
	}

	/**
	 * 发送系统消息给客户端
	 * 
	 * @param msg
	 */
	public void sendSystemMsg(String msg) {
		if (msg == null) {
			return;
		}
		if (session == null) {
			return;
		}
		ChatCP.sendSystemMessage(this, msg);
	}

	private Player() {
		super(TYPE_PLAYER);
		this.playerLevel = LEVEL_PLAYER;
	}

	/**
	 * 生成游戏ID
	 */
	protected void generateGameID() {
		gameID = generateGameID(getId());
	}

	/**
	 * 生成GameID
	 * 
	 * @param id
	 * @return
	 */
	public static int generateGameID(int id) {
		return id | 0x10000000;
	}

	/**
	 * 通过gameID获得roleID
	 * 
	 * @param gameID
	 * @return
	 */
	public static int getRoleID(int gameID) {
		return gameID & 0x0FFFFFFF;
	}

	public NioSession getIoSession() {
		return session;
	}

	public void setIoSession(NioSession session) {
		this.session = session;

	}

	/**
	 * 通知客户端target的HP变化情况 此函数会发送掉血和招架躲闪情况
	 */
	public void notifiedHPChange(Creature target, int hpChange, byte attackTable, boolean isSkill) {
	}

	/**
	 * @author liuzg 信息更改
	 */
	public void changeInfo() {
		isChangeInfo = true;
	}

	/**
	 * @author liuzg 更新相关信息
	 */
	public void notifiedInfoChange() {
		PlayerCP.getInstance().updatePlayerMessage(this);
	}

	/**
	 * 换地图
	 * 
	 * @param toMap
	 */
	public boolean transforMap(Scene toMap) {
		if (toMap.playerFull(this)) {
			if ((ticker & 0x03) == 0x03) {
				logger.info(this + "传送到" + toMap + "失败：地图已满");
			}
			sendResult("地图已满，请稍候尝试");
			return false;
		}
		if (currentScene != null) {
			currentScene.removeCreature(this);
		}
		setCurrentScene(toMap);
		setSceneID(toMap.getSceneID());
		currentScene.addCreature(this);
		return true;
	}

	/**
	 * 玩家的经验改变
	 * 
	 * @param xp
	 *            增加的经验
	 * @param isNeed
	 *            是否需要提示 false:不需要提示
	 * @return
	 */
	public synchronized boolean addXp(long xp, boolean isNeed) {
		if (xp <= 0) {
			return false;
		}
		logger.info(name + "增加经验:" + xp);
		currentExp += xp;
		int levelValue = countLevel();
		if (levelValue > 0) {
			levelUp(levelValue);
		}
		this.changeInfo();
		if (isNeed) {
			PromptData promptData = PromptData.getDataById(105);
			if (promptData != null) {
				String msg = UISystemCP.getInstance().getResultMsg(promptData.msg, xp + "");
				UISystemCP.sendMessageForType(this.getIoSession(), promptData.type, msg, promptData.id,
						new String[] { xp + "" });
			}
		}
		return true;
	}

	/**
	 * 玩家的经验改变
	 * 
	 * @param xp
	 */
	public synchronized boolean addXp(long xp) {
		return addXp(xp, true);
	}

	/**
	 * 计算当前经验能升多少级
	 * 
	 * @return
	 */
	private int countLevel() {
		int levelValue = 0;
		for (int i = 0; i < MAXLEVEL; i++) {
			if (currentExp >= getLevelUpExp(i)) {
				levelValue += 1;
				currentExp -= getLevelUpExp(i);
			} else {
				break;
			}
		}
		return levelValue;
	}

	/**
	 * 升级
	 * 
	 * @return
	 */
	public boolean levelUp(int addLevel) {
		if (addLevel <= 0) {
			return false;
		}
		if (level >= MAXLEVEL) {
			return false;
		}
		if (addLevel >= 0) {
			if (level + addLevel >= MAXLEVEL) {
				level = MAXLEVEL;
			} else {
				level += addLevel;
			}
		}
		if (level >= MAXLEVEL) {
			SMTPSender.sendMail("upLevel", "人物达到等级上限:[服务器]" + ServerConfigurationNew.id + "\r\n" + "[" + getId() + "]"
					+ name + "升级到了" + MAXLEVEL + "级。");
		}
		logger.info(this + "升到了" + level + "级,当前经验：" + currentExp);
		// if (this.playerCompetitionInfoEntry.getGuideCompleteState()>=17) {//
		// 强化引导完成才有
		restoreLife();// 满血
		// }
		this.changeInfo();
		return true;
	}

	public String getMapName() {
		if (currentScene == null)
			return "未知地图";
		return currentScene.getName();
	}

	/**
	 * 返回升级所需经验
	 * 
	 * @return
	 */
	public long getLevelUpExp(int addLevel) {
		ParamPlayerBaseData param = ParamPlayerBaseData.getDataForLevel(level + addLevel);
		if (param == null) {
			return 100000L;
		}
		return param.max_exp;
	}

	/**
	 * 返回本级已获取经验
	 * 
	 * @return
	 */
	public int getCurrentExp() {
		return currentExp;
	}

	public long lastLinkTickTime = System.currentTimeMillis();// 连接心跳时间
	private static long MAX_LINK_TIME_INTERVAL = Util.ONE_MIN *3;
	private boolean isSendWelcome = true;

	/**
	 * @author liuzg 玩家心跳
	 */
	public void tick() {
		try {
			if(isLogout){
				World.addToBuffer(this);
				logger.info(this.getName()+"退出游戏,将其移入缓冲区!");
				return;
			}
			if (needSendStatusChange && ((ticker & 0x00003) == 3)) {
				needSendStatusChange = false;
				long useTime = System.currentTimeMillis() - this.getLastActionRecoveryTime();// 已过去的时间
				if (useTime >= ACTIONRECOVERYINTERVAL) {
					// 体力恢复
					this.addActionValue(ACTIONRECOVERYNUM);
					this.setLastActionRecoveryTime(System.currentTimeMillis());
					this.isChangeInfo = true;
				}
			}
			if (isChangeInfo) {
				isChangeInfo = false;
				notifiedInfoChange();
			}
			if (isDBException == 2) {
				UISystemCP.openDialog(session, "您的角色在例行更新时出现异常,我避免您的更大损失,请暂时退出游戏,10分钟后可再次登录,并及时联系客服人员!");
				isDBException++;
			}
			if (isDBException == 3) {
				this.logout("DB存储出现异常");
				isDBException = 0;
			}
			if (System.currentTimeMillis() - lastLinkTickTime > MAX_LINK_TIME_INTERVAL) {
				this.logout(this.getName() + "心跳超时");
				lastLinkTickTime = System.currentTimeMillis();
			}
			if (isSendWelcome) {
				sendSystemMsg("你好" + ChatCP.getPlayerNameForColor(this) + "," + World.anounceMent + "!当前游戏在线人数"
						+ (World.players.size() * MathUtils.random(2, 8)) + "人!");
				isSendWelcome = false;
			}

		} catch (Exception e) {
			logger.error(name + "心跳异常:", e);
		}
	}

	public void restoreLife() {
		super.restoreLife();
	}

	/**
	 * @author liuzhigang
	 * @param userName
	 * @return 创建游客角色
	 */
	public static Player create(String userName) {
		int id = IDManager.getInstance().getCurrentPlayerID();
		Player p = new Player();
		p.setId(id);
		p.setName("HOW" + id);
		p.setGender(1);
		p.setUserName(userName);
		p.setLevel(1);
		p.setRegisterIP(p.getClientIP());
		p.setRegisterTime(new Date());
		p.setSceneID(MapCP.MAIN_MAP);
		p.setLastLogoutTime(System.currentTimeMillis());
		p.setLeaderShip(0);
		p.setActionValue(0);
		p.setAgile(0);
		p.setCurrentSkill(0);
		p.setCurrentSkillValue(0);
		// p.isYouKe=true;
		if (DBPlayerImp.getInstance().saveInitPlayer(p) == false) {// 存数据库
			logger.error(userName + "在新建角色时出现异常:name=" + userName);
			return null;
		}
		LoginLogger.registerInfo(p);// 注册日志
		logger.info("新建角色:" + p.getName());
		return p;
	}

	/**
	 * @author liuzg
	 * @param name
	 * @param gender
	 * @param race
	 * @param profession
	 * @return 创建角色
	 */
	public static Player createPlayer(String userName, String name, int gender) {
		try {
			Player p = create(userName);
			p.setName(name);
			p.setGender(gender);
			return p;
		} catch (Exception e) {
			logger.error("创建角色时出现异常:", e);
			return null;
		}
	}

	/**
	 * 玩家第一次登陆要做的初始化动作
	 */
	public void initFirstLogin() {
		try {
			logger.info(this + "第一次登陆到游戏世界，初始化数据");
			this.actionValue = 100;
		} catch (Exception e) {
			logger.info("出现异常,关闭session...");
			getIoSession().close();
		}
	}

	/**
	 * @author liuzg 建立玩家时初始化各个数据库实体
	 *         此处在登录时进行相关判断，是为了保证数据的完整性，因为在玩家建立之后，在其他信息保存至数据库之前，如果突然宕机，会造成数据损坏
	 */
	public void initDBEntry() {

		if (heroEntry == null) {
			/* 初始化英雄实体 */
//			Hero heroEntry = Hero.create();
//			heroEntry.initDBEntry(this);
//			this.addHeroEntry(heroEntry);
			heroEntry = new HashSet<Hero>();
			/* 初始化英雄实体 */
		}
		
		//测试使用
        if(heroEntry.size()==0){
        	for(int index=1;index<=7;index++){
        		Hero hero=Hero.create(10000+index);
        		hero.initDBEntry(this);
        		this.addHeroEntry(hero);
        	}
        }else{
        	int i=1;
        	Iterator<Hero> it=heroEntry.iterator();
        	List<Hero> removeHero=new ArrayList<Hero>();
        	while(it.hasNext()){
        		Hero hero=it.next();
        		if(i%2==0){
        			removeHero.add(hero);
        		}
        		i++;
        	}
        	for(Hero hero:removeHero){
        		heroEntry.remove(hero);
        	}
        	removeHero.clear();
        	removeHero=null;
        }
		if (playerPackEntry == null) {
			/* 初始化背包实体 */
			PlayerPack pack = PlayerPack.create();
			pack.initDBEntry(this);
			this.setPlayerPackEntry(pack);
		}

		if (playerNonBasicInfoEntry == null) {
			/* 初始化玩家非基本信息实体 */
			playerNonBasicInfoEntry = PlayerNonBasicInfo.create();
			playerNonBasicInfoEntry.initDBEntry(this);
			this.setPlayerNonBasicInfoEntry(playerNonBasicInfoEntry);
			/* 初始化玩家非基本信息实体 */
		}
		if (playerCompetitionInfoEntry == null) {
			/* 初始化玩家比赛完成信息实体 */
			playerCompetitionInfoEntry = PlayerCompetitionInfo.create();
			playerCompetitionInfoEntry.initDBEntry(this);
			this.setPlayerCompetitionInfoEntry(playerCompetitionInfoEntry);
			/* 初始化玩家比赛完成信息实体 */
		}
		if (livenessEntry == null) {
			/* 初始化活跃度实体 */
			Liveness liveness = Liveness.create();
			StringBuffer sb = new StringBuffer();
			for (LivenessListData livenessListData : LivenessListData.getData().values()) {
				if (livenessListData == null) {
					continue;
				}
				sb.append(livenessListData.id).append("#").append("0").append(",");
			}
			String result = sb.substring(0, sb.length() - 1);
			liveness.initDBEntry(this, result, true);
			this.setLivenessEntry(liveness);
		}
		if (jitan == null) {
			/* 初始化祭坛实体 */
			jitan = JiTan.create();
			jitan.initDBEntry(this);
			this.setJitan(jitan);
		}
	}

	public void setGameId(int gameId) {
		this.gameID = gameId;
	}

	/**
	 * 玩家死亡
	 */
	public void die() {
		if (state != STATE_DEAD) {
			logger.info(name + "已死亡");
			super.die();
		}
	}

	/**
	 * 角色死亡公告
	 */
	public void announceDead() {

	}

	/**
	 * 发送弹板信息
	 * 
	 * @param message
	 */
	public void sendResult(String message) {
		try {
			UISystemCP.openDialog(getIoSession(), message);
		} catch (Exception e) {
			logger.error("发送消息：" + this + "失败", e);
		}
	}

	public boolean isLogining = false;

	/**
	 * 登录
	 * 
	 * @param user
	 * @param index
	 * @param session
	 */
	public void login(NioSession session) {
		try {
			if (isLogining) {
				return;
			}
			isLogining = true;
			if (session != null) {
				logger.info(this.getName() + "开始处理玩家登录信息,session:" + session);
				session.setAttribute(PLAYERKEY, this);
				this.setIoSession(session);
			}
			/*
			 * 进行一次数据完整性检测
			 */
			initDBEntry();

			World.addToPlayer(this);
			if (getOnLineTime() == 0) {// 获取上次登录时间，判断是否是第一次登陆
				initFirstLogin();
				onLineTime++;// 防止重复判断
			}
			initLogin();
			tickFighting();
			lastTickTime = System.currentTimeMillis();
			this.setSceneID(MapCP.MAIN_MAP);
			loginTime = World.getInstance().getCurrentTime();// 记录登录时间
			if (this.getPlayerNonBasicInfoEntry() != null) {
				this.getPlayerNonBasicInfoEntry().convertToList();
			}
			if (this.getPlayerCompetitionInfoEntry() != null) {
				this.getPlayerCompetitionInfoEntry().convertToList();
			}
			World.login(this);
			logger.info("玩家<" + this.getName() + ">登录服务器完成");
			LoginLogger.loginInfo(this);
			SMTPSender.sendMail("Login", Util.getCurrentAllTime() + "---" + this.getName() + "登录了一次"
					+ ServerConfigurationNew.id + "服务器!");
			isLogout = false;
			this.restoreLife();
			PlayerCP.getInstance().initPlayerStatus(this);
			isCompleteLogin = true;
			World.LOGIN_PLAYERS_COUNT++;
			isSendWelcome = true;
			isLogining = false;
			loginLevel = level;
			loginXP = currentExp;

			ManagerDBUpdate.ISLOGININGPLAYER.remove(userName);
			AutoFightManager.logoutAutoFight(this);
			lastAttendContestTime = -1;
			setLastLogoutTime(System.currentTimeMillis());
			sendResult("已正式进入游戏...");
		} catch (Exception e) {
			logger.error(this + " login()", e);
			session.close();
			isLogining = false;
		}
	}

	/**
	 * 初始化登陆相关
	 * 
	 * @param role
	 */
	private void initLogin() {
		/** 登录时初始化各关联实体，主要设置各个关联对象的Player属性，以此来弥补去除多对一关联后的损失 */

		try {
			if (playerPackEntry != null) {// 背包
				playerPackEntry.setHolder(this.getId());
			}
			if (playerNonBasicInfoEntry != null) {// 玩家非基本信息实体
				playerNonBasicInfoEntry.setHolder(this.getId());
			}
			if (playerCompetitionInfoEntry != null) {// 玩家比赛完成信息实体
				playerCompetitionInfoEntry.setHolder(this.getId());
			}
			if (livenessEntry != null) {// 活跃度
				livenessEntry.initLiveness();
			}
			if (jitan != null) {
				jitan.setHolder(this.getId());
			}

		} catch (Exception e) {
			logger.error(this.getName() + "初始化登录信息时出现异常:", e);
			UISystemCP.openDialog(session, "初始化登录信息时出现错误,请联系客服解决!");
			logger.error(session + "初始化登录异常,关闭session...");
			this.getIoSession().close();
		}
		if (this.getActionValue() > MAXACTIONVALUE) {
			// 修正玩家最大体力值,主要针对下线后由系统增加的体力
			this.setActionValue(MAXACTIONVALUE);
		}
		// 初始化体力恢复
		if (this.getLastActionRecoveryTime() > 0) {
			long useTime = System.currentTimeMillis() - this.getLastActionRecoveryTime();// 已过去的时间
			long addActionValue = useTime / ACTIONRECOVERYINTERVAL * ACTIONRECOVERYNUM;// 可以恢复多少体力
			this.addActionValue((int) addActionValue);
			long residueTime = useTime % ACTIONRECOVERYINTERVAL;// 剩余时间,即不满足间隔时间
			this.setLastActionRecoveryTime(System.currentTimeMillis() - residueTime);
		}
	}

	/**
	 * @author lzg------2010-10-11 玩家退出游戏
	 */
	public void logout(String msg) {
		try {
			long time = (World.getInstance().getCurrentTime() - loginTime);
			logoutLogger(time);// 日志记录
			if (getCurrentScene() != null) {
				getCurrentScene().removeCreature(this);
			}
			setOnLineTime(getOnLineTime() + time);
			isLogout = true;
		} catch (Exception e) {
			logger.error("玩家退出出错" + this, e);
		}
		if (session != null) {
			session.removeAttribute(PLAYERKEY);
		}
		if (this.isDBException == 3) {
			World.addToBuffer(this);
			logger.error(name + "退出游戏,原因:" + msg);
			if (session != null) {
				LoginLogger.logoutInfo(this);
				session = null;
			}
		}
	}

	private void logoutLogger(long time) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<" + this.getName()).append("> 本次登录时长：").append(time / 1000).append("秒,").append(",级别变化:")
					.append(loginLevel).append("-->").append(level + ",经验变化：").append(loginXP + "-->" + currentExp);
			logger.info(sb.toString());
		} catch (Exception e) {
			logger.error(this + "退出日志记录异常", e);
		}
	}

	@Override
	public void initDBEntry(Player p) {
	}

	@Override
	public String toString() {
		return "玩家ID:" + this.getId() + ",gameID=" + this.getGameID() + ",name:" + this.getName() + ",level:"
				+ this.level + ",exp:" + this.getCurrentExp();
	}

	public String getRegisterIP() {
		return registerIP;
	}

	public void setRegisterIP(String registerIP) {
		this.registerIP = registerIP;
	}
	public int getSignInDay() {
		return signInDay;
	}

	public void setSignInDay(int signInDay) {
		this.signInDay = signInDay;
	}

	public void closeChat(long lastTime) {
		setCloseChatTime(System.currentTimeMillis());
		setCloseChatLastTime(lastTime);
	}

	/**
	 * @author liuzg 产生DB异常
	 */
	public void createDBException(String desc) {
		logger.error(getName() + "您的角色在例行更新时出现异常,为避免您的更大损失,请及时退出游戏,并及时联系客服人员!" + desc);
		// UISystemCP.sendResult(session,
		// "您的角色在例行更新时出现异常,为避免您的更大损失,请暂时退出游戏,15分钟后可再次登录,并及时联系客服人员!");
		isDBException++;
		DBExceptionTime = System.currentTimeMillis();
	}

	/**
	 * @author liuzg 更新玩家的乐观锁标识，用于更新时同步玩家上线
	 */
	public void updateVersion() {
		logger.error(getName() + "更新完成之后玩家上线了");
		int version = DBPlayerImp.getInstance().getPlayerVersion(getId());
		if (version > 0) {
			logger.info(getName() + "从数据库中获取新的version=" + version + ",currentVersion=" + getVersion());
			setVersion(version);
		}
	}

	/**
	 * @author liuzg
	 * @return 获取客户端的原始IP地址
	 * 
	 */
	public String getClientIP() {
		// /123.179.226.239:2794
		try {
			if (session == null) {
				return "123.179.226.239";
			}
			if(session.getRemoteAddress()==null){
				return "127.0.0.1";
			}
			String ip = session.getRemoteAddress().toString();
			ip = ip.substring(1);
			ip = ip.split(":")[0];
			return ip;
		} catch (Exception e) {
			logger.error("ip解析异常:", e);
			return "123.179.226.239";
		}
	}

	public Set<Hero> getHeroEntry() {
		return heroEntry;
	}
    
	public void setHeroEntry(Set<Hero> heroEntry) {
		this.heroEntry = heroEntry;
	}

	public boolean addHeroEntry(Hero hero) {
		if (heroEntry == null) {
			heroEntry = new HashSet<Hero>();
		}
		heroEntry.add(hero);
		setHeroEntry(heroEntry);
		return true;
	}
    
	public Hero getHeroEntry(int id) {
		Iterator<Hero> it = heroEntry.iterator();
		while (it.hasNext()) {
			Hero hero = it.next();
			if (hero.getId() == id) {
				return hero;
			}
		}
		return null;
	}
    
	public int getLeaderShip() {
		return leaderShip;
	}

	public void setLeaderShip(int leaderShip) {
		this.leaderShip = leaderShip;
	}

	public int getActionValue() {
		return actionValue;
	}

	public void setActionValue(int actionValue) {
		this.actionValue = actionValue;
	}

	public void addActionValue(int actionValue) {
		this.actionValue += actionValue;
		if (this.actionValue > MAXACTIONVALUE) {
			this.actionValue = MAXACTIONVALUE;
		}
	}

	public int getCurrentSkill() {
		return currentSkill;
	}

	public void setCurrentSkill(int currentSkill) {
		this.currentSkill = currentSkill;
	}

	public int getCurrentSkillValue() {
		return currentSkillValue;
	}

	public void setCurrentSkillValue(int currentSkillValue) {
		this.currentSkillValue = currentSkillValue;
	}

	public long getLastActionRecoveryTime() {
		return lastActionRecoveryTime;
	}

	public void setLastActionRecoveryTime(long lastActionRecoveryTime) {
		this.lastActionRecoveryTime = lastActionRecoveryTime;
	}

	private String getSkills() {
		return skills;
	}

	private void setSkills(String skills) {
		this.skills = skills;
	}

	public String[] getSkillList() {
		return skills.split(",");
	}

	public void addSkill(String skill) {
		if (this.skills.length() > 0) {
			this.skills = this.skills + "," + skill;
		} else {
			this.skills = skill;
		}
	}

	public JiTan getJitan() {
		return jitan;
	}

	public void setJitan(JiTan jitan) {
		this.jitan = jitan;
	}

	public Hero[] getCurrentFightHero() {
		return currentFightHero;
	}

	public void setCurrentFightHero(int pos, Hero hero) {
		if (pos >= 1 && pos <= 9) {
			currentFightHero[pos] = hero;
		}
	}
	/**
	 * @author liuzhigang
	 * @param hero
	 * @return
	 * 获取英雄上阵位置
	 */
	public int getHeroPostion(Hero hero){
		for(int index=1;index<=9;index++){
			if(currentFightHero[index]!=null &&  currentFightHero[index].getId()==hero.getId()){
				return index;
			}
		}
		return 0;
	}

	@Override
	public int isHaveAttackSkill() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int isHaveShieldSkill() {
		// TODO Auto-generated method stub
		return 0;
	}
}