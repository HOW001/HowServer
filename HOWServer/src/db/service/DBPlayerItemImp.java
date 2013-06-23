package db.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import db.model.Item;
/**
 * @author fengmx
 */
public class DBPlayerItemImp extends DataBaseImp {
	private static Logger logger = Logger.getLogger(DBPlayerItemImp.class);
	private static DBPlayerItemImp instance = null;

	public static DBPlayerItemImp getInstance() {
		if (instance == null) {
			instance = new DBPlayerItemImp();
		}
		return instance;
	}

	public void saveItems(final List<Item> itemsList) {
		long times = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		try {
			tx.begin();
			for (Item item : itemsList) {
				session.saveOrUpdate(item);
			}
			tx.commit();
		} catch (HibernateException e) {
			logger.error("保存实体时出现异常:", e);
			tx.rollback();
		} finally {
			HibernateUtil.closeSession(session);
		}
		long useTimes = System.currentTimeMillis() - times;
		if (useTimes >= 100) {
			logger.error("saveItems(final List<Item> itemsList)线程运行时间过长"
					+ useTimes);
		}
	}

	public void deleteItems(final List<Item> itemsList) {
		logger.info("删除物品");
		long times = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		try {
			tx.begin();
			for (Item item : itemsList) {
				session.delete(item);
			}
			tx.commit();
		} catch (HibernateException e) {
			logger.error("保存实体时出现异常:", e);
			tx.rollback();
		} finally {
			HibernateUtil.closeSession(session);
		}
		long useTimes = System.currentTimeMillis() - times;
		if (useTimes >= 100) {
			logger.error("deleteItems( final List<Item> itemsList)线程运行时间过长"
					+ useTimes);
		}
	}
}
