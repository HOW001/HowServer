//package db.service;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.hibernate.HibernateException;
//import org.hibernate.Query;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
//
//import server.ServerEntrance;
//import world.World;
//import common.Logger;
//import db.model.Car;
//import db.model.CarBarn;
//import db.model.Helper;
//import db.model.Player;
//import db.model.Skill;
////import db.model.User;
//
//public class DBTestImp extends DataBaseImp {
//	private static Logger logger=Logger.getLogger(DBTestImp.class);
//	private static DBTestImp instance;
//	public static DBTestImp getInstance(){
//		if(instance==null){
//			instance=new DBTestImp();
//		}
//		return instance;
//	}
//	public void test(){
////		testLoadFirendCar();
////		this.testLogin();
////		testOneToMany();
////		Player p = DBPlayerImp.getInstance().getInitPlayer(6793, (byte)0);
////		Friend f = new Friend();
////		f.initDBEntry(p);
////		f.setHolder(p.getId());
////		f.setFid(6794);
////		f.setRelation(1);
////		
////		DBFriendImp.getInstance().save(f);
//		
//	}
//	/**
//	 * @author liuzg
//	 * 已知:不在线好友名字
//	 * 结果:列出该好友车库中所有玩家的车辆名字
//	 * 操作流程:
//	 * 1.通过玩家名字获取玩家ID
//	 * 2.通过玩家ID获取玩家车库信息
//	 * 3.通过车库信息获取车库中的车辆信息
//	 */
//	public void testLoadFirendCar(){
//		final String nonLineFriendName="L1";
//		Player p=World.getInstance().getPlayerByName(nonLineFriendName);
//		if(p==null){
//			p=World.getInstance().getBufferPlayerByName(nonLineFriendName);
//		}
//		if(p!=null){
//			logger.info("玩家在线");
//			return;
//		}
//		Runnable run=new Runnable(){
//			public void run(){
//				long times = System.currentTimeMillis();
//				int nonLineID=DBPlayerImp.getInstance().getPlayerIDForName(nonLineFriendName);
//				if(nonLineID<=0){
//					logger.info("玩家不存在:name="+nonLineFriendName);
//					return;
//				}
//				CarBarn carBarn=DBCarBarnImp.getInstance().getCarBarnForPlayerID(nonLineID);
//				if(carBarn==null){
//					logger.info("车库不存在:"+nonLineID);
//					return;
//				}
//				List<Integer> carID=new ArrayList<Integer>();
//				for(String str:carBarn.getCarsData().split(",")){
//					carID.add(Integer.parseInt(str));
//				}
//				List<Car> carList=DBCarImp.getInstance().getCar(carID);
//				if(carList==null || carList.size()==0){
//					logger.info("车库没有停驻车辆");
//					return;
//				}
//				for(Car car:carList){
//					logger.info("停驻车辆:"+car.getCarName());
//				}
//				//用完后请手动释放一下
//				carBarn=null;
//				carList.clear();
//				carList=null;
//				long useTimes = System.currentTimeMillis() - times;
//				if(useTimes>=100){
//					logger.error("clearAll()线程运行时间过长" + useTimes);
//				}
//			}
//		};
//		ServerEntrance.runThread(run);
//	}
//	/**
//	 * @author liuzg
//	 * 登录流程测试
//	 */
//	public void testLogin() {
//		try {
//			for (int index = 1; index < 5000; index++) {			
//				String userName = "L" + index;
////				User user = ManagerDBUpdate.getInstance().getUser(userName);
//				Player player;
////				if (user == null) {
////					user = DBUserImp.getInstance().getUser(userName);
////					if (user == null) {// 用户不存在
////						user = new User(userName, userName);
////						user.setRole0_ip("127.0.0.1");
////						DBUserImp.getInstance().save(user);
////					}
////				}
//				String serverName="测试一区";
//				int playerID=DBPlayerImp.getInstance().getPlayerIDForOpenID(userName,serverName);
//				player = DBPlayerImp.getInstance().getInitPlayer(playerID);
//				if (player == null) {
////					player = Player.createPlayer("L" + index, 1, 1,
////							userName,serverName,"127.0.0.1");
////					user.setRole0_id(player.getId());
////					DBUserImp.getInstance().update(user);
//				} else {
//					logger.info("玩家:" + player.getName() + "车辆数量:"
//							+ player.getCarsEntry().size());
//				}
//				if (player != null) {
//					World.login(player);
//					player.setCurrentCar(player.getCurrentCar());
//				}
//				HibernateUtil.printCacheUseInfo();
//				
//			HibernateUtil.printCacheUseInfo();
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	/**
//	 * @author liuzg
//	 * @param p
//	 * @param car
//	 * 删除一个实体
//	 */
//	public void removeEntry(Player p){
//        Car car=null;
//		if(p.getCarsEntry().size()>0){
//        	for(Car temp:p.getCarsEntry()){
//        		car=temp;
//        		break;
//        	}
//        }else{
//        	return;
//        }
//		Session session=HibernateUtil.getSession();
//		Transaction tx=session.beginTransaction();
//		p.getCarsEntry().remove(car);
//		session.delete(car);
//		tx.commit();
//		session.flush();
//		HibernateUtil.closeSession(session);
//	}
//	/**
//	 * @author liuzg
//	 * @param p
//	 * @param car
//	 * 添加一个实体
//	 */
//	public void addEntry(Player p) {
//try {
//			for (int i = 1; i <= 5; i++) {
//	//			Helper helper=new Helper();
//	//			helper.initDBEntry(p);
//	//			helper.setName("aaa");
//				Car car = new Car();
//				car.initDBEntry(p,p.getCarType(),false);
//				car.setCarName("liu"+i);
//	//			Skill skill=new Skill();
//	//			skill.initDBEntry(p);
//	//			skill.setSkills("aaa");
//	//			p.setSkillEntry(skill);
////			    Title title=new Title();
////			    title.initDBEntry(p);
//				Session session = HibernateUtil.getSession();
//				Transaction tx = session.beginTransaction();
//				session.saveOrUpdate(car);
//				tx.commit();
//				session.flush();
//				HibernateUtil.closeSession(session);
//			}
//} catch (HibernateException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//	}
//	/**
//	 * @author liuzg
//	 * 设置一对一
//	 * 支持添加更新，不支持子对象主动的删除
//	 */
//	public void testOneToOne(){
//		try {
//			Session session=HibernateUtil.getSession();
//			Query query=session.createQuery("from Player where ID=:id");
//			query.setInteger("id",1);
//			query.setMaxResults(1).uniqueResult();
//			Player player=(Player)query.list().get(0);
//			logger.info("name="+player.getName());
//			Set<Skill> skillsEntry = player.getSkillsEntry();
//			for(Skill skill:skillsEntry){
//				if(skill==null){
//					skill=new Skill();
//					skill.initDBEntry(player);
//					player.addSkillEntry(skill);
//					logger.info("id=="+skill.getHolder());
//					session.saveOrUpdate(player);
//					session.flush();
//				
//				}	
//			}
////			Skill skill=player.getSkillEntry();
////			if(skill==null){
////				skill=new Skill();
////				skill.initDBEntry(player);
////				player.setSkillEntry(skill);
////			
////			}			
////			logger.info("id=="+skill.getHolder());
////			session.saveOrUpdate(player);
////			session.flush();
////			skill.setSkills("000");
//			session.saveOrUpdate(player);
//			session.flush();
//		} catch (HibernateException e) {
//			logger.error("异常:",e);
//		}
//	
//	}
//	/**
//	 * @author liuzg
//	 * 设置一对多
//	 */
//	public  void testOneToMany(){
//		Session session=HibernateUtil.getSession();
//		try {
//			
//			Query query=session.createQuery("from Player where ID=:id");
//			query.setInteger("id",1000);
//			query.setMaxResults(1).uniqueResult();
//			Player player=(Player)query.list().get(0);
//			logger.info("name="+player.getName());
//			Car temp=null;
//			for(Car car:player.getCarsEntry()){
//				logger.info("carName="+car.getCarName());
//				temp=car;
//			}
//			if(player.getCarsEntry()==null || player.getCarsEntry().size()==0){
//				temp=new Car();
//				temp.initDBEntry(player,player.getCarType(),false);
//			}
//			temp.setMark(300);
//			session.saveOrUpdate(player);
//			session.flush();
//		} catch (HibernateException e) {
//			logger.error("异常:",e);
//			e.printStackTrace();
//		}finally{
//			HibernateUtil.closeSession(session);
//		}
//	}
//	/**
//	 * @author liuzg
//	 * 针对二级缓存使用的一个测试
//	 */
//	public  void testCache(){
////		for(int i=0;i<10;i++){
////			Session session =HibernateUtil.getSession();	
////			Transaction tx=session.beginTransaction();
////			Car car;
////			Query query=session.createQuery("from Car where mark>= ? " );
////			query.setInteger(0, 0);
////			/**
////			 * query.iterate()方法的搜索方式先从缓存中以ID查找，如果未查到则从数据库加载，但是一个条件一条语句的形式
////			 */
////			Iterator it=query.iterate();
////			while(it.hasNext()){	
////				car=(Car)it.next();
////				logger.info(car.getCarName());
////			}
////			HibernateUtil.printCacheUseInfo();
////			/**
////			 * query.list()方法直接从数据库中加载，之后再筛选，如果数据量较小，可以作用这种方法，但海量数据则内存难以承受
////			 */
//////			List list=query.list();
//////			for(int index=0;index<list.size();index++){
//////				car=(Car)list.get(index);
//////				logger.info(car.getCarName());
//////			}
////			HibernateUtil.closeSession(session);
////			}
//		for(int i=0;i<100;i++){
//		Session session =HibernateUtil.getSession();	
//		Transaction tx=session.beginTransaction();
//		
//		Query query=session.createQuery("from Test where a>? " );
//		query.setInteger(0,500);
//		/**
//		 * query.iterate()方法的搜索方式先从缓存中以ID查找，如果未查到则从数据库加载，但是一个条件一条语句的形式
//		 */
//		Iterator it=query.iterate();
//		while(it.hasNext()){	
//
////			test=(Test)it.next();	
////			test.getB();
//			
//		}
//		HibernateUtil.printCacheUseInfo();
//		logger.info("执行一次查询");
//		/**
//		 * query.list()方法直接从数据库中加载，之后再筛选，如果数据量较小，可以作用这种方法，但海量数据则内存难以承受
//		 */
////		List list=query.list();
////		for(int index=0;index<list.size();index++){
////			car=(Car)list.get(index);
////			logger.info(car.getCarName());
////		}
//		tx.commit();
//		HibernateUtil.closeSession(session);	
//		}
//	}
//	/**
//	 * @author liuzg
//	 * 针对乐观锁的一个测试
//	 */
//	public static void testVersion(){
//		 try {
//			Session session =HibernateUtil.getSession();	
//			Transaction tx=session.beginTransaction();
//			tx.begin();
////		 Query query=session.createQuery("from Car where holder= ? " );
////			query.setInteger(0, 1);
////			Car car1;
////			Car car2;
////			if(query.list()==null || query.list().size()<=0){
////				car1=new Car();
////				car1.setHolder(1);
////				car1.setCarName("liuzg");
////			    car1.setMark(1000);
////			    DBCarImp.getInstance().save(session,car1);
////			}else{
////				 car1=(Car)query.list().get(0);
////			}
////			HibernateUtil.closeSession(session);
////			session=HibernateUtil.getSession();
////			query=session.createQuery("from Car where holder= ? " );
////			query.setInteger(0, 1);
////			 car2=(Car)query.list().get(0);
////		System.out.println("car1Version="+car1.getVersion());
////		System.out.println("car21Version="+car2.getVersion());
////		test_car1(car1);
////		test_car2(car2);
//			
//			
//			 Query query=session.createQuery("from Helper where holder= ? " );
//				query.setInteger(0, 1);
//				
//				Helper helper1;
//				Helper helper2;
//				if(query.list()==null || query.list().size()<=0){
//					helper1=new Helper();
//					helper1.setHolder(1);
//					helper1.setName("liuzg");
//					helper1.setMark(1000);
////				    DBCarImp.getInstance().save(session,helper1);
//				    session.save(helper1);
//				}else{
//					helper1=(Helper)query.list().get(0);
//				}
////			HibernateUtil.closeSession(session);
//				tx.commit();
//				session=HibernateUtil.getSession();
//				tx=session.beginTransaction();
//				tx.begin();
//				query=session.createQuery("from Helper where holder= ? " );
//				query.setInteger(0, 1);
//				helper2=(Helper)query.list().get(0);
//				tx.commit();
////			HibernateUtil.closeSession(session);
////			for(int i=1;i<=5;i++){
//					test_helper1(helper1);
////			}
////		test_helper2(helper2);
//					
//		} catch (HibernateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	public static void test_helper2(Helper helper){
//		Session session=HibernateUtil.getSession();
//		
//        Transaction tx=session.beginTransaction();
//        try {
//			tx.begin();
//			helper.setName("liuzg");
//			System.out.println("2:mark:="+helper.getMark());
//			helper.setMark(helper.getMark()-1000);
//			System.err.println("helper2此处执行就错误!");
//			session.update(helper);
//			tx.commit();
//		} catch (Exception e) {
//			logger.error("更新实体时出现异常:",e);
//			tx.rollback();
//		}finally{
//			HibernateUtil.closeSession(session);
//		}
//	}
//	public static void test_helper1(Helper helper){
//		for(int index=0;index<=9;index++){
//				 Session session=HibernateUtil.getSession();
//		         Transaction tx=session.beginTransaction();
//		         try {tx.begin();	        			 			
//		 			helper.setMark(helper.getMark()-100);
//		 			System.out.println("1:mark:="+helper.getMark()+",session="+session);
//		 			session.update(helper);
//		        	tx.commit();
//		 		} catch (Exception e) {
//		 			logger.error("更新实体时出现异常:",e);
//		 			tx.rollback();
//		 		}finally{
////		 			HibernateUtil.closeSession(session);
//		 		}
//		}
//	}
//	public static void test_car2(final Car car){
//		
//		
//		 Session session=HibernateUtil.getSession();
//         Transaction tx=session.beginTransaction();
//         try {
// 			tx.begin();
// 			car.setCarName("liuzg");
// 			System.out.println("2:mark:="+car.getMark()+",version="+car.getVersion());
// 		    System.err.println("car2此处执行就错误!");
// 		    car.setMark(car.getMark()-100);
// 		   session.update(car);
// 		   
// 			tx.commit();
// 		} catch (Exception e) {
// 			logger.error("更新实体时出现异常:",e);
// 			tx.rollback();
// 		}finally{
// 			HibernateUtil.closeSession(session);
// 		}
//	}
//	public static void test_car1(final Car car){
//				 Session session=HibernateUtil.getSession();
//		         Transaction tx=session.beginTransaction();
//		         try {
//		 			tx.begin();
//		 			car.setCarName("fengmx");
//		 			car.setMark(car.getMark()-100);
//		 			System.out.println("1:mark:="+car.getMark()+",version="+car.getVersion());
//		 			session.update(car);
//		 			tx.commit();
//		 		} catch (Exception e) {
//		 			logger.error("更新实体时出现异常:",e);
//		 			tx.rollback();
//		 		}finally{
//		 			HibernateUtil.closeSession(session);
//		 		}
//	}
//	/**
//	 * @author liuzg
//	 * 测试对象缓存压力
//	 * 测试结果:
//	 * 内session平均3000毫秒
//	 * 外session 外flush 平均1700毫秒
//	 * 外session 内flush 平均1800毫秒
//	 */
//	public static void testCacheTime(){
//		/**外session 内flush方式*/
//		Session session = HibernateUtil.getSession();
//		Transaction tx = session.beginTransaction();
////		try {
////			tx.begin();
////			long times=System.currentTimeMillis();
////			for(int index=1;index<=1000;index++){
////				Car car=new Car();
////				car.setHolder(index);
////				car.setCarName("c"+index);
////				car.setMark(car.getMark()+1);	
////				DBCarImp.getInstance().update(session,car);
////				session.flush();
////				session.clear();
////			}			
////			System.out.println("1000个对象用时:"+(System.currentTimeMillis()-times));
////			tx.commit();
////		} catch (Exception e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		
//		/**内session方式*/
//		try {
//			tx.begin();
//			
//			long times=System.currentTimeMillis();
//			for(int index=1;index<=10;index++){
//				Query query=session.createQuery("from Car");
//				query.list();
//				Car car=new Car();
//				car.setHolder(index);
//				car.setCarName("c"+index);
//				car.setMark(car.getMark()+1);	
//				try {
//					DBCarImp.getInstance().update(car);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			System.out.println("1000个对象用时:"+(System.currentTimeMillis()-times));
//			tx.commit();
//		} catch (HibernateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	public static void testMasterKey(){
//		Session session = HibernateUtil.getSession();
//		Transaction tx = session.beginTransaction();
//		try {
//			tx.begin();
//			for(int i=1;i<=1;i++){
//				Skill skill=new Skill();
//				skill.setHolder(2);
////				skill.setSkills("0");
//				session.save(skill);
//			}
//			tx.commit();
//			session.flush();
//		} catch (HibernateException e) {
//			logger.error(e);
//		}finally{
//			HibernateUtil.closeSession(session);
//		}
//		
//	}
//}
