package server.cmds;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.mina.transport.socket.nio.NioSession;

import db.model.Player;
import server.netserver.MsgOutEntry;
import util.ByteArray;

/**
 * 发送UI界面和dat文件 客户端发送过来的是 0x000500** （**是界面编号） 服务器返回的时候返回 0x80050001 UI文件
 * 0x80050002 DAT文件 parse函数noUse了
 */
public class UISystemCP extends CmdParser {
	private static Logger logger = Logger.getLogger(UISystemCP.class);

	/**
	 * 弹板类型
	 */
	public static final int CMD_TYPE_DEFAULT = 0x0000;// 默认弹板
	/**
	 * 弹板命令以弹板类型做为区分
	 */
	private static final int CMD_SEND_DIALOG_UI = 0X0001;// 发送一个最简单的提示框,不支持任何交互
	private static final int CMD_SEND_DATA_UI = 0x0002;// 发送弹板信息,支持交互
	private static final int CMD_SEND_MESSAGE = 0x0003;// 发送飘动的字体信息
	private static final int CMD_SEND_MESSAGE2 = 0x0004;// 发送飘动的字体信息
	private static final int CMD_RECEIVE_UI_OK = 0xF001;// 接收确认信息
	private static final int CMD_RECEIVE_UI_CANCEL = 0XF002;// 接收取消信息

	private static UISystemCP instance;

	public static UISystemCP getInstance() {
		if (instance == null) {
			instance = new UISystemCP();
		}
		return instance;
	}

	private UISystemCP() {
		super(TYPE_UI);
	}

