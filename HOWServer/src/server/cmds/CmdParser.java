package server.cmds;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.log4j.Logger;

/**
 * 
 * 命令解析器
 *
 */
public abstract class CmdParser {
//	public static final int CMD_SYSTEM  = 0x80;//SERVER端
	public static final int CMD_CLIENT  = 0x00;//客户端
	public static final int TOTLE_TYPE_NUMBER = 0x3F;//种类总个数
	public static final byte TYPE_SYSTEM    =0X00;//系统
	public static final byte TYPE_REGISTER 	= 0x01;//注册
	public static final byte TYPE_LOGIN	 	= 0x02;//登录相关
	public static final byte TYPE_MAP	 	= 0x03;//地图相关
	public static final byte TYPE_PLAYER 	= 0x04;//君主相关
	public static final byte TYPE_FIGHT     =0X05;//战斗
	public static final byte TYPE_CHAT 		= 0x06;//聊天
	public static final byte TYPE_PACK      = 0x07;//背包   
	public static final byte TYPE_UI 	 	= 0x08;//UI系统
	public static final byte TYPE_LIVENESS = 0X09;//活跃度
	public static final byte TYPE_MAIL = 0x0A;//邮件
	public static final int BOARD_SUCC = 0;//操作成功的弹板类型
	public static final int BOARD_FAIL = 0;//操作失败的弹板类型
	private int type;
	
	/**
	 * 工厂方法
	 * 添加新CP只需要再次添加
	 * @param type
	 * @return
	 */
	public static CmdParser createCmdParser(int type){
//		System.out.println("创建的命令号:"+type);
		switch(type){
		case TYPE_LOGIN:
			return LoginCP.getInstance();
		case TYPE_MAP:
			return MapCP.getInsatance();
		case TYPE_PLAYER:
			return PlayerCP.getInstance();
		case TYPE_FIGHT:
			return FightCP.getInstance();
		case TYPE_UI:
			return UISystemCP.getInstance();
		case TYPE_CHAT:
			return ChatCP.getInstance();
		case TYPE_REGISTER:
			return RegisterCP.getInstance();
		case TYPE_PACK:
			return PackCP.getInstance();
		case TYPE_LIVENESS:
			return LivenessCP.getInstance();
		case TYPE_MAIL:
			return MailCP.getInstance();
		}
		return null;
	}
	/**
	 * @author liuzg
	 * @param type
	 * @return
	 * 是否有效命令
	 */
	public static boolean isValid(int type){
		switch(type){
		case  TYPE_REGISTER ://注册
		case TYPE_LOGIN	 	://登录相关
		case TYPE_MAP	 	://地图相关
		case TYPE_PLAYER 	://可见列表
		case TYPE_FIGHT		://战斗
		case TYPE_UI 	 	://UI系统
		case TYPE_CHAT 		://聊天
		case TYPE_PACK      ://背包   
              return true;
         default:
        	  return false;
		}
	}
	/**
	 * 生成所有的命令解析器
	 *
	 */
	public static void createAllCmdParser(){
		for(int i=0;i<TOTLE_TYPE_NUMBER;i++){
			createCmdParser(i);
		}
	}
	

	public CmdParser(int type){
		this.type = type;
		CmdDispatch.getInstance().addParser(this);
	}
	
	public int getType(){
		return type;
	}
	
	/**
	 * 是否是客户端命令
	 * @param command
	 * @return
	 */
	public static boolean isClientCommand(int command){
		return ((command >>> 24) & 0xFF) == CMD_CLIENT;
	}
	/**
	 * 取得命令type
	 * @param command
	 * @return
	 */
	public static int getType(int command){
		int type = (command >> 16);
		return (type & 0x00FF);
	}
	
	/**
	 * 取得子命令号
	 * @param command
	 * @return
	 */
	public static int getCommand(int command){
		return (command & 0x0000FFFF);
	}
	
	/**
	 * 解析命令 
	 * @author liuzg
	 * @param handler
	 * @param command
	 * @param bytes
	 */
	public abstract void parse(NioSession session, int command,byte[] bytes);
	/**
	 * 解析Http命令
	 * @author liuzg
	 * @param session
	 * @param command
	 * @param bytes
	 */
	public abstract void parseForHttp(NioSession session, int command,byte[] bytes);
	/**
	 * 包装命令号
	 * @param type
	 * @param cmd
	 * @return
	 */
	public static int generateCMD(int type,int cmd) {
		int command = 0;
		command += type << 16;
		command += cmd;
		if(showDebug){
			logger.info("generateCMD:" + Integer.toHexString(command));
		}
		return command;
	}
	/**
	 * 包装命令号
	 * @param cmd
	 * @return
	 */
	public int getCmd(int cmd){
		int command = 0;
		command += type << 16;
		command += cmd;
		if(showDebug){
			logger.info("PACKCOMMAND:" + Integer.toHexString(command));
		}
		return command;
	}
	
	private static boolean showDebug = false;
	protected static Logger logger = Logger.getLogger(CmdParser.class);

}
