/**
 * 
 */
package db.model;


import org.apache.log4j.Logger;
import db.service.DataBaseImp;
import db.service.IDManager;

/**
 * @author liuzg
 * 玩家非基本信息
 */
public class PlayerNonBasicInfo implements DataBaseEntry {
	private static Logger logger=Logger.getLogger(PlayerNonBasicInfo.class);
	private PlayerNonBasicInfo(){};
	public static PlayerNonBasicInfo create(){
		PlayerNonBasicInfo info=new PlayerNonBasicInfo();
		info.id=IDManager.getInstance().getCurrentPlayerNonBasicInfoID();
		return info;
	}
	private int id;
	private int version;
	
	/**
	 * 持有人
	 */
    private int holder;
    
   
	/* (non-Javadoc)
	 * @see db.model.DataBaseEntry#initDBEntry(db.model.Player)
	 */
	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());
//		boolean isSave=false;
//		if(isSave){
//        try {
//        	DataBaseImp.getInstance().save(this);
//		} catch (Exception e) {
//			logger.error("初始化玩家非基本信息异常:",e);
//		
//		}
//		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getHolder() {
		return holder;
	}
	public void setHolder(int holder) {
		this.holder = holder;
	}
	/**
	 * 将数据库信息转换为List,在玩家登录时调用
	 */
	public void convertToList() {

	}
}
