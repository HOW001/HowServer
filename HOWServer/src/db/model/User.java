package db.model;

import java.util.Date;

import db.service.IDManager;

public class User implements DataBaseEntry {
    private User(){};
    public static User create(){
    	User user=new User();
    	user.id=IDManager.getInstance().getCurrentUserID();
    	return user;
    }
	private int id;
	private int version;
	private String userName;
	private String pwd;
	private int point;
	private String stateCode;
	private int lastLoginServerID;
	private Date createTime;
	@Override
	public void initDBEntry(Player p) {
		// TODO Auto-generated method stub

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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public int getLastLoginServerID() {
		return lastLoginServerID;
	}
	public void setLastLoginServerID(int lastLoginServerID) {
		this.lastLoginServerID = lastLoginServerID;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
