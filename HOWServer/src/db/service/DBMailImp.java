package db.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import db.model.Mail;

/**
 * mail数据库操作dao
 * 
 * @author 刘松
 * 
 */
public class DBMailImp extends DataBaseImp {

	private static Logger logger = Logger.getLogger(DBMailImp.class);
	private static DBMailImp instance;

	private DBMailImp() {

	}

	public static DBMailImp getInstance() {
		if (instance == null) {
			instance = new DBMailImp();
		}
		return instance;
	}

	/**
	 * 获取玩家的邮件
	 */
	public List<Mail> getMailsByPlayerId(int playerId) {
		Session session = HibernateUtil.getSession();
		try {
			Query query = session
					.createQuery("from Mail as m where m.holder=? order by m.acceptTime");
			query.setInteger(0, playerId);
			List<Mail> list = query.list();
			return list;
		} catch (Exception e) {
			logger.error("dbmailImp.getMailsByPlayerId() 方法出错,playerid为："
					+ playerId, e);
			return null;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}

	/**
	 * 获取全服邮件
	 */
	public List<Mail> getOverAllMails() {
		Session session = HibernateUtil.getSession();
		try {
			Query query = session
					.createQuery("from Mail as m where m.mailType="
							+ Mail.MAILTYPE_ALL);
			List<Mail> list = query.list();
			return list;
		} catch (Exception e) {
			logger.error("dbmailImp.getOverAllMails() 方法出错", e);
			return null;
		} finally {
			HibernateUtil.closeSession(session);
		}

	}

	/**
	 * 删除所有过期数据
	 */
	public boolean deleteTimeOut(long compareTime) {
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		try {
			tx.begin();
			Query query = session
					.createQuery("delete from Mail as m where m.acceptTime<?");
			query.setLong(0, compareTime);
			int executeUpdate = query.executeUpdate();
			tx.commit();
			if (executeUpdate > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("dbmailImp.deleteTimeOut 出错", e);
			tx.rollback();
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
}
