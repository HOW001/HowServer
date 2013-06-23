package db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import db.service.IDManager;

/**
 * 邮件
 * 
 * @author liusong
 * 
 */
public class Mail implements DataBaseEntry {

	public static final long MAX_SAVE_TIME = 15 * 24 * 60 * 60 * 1000;
	public static final byte MAILTYPE_ALL = 1;// 全服邮件
	public static final byte MAILTYPE_SINGLE = 2;// 单人邮件
	public static final int MONEY_TYPE = 1;// 金钱类型
	public static final byte MAILSTATE_UNREAD = 0;// 未读
	public static final byte MAILSTATE_READ = 1;// 已读
	public static final byte MAILSTATE_GETAWARDS = 2;// 已收取物品
	public static final byte MAILSTATE_DELETE = 3;// 已手动删除

	private static Logger logger = Logger.getLogger(Mail.class);
	public static boolean canDelete = true;// 是否能够删除
	private int id;// id
	private int version;// 版本号
	private int holder;// 对应玩家id
	private String title;// 标题
	private String content;// 信件内容
	private String addresser;// 发件人
	private String itemsData;// 目前最多为5个，暂定一个非常充裕的长度varchar（128）
	private byte mailState;// 邮件状态,0为未读，1为已读，2为已收取奖励，3为个人删除
	private long acceptTime;// 收信的时间戳
	private byte mailType;// 邮件类型
	private int otherMailId = -1;// 对应全服邮件id
	private Map<Integer, Integer> items;

	private Mail() {
	}
    public static Mail create(){
    	Mail mail=new Mail();
    	mail.id=IDManager.getInstance().getCurrentMailID();
    	return mail;
    }
	public Mail(int holder, String title, String content, String addresser,
			byte mailState, long acceptTime, byte mailType,
			Map<Integer, Integer> items) {
		this.id=IDManager.getInstance().getCurrentMailID();
		this.holder = holder;
		this.title = title;
		this.content = content;
		this.addresser = addresser;
		this.mailState = mailState;
		this.acceptTime = acceptTime;
		this.mailType = mailType;
		this.items = items;
	}

	/**
	 * 从数据库取出数据时调用
	 */
	public void init() {
		try {
			items = new HashMap<Integer, Integer>();
			if (itemsData == null || "".equals(itemsData)) {
				return;
			}
			String[] split = StringUtils.split(itemsData, ",");
			int length = split.length;
			for (int i = 0; i < length; i++) {
				if ("".equals(split[i])) {
					continue;
				}
				String[] split2 = StringUtils.split(split[i], ":");
				items.put(Integer.parseInt(split2[0]), Integer
						.parseInt(split2[1]));
			}
		} catch (Exception e) {
			logger.error("玩家(id为：" + holder + ")，mail.init()出错", e);
		}
	}

	/**
	 * 保存数据前调用
	 */
	public void beforeSave(){
		if(items == null ){
			items = new HashMap<Integer, Integer>();
		}
		if(items.size() == 0){
			itemsData = "";
			return ;
		}
		Set<Entry<Integer, Integer>> entrySet = items.entrySet();
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Integer> entry : entrySet) {
			if(entry == null){
				continue;
			}
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
		}
		if(sb.length() > 0){
			sb.deleteCharAt(sb.length() - 1 );
		}
		itemsData = sb.toString();
	}
	
	public long getAcceptTime() {
		return acceptTime;
	}

	public void setAcceptTime(long acceptTime) {
		this.acceptTime = acceptTime;
	}

	public byte getMailState() {
		return mailState;
	}

	public void setMailState(byte mailState) {
		this.mailState = mailState;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAddresser() {
		return addresser;
	}

	public void setAddresser(String addresser) {
		this.addresser = addresser;
	}

	public String getItemsData() {
		return itemsData;
	}

	public void setItemsData(String itemsData) {
		this.itemsData = itemsData;
	}

	public Map<Integer, Integer> getItems() {
		return items;
	}

	public void setItems(Map<Integer, Integer> items) {
		this.items = items;
	}

	public byte getMailType() {
		return mailType;
	}

	public void setMailType(byte mailType) {
		this.mailType = mailType;
	}

	public int getOtherMailId() {
		return otherMailId;
	}

	public void setOtherMailId(int otherMailId) {
		this.otherMailId = otherMailId;
	}

	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());
	}

}
