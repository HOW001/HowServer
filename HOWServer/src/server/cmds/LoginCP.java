package server.cmds;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.log4j.Logger;
import server.ServerEntrance;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.Util;
import world.World;
import db.model.Player;
import db.service.DBPlayerImp;
import db.service.ManagerDBUpdate;

public class LoginCP extends CmdParser {

	public static final int PLAYER_TICK = 0x0001;// 检测玩家是否在线
	
	public static final byte ERROR_TYPE_PLAYER_NOT_EXSITE = 1;// 玩家不存在
	public static final byte ERROR_TYPE_PLAYER_BANNED = 2;// 角色被封停
	public static final byte ERROR_TYPE_PLAYER_CREATE = 3;// 创建角色失败
	private static Logger logger = Logger.getLogger(LoginCP.class);

	public static int loginNumber = 1;
	public static long loginUseTime = 1;
	private final static LoginCP instance = new LoginCP();

	public static LoginCP getInstance() {
		return instance;
	}

	private LoginCP() {
		super(TYPE_LOGIN);

	}

	public static int getCMD(int command) {
		return generateCMD(TYPE_LOGIN, command);
	}

	public void parse(final NioSession session, int packCommand, byte[] bytes) {
		try {
			switch (getCommand(packCommand)) {
			// case PLAYER_CHANGE_OFFER://玩家转正
			// requestPlayerChangeOffer(session,bytes);
			// break;
			// case CREAT_PLAYER: // 创建新玩家ok
			// registerPlayer(session, bytes);
			// break;
			case PLAYER_TICK:
				long tickTime = 0;
				if (session.getAttribute("tickTime") == null) {
					session.setAttribute("tickTime", System.currentTimeMillis());
				}
				tickTime = (Long) session.getAttribute("tickTime");
				if (System.currentTimeMillis() - tickTime > Util.ONE_MIN * 2) {
					logger.info(session + "收到一次心跳信息");
					session.setAttribute("tickTime", System.currentTimeMillis());
				}
				ByteArray ba = new ByteArray();
				ba.writeInt(getCmd(PLAYER_TICK));
				ba.writeInt(0);
				MsgOutEntry out = new MsgOutEntry(session);
				out.flush(ba.toArray());
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("LoginCP解析命令出现异常", e);
		}
	}

	/*
	 * 记录玩家的登录时间，主要防止两次线程登录
	 */
	private static ConcurrentHashMap<String, Long> lastLoginTime = new ConcurrentHashMap<String, Long>();

	/**
	 * 登录角色
	 * 
	 * @param session
	 * @param index
	 * @throws IOException
	 */
	public void parseLogin(final NioSession session, final String userName, final int playerID) {
		Runnable r = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				try {
					if (lastLoginTime.get(userName) != null) {
						if (System.currentTimeMillis() - lastLoginTime.get(userName) < 1000 * 15) {
							logger.error(userName + "两次登录间隔小于15秒,后一次不处理");
							return;
						}
					}
					lastLoginTime.put(userName, System.currentTimeMillis());
					if (ManagerDBUpdate.ISLOGININGPLAYER.contains(userName) == false) {
						// 添加到登录状态信息
						ManagerDBUpdate.ISLOGININGPLAYER.add(userName);
					}
					logger.info(userName + "选择角色登录");
					Player role = World.getInstance().loginFindBufferPlayer(playerID);// .getBufferPlayer(playerID);//
																						// 在游戏世界中查找
					if (role != null) {
						if (System.currentTimeMillis() - role.DBExceptionTime < Util.ONE_MIN * 10) {
							logger.info(role.getName() + "数据出现问题正在等待修复...");
							long time = (Util.ONE_MIN * 10) - (System.currentTimeMillis() - role.DBExceptionTime);
							UISystemCP.openDialog(session, "亲,系统正在处理您的数据异常,请您" + (time / Util.ONE_MIN) + "分钟后再次游戏!");
							World.addToBuffer(role);// 再次放回缓存
							return;
						}
						logger.info("从游戏世界和缓冲池中取得玩家角色：" + role);
						role.sendResult("有相同账号登录...");
						World.closeHandler(role.getIoSession(), "有相同玩家登陆，您被迫断线");
						logger.info("开始自动登录玩家:name=" + role.getName());
						loginToServer(session, role);
					} else {
						Player loginRole = World.getPlayer(playerID);
						if (loginRole == null) {
							loginRole = DBPlayerImp.getInstance().getInitPlayer(playerID);
							logger.info("从数据库取得角色" + loginRole);
						} else {
							logger.info("从缓存中取得角色" + loginRole);
						}
						if (loginRole == null) {
							UISystemCP.openDialog(session, "玩家为空!");
							return;
						}
						try {
							loginToServer(session, loginRole);
						} catch (Exception e) {
							logger.error("LoginCP.parseLogin.run()", e);
						}
					}
					long useTimes = System.currentTimeMillis() - times;
					if (useTimes >= 10) {
						logger.error(userName + "登录用时:" + useTimes);
					}
					if (loginUseTime + useTimes + 1000 > Long.MAX_VALUE) {
						loginUseTime = 1;
						loginNumber = 1;
					}
					loginUseTime += useTimes;
					loginNumber++;
				} catch (Exception e) {
					logger.error("玩家登录游戏时出现异常:", e);
				}

			}
		};
		ServerEntrance.runThread(r);

	}

	/**
	 * 玩家登陆到服务器 做一些初始化处理
	 * 
	 * @param session
	 * @param hs
	 * @param role
	 * @throws IOException
	 */
	private void loginToServer(NioSession session, Player role) throws Exception {
		if (role != null) {
			if (role.getPlayerLevel() == Player.LEVEL_BANNED) {
				UISystemCP.openDialog(session, "您的角色处在被封停状态，请与客服联系。");
				World.closeHandler(session, "您的角色处在被封停状态，请与客服联系。");
				logger.error(role + "角色处在被封停状态");
				return;
			}
			role.login(session);

		}
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {

	}
}
