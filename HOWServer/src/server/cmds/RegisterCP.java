package server.cmds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;
import db.model.Player;
import db.model.User;
import db.service.DBUserImp;
import db.service.IDManager;
import db.service.ManagerDBUpdate;
import server.ServerEntrance;
import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.DataValidate;
import util.EncryptUtil;
import util.MathUtils;
import util.Util;
import util.binreader.BannedList;
import util.binreader.LegalNameData;
import util.binreader.PromptData;
import world.World;

public class RegisterCP extends CmdParser {
	private static final int CREATE_USER = 0X0001;// 请求创建用户
	private static final int LOGIN_USER = 0X0002;// 用户登录
	private static final int SERVER_LIST = 0X0003;// 请求服务器列表
	private static final int ROLE_LOGIN_SERVER = 0x0004;// 角色登录游戏
	private static final int CREATE_ROLE=0x0005;//角色创建
	private static final int REQUEST_RANDOM_NAME = 0X0006;// 请求随机名称
	private static final int CHECK_NAME = 0X0007;// 检测名称

	public static final int RECEIVE_LARGE_FILE = 0X0097;// 获取超大文件
	public static final int CHANGE_ENCRYPT_KEY = 0X0098;// 获取加密密钥
	private static final int TEST_LINK = 0X0099;
	public static final int MAX_PLAYER_NAME_LENGTH = 8;// 最大角色名字长度
	public static final int MAX_OPENID_LENGTH = 32;// 最大用户名长度
	public static final String NULL = " ";
	public static boolean isCanRegister = true;// 0--true开放,1--false关闭
	private static Logger logger = Logger.getLogger(RegisterCP.class);
	private static RegisterCP instance;
	public static List<String> WriteNameList = new ArrayList<String>();

	public RegisterCP() {
		super(TYPE_REGISTER);

	}

	public static RegisterCP getInstance() {
		if (instance == null) {
			instance = new RegisterCP();
		}
		return instance;
	}

	public void parse(NioSession session, int command, byte[] bytes) {
		try {
			logger.debug(session + "请求命令:" + Integer.toHexString(command));
			ByteArray ba = new ByteArray(bytes);
			switch (getCommand(command)) {
			case CREATE_USER:// 请求创建用户
				requestCreateUser(session, bytes);
				break;
			case LOGIN_USER:// 用户登录
				requestLoginUser(session, bytes);
				break;
			case ROLE_LOGIN_SERVER:// 角色登录
				requestRoleLoginServer(session, bytes);
				break;
			case CREATE_ROLE://角色创建
				registerPlayer(session, bytes);
				break;
			case REQUEST_RANDOM_NAME:// 请求随机名称
				requestRandomRoleName(session, bytes);
				break;
			case TEST_LINK:// 测试连接
				// int num = ba.readInt();
				// String str = ba.readUTF();
				// logger.info(session + "收到信息:num=" + num + ",str=" + str);
				// str = session + "#" + num;
				// num++;
				// ba = new ByteArray();
				// ba.writeInt(getCmd(TEST_LINK));
				// ba.writeInt(num);
				// ba.writeUTF(str);
				// MsgOutEntry om = new MsgOutEntry(session);
				// om.flush(ba.toArray());
				break;
			case CHECK_NAME:
				String name = ba.readUTF();
				if (name.length() > MAX_PLAYER_NAME_LENGTH) {
					PromptData prompt = PromptData.getDataById(23);
					if (prompt != null) {
						UISystemCP.sendMessageForType(session, prompt.type,
								prompt.msg, prompt.id, new String[] { "" });
					} else {
						UISystemCP.openDialog(session, "角色名长度不能多于"
								+ MAX_PLAYER_NAME_LENGTH + "个字母或汉字!");
					}
					return;
				}
				String result = checkName(session, name).trim();
				if (result.equals("OK")) {
					// UISystemCP.sendResult(session, name + "，可用");
					UISystemCP.sendFlutterMessageForWarn(session, name
							+ "可以注册!");
				} else {
					// UISystemCP.sendResult(session, name + "，不可用");
				}
				break;
			case RECEIVE_LARGE_FILE:// 测试用命超大文件
				// int len = ba.readInt();// ba.readUnsignedInt();
				// logger.info("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝收到信息编号:" + len);
				break;
			case CHANGE_ENCRYPT_KEY:// 请求加密钥
				changeEncryptKey(session);
				break;
			}
		} catch (Exception e) {
			logger.error("注册模块命令解析异常:", e);
		}

	}

