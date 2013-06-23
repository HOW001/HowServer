package db.service;

import java.io.File;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.apache.log4j.Logger;

import server.ServerConfigurationNew;
import server.ServerEntrance;

/**
 * Hibernate基本配置
 * @author liuzg
 */
public class HibernateUtil {
	private static Logger logger = Logger.getLogger(HibernateUtil.class);
	private static Configuration configuration; // 一个hibernate配置环境
	private static SessionFactory sessionFactory; // 一个安全session连接
	public static long sessionOpenTimes=0;
	private static File serverFile;
	private static String url;
	private static String user;
	private static String pwd;
	static {
		try {
			configuration = new Configuration().setInterceptor(new HibernateInterceptor());
			serverFile = new File(ServerEntrance.serverPath+"provider/hibernate.cfg.xml");
			if (serverFile.exists() == false) {
				logger.info("服务器配置文件不存在:" + serverFile.getPath());
				throw new HibernateException("服务器配置文件不存在:");
			}
			sessionFactory = configuration.configure(serverFile)
					.buildSessionFactory();
			url = configuration.getProperty("connection.url");
			user = configuration.getProperty("connection.username");
			pwd = configuration.getProperty("connection.password");
			
		} catch (HibernateException e) {
			logger.error("加载hibernate配置文件失败", e);
		}
	}

	/**
	 * 加载这个类
	 * 
	 */
	public static void loadHibernate() {
		logger.info("ServerDB_URL:" + url);
		logger.info("ServerDB_User:" + user);
		logger.info("ServerDB_PWD:" + pwd);
		logger.info("加载hibernate配置文件成功");
		logger.info("加载hibernate配置文件成功");
	}

	/**
	 * 得到一个数据库连接
	 * 
	 * @return
	 * @throws DbException
	 * 一个session的使用步骤:
	 * 1.打开session
	 * 2.开启事务
	 * 3.处理相关内容
	 * 4.同步sesssion即flush
	 * 5.提交事务
	 * 6.处理异常
	 * 7.关闭session
	 */
	public static Session getSession() throws HibernateException {
		try {
			Session session = sessionFactory.openSession(); //实现单线程session，支持不同session的各自close
//			Session session = sessionFactory.getCurrentSession();//实现多个操作共用一个session，需要在配置文件中增加<property name="current_session_context_class">thread</property>
			session.beginTransaction();
			return session;
		} catch (HibernateException e) {
			logger.error("HibernateUtil.getSession异常", e);
			ServerEntrance.shutdown("数据库连接出现异常");
			throw new HibernateException(e.getMessage());
		}
	}

	/**
	 * 关闭连接池工厂
	 * 
	 */
	public static void close() {
		try {
			sessionFactory.close();
		} catch (HibernateException e) {
			logger.error("HibernateUtil.close异常", e);
		}
	}

	/**
	 * 关闭一个连接
	 * 
	 * @param session
	 * @throws DbException
	 */
	public static void closeSession(Session session) {
		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (HibernateException e) {
			logger.error("HibernateUtil.closeSession异常", e);
		}
	}

	/**
	 * 回滚一个事务
	 * 
	 * @param transaction
	 * @throws DbException
	 */
	public static void rollbackTransaction(Transaction transaction) {
		try {
			if (transaction != null) {
				transaction.rollback();
			}
		} catch (HibernateException e) {
			logger.error("HibernateUtil.rollbackTransaction异常", e);
		}
	}
   /**
    * @author liuzg
    * 打印缓存使用信息
    */
   public static void printCacheUseInfo(){
	   /*正式部署时关闭此打印信息*/
		if (ServerConfigurationNew.debug) {
			Statistics stat = sessionFactory.getStatistics();
			logger.info("二级缓存放入率:" + stat.getSecondLevelCachePutCount());
			logger.info("二级缓存命中率:" + stat.getSecondLevelCacheHitCount());
			logger.info("二级缓存miss率:" + stat.getSecondLevelCacheMissCount());
		}
	   logger.info("session打开次数:"+sessionOpenTimes);
   }
}
