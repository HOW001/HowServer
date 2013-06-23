//package server.netserver;
//
//import org.apache.mina.core.session.IoSession;
//
//import db.model.Player;
//
///**
// * 
// * @author liuzg
// * 
// * 需要在session中保存的实体对象
// */
//public class SessionAttributeEntry {
//	// 角色实体
//	private static final String playerKey = "PLAYER";
//	// 用户实体
//	private static final String userKey = "USER";
//
//	public static void setPlayer(IoSession session, Player player) {
//		session.setAttribute(playerKey, player);
//	}
//
//	public static Player getPlayer(IoSession session) {
//		if(session==null){
//			return null;
//		}
//		if (session.getAttribute(playerKey) == null) {
//			return null;
//		}
//		if (session.getAttribute(playerKey) instanceof Player == false) {
//			return null;
//		}
//		return (Player) session.getAttribute(playerKey);
//	}
//
//	public static void clearPlayer(IoSession session) {
//		session.removeAttribute(playerKey);
//	}
//
//	public static void setUserName(IoSession session,String userName) {
//		session.setAttribute(userKey, userName);
//	}
//
//	public static String getUserName(IoSession session) {
//		if(session==null){
//			return null;
//		}
//		if (session.getAttribute(userKey) == null) {
//			return null;
//		}
//		if (session.getAttribute(userKey) instanceof String == false) {
//			return null;
//		}
//		return (String) session.getAttribute(userKey);
//	}
//
//	public static void clearUser(IoSession session) {
//		session.removeAttribute(userKey);
//	}
//}