	/**
	 * @author liuzhigang
	 * @param session
	 * @param bytes
	 *            账户登录
	 */
	public void requestLoginUser(NioSession session, byte[] bytes) {
		ByteArray ba = new ByteArray(bytes);
		String userName = ba.readUTF();
		String pwd = ba.readUTF();
		Object[] result = DBUserImp.getInstance().isLoginSuccess(userName, pwd);
		ba=new ByteArray();
		ba.writeInt(getCmd(LOGIN_USER));
		if ((Integer) result[0] == DBUserImp.RESULT_SUCCESS) {
			ba.writeBoolean(true);
			ba.writeUTF("用户登录成功,已返回服务器列表");
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om = null;
			// 登录成功,跳转到服务器列表界面
			User user = (User) result[1];
			requestServerList(session, user.getLastLoginServerID());
		} else {
			ba.writeBoolean(false);
			ba.writeUTF("用户登录失败,code="+result[0]);
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om = null;
			logger.error("登录失败进行相关处理!");
		}
	}

	/**
	 * @author liuzhigang
	 * @param session
	 * @param bytes
	 *            请求创建用户
	 */
	public void requestCreateUser(NioSession session, byte[] bytes) {
		ByteArray ba = new ByteArray(bytes);
//		String userName = ba.readUTF();
//		String pwd = ba.readUTF();
		String userName="how"+IDManager.getInstance().getCurrentUserID();
		String pwd=userName;
		User user = User.create();
		user.setUserName(userName);
		user.setPwd(pwd);
		user.setPoint(1000);
		user.setStateCode(Util.getDateFormateMedium(System.currentTimeMillis()));
		user.setLastLoginServerID(1);
		user.setCreateTime(new Date());
		int state = DBUserImp.getInstance().createUser(user);
		ba=new ByteArray();
		ba.writeInt(getCmd(CREATE_USER));
		if (state == DBUserImp.RESULT_SUCCESS) {
			ba.writeBoolean(true);
			ba.writeUTF("用户创建成功,已返回服务器列表");
			ba.writeUTF(userName);
			ba.writeUTF(pwd);
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om = null;
			// 跳转到服务器列表界面
			requestServerList(session,user.getLastLoginServerID());
		} else {
			ba.writeBoolean(false);
			ba.writeUTF("用户创建失败:code="+state);
			ba.writeUTF(userName);
			ba.writeUTF(pwd);
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om = null;
			logger.error("创建失败返回相应错误信息");
		}
	}

	/**
	 * @author liuzhigang
	 * @param session
	 *            请求服务器列表
	 */
	public void requestServerList(NioSession session, int lastLoginServerID) {
		logger.info(session+"收到服务器列表");
		int serverCount = 3;
		ByteArray ba = new ByteArray();
		ba.writeInt(getCmd(SERVER_LIST));
		ba.writeInt(1);//默认服务器ID
		ba.writeInt(lastLoginServerID);// 上次登录服务器的ID
		ba.writeShort(serverCount);// 服务器数量
		for (int index = 1; index <= serverCount; index++) {
			ba.writeInt(index);// 服务器ID
			ba.writeUTF("GameServer_" + index);// 服务器名称
			ba.writeUTF("127.0.0.1");// 服务器IP
			ba.writeInt(60000);// 服务器port
			ba.writeUTF("空闲");// 服务器状态
		}
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
		om = null;
		
		UISystemCP.sendTestDataResult(session, "已向您发送了服务器列表");
	}

