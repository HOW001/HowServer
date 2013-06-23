package db.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;


/**
 * 
 * @author liuzhigang 游戏中使用的所有ID,均在此管理
 */
public class IDManager {
	private static Logger logger=Logger.getLogger(IDManager.class);
	private IDManager() {
	};

	private static IDManager instance = null;

	public static IDManager getInstance() {
		if (instance == null) {
			instance = new IDManager();
		}
		return instance;
	}
	
	private AtomicInteger playerID=new AtomicInteger(0);
	private AtomicInteger heroID=new AtomicInteger(0);
	private AtomicInteger itemID=new AtomicInteger(0);
	private AtomicInteger mailID=new AtomicInteger(0);
	private AtomicInteger playerCompetitionInfoID=new AtomicInteger(0);
	private AtomicInteger playerNonBasicInfoID=new AtomicInteger(0);
	private AtomicInteger playerPackID=new AtomicInteger(0);
	private AtomicInteger userID=new AtomicInteger(0);
	private AtomicInteger livenessID=new AtomicInteger(0);
	private AtomicInteger jitanID=new AtomicInteger(0);
	
	/**
	 * @author liuzhigang
	 * @return
	 * 取得当前的JiTanID
	 */
	public int getCurrentJiTanID(){
		return jitanID.incrementAndGet();
	}
	
	/**
	 * @author liuzhigang
	 * @return
	 * 取得当前的livenessID
	 */
	public int getCurrentLivenessID(){
		return livenessID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前用的userID
	 */
	public int getCurrentUserID(){
		return userID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的playerPackID
	 */
	public int getCurrentPlayerPackID(){
		return playerPackID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的PlayerNonBasicInfoID
	 */
	public int getCurrentPlayerNonBasicInfoID(){
		return playerNonBasicInfoID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的playerCompetitionInfoID
	 */
	public int getCurrentPlayerCompetitionInfoID(){
		return playerCompetitionInfoID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的mail.ID
	 */
	public int getCurrentMailID(){
		return mailID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的itemID
	 */
	public int getCurrentItemID(){
		return itemID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取可用的英雄ID
	 */
	public int getCurrentHeroID(){
		return heroID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * @return
	 * 获取当前可用的玩家ID
	 */
	public int getCurrentPlayerID(){
		return playerID.incrementAndGet();
	}
	/**
	 * @author liuzhigang
	 * 初始化所有ID
	 */
	public void initID(){
		logger.info("开始初始化所有ID信息...");
		Session session = HibernateUtil.getSession();
		try {
			logger.info("开始初始化Player.ID信息...");
			Query query = session
					.createQuery("select max(id) from Player");
			query.setMaxResults(1);
			Integer result=null;
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					playerID.set(1000);
					logger.info("未找到任何Player记录...");
				}else{
					playerID.set(result);
					logger.info("当前最大Player.ID="+result);
				}
			}
			logger.info("完成初始化Player.ID信息...");
			logger.info("开始初始化Hero.ID...");
			query=session.createQuery("select max(id) from Hero");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					heroID.set(1000);
					logger.info("未找到任何Hero记录...");
				}else{
					heroID.set(result);
					logger.info("当前最大Hero.ID="+result);
				}
			}
			logger.info("完成初始化Hero.ID...");
			logger.info("开始初始化Item.ID...");
			query=session.createQuery("select max(id) from Item");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					itemID.set(1000);
					logger.info("未找到任何Item记录...");
				}else{
					itemID.set(result);
					logger.info("当前最大Item.ID="+result);
				}
			}
			logger.info("完成初始化Item.ID...");
			logger.info("开始初始化Mail.ID...");
			query=session.createQuery("select max(id) from Mail");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					mailID.set(1000);
					logger.info("未找到任何Mail记录...");
				}else{
					mailID.set(result);
					logger.info("当前最大Mail.ID="+result);
				}
			}
			logger.info("完成初始化Mail.ID...");
			logger.info("开始初始化PlayerCompetitionInfo.ID...");
			query=session.createQuery("select max(id) from PlayerCompetitionInfo");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					playerCompetitionInfoID.set(1000);
					logger.info("未找到任何PlayerCompetitionInfo记录...");
				}else{
					playerCompetitionInfoID.set(result);
					logger.info("当前最大PlayerCompetitionInfo.ID="+result);
				}
			}
			logger.info("完成初始化PlayerCompetitionInfo.ID...");
			logger.info("开始初始化PlayerNonBasicInfo.ID...");
			query=session.createQuery("select max(id) from PlayerNonBasicInfo");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					playerNonBasicInfoID.set(1000);
					logger.info("未找到任何PlayerNonBasicInfo记录...");
				}else{
					playerNonBasicInfoID.set(result);
					logger.info("当前最大PlayerNonBasicInfo.ID="+result);
				}
			}
			logger.info("完成初始化PlayerNonBasicInfo.ID...");
			logger.info("开始初始化PlayerPack.ID...");
			query=session.createQuery("select max(id) from PlayerPack");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					playerPackID.set(1000);
					logger.info("未找到任何PlayerPack记录...");
				}else{
					playerPackID.set(result);
					logger.info("当前最大PlayerPack.ID="+result);
				}
			}
			logger.info("完成初始化PlayerPack.ID...");
			logger.info("开始初始化User.ID...");
			query=session.createQuery("select max(id) from User");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					userID.set(1000);
					logger.info("未找到任何User记录...");
				}else{
					userID.set(result);
					logger.info("当前最大User.ID="+result);
				}
			}
			logger.info("完成初始化User.ID...");
			logger.info("开始初始化Liveness.ID...");
			query=session.createQuery("select max(id) from Liveness");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					livenessID.set(1000);
					logger.info("未找到任何Liveness记录...");
				}else{
					livenessID.set(result);
					logger.info("当前最大Liveness.ID="+result);
				}
			}
			logger.info("完成初始化Liveness.ID...");
			logger.info("开始初始化JiTan.ID...");
			query=session.createQuery("select max(id) from JiTan");
			query.setMaxResults(1);
			if(query.list()!=null && query.list().size()==1){
				result=(Integer)query.list().get(0);
				if(result==null){
					//当前还没有数据记录
					jitanID.set(1000);
					logger.info("未找到任何JiTan记录...");
				}else{
					jitanID.set(result);
					logger.info("当前最大JiTan.ID="+result);
				}
			}
			logger.info("完成初始化JiTan.ID...");
		} catch (Exception e) {
			logger.error("初始化所有ID时出现异常!", e);
			System.exit(0);
		} finally {
			HibernateUtil.closeSession(session);
		}
		logger.info("完成初始化所有ID信息...");
	}
}