	public void parse(NioSession session, int packCommand, byte[] bytes) {
		try {
			Player player = session.getAttribute(Player.PLAYERKEY) != null
					&& session.getAttribute(Player.PLAYERKEY) instanceof Player ? (Player) session
					.getAttribute(Player.PLAYERKEY) : null;
			if (player == null) {
				return;
			}
			ByteArray ba = new ByteArray(bytes);
			switch (getCommand(packCommand)) {
			case CMD_RECEIVE_UI_OK:// 从前端返回的信息
				int cmdType = ba.readShort();
				short len = ba.readShort();
				String[] data = new String[len];
				for (int index = 0; index < len; index++) {
					data[index] = ba.readUTF();
				}
				if (data.length == 1) {
					data = data[0].split(",");
				}
				receiveOK(player, cmdType, data);
				break;
			case CMD_RECEIVE_UI_CANCEL:// 从前端返回的信息
				cmdType = ba.readShort();
				len = ba.readShort();
				data = new String[len];
				for (int index = 0; index < len; index++) {
					data[index] = ba.readUTF();
				}
				if (data.length == 1) {
					data = data[0].split(",");
				}
				receiveCancel(player, cmdType, data);
				break;
			}
		} catch (Exception e) {
			logger.error("UI命令解析异常:", e);
		}
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param data
	 *            前端点击确认按键返回此处
	 */
	private void receiveOK(Player player, int cmdType, String[] data) {
		try {
			logger.info(player.getName() + "从前端返回的信息是:");
			switch (cmdType) {
			case CMD_TYPE_DEFAULT:
				for (String temp : data) {
					logger.info(temp);
				}
				break;
			default:
				logger.error("无法识别的弹板类型:" + cmdType);
			}
		} catch (Exception e) {
			logger.info(player.getName() + "出现错误的弹板类型是:" + cmdType);
		}
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param data
	 *            前端点击取消按键返回此处
	 */
	private void receiveCancel(Player player, int cmdType, String[] data) {
		logger.info(player.getName() + "从前端返回的信息是:");
		switch (cmdType) {
		case CMD_TYPE_DEFAULT:
			for (String temp : data) {
				logger.info(temp);
			}
			break;
		default:
			logger.error("无法识别的弹板类型:" + cmdType);
		}
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param describe
	 *            向前端发送简单信息提示
	 */
	public static void openDialog(NioSession session, String describe) {
		try {
			openDialog(session, describe, CMD_SEND_DIALOG_UI);

		} catch (IOException e) {
			logger.error(session.getAttribute(Player.PLAYERKEY) + "send result error", e);
		}
	}

	/**
	 * @author liuzhigang
	 * @param session
	 * @param describe
	 * @param type
	 *            发送不带交互的弹板
	 */
	private static void openDialog(NioSession session, String describe, int type) throws IOException {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_DIALOG_UI));
		ba.writeShort(type);
		ba.writeUTF(describe);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * @author liuzg
	 * @param session
	 * @param describe
	 *            测试交互弹板
	 * 
	 */
	public static void sendTestDataResult(NioSession session, String describe) {

		try {
			sendDataResult(session, CMD_TYPE_DEFAULT, describe, "测试", "取消", generateCMD(TYPE_UI, CMD_RECEIVE_UI_OK),
					new String[] { "aaa", "123", "false" }, generateCMD(TYPE_UI, CMD_RECEIVE_UI_CANCEL), new String[] {
							"五笔字型", "热血青年" });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author Administrator
	 * @param session
	 * @param type
	 * @param describe
	 * @param okStr
	 * @param okData
	 * @param cancelStr
	 * @param cancelData
	 *            外部方法调用此接口，前提是确定弹板类型
	 */
	public static void sendDataResult(NioSession session, int type, String describe, String okStr, String[] okData,
			String cancelStr, String[] cancelData) {
		try {
			sendDataResult(session, type, describe, okStr, cancelStr, generateCMD(TYPE_UI, CMD_RECEIVE_UI_OK), okData,
					generateCMD(TYPE_UI, CMD_RECEIVE_UI_CANCEL), cancelData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 控制客户端弹板
	 * 
	 * 
	 * @param session
	 * @param type
	 *            弹板类型
	 * @param describtion
	 *            描述信息
	 * @param leftMessage
	 *            左键信息
	 * @param rightMessage
	 *            右键信息
	 * @param leftCommand
	 *            左键命令
	 * @param leftData
	 *            左键回复数据
	 * @param rightCommand
	 *            右键命令
	 * @param rightData
	 *            右键回复数据
	 * @throws IOException
	 */
	private static void sendDataResult(NioSession session, int type, String describtion, String leftMessage,
			String rightMessage, int leftCommand, String[] leftData, int rightCommand, String[] rightData)
			throws IOException {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_DATA_UI));
		ba.writeShort(type);
		ba.writeUTF(describtion);
		ba.writeUTF(leftMessage);
		ba.writeUTF(rightMessage);
		ba.writeInt(leftCommand);
		ba.writeShort(leftData.length);
		for (String data : leftData) {
			ba.writeUTF(data);
		}
		ba.writeInt(rightCommand);
		ba.writeShort(rightData.length);
		for (String data : rightData) {
			ba.writeUTF(data);
		}
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	public static final int flutterTime = 3;
	public static final int type_warn = 1;
	public static final int type_ok = 2;
	public static final int type_null = 3;
	public static final int type_null_red = 4;

	/**
	 * 发送飘动字体（！感叹号）
	 * 
	 * @param session
	 * @param type
	 * @param describtion
	 */
	public static void sendFlutterMessageForWarn(NioSession session, String describtion) {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_MESSAGE));
		ba.writeInt(type_warn);
		ba.writeUTF(describtion);
		ba.writeInt(flutterTime);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * 发送飘动字体(对勾)
	 * 
	 * @param session
	 * @param type
	 * @param describtion
	 */
	public static void sendFlutterMessageForOK(NioSession session, String describtion) {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_MESSAGE));
		ba.writeInt(type_ok);
		ba.writeUTF(describtion);
		ba.writeInt(flutterTime);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * 发送纯文字（+）字体颜色不同
	 * 
	 * @param session
	 * @param describtion
	 */
	public static void sendFlutterMessageForNull(NioSession session, String describtion) {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_MESSAGE));
		ba.writeInt(type_null);
		ba.writeUTF(describtion);
		ba.writeInt(flutterTime);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * 发送纯文字（-）字体颜色不同
	 * 
	 * @param session
	 * @param describtion
	 */
	public static void sendFlutterMessageForNullRed(NioSession session, String describtion) {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_MESSAGE));
		ba.writeInt(type_null_red);
		ba.writeUTF(describtion);
		ba.writeInt(flutterTime);
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}

	/**
	 * 根据提示类型，发送提示信息
	 */
	public static void sendMessageForType(NioSession session, int type, String msg, int id, String[] xyz) {
		switch (type) {
		case 1:
			sendFlutterMessageForWarn(session, msg);
			break;
		case 2:
			sendFlutterMessageForOK(session, msg);
			break;
		case 3:
			sendFlutterMessageForNull(session, msg);
			break;
		case 4:
			openDialog(session, msg);
			break;
		case 5:
		case 6:
		case 8:
			sendMessageForDesign(type, id, xyz);
			break;
		case 7:
			sendFlutterMessageForNullRed(session, msg);
			break;
		default:
			break;
		}
	}

	/**
	 * @author liuzg
	 * @param type类型
	 * @param id
	 *            策划提示ID
	 * @param xyz
	 *            对应策划XYZ 发送策划提示
	 */
	public static void sendMessageForDesign(int type, int id, String[] xyz) {
		String msg = "" + id;
		if (xyz != null && xyz.length > 0) {
			for (int index = 0; index < xyz.length; index++) {
				if (xyz[index] == null || xyz[index].length() == 0) {
					continue;
				}
				msg += "@" + xyz[index];
			}
		}
		ChatCP.sendDesignMsgToAllPlayerFromGame(type, msg);

	}

	/**
	 * 解析字符串 一个参数的
	 * 
	 * @param msg
	 * @param x
	 * @return
	 */
	public String getResultMsg(String msg, String x) {
		msg = msg.toUpperCase();
		String[] msgArr = msg.split("#");
		if (msgArr.length == 1) {
			return msg;
		}
		for (int i = 0; i < msgArr.length; i++) {
			if (msgArr[i].equalsIgnoreCase("X")) {
				msgArr[i] = x;
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < msgArr.length; i++) {
			sb.append(msgArr[i]);
		}
		return sb.toString();
	}

	/**
	 * 解析字符串 两个参数的
	 * 
	 * @param msg
	 * @param x
	 * @param y
	 * @return
	 */
	public String getResultMsg(String msg, String x, String y) {
		msg = msg.toUpperCase();
		String[] msgArr = msg.split("#");
		if (msgArr.length == 1) {
			return msg;
		}
		for (int i = 0; i < msgArr.length; i++) {
			if (msgArr[i].equalsIgnoreCase("X")) {
				msgArr[i] = x;
			} else if (msgArr[i].equalsIgnoreCase("Y")) {
				msgArr[i] = y;
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < msgArr.length; i++) {
			sb.append(msgArr[i]);
		}
		return sb.toString();
	}

	/**
	 * 解析字符串 三个参数的
	 * 
	 * @param msg
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public String getResultMsg(String msg, String x, String y, String z) {
		msg = msg.toUpperCase();
		String[] msgArr = msg.split("#");
		if (msgArr.length == 1) {
			return msg;
		}
		for (int i = 0; i < msgArr.length; i++) {
			if (msgArr[i].equalsIgnoreCase("X")) {
				msgArr[i] = x;
			} else if (msgArr[i].equalsIgnoreCase("Y")) {
				msgArr[i] = y;
			} else if (msgArr[i].equalsIgnoreCase("Z")) {
				msgArr[i] = z;
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < msgArr.length; i++) {
			sb.append(msgArr[i]);
		}
		return sb.toString();
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {

	}

	/**
	 * 3秒弹窗立即装备 外部调用
	 */
	public static void sendDataResult(NioSession session, int cmdType, int index, String describe, String left) {
		try {
			sendResult(session, cmdType, index, describe, left);
		} catch (IOException e) {
			logger.error("出现异常201208281342:", e);
		}
	}

	private static void sendResult(NioSession session, int type, int index, String describtion, String leftMessage)
			throws IOException {
		ByteArray ba = new ByteArray();
		ba.writeInt(generateCMD(TYPE_UI, CMD_SEND_MESSAGE2));
		ba.writeShort((short) type);
		ba.writeInt(index);
		ba.writeUTF(describtion);
		ba.writeUTF(leftMessage);
		// ba.writeInt(leftCommand);
		// ba.writeShort(1);// 最外层结构为1
		// ba.writeShort(leftData.length);
		// for (String data : leftData) {
		// ba.writeUTF(data);
		// }
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
	}
}
