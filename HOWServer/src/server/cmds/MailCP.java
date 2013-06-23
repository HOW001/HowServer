package server.cmds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.mina.transport.socket.nio.NioSession;

import server.ServerEntrance;
import server.netserver.MsgOutEntry;
//import server.netserver.SessionAttributeEntry;
import util.ByteArray;
import util.binreader.ItemData;
import util.logger.ItemLogger;
import util.logger.MoneyLogger;
import world.World;
import db.model.Mail;
import db.model.Player;
import db.service.DBMailImp;
import db.service.DBPlayerImp;

public class MailCP extends CmdParser {

	private static MailCP instance;

	// 时间格式
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static final byte ADD = 0;// 增
	public static final byte DELETE = 1;// 删
	public static final byte UPDATE = 2;// 改

	private static final int MAILS_LIST = 0X1;// 信件列表
	private static final int READ_MAIL = 0x2;// 查看邮件
	private static final int GET_ITEMS = 0X3;// 获取附件
	private static final int DELETE_MAIL = 0X4;// 删除邮件
	private static final int GET_ALL_ITEMS = 0X5;// 全部收取
	private static final int DELETE_ALL_MAILS = 0X6;// 删除全部邮件
	private static final int UPDATE_MAILS = 0X7;// 邮件更新

	private MailCP() {
		super(CmdParser.TYPE_MAIL);
	}

	public static MailCP getInstance() {
		if (instance == null) {
			instance = new MailCP();
		}
		return instance;
	}

	@Override
	public void parse(NioSession session, int command, byte[] data) {
		Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
		if(player==null){
			return;
		}
		ByteArray baIn = new ByteArray(data);
		switch (getCommand(command)) {
		case READ_MAIL:
			int mailId = baIn.readInt();
			readMail(session, player, mailId);
			break;
		case MAILS_LIST:
			mailsList(session, player);
			break;
		case GET_ITEMS:
			int mailId1 = baIn.readInt();
			getItems(session, player, mailId1);
			break;
		case DELETE_MAIL:
			int mailId2 = baIn.readInt();
			deleteMail(session, player, mailId2);
			break;
		case GET_ALL_ITEMS:
			getAllItems(session, player);
			break;
		case DELETE_ALL_MAILS:
			deleteAllMails(session, player);
			break;
		default:
			break;
		}
	}

	/**
	 * 邮件更新
	 */
	public void updateMails(Player player, byte updateType, List<Mail> mails) {
		if (player == null || mails == null || mails.size() == 0) {
			return;
		}
		ByteArray baOut = new ByteArray();
		baOut.writeInt(getCmd(UPDATE_MAILS));
		baOut.writeByte(updateType);// 类型
		baOut.writeShort(mails.size());// 长度
		long rightNow = System.currentTimeMillis();
		for (Mail mail : mails) {
			writeMailInfo(baOut, mail, rightNow);
		}
		if (player.getIoSession() != null) {
			sendData(player.getIoSession(), baOut);
		}
	}

