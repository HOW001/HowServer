package util.logger;
import org.apache.log4j.Logger;

import server.ServerConfigurationNew;
import db.model.Player;

/**
 *  通行证专用日志
 *  格式：
    时间|IP|通行证帐号|手机号|手机型号|渠道标识|游戏|分区|操作|操作子参数|角色ID|昵称|性别|种族|职业|金钱|等级|经验
 */
public class LoginLogger {

	private static Logger logger = Logger.getLogger(LoginLogger.class);
	private static final char DIV = '|';
//	private static final char NULL = ' ';
	private static final String GAME = "rekoocar";
	private static final String OPERATE_LOGIN = "login";
	private static final String OPERATE_LOGOUT = "logout";
	private static final String OPERATE_REGISTER = "regist";
	
	
	private static void info(String s){
		logger.info(s);
	}
	
	/**
	 * 登陆日志
	 */
	public static void loginInfo(Player p){
		StringBuffer sb = new StringBuffer();
		sb.append("").append(DIV);//手机号
		sb.append("").append(DIV);//手机型号
		sb.append(GAME).append(DIV);//游戏
		sb.append(ServerConfigurationNew.subgame).append(DIV);//分区
		sb.append(OPERATE_LOGIN).append(DIV);//操作
		sb.append("").append(DIV);//操作子参数，先置空
		sb.append(p.getId()).append(DIV);//角色ID
		sb.append("").append(DIV);//昵称 改成空""
		sb.append(p.getGender() == 0 ? "M" : "F").append(DIV);//性别
//		sb.append(p.getGold()).append(DIV);//金钱
		sb.append(p.getLevel()).append(DIV);//等级
		sb.append(p.getCurrentExp()).append(DIV);//经验
		if(p.getIoSession()==null){
			sb.append("127.0.0.1");//IP
		} else {
			if(p.getIoSession().getRemoteAddress()==null){
				sb.append("127.0.0.1");//IP
			} else {
				sb.append(p.getIoSession().getRemoteAddress().toString());//IP
			}
		}
		info(sb.toString());
	}
	
	/**
	 * 推出日志
	 */
	public static void logoutInfo(Player p){
		StringBuffer sb = new StringBuffer();
		sb.append(GAME).append(DIV);//游戏
		sb.append(ServerConfigurationNew.subgame).append(DIV);//分区
		sb.append(OPERATE_LOGOUT).append(DIV);//操作
		sb.append("").append(DIV);//操作子参数，先置空
		sb.append(p.getId()).append(DIV);//角色ID
		sb.append("").append(DIV);//昵称 改成空""
		sb.append(p.getGender() == 0 ? "M" : "F").append(DIV);//性别
//		sb.append(p.getGold()).append(DIV);//金钱
		sb.append(p.getLevel()).append(DIV);//等级
		sb.append(p.getCurrentExp());//经验
		info(sb.toString());
	}
	
	/**
	 * 注册日志
	 */
	public static void registerInfo(Player p){
		StringBuffer sb = new StringBuffer();
		sb.append("").append(DIV);//手机号
		sb.append("").append(DIV);//手机型号
		sb.append(GAME).append(DIV);//游戏
		sb.append(ServerConfigurationNew.subgame).append(DIV);//分区
		sb.append(OPERATE_REGISTER).append(DIV);//操作
		sb.append("").append(DIV);//操作子参数，先置空
		sb.append(p.getId()).append(DIV);//角色ID
		sb.append("").append(DIV);//昵称 改成空""
		sb.append(p.getGender() == 0 ? "M" : "F").append(DIV);//性别
		sb.append(0).append(DIV);//金钱
		sb.append(1).append(DIV);//等级
		sb.append(0);//经验
		info(sb.toString());
	}
}
