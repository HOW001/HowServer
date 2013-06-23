package db.service;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import world.World;

import db.model.PlayerPack;

/**
 * @author fengmx
 */
public class DBPlayerPackImp extends DataBaseImp{
	private static DBPlayerPackImp instance = null;
	public static DBPlayerPackImp getInstance(){
		if(instance == null){
			instance = new DBPlayerPackImp();
		}
		return instance;
	}
	private static Logger logger = Logger.getLogger(DBPlayerPackImp.class);
	/**
	 * 根据玩家id获取玩家背包信息
	 * @return
	 */
	public PlayerPack getPlayerPackByPlayerId(int playerId){
		if(World.getPlayer(playerId)!=null){
			logger.error("玩家已在游戏中,无法加载背包.....");
			return null;
		}
		Session session = HibernateUtil.getSession();
		try {
			Query query = session.createQuery("from PlayerPack as p where p.holder=?");
			query.setParameter(0, playerId);
			PlayerPack pack = (PlayerPack) query.uniqueResult();
			return pack;
		} catch (Exception e) {
			logger.info("根据玩家id:"+playerId+"获取背包信息出错");
			return null;
		}finally{
			HibernateUtil.closeSession(session);
		}
	}
}