	/**
	 * 删除所有邮件
	 * 
	 * @param session
	 * @param player
	 */
	private void deleteAllMails(final NioSession session, final Player player) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					logger.info("玩家:" + player.getName() + "开始删除所有邮件");
					if (player.getMails() == null
							|| player.getMails().size() == 0) {
						return;
					}
					for (Mail mail : player.getMails()) {
						if (mail == null
								|| mail.getMailState() == Mail.MAILSTATE_DELETE) {
							continue;
						}
						// 已经领过道具的可以直接删除
						if (mail.getMailState() == Mail.MAILSTATE_GETAWARDS
								|| mail.getItems() == null
								|| mail.getItems().size() == 0) {
							mail.setMailState(Mail.MAILSTATE_DELETE);
							DBMailImp.getInstance().update(mail);
							logger.info("玩家：" + player.getName()
									+ "删除邮件(邮件主题为：" + mail.getTitle() + ")成功");
							continue;
						}
						logger.info("玩家：" + player.getName()
								+ "有邮件的附件未领取，邮件主题：" + mail.getTitle());
						UISystemCP.sendFlutterMessageForWarn(session,
								"无法删除，请先收取附件");
					}
					logger.info("玩家:" + player.getName() + "删除所有邮件结束");
					// 通知前端
					mailsList(session, player);
				} catch (Exception e) {
					logger.error("玩家：" + player.getName() + "删除所有邮件出错", e);
				}
			}

		};
		ServerEntrance.runThread(runnable);
	}

	/**
	 * 获取全部附件
	 * 
	 * @param session
	 * @param player
	 */
	private void getAllItems(final NioSession session, final Player player) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					if (player.getMails() == null
							|| player.getMails().size() == 0) {
						logger.info("玩家:" + player.getName() + "没有邮件，无从领取附件");
						return;
					}
					for (Mail mail : player.getMails()) {
						if (mail == null) {
							continue;
						}
						// 已经领过奖励了
						if (mail.getMailState() == Mail.MAILSTATE_DELETE
								|| mail.getMailState() == Mail.MAILSTATE_GETAWARDS) {
							logger.info("玩家:" + player.getName()
									+ "已经领取过邮件(邮件主题为：" + mail.getTitle()
									+ "),不能再次领取");
							continue;
						}
						if (mail.getItems() == null
								|| mail.getItems().size() == 0) {
							logger.info("玩家:" + player.getName() + "，主题为：\""
									+ mail.getTitle() + "\"的邮件没有附件可以领取");
							continue;
						}
						// 判定背包是否充足
						boolean pagEnough = pagEnough(player, mail.getItems());
						if (!pagEnough) {
							logger.info("玩家:" + player.getName()
									+ "背包不足，不足以放置邮件物品");
							UISystemCP.sendFlutterMessageForWarn(session,
									"包裹空间不够，无法领取附件");
							mailsList(session, player);
							return;
						}
						// 领取道具过程
						for (Entry<Integer, Integer> entry : mail.getItems()
								.entrySet()) {
							if (entry == null) {
								continue;
							}
							if (entry.getKey() == Mail.MONEY_TYPE) {// 领钱
//								player.addBindGold(entry.getValue(),
//										MoneyLogger.moneyAdd[19]);
								logger.info("玩家：" + player.getName() + "通过邮件领取"
										+ entry.getValue() + "银币");
								continue;
							}
							player.getPlayerPackEntry().addItem(entry.getKey(),
									entry.getValue(), ItemLogger.itemAdd[26]);
							logger.info("玩家:" + player.getName() + "通过邮件领取道具<"
									+ entry.getKey() + "," + entry.getValue()
									+ ">");
						}
						// 改变标示
						mail.setMailState(Mail.MAILSTATE_GETAWARDS);
						DBMailImp.getInstance().update(mail);
						logger.info("玩家:" + player.getName() + "领取邮件(主题为:"
								+ mail.getTitle() + ")附件成功");
					}
					mailsList(session, player);
				} catch (Exception e) {
					logger.error("玩家：" + player.getName() + "一键领取邮件附件出错", e);
				}
			}
		};
		ServerEntrance.runThread(runnable);
	}

	/**
	 * 删除单个邮件
	 * 
	 * @param session
	 * @param player
	 * @param mailId2
	 */
	private void deleteMail(NioSession session, Player player, int mailId) {
		try {
			if (player.getMails() == null || player.getMails().size() == 0) {
				return;
			}
			for (Mail mail : player.getMails()) {
				if (mail == null || mail.getId() != mailId) {
					continue;
				}
				mail.setMailState(Mail.MAILSTATE_DELETE);
				DBMailImp.getInstance().update(mail);
				// 通知前端变更
				List<Mail> list = new ArrayList<Mail>();
				list.add(mail);
				updateMails(player, DELETE, list);
				return;
			}
			logger
					.info("玩家:" + player.getName() + "无此邮件(邮件id为：" + mailId
							+ ")");
		} catch (Exception e) {
			logger.error("玩家:" + player.getName() + "删除 邮件出错,邮件id为：" + mailId,
					e);
		}
	}

	/**
	 * 获取单个附件
	 * 
	 * @param session
	 * @param player
	 * @param mailId1
	 */
	private void getItems(NioSession session, Player player, int mailId1) {
		try {
			if (player.getMails() == null || player.getMails().size() == 0) {
				logger.info("玩家:" + player.getName() + "没有邮件，无从领取附件");
				return;
			}
			for (Mail mail : player.getMails()) {
				if (mail == null || mail.getId() != mailId1) {
					continue;
				}
				// 已经领过奖励了
				if (mail.getMailState() == Mail.MAILSTATE_DELETE
						|| mail.getMailState() == Mail.MAILSTATE_GETAWARDS) {
					logger.info("玩家:" + player.getName() + "已经领取过邮件(邮件主题为："
							+ mail.getTitle() + "),不能再次领取");
					return;
				}
				if (mail.getItems() == null || mail.getItems().size() == 0) {
					logger.info("玩家:" + player.getName() + "，主题为：\""
							+ mail.getTitle() + "\"的邮件没有附件可以领取");
					UISystemCP.sendFlutterMessageForWarn(session, "没有可以收取的附件");
					return;
				}
				// 判定背包是否充足
				boolean pagEnough = pagEnough(player, mail.getItems());
				if (!pagEnough) {
					logger.info("玩家:" + player.getName() + "背包不足，不足以放置邮件物品");
					UISystemCP.sendFlutterMessageForWarn(session,
							"包裹空间不够，无法领取附件");
					return;
				}
				// 领取道具过程
				for (Entry<Integer, Integer> entry : mail.getItems().entrySet()) {
					if (entry == null) {
						continue;
					}
					if (entry.getKey() == Mail.MONEY_TYPE) {// 领钱
//						player.addBindGold(entry.getValue(),
//								MoneyLogger.moneyAdd[19]);
						logger.info("玩家：" + player.getName() + "通过邮件领取"
								+ entry.getValue() + "银币");
						continue;
					}
					player.getPlayerPackEntry().addItem(entry.getKey(),
							entry.getValue(), ItemLogger.itemAdd[26]);
					logger.info("玩家:" + player.getName() + "通过邮件领取道具<"
							+ entry.getKey() + "," + entry.getValue() + ">");
				}
				// 改变标示
				mail.setMailState(Mail.MAILSTATE_GETAWARDS);
				DBMailImp.getInstance().update(mail);
				logger.info("玩家:" + player.getName() + "领取邮件(主题为:"
						+ mail.getTitle() + ")附件成功");
				UISystemCP.sendFlutterMessageForOK(session, "收取附件成功");
				//通知前端改变
				List<Mail> list = new ArrayList<Mail>();
				list.add(mail);
				updateMails(player, UPDATE, list);
				return;
			}
			logger.info("玩家:" + player.getName() + "无此邮件(邮件id为：" + mailId1
					+ ")");

		} catch (Exception e) {
			logger.error("玩家：" + player.getName() + "领取邮件附件出错", e);
		}
	}

	/**
	 * 背包是够充足
	 */
	private boolean pagEnough(Player player, Map<Integer, Integer> items) {
		if (player == null || player.getPlayerPackEntry() == null) {
			return false;
		}
		if (items == null || items.size() == 0) {
			return true;
		}
		Set<Entry<Integer, Integer>> entrySet = items.entrySet();
		int itemCount = 0;
		for (Entry<Integer, Integer> entry : entrySet) {
			if (entry.getKey() <= 0 || entry.getKey() == Mail.MONEY_TYPE) {
				continue;
			}
			ItemData itemData = ItemData.getItemData(entry.getKey());
			if (itemData == null) {
				continue;
			}
			int c = itemData.getPackNumber() > 1 ? entry.getValue()
					/ itemData.getPackNumber() + 1 : entry.getValue()
					/ itemData.getPackNumber();
			itemCount += c;
		}
		if (itemCount > player.getPlayerPackEntry().getSpaceNumber()) {
			return false;
		}
		return true;
	}

	/**
	 * 登陆时调用
	 */
	public void login(final Player player) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					List<Mail> overAllMails = DBMailImp.getInstance()
							.getOverAllMails();
					List<Mail> mailsByPlayerId = DBMailImp.getInstance()
							.getMailsByPlayerId(player.getId());
					List<Mail> newList = new ArrayList<Mail>();
					if (overAllMails == null || overAllMails.size() == 0) {
						if (mailsByPlayerId == null) {
							logger.info("玩家:" + player.getName() + "没有邮件");
							player.setMails(newList);
						} else {
							for(Mail mail : mailsByPlayerId){
								if(mail != null){
									mail.init();
								}
							}
							player.setMails(mailsByPlayerId);
							logger.info("玩家:" + player.getName() + "初始化"
									+ player.getMails().size() + "条邮件");
						}
						// 向前端推送邮件列表
						if (player.getIoSession() != null) {
							mailsList(player.getIoSession(), player);
						}
						return;
					}
					
					//本人无邮件的情形
					if(mailsByPlayerId == null || mailsByPlayerId.size() == 0){
						for (Mail overAllMail : overAllMails) {
							if (overAllMail == null) {
								continue;
							}
							if(overAllMail.getAcceptTime() < player.getRegisterTime().getTime()){
								continue;
							}
							overAllMail.init();
							Mail newMail = new Mail(player.getId(), overAllMail
									.getTitle(), overAllMail.getContent(),
									overAllMail.getAddresser(), overAllMail
											.getMailState(), overAllMail
											.getAcceptTime(),
									Mail.MAILTYPE_SINGLE, overAllMail
											.getItems());
							newMail.setOtherMailId(overAllMail.getId());
							newMail.beforeSave();
							DBMailImp.getInstance().save(newMail);
							newList.add(newMail);
							logger.info("玩家:" + player.getName()
									+ "增加一条全服邮件,邮件id为:" + newMail.getId());
						}
						player.setMails(newList);
						logger.info("玩家:" + player.getName() + "初始化"
								+ player.getMails().size() + "条邮件");
						// 向前端推送邮件列表
						if (player.getIoSession() != null) {
							mailsList(player.getIoSession(), player);
						}
						return;
					}

					//是否是第一次循环
					boolean first = true;
					for (Mail overAllMail : overAllMails) {
						if (overAllMail == null) {
							continue;
						}
						if(overAllMail.getAcceptTime() < player.getRegisterTime().getTime()){
							continue;
						}
						//标志是否可以添加
						boolean flag = true;
						for (Mail mail : mailsByPlayerId) {
							if (mail == null) {
								continue;
							}
							if(first){
								mail.init();
								newList.add(mail);
							}
							// 已经在身上的情形
							if (overAllMail.getId() == mail.getOtherMailId()) {
								flag = false;
							}
						}
						first = false;
						if(flag){
							overAllMail.init();
							Mail newMail = new Mail(player.getId(), overAllMail
									.getTitle(), overAllMail.getContent(),
									overAllMail.getAddresser(), overAllMail
									.getMailState(), overAllMail
									.getAcceptTime(),
									Mail.MAILTYPE_SINGLE, overAllMail
									.getItems());
							newMail.beforeSave();
							DBMailImp.getInstance().save(newMail);
							newList.add(newMail);
							logger.info("玩家:" + player.getName()
									+ "增加一条全服邮件,邮件id为:" + newMail.getId());
						}
					}
					player.setMails(newList);
					logger.info("玩家:" + player.getName() + "初始化"
							+ player.getMails().size() + "条邮件");
					// 向前端推送邮件列表
					if (player.getIoSession() != null) {
						mailsList(player.getIoSession(), player);
					}
				} catch (Exception e) {
					logger.error("玩家:" + player.getName() + "登录时处理邮件出错", e);
				}
			}
		};
		ServerEntrance.runThread(runnable);
	}

	/**
	 * 邮件列表
	 * 
	 * @param session
	 * @param player
	 */
	private void mailsList(NioSession session, Player player) {
		if (player.getMails() == null) {
			return;
		}
		ByteArray baOut = new ByteArray();
		baOut.writeInt(getCmd(MAILS_LIST));
		int temp = 0;
		long rightNow = System.currentTimeMillis();
		ByteArray tempByteArray = new ByteArray();
		for (Mail mail : player.getMails()) {
			if (mail == null || mail.getMailState() == Mail.MAILSTATE_DELETE) {
				continue;
			}
			writeMailInfo(tempByteArray, mail, rightNow);
			temp++;
		}
		baOut.writeShort(temp);
		baOut.writeByteArray(tempByteArray.toArray());
		sendData(session, baOut);
	}

	/**
	 * 写入邮件信息
	 * 
	 * @param baOut
	 * @param mail
	 */
	private void writeMailInfo(ByteArray baOut, Mail mail, long rightNow) {
		if (baOut == null) {
			return;
		}
		if (mail != null) {
			baOut.writeInt(mail.getId());// id
			baOut.writeByte(mail.getMailState());// 状态
			baOut.writeUTF(mail.getAddresser());// 发件人
			baOut.writeUTF(mail.getTitle());// 标题
			baOut.writeUTF(mail.getContent());// 内容
			baOut.writeUTF(format.format(new Date(mail.getAcceptTime())));// 收件时间
			// 剩余天数
			int days = (int) ((Mail.MAX_SAVE_TIME - rightNow + mail.getAcceptTime()) / 1000 / 60
					/ 60 / 24 + 1);
			baOut.writeInt(days);// 剩余天数
			baOut.writeShort(mail.getItems().size());
			//写入道具
			for(Entry<Integer, Integer> entry : mail.getItems().entrySet()){
				baOut.writeInt(entry.getKey());
				baOut.writeInt(entry.getValue());
			}
		} else {
			baOut.writeInt(-1);// id
			baOut.writeByte(0);// 状态
			baOut.writeUTF("");// 发件人
			baOut.writeUTF("");// 标题
			baOut.writeUTF("");// 内容
			baOut.writeUTF("");// 收件时间
			baOut.writeInt(0);// 剩余天数
			baOut.writeShort(0);//道具
		}
	}

	/**
	 * 查看邮件
	 * 
	 * @param session
	 * @param player
	 * @param mailId
	 */
	private void readMail(NioSession session, Player player, int mailId) {
		if (player.getMails() == null || player.getMails().size() == 0) {
			return;
		}
		for (Mail m : player.getMails()) {
			if (m == null || m.getMailState() != Mail.MAILSTATE_UNREAD) {
				continue;
			}
			if (m.getId() == mailId) {
				m.setMailState(Mail.MAILSTATE_READ);
				try {
					DBMailImp.getInstance().update(m);
					logger.info("玩家:" + player.getName() + "查看邮件时修改邮件状态成功");
				} catch (Exception e) {
					logger.error("玩家" + player.getName() + "查看邮件时修改邮件状态出错", e);
				}

				// 向前端发送信息
				ArrayList<Mail> list = new ArrayList<Mail>();
				list.add(m);
				updateMails(player, UPDATE, list);

				break;
			}
		}
	}

	/**
	 * 写信
	 * @param playerName
	 * @param title
	 * @param content
	 * @param items
	 */
	public void writeMailToSinglePlayer(String playerName, String title,
			String content, Map<Integer, Integer> items) {
		int playerId = DBPlayerImp.getInstance().getPlayerIDForName(playerName);
		if(playerId < 0){
			logger.info("名称为:"+playerName+"的玩家不存在,写信失败");
			return;
		}
		writeMailToSinglePlayer(playerId, title, content, items);
	}
	/**
	 * 写信
	 * 
	 * @param playerId
	 * @param title
	 * @param content
	 * @param items
	 *            :<道具id,道具数量>,若为金钱<Mail.MONEY_TYPE,金钱数量>
	 */
	public void writeMailToSinglePlayer(int playerId, String title,
			String content, Map<Integer, Integer> items) {
		Set<Entry<Integer, Integer>> entrySet = items.entrySet();
		for (Entry<Integer, Integer> entry : entrySet) {
			logger.error(entry.getKey() +" : "+entry.getValue());
		}
		Mail mail =Mail.create();
		mail.setAcceptTime(System.currentTimeMillis());
		mail.setAddresser("系统邮件");
		mail.setTitle(title);
		mail.setContent(content);
		mail.setHolder(playerId);
		mail.setItems(items);
		mail.setMailState(Mail.MAILSTATE_UNREAD);
		mail.setMailType(Mail.MAILTYPE_SINGLE);
		mail.beforeSave();
		try {
			DBMailImp.getInstance().save(mail);
			logger.info("系统向玩家(id为：" + playerId + ")发送邮件成功,mailid为:"
					+ mail.getId());
		} catch (Exception e) {
			logger.error(e);
		}
		Player player = World.getPlayer(playerId);
		if (player != null) {
			if (player.getMails() == null) {
				player.setMails(new ArrayList<Mail>());
			}
			player.getMails().add(mail);
			logger.info("在线玩家:" + player.getName() + "添加邮件成功,mailId为："
					+ mail.getId());
			// 向前端发送数据
			List<Mail> list = new ArrayList<Mail>();
			list.add(mail);
			updateMails(player, ADD, list);
		}
	}

	/**
	 * 全服系统信件
	 */
	public void writeToALL(final String title, final String content,
			final Map<Integer, Integer> items) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				logger.info("开始发送全服邮件,邮件标题为:" + title);
				try {
					long rightNow = System.currentTimeMillis();
					Mail mail = new Mail(-1, title, content, "系统邮件",Mail.MAILSTATE_UNREAD, rightNow, Mail.MAILTYPE_ALL,items);
					mail.beforeSave();
					DBMailImp.getInstance().save(mail);
					logger.info("全服邮件创建成功,mailid为:" + mail.getId());

					List<Mail> tempList = new ArrayList<Mail>();
					// 处理在线玩家
					for (Player player : World.players.values()) {
						if (player == null) {
							continue;
						}
						Mail m = new Mail(player.getId(), title, content,"系统邮件", Mail.MAILSTATE_UNREAD, rightNow,Mail.MAILTYPE_SINGLE, items);
						m.setOtherMailId(mail.getId());
						m.beforeSave();
						DBMailImp.getInstance().save(m);
						if (player.getMails() == null) {
							player.setMails(new ArrayList<Mail>());
						}
						player.getMails().add(m);
						logger.info("在线玩家：" + player.getName() + "接收全服邮件成功,mailid为：" + m.getId());
						// 向前端发送数据
						tempList.clear();
						tempList.add(m);
						updateMails(player, ADD, tempList);
					}
				} catch (Exception e) {
					logger.error("发送全服系统邮件出错", e);
				}
				logger.info("发送全服邮件结束,邮件标题为:" + title);
			}
		};

		ServerEntrance.runThread(runnable);
	}

	/**
	 * 0点删除过期数据
	 */
	public void deleteTimeOut(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (Mail.canDelete && hour >= 0 && hour < 23) {
			logger.info("邮件系统开始删除过期数据");
			DBMailImp.getInstance().deleteTimeOut(calendar.getTimeInMillis() - Mail.MAX_SAVE_TIME);
			Mail.canDelete = false;
			// 处理在线玩家
			deleteTimeOutWhenPlayerOnline(calendar.getTimeInMillis() - Mail.MAX_SAVE_TIME);
			logger.info("邮件系统删除过期数据结束");
			return;
		}
		if (!Mail.canDelete && hour >= 23) {
			Mail.canDelete = true;
		}
	}

	/**
	 * 删除在线玩家数据
	 */
	private void deleteTimeOutWhenPlayerOnline(long compareTime) {
		logger.info("邮件系统删除在线玩家数据开始");
		for (Player player : World.players.values()) {
			if (player == null || player.getMails() == null) {
				continue;
			}
			List<Mail> list = new ArrayList<Mail>();
			list.addAll(player.getMails());
			// 删除过期数据
			for (Mail mail : list) {
				if (mail == null) {
					continue;
				}
				if (mail.getAcceptTime() < compareTime) {
					player.getMails().remove(mail);
				}
			}
			// 向前端发送数据变更
			if(player.getIoSession() != null){
				mailsList(player.getIoSession(), player);
			}
		}
		logger.info("邮件系统删除在线玩家数据结束");
	}

	/**
	 * 发送数据
	 */
	private void sendData(NioSession session, ByteArray baOut) {
		MsgOutEntry mo = new MsgOutEntry(session);
		mo.flush(baOut.toArray());
		mo = null;
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {

	}

}
