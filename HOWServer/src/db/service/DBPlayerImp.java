package db.service;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.*;

import util.Util;
import world.World;

import db.model.*;

/**
 * player相关处理
 * 
 * @author Administrator
 * 
 */
public class DBPlayerImp extends DataBaseImp {
	private static Logger logger = Logger.getLogger(DBPlayerImp.class);

	public static List<String> playerList=new ArrayList<String>();
	private DBPlayerImp() {
	};

	private static DBPlayerImp instance;

	public static DBPlayerImp getInstance() {
		if (instance == null) {
			instance = new DBPlayerImp();
		}
		return instance;
	}

	/**
	 * @author liuzg
	 * @return 获取评分大于某值的某些玩家
	 */
	public String[] getPlayerNameForMark(int value) {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("select name from Player where mark>=:mark");
			query.setInteger("mark", value);
			List list = query.list();
			tx.commit();
			if (list != null && list.size() > 0) {
				String[] temp = new String[list.size()];
				for (int index = 0; index < temp.length; index++) {
					temp[index] = (String) list.get(index);
				}
				return temp;
			}
			return null;
		} catch (Exception e) {
			logger.error("DbPlayerImp.getMarkOrder异常", e);
			logger.error("异常原因:", e.getCause());
			tx.rollback();
			return null;
		} finally {
			HibernateUtil.closeSession(session);

		}
	}

	/**
	 * 保存注册player数据信息
	 * 
	 * @param player
	 */
	public boolean saveInitPlayer(Player player) {
		if (player == null) {
			return false;
		}
		boolean isSave=false;
		if(isSave==false){
			return true;
		}
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(player);
			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("saveInitPlayer()" + player, e);
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}

	/**
	 * @author liuzg
	 * @param value
	 * @return 获取魅力值排名
	 */
	public int getGlamourOrder(int value) {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("select count(*) from Player where glamour>=:glamour");
			query.setInteger("glamour", value);
			long order = 0;
			List list = query.list();
			if (list == null || list.size() == 0) {
				order = 1;
			} else {
				order = (Long) query.list().get(0);
			}
			tx.commit();
			return (int) order;
		} catch (Exception e) {
			logger.error("DbPlayerImp.getGlamourOrder异常", e);
			logger.error("异常原因:", e.getCause());
			tx.rollback();
			return 1000;
		} finally {
			HibernateUtil.closeSession(session);

		}
	}

	/**
	 * @author liuzg
	 * @param value
	 * @return 总评分排名
	 */
	public int getMarkOrder(int value) {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("select count(*) from Player where mark>=:mark");
			query.setInteger("mark", value);
			long order = 0;
			List list = query.list();
			if (list == null || list.size() == 0) {
				order = 1;
			} else {
				order = (Long) query.list().get(0);
			}
			tx.commit();
			return (int) order;
		} catch (Exception e) {
			logger.error("DbPlayerImp.getMarkOrder异常", e);
			logger.error("异常原因:", e.getCause());
			tx.rollback();
			return 1000;
		} finally {
			HibernateUtil.closeSession(session);

		}
	}

	/**
	 * 获取登陆player数据信息
	 * 
	 * @param player
	 * 只在玩家登录时加载
	 */
	public Player getInitPlayer(int id) {
		if(id<=0){
			logger.error(id + "玩家加载时不存在！id=0");
			return null;
		}
		Session session = HibernateUtil.getSession();
		Player player = null;
		try {
			// ************************加载player************************
			player = (Player) session.get(Player.class, id);
			if (player == null) {
				logger.error(id + "玩家加载时不存在！");
				return null;
			}
			return player;
		} catch (Exception e) {
			logger.error("DbPlayerImp.getInitPlayer异常", e);
			logger.error("异常原因:", e.getCause());
			return null;
		} finally {
			HibernateUtil.closeSession(session);

		}
	}

	public static void main(String[] args) {

	}
	/**
	 * @author liuzg
	 * @param player
	 * 更新玩家信息
	 */
	public boolean updatePlayer(Player player,String from){
		try {
			synchronized (player) {
				long time = System.currentTimeMillis();
				if (DBPlayerImp.getInstance().update(player) == false) {
					return false;
				}
				if (System.currentTimeMillis() - time > 1) {
					logger.info(player.getName() + "updatePlayer用时:"
							+ (System.currentTimeMillis() - time));
				}
				return true;
			}
		} catch (Exception e) {
			logger.error(player.getName() + "保存玩家信息,from="+from+"时出现异常:", e);
			return false;
		}
	}
	/**
	 * @author liuzg
	 * @param usedNameList
	 * 初始化玩家名字
	 */
	public void initPlayerName(Map<String,Integer> usedNameMaps){
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("select name,id from Player");
			List nameList=query.list();
			if (nameList == null || nameList.size() == 0) {
				return;
			} else {
				for(Object obj:nameList){
					Object[] objs=(Object[])obj;
					String name=(String)objs[0];
					Integer id=(Integer)objs[1];
					name=name.toLowerCase();
					usedNameMaps.put(name, id);
				}
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("DbPlayerImp.isExistPlayer异常", e);
			logger.error("异常原因:", e.getCause());
			return;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
   /**
    * @author liuzg
    * @param playerIDMaps
    * 初始化玩家ID的对应关系
    */
   public void initPlayerIDForOpenIDAndServerName(Map<String,Integer> playerIDMaps){
	   Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("select userName,id from Player");
			List lists=query.list();
			if (lists != null && lists.size()>0) {
				for(Object obj:lists){
					Object [] objs=(Object[])obj;
					String openID=(String)objs[0];
					Integer id=(Integer)objs[1];
					playerIDMaps.put(openID,id);
				}
			}
			tx.commit();
			return;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error("getInitPlayerIdForName出错", e);
			return;
		} finally {
			HibernateUtil.closeSession(session);
		}
   }
   /**
    * @author zhangqiang
    * 获取玩家信息
    */
   public List<String> playerInfoList = new ArrayList<String>();
   public List<String> getPlayerForName(String name){
	   if (name == null) {
			return null;
		}
		playerInfo.clear();
		Session session = HibernateUtil.getSession();
		try{
			Query query = session.createQuery("select id,name,gender,bindgold,gold,dailyonlinetime,exp,openid,registertime,servername,totaloltime,viplevel,levels,playerlevel,MARK,lastlogouttime from Player where name=?");
			query.setString(0, name);
			List list = query.list();
			for (Object obj : list) {
				Object[] objs = (Object[]) obj;
				int id = (Integer)objs[0];
				String playerName = (String) objs[1];
				int gender = (Integer)objs[2];
				long bindgold = (Integer) objs[3];
				long gold = (Integer) objs[4];
				int dailyonlinetime = (Integer)objs[5];
				long exp = (Long)objs[6];
				String openid = (String)objs[7];
				String registertime = (String) objs[8];
				String serverName = (String)objs[9];
				int totaloltime = (Integer)objs[10];
				int viplevel = (Integer)objs[11];
				int level = (Integer) objs[12];
				int playerlevel = (Integer)objs[13];
				String mark = (String)objs[14];
				String lastlogoutime = (String)objs[15];
				playerInfo.add(id + "#" + openid + "#" + playerName + "#" + level+ "#" + exp+"#"+totaloltime+"#"+mark+"#"+serverName+"#"+playerlevel+"#"+gender+"#"+registertime+"#"+dailyonlinetime+"#"+viplevel+"#"+lastlogoutime+"#"+gold+"#"+bindgold);
			}
			logger.info("playerInfoList========================>"+playerInfoList);
		}catch(Exception e){
			logger.info("获取充值排行榜失败"+e);
		}
		return playerInfo;
   }
	/**
	 * 通过角色名称获取id,仅GM工具可调用，其他方法调用，使用ManagerDBUpdate中的相关方法
	 */
	public int getPlayerIDForName(String name) {
		if (name == null) {
			return -1;
		}
		Session session = HibernateUtil.getSession();
		try {
			Query query = session
					.createQuery("select id from Player where name=?");
			query.setString(0, name);
			List list = query.list();
			int id = -1;
			if (list != null && list.size() == 1) {
				id = (Integer) list.get(0);
			}
			return id;
		} catch (Exception e) {
			logger.error("getInitPlayerIdForName出错", e);
			return -1;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	public List<String> playerInfo = new ArrayList<String>();
	public List<String> getPlayerInfoForUID(String uid){
		if (uid == null) {
			return null;
		}
		playerInfo.clear();
		Session session = HibernateUtil.getSession();
		try{
			Query query = session.createQuery("SELECT name ,level,bindGold,gold,lastLogoutTime,qqGold from Player where id=?");
			query.setString(0, uid);
			List list = query.list();
			for (Object obj : list) {
				Object[] objs = (Object[]) obj;
				String name = (String) objs[0];
				int level = (Integer) objs[1];
				int bindgold = (Integer) objs[2];
				int gold = (Integer) objs[3];
				long time = (Long) objs[4];
				int qqGold = (Integer)objs[5];
				playerInfo.add(name + "#" + level + "#" + bindgold + "#" + gold+ "#" + time+"#"+qqGold);
			}
			logger.info("playerInfo========================>"+playerInfo);
		}catch(Exception e){
			logger.info("获取充值排行榜失败"+e);
		}
		return playerInfo;
	}
	/**
	 * 通过id获取玩家信息（基本信息等）
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getPlayerGenderForId(int id) {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("select p.name,p.gender,p.openID,p.level,p.glamour from Player as p where p.id=?");
			query.setParameter(0, id);
			List<Object[]> list= query.list();
			tx.commit();
			return list;
		} catch (Exception e) {
			if(tx!=null){
			tx.rollback();
			}
			logger.error("getInitPlayerIdForName出错", e);
			return null;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * 根据id获取玩家训练值等信息
	 */
	public Object[] getTrainInfoById(int playerId){
		Session session = HibernateUtil.getSession();
		try {
			Query query = session.createQuery("select p.name,p.trainLevel,p.currentTrainExp,p.vipLevel from Player as p " +
					"where p.id=?");
			query.setParameter(0, playerId);
			Object[] uniqueResult = (Object[]) query.uniqueResult();
			return uniqueResult;
		} catch (Exception e) {
			logger.info("根据玩家id获取玩家训练信息出错");
			return null;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	
	/**
	 * 通过id更新玩家金钱（玩家不在线时调用）
	 */
	public boolean setPlayerGoldForId(int id,int gold,int bindGold) {
		logger.info("玩家id="+id+",更新玩家金钱");
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("update Player as p set p.gold=p.gold+:gold,p.bindGold=p.bindGold+:bindGold where p.id=:id");
			query.setParameter("gold", gold);
			query.setParameter("bindGold", bindGold);
			query.setParameter("id", id);
			int executeUpdate = query.executeUpdate();
			tx.commit();
			if(executeUpdate > 0){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			if(tx!=null){
			tx.rollback();
			}
			logger.error("setPlayerGoldForId出错", e);
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	
	/**
	 * 通过id更新玩家权限（玩家不在线时调用）
	 */
//	public boolean setPlayerState(int id,int playerLevel) {
//		logger.info("玩家id="+id+",更新玩家权限");
//		Session session = HibernateUtil.getSession();
//		Transaction tx = null;
//		try {
//			tx = session.beginTransaction();
//			Query query = session.createQuery("update Player as p set p.playerLevel=:playerLevel where p.id=:id");
//			query.setParameter("playerLevel", playerLevel);
//			query.setParameter("id", id);
//			int executeUpdate = query.executeUpdate();
//			tx.commit();
//			if(executeUpdate > 0){
//				return true;
//			}else{
//				return false;
//			}
//		} catch (Exception e) {
//			if(tx!=null){
//			tx.rollback();
//			}
//			logger.error("setPlayerGoldForId出错", e);
//			return false;
//		} finally {
//			HibernateUtil.closeSession(session);
//		}
//	}
	/**
	 * 通过id更新玩家魅力值（玩家不在线时调用）
	 */
	public boolean setPlayerGlamourForId(int id,int glamour) {
		logger.info("玩家id="+id+",更新玩家魅力");
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("update Player as p set p.glamour=p.glamour+:glamour where p.id=:id");
			query.setParameter("glamour", glamour);
			query.setParameter("id", id);
			int executeUpdate = query.executeUpdate();
			tx.commit();
			if(executeUpdate > 0){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			if(tx!=null){
			tx.rollback();
			}
			logger.error("setPlayerGlamourForId出错", e);
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	
	/**
	 * 通过id更新玩家登录权限（玩家不在线时调用）
	 */
	public boolean setPlayerLevel(int id,int playerLevel) {
		logger.info("玩家id="+id+",变更登录权限");
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("update Player as p set p.playerLevel=:playerLevel  where p.id=:id");
			query.setParameter("id", id);
			query.setParameter("playerLevel", (byte)playerLevel);
			int executeUpdate = query.executeUpdate();
			tx.commit();
			if(executeUpdate > 0){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			if(tx!=null){
			tx.rollback();
			}
			logger.error("setPlayerGoldForId出错", e);
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author zhangqiang
	 * 根据时间取出所有玩家列表
	 */
	public List<String> getAllPlayerListForTime(Timestamp times){
		Session session = HibernateUtil.getSession();
		playerList.clear();
		try {
			String sql="";
			sql="select name,registerTime from Player where registerTime < ?";
			Query query = session.createQuery(sql);
			query.setTimestamp(0, times);
			List list=query.list();
			for(Object obj:list){
				Object[] objs=(Object[])obj;
				String name=(String)objs[0];
				Timestamp t=(Timestamp)objs[1];
				playerList.add(name);
			}
			return playerList;
		} catch (Exception e) {
			logger.info("根据日期获得注册玩家",e);
			return null;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author lzg
	 * @param before
	 * @param times
	 * @return
	 * 返回玩家注册时间
	 */
	public List<String> getPlayerListForTime(Timestamp times,Timestamp times1){
		Session session = HibernateUtil.getSession();
		playerList.clear();
		try {
			String sql="";
			sql="select name,openID,registerTime from Player where registerTime >=? and registerTime<?";
			Query query = session.createQuery(sql);
			query.setTimestamp(0, times);
			query.setTimestamp(1, times1);
			List list=query.list();
			for(Object obj:list){
				Object[] objs=(Object[])obj;
				String name=(String)objs[0];
				String openID = (String)objs[1];
				Timestamp t=(Timestamp)objs[2];
				playerList.add(name+"#" +openID+"#"+t);
			}
			return playerList;
		} catch (Exception e) {
			logger.info("根据日期获得注册玩家",e);
			return null;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author liuzg
	 * @return
	 * 获取所有玩家的ID信息
	 */
	public List<Integer> getPlayerListID(){
		List<Integer> listID=new ArrayList<Integer>();
		Session session=HibernateUtil.getSession();
		try {
			Query query=session.createQuery("select id from Player");
			if(query.list()==null){
				return listID;
			}
			for(Object obj:query.list()){
				listID.add((Integer)obj);
			}
			return listID;
		} catch (HibernateException e) {
			logger.error("获取所有玩家ID信息时出现异常",e);
			return listID;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author liuzg
	 * @param playerID
	 * @param exp
	 * @param gold
	 * 更新不在线玩家的经验和金钱
	 */
	public void updatePlayerExpAndGold(int playerID,long exp,int gold){
		if(World.getInstance().getPlayerByID(playerID)!=null){
			logger.error("玩家ID:"+playerID+"是在线玩家,无法执行此操作!");
			return;
		}
		Session session=HibernateUtil.getSession();
		Transaction tx=session.beginTransaction();
		try {
			tx.begin();
			Query query=session.createQuery("update Player as p set p.currentExp=p.currentExp+:exp,p.bindGold=p.bindGold+:gold where p.id=:playerID");
			query.setParameter("exp", exp);
			query.setParameter("gold", gold);
			query.setParameter("playerID", playerID);
			query.executeUpdate();
			tx.commit();
			logger.info("更新玩家ID:"+playerID+"的经验:"+exp+",银币:"+gold+"成功");
		} catch (Exception e) {
			logger.error("更新玩家ID:"+playerID+"的经验和银币时出现异常:",e);
			tx.rollback();
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author zhangqiang
	 * @return 返回前一天所有登录到主界面的玩家
	 */
	public int getPlayerForBI(String time){
		Session session=HibernateUtil.getSession();
//		String str="id\tname\tlevel\tcurrExp\tonLineTime\topenID\tserverName\tplayerLevel\tgender\tregisterTime\tmark\tgold\tbindGold\tdailyOnLineTime\tvipLevel\tlastLogoutTime";
//		result.add(str);
		try {
			long times = System.currentTimeMillis();
//			Query query=session.createQuery("select name from Player where sceneID>1000");
			Query query=session.createQuery("select count(*) from Player as p where p.sceneID>1000 and DATE_FORMAT(registerTime,'%Y-%m-%d')=?");
			query.setString(0, time);
			long length =  Long.parseLong(query.list().get(0).toString());
			long useTimes = System.currentTimeMillis() - times;
			logger.error("查询用时：" + useTimes + ",长度：" + length);
			return (int)length;
		} catch (HibernateException e) {
			logger.error("获取发送给BI的玩家信息时出现异常:" ,e);
			return -1;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author zhangqiang
	 * @return 返回前一天所有注册的玩家
	 */
	public int getRegisteForBI(String time){
		Session session=HibernateUtil.getSession();
//		String str="id\tname\tlevel\tcurrExp\tonLineTime\topenID\tserverName\tplayerLevel\tgender\tregisterTime\tmark\tgold\tbindGold\tdailyOnLineTime\tvipLevel\tlastLogoutTime";
//		result.add(str);
		long result =0;
		try {
			Query query=session.createQuery("select count(*)from Player  where DATE_FORMAT(registerTime,'%Y-%m-%d') =?");
			query.setString(0, time);
			result = Long.parseLong(query.list().get(0).toString());
		    return (int) result;
		} catch (HibernateException e) {
			logger.error("获取发送给BI的玩家信息时出现异常:" ,e);
			return -1;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * 获取当天最高在线人数
	 * @return
	 */
	public int getMaxOnlineNumber(){
		Session session = HibernateUtil.getSession();
		long result = 0;
		try{
			Query query = session.createQuery("select max(max_online_number) FROM OnlineCheck where DATE_FORMAT(time,'%Y-%m-%d')= DATE_FORMAT(NOW(),'%Y-%m-%d')");
//			query.setInteger(0, 1);
			List list=query.list();
			if(list!=null&& list.size()==1){
				Object obj=list.get(0);
				if(obj!=null){
					result = (Integer)obj;
				}
			}
		    return (int) result;
		}catch(Exception e ){
			logger.error("获取最高人数时出现异常:" ,e);
			return -1;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author liuzg
	 * @param playerID
	 * @return
	 * 获取玩家的version
	 */
	public int getPlayerVersion(int playerID){
		Session session = HibernateUtil.getSession();
		try {
			Query query = session
					.createQuery("select version from Player where id=?");
			query.setInteger(0, playerID);
			List list = query.list();
			int version = -1;
			if (list != null && list.size() == 1) {
				version = (Integer) list.get(0);
			}
			return version;
		} catch (Exception e) {
			logger.error("getInitPlayerIdForName出错", e);
			return -1;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
}
