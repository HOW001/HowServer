/**
 * 
 */
package db.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Util;
import world.World;

import db.model.DataBaseEntry;
import db.model.Player;

/**
 * @author liuzg
 * 如果是大批量保存，使用外session方式，这样可以节省大量保存时间
 * 如果是大批量保存，使用外session方式的同时，使用内flush，这样可马节省大量缓存
 */
public class DataBaseImp {
	private static Logger logger = Logger.getLogger(DataBaseImp.class);
   
	private final static DataBaseImp instance=new DataBaseImp();
    public  static DataBaseImp getInstance(){
    	return instance;
    }
    public DataBaseImp(){}
	/**
	 * @author liuzg
	 * @param entry
	 * @return 独立session保存一个数据实体
	 */
	public  boolean save(DataBaseEntry entry) throws Exception {
		if(entry==null){
			return false;
		}
		synchronized (entry) {
			logger.debug("DB保存对象实体:" + entry.getClass());
			Session session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			try {
				tx.begin();
				session.saveOrUpdate(entry);
				tx.commit();
				return true;
			} catch (HibernateException e) {
				logger.error("保存实体:" + this.getClass());
				logger.error("保存实体时出现异常:", e);
				tx.rollback();
				return false;
			} finally {
				HibernateUtil.closeSession(session);
			}
		}
	}

	/**
	 * @author liuzg
	 * @param entry
	 * @return 独立session更新一个数据实体
	 */
	public  boolean update(DataBaseEntry entry) throws Exception {
		if(entry==null){
			return false;
		}
		synchronized (entry) {
			logger.debug("开始DB更新对象实体:" + entry.getClass());
			Session session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			try {
				tx.begin();
				session.saveOrUpdate(entry);
				tx.commit();
				logger.debug("完成DB更新对象实体:" + entry.getClass());
				return true;
				
			} catch (HibernateException e) {
				logger.error("更新实体:" + this.getClass());
				logger.error(this.getClass() + "更新实体时出现异常:", e);
				tx.rollback();
				return false;
			} finally {
				HibernateUtil.closeSession(session);
			}
		}
	}

	/**
	 * @author liuzg
	 * @param entry
	 * @return 独立session删除一个实体
	 */
	public  boolean delete(DataBaseEntry entry) throws Exception {
		if(entry==null){
			return false;
		}
		logger.debug("DB删除对象实体:"+entry.getClass());
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		try {
			tx.begin();
			session.delete(entry);
			tx.commit();
			return true;
		} catch (HibernateException e) {
			logger.error("删除实体:"+this.getClass());
			logger.error("删除实体时出现异常:", e);
			tx.rollback();
			return false;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
	/**
	 * @author liuzg
	 * 处理过期shop信息
	 */
	public void processPastDueShop(){
		int days=14;//超过天数的数据清理
		Calendar c=Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -days);
//		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)-days);
		Timestamp time=Util.getTimestampFromCalendar(c);
		Session session=HibernateUtil.getSession();
		Transaction tx=session.beginTransaction();
		try {
			logger.info("开始清理过期Shop信息...");
			tx.begin();
			Query query=session.createQuery("delete from Shop where buyTime < ?");
			query.setTimestamp(0, time);
			query.executeUpdate();
			tx.commit();
			logger.info("完成清理过期Shop信息...");
			logger.info("开始清理过期BIUseing信息...");
			tx=session.beginTransaction();
			tx.begin();
			query=session.createQuery("delete from BIUseing where loginTime < ?");
			query.setTimestamp(0, time);
			int result=query.executeUpdate();
			tx.commit();
			logger.info("完成清理过期BIUseing信息...");
		} catch (Exception e) {
			tx.rollback();
			logger.error("清理过期shop信息时出现异常:",e);
		}finally{
			HibernateUtil.closeSession(session);
		}
		
	}
}
