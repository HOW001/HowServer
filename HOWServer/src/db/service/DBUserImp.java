package db.service;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import common.Logger;

import db.model.User;

public class DBUserImp extends DataBaseImp {
	private static Logger logger = Logger.getLogger(DBUserImp.class);
	public static final Integer RESULT_SUCCESS = 0;// 成功
	public static final Integer RESULT_NAME_EXSITE = 1;// 用户名已存在
	public static final Integer RESULT_DB_ERROR = 2;// 数据库异常
	public static final Integer RESULT_NAMEORPWD_ERROR=3;//用户名或密码错误

	private DBUserImp() {
	};

	private static DBUserImp instance = null;

	public static DBUserImp getInstance() {
		if (instance == null) {
			instance = new DBUserImp();
		}
		return instance;
	}
	/**
	 * @author liuzhigang
	 * @param userName
	 * @param pwd
	 * @return
	 *  登录检测
	 */
    public Object[] isLoginSuccess(String userName,String pwd){
    	Session session = HibernateUtil.getSession();
		try {
			Query query = session.createQuery("from User where userName = ? and pwd = ?");
			query.setString(0,userName);
			query.setString(1, pwd);
			query.setMaxResults(1);
			if (query.list() != null && query.list().size() > 0) {
				User user=(User)query.list().get(0);
				logger.info("userName="+userName+",pwd="+pwd+"登录检测成功!");
				return new Object[]{RESULT_SUCCESS,user};
			}
			logger.info("userName="+userName+",pwd="+pwd+"登录检测失败!");
			return new Object[]{RESULT_NAMEORPWD_ERROR};
		} catch (Exception e) {
			logger.error("登录检测时出现异常:",e);
			return new Object[]{RESULT_DB_ERROR};
		} finally {
			HibernateUtil.closeSession(session);
		}
    }
	/**
	 * @author liuzhigang
	 * @param user
	 * @return
	 * 创建用户
	 */
	public synchronized int createUser(User user) {
		Session session = HibernateUtil.getSession();
		try {
			Query query = session.createQuery("from User where userName = ?");
			query.setString(0, user.getUserName());
			if (query.list() != null && query.list().size() > 0) {
				logger.error(user.getUserName() + "已存在!");
				return RESULT_NAME_EXSITE;
			}
			Transaction tx = session.beginTransaction();
			tx.begin();
			session.save(user);
			tx.commit();
			return RESULT_SUCCESS;
		} catch (Exception e) {
			logger.error("创建账户时出现异常:",e);
			return RESULT_DB_ERROR;
		} finally {
			HibernateUtil.closeSession(session);
		}
	}
}