	/**
	 * @author liuzg
	 * @param session
	 *            改变加密密钥
	 */
	public void changeEncryptKey(NioSession session) {
		if (session.getAttribute("keyIndex") == null) {
			int index = EncryptUtil.changeEncryptIndex();
			byte[] keys = EncryptUtil.getEncryptKey(index);
			session.setAttribute("keyIndex", index);
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(CHANGE_ENCRYPT_KEY));
			ba.writeShort(keys.length);
			ba.writeByteArray(keys);
			logger.info(session + "发送密钥完毕");
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			om = null;
		} else {
			logger.error(session + "已为其发送密钥");
		}
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param bytes
	 *            请求随机名称
	 */
	private void requestRandomRoleName(NioSession session, byte[] bytes) {
		ByteArray ba = new ByteArray(bytes);
		int gender = ba.readInt();// 由前端传递
		ba = new ByteArray();
		String roleName = getRandomName(gender);
		ba.writeInt(getCmd(REQUEST_RANDOM_NAME));
		ba.writeUTF(roleName);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param bytes
	 * 请求角色列表
	 * 如果没有则进入新建界面
	 * 如果有角色则直接登入游戏           
	 */
	private void requestRoleLoginServer(final NioSession session, final byte[] bytes) {
		Runnable roleLogin = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				try {
					ByteArray ba = new ByteArray(bytes);
					String userName = ba.readUTF();
					logger.info(userName + "请求登录服务器...");
					if (World.getInstance().isFull()) {
						UISystemCP.openDialog(session,
								"亲,服务器已爆满,请稍候登录或登录其他分区进行游戏");
						logger.info("服务器已满，请稍候登录,关闭session...");
						// session.close();
						return;
					}
					int playerID = ManagerDBUpdate.getPlayerIDForUserName(userName);
					if (isCanRegister == false) {
						if (WriteNameList.contains(userName) == false) {
							// 关闭注册
							logger.info(session + "尚未开放注册,请耐心等待!");
							UISystemCP.openDialog(session, "亲,本服尚未开放,请耐心等待!");
							return;
						}
					}
					if (playerID > 0) {
						final Player oldPlayer = World.getPlayer(playerID);
						if (oldPlayer != null) {
							try {
								oldPlayer.logout("有相同账号在<" + oldPlayer.getClientIP() + ">登录，请求退出!");
								oldPlayer.sendResult("有相同账号在<" + oldPlayer.getClientIP()
										+ ">登录，请求退出!");
								logger.error(oldPlayer + "有相同账号<" + oldPlayer.getClientIP()
										+ ">登录，请求退出!");
								World.closeHandler(oldPlayer.getIoSession(),
										"有人用相同的帐号登录，您被迫下线");
							} catch (Exception e) {
								logger.error("相同账号登录0.5秒后关闭之前连接:", e);
							}
						}
					}
					session.setAttribute(Player.USERKEY, userName);
					logger.info(userName + "登录到角色列表" + session);
					if (playerID > 0) {
						// 直接登录游戏
						LoginCP.getInstance().parseLogin(session, userName,
								playerID);
					} else {
						//没有角色
						sendCreateNewRole(session, userName);
					}
				} catch (Exception e) {
					logger.error("返回角色列表异常:", e);
				}
				long useTimes = System.currentTimeMillis() - times;
				if (useTimes >= 1) {
					logger.error("requestRoleList()线程运行时间过长" + useTimes);
				}
			}
		};
		ServerEntrance.runThread(roleLogin);
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {
		// 处理HTTP信息
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(command);
			ba.writeUTF("收到命令:" + Integer.toHexString(command));
			MsgOutEntry out = new MsgOutEntry(session);
			out.flushForHttp(ba.toArray());
			logger.info("处理HTTP信息收到命令:" + Integer.toHexString(command));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzg 获取一个系统随机名称
	 */
	public String getRandomName(int gender) {
		String player_name = "";
		for (int index = 1; index <= 1000; index++) {
			player_name = LegalNameData.getRandomName(gender);
			if (ManagerDBUpdate.isUsedName(player_name)) {
				continue;
			}
			return player_name;
		}
		if (player_name.length() < MAX_PLAYER_NAME_LENGTH) {
			player_name += Util.getChinesseWords(MAX_PLAYER_NAME_LENGTH
					- player_name.length());
		}
		return player_name;
	}

	/**
	 * @author liuzg
	 * @param name
	 * @return 检测名称否合法
	 */
	public String checkName(NioSession session, String name) {
		if (name.trim().length() < 2) {
			logger.info("名称太短: " + name);
			if (session != null) {
				UISystemCP.sendFlutterMessageForWarn(session, "名字太短了吧!");
			}
			return "名字太短!";
		}
		if (ManagerDBUpdate.isUsedName(name)) {
			PromptData prompt = PromptData.getDataById(22);
			if (prompt != null) {
				if (session != null) {
					UISystemCP.sendMessageForType(session, prompt.type,
							prompt.msg, prompt.id, new String[] { "" });
				}
				return prompt.msg;
			} else {
				return "名称已被注册!";
			}
		}
		name = name.toUpperCase();
		if (!BannedList.legal(name)) {// 通过forbidename.txt获取非法用户名
			logger.info("名称不合法,注册失败，注册名称为name: " + name);
			PromptData prompt = PromptData.getDataById(21);
			if (prompt != null) {
				if (session != null) {
					UISystemCP.sendMessageForType(session, prompt.type,
							prompt.msg, prompt.id, new String[] { "" });
				}
				return prompt.msg;
			} else {
				return "名称不合法,注册失败!";
			}
		}
		if (name == null || name.contains(NULL) || name.equalsIgnoreCase("")) {
			logger.info("名称不能为空,注册失败name: " + name);
			if (session != null) {
				UISystemCP.sendFlutterMessageForWarn(session, "名称不能为空!");
			}
			return "名称不能为空!";
		}
		if (!DataValidate.isLegalUserName(name)) {
			logger.info("名称中存在非法字符，请再输入新的名称！name: " + name);
			PromptData prompt = PromptData.getDataById(21);
			if (prompt != null) {
				if (session != null) {
					UISystemCP.sendMessageForType(session, prompt.type,
							prompt.msg, prompt.id, new String[] {});
				}
				return prompt.msg;
			} else {
				return "名称中存在非法字符!";
			}
		}
		// if (DBPlayerImp.getInstance().isExistPlayer(name)) {

		return "OK";
	}

	/**
	 * 发送新建角色
	 * 
	 * @param userName
	 * @param openKey
	 * @param pf
	 */
	public void sendCreateNewRole(final NioSession session, final String userName) {
		try {
			int gender = MathUtils.random(0, 100);
			gender = gender % 2 + 1;
			String roleName = getRandomName(gender);
			logger.info(userName + "尚未创建角色,系统创建随机名:"+roleName);
			/** 向玩家发送新建角色 */
			ByteArray ba = new ByteArray();
			ba.writeInt(RegisterCP.getInstance().getCmd(CREATE_ROLE));
			ba.writeUTF(roleName);
		    ba.writeByte(gender);
			MsgOutEntry om = new MsgOutEntry(session);
			om.flush(ba.toArray());
			logger.info("向<" + userName + ">返回角色列表成功!");
		} catch (Exception e) {
			logger.error("异常:" + e);
		}
	}
	/**
	 * @author lzg 2010-6-24
	 * @param ba
	 * @param session
	 *            新建角色
	 */
	private void registerPlayer(final NioSession session, final byte[] bytes) {
		// 新建角色需要调用数据库，故使用线程
		Runnable registerRun = new Runnable() {
			public void run() {
				long times = System.currentTimeMillis();
				try {
					String userName = null;
					if (session.getAttribute(Player.USERKEY) != null
							&& session.getAttribute(Player.USERKEY) instanceof String) {
						userName = (String) session
								.getAttribute(Player.USERKEY);
					}
					if (userName == null) {
						logger.error("出现session异常,无法打到session中包含的userName");
						return;
					}
					ByteArray ba = new ByteArray(bytes);
					String player_name = ba.readUTF();// 角色名称
					int gender = ba.readByte();// 性别:1.男 2.女
					player_name = player_name.trim();

					if (player_name.length() > RegisterCP.MAX_PLAYER_NAME_LENGTH) {
						PromptData prompt = PromptData.getDataById(23);
						if (prompt != null) {
							UISystemCP.sendMessageForType(session, prompt.type,
									prompt.msg, prompt.id, new String[] { "" });
						} else {
							UISystemCP.openDialog(session, "角色名长度不能多于"
									+ RegisterCP.MAX_PLAYER_NAME_LENGTH
									+ "个字母或汉字!");
						}
						return;
					}
					int playerID = ManagerDBUpdate.getPlayerID(userName);
					if (playerID > 0) {
						logger.error(userName + "已建立角色,无法再次创建,userName="
								+ userName);
						UISystemCP.openDialog(session, "已建立角色,无法再次创建");
						return;
					}
					playerID = ManagerDBUpdate.getPlayerID(player_name);
					if (RegisterCP.isCanRegister == false) {
						if (RegisterCP.WriteNameList.contains(userName) == false) {
							// 关闭注册
							logger.info(session + "尚未开放注册,请耐心等待!");
							UISystemCP.openDialog(session, "亲,本服尚未开放,请耐心等待!");
							return;
						}
					}

					if (RegisterCP.getInstance()
							.checkName(session, player_name).equals("OK") == false) {
						PromptData prompt = PromptData.getDataById(22);
						if (prompt != null) {
							if (RegisterCP.getInstance()
									.checkName(session, player_name)
									.equals(prompt.msg) == true) {
								UISystemCP.sendMessageForType(session,
										prompt.type, prompt.msg, prompt.id,
										new String[] { "" });
							}
						} else {
							if (RegisterCP.getInstance()
									.checkName(session, player_name)
									.equals("名称已被注册!") == true) {
								UISystemCP.sendFlutterMessageForWarn(session,
										"名称已被注册!");
							}
						}
						return;
					}
					logger.error(userName + "创建角色" + playerID);
					if (ManagerDBUpdate.isUsedName(player_name) == false) {
						if (playerID <= 0
								|| ManagerDBUpdate.isUsedPlayerID(playerID) == false) {
							long createTime = System.currentTimeMillis();
							ManagerDBUpdate.addUsedName(player_name);
							// 弹板 创建玩家成功
							Player p = Player.createPlayer(userName,
									player_name, gender);
							if (p == null) {
								logger.error(userName + "创建角色对象是出现错误...");
								logger.error("创建角色失败,关闭session...");
								session.close();
								return;
							}
							World.addToBuffer(p);
							World.NEWREGISTERPLAYER++;
							logger.info(userName + "创建角色用时:"
									+ (System.currentTimeMillis() - createTime)
									+ ",当前玩家数量:" + World.players.size());
							ManagerDBUpdate.addUsedName(player_name, p.getId());
							ManagerDBUpdate.addPlayerID(userName, p.getId());
//							try {
//								ScribeEntry entry = ScribeEntry.getScribeEntry(
//										p, MapCP.MAIN_MAP,
//										ScribeEntry.ACTION_PLAYER_LCREATE,
//										"{\"玩家\":\"" + player_name + "\"}", "",
//										"");
//								if (p.getIoSession() != null) {
//									ScribeOperation.getInstance().addScribeLog(
//											entry);
//								}
//							} catch (Exception e) {
//								logger.error("创建车手日志异常:", e);
//							}
							LoginCP.getInstance().parseLogin(session, userName,
									p.getId());

						} else {
							UISystemCP.openDialog(session,
									"在同一个位置上建立多个角色,创建失败! ");
							logger.info(session
									+ "在同一个位置上建立多个角色,注册失败,username: "
									+ userName);
						}
					}
				} catch (Exception e) {
					UISystemCP.openDialog(session, "角色创建出现异常:" + e.toString());
					logger.error("新建角色异常:", e);
					logger.error("创建角色异常,关闭session...");
					session.close();
				}
				long useTimes = System.currentTimeMillis() - times;
				if (useTimes >= 100) {
					logger.error("registerPlayer()线程运行时间过长" + useTimes);
				}
			}
		};
		ServerEntrance.runThread(registerRun);
	}

	public static void main(String[] args) {
	}
}
