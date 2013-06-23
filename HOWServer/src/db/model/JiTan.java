package db.model;

import java.util.Calendar;
import java.util.Date;

import db.service.IDManager;

public class JiTan implements DataBaseEntry {
	/*
	 * 刷新类型
	 */
	public static final int FRESH_TYPE_WHITE = 1001;
	public static final int FRESH_TYPE_GREEN = 1002;
	public static final int FRESH_TYPE_BLUE = 1003;
	public static final int FRESH_TYPE_PURPLE = 1004;
	/*
	 * 1天之内的免费刷新次数
	 */
	public static final int FRESH_TIMES_WHITE = 10;
	public static final int FRESH_TIMES_GREEN = 5;
	public static final int FRESH_TIMES_BLUE = 2;

	/*
	 * 刷新间隔,单位秒
	 */
	public static final int FRESH_INTERVAL_WHITE = 10*60;
	public static final int FRESH_INTERVAL_GREEN = 60*60;
	public static final int FRESH_INTERVAL_BLUE = 6 * 60*60;
	public static final int FRESH_INTERVAL_PURPLE = 3 * 24 * 60*60;

	private JiTan() {
	};

	public static JiTan create() {
		JiTan jitan = new JiTan();
		jitan.id = IDManager.getInstance().getCurrentJiTanID();
		return jitan;
	}

	private int id;
	private int version;
	private int holder;

	private Date whiteFreshTime;// 白色刷新时间
	private int whiteFreeFreshDay;// 白色免费刷新当天天数
	private int whiteFreeFreshTimes;// 白色当天免费刷新次数

	private Date greenFreshTime;// 绿色刷新时间
	private int greenFreeFreshDay;// 绿色免费刷新当天天数
	private int greenFreeFreshTimes;// 绿色当天刷新次数

	private Date blueFreshTime;// 蓝色刷新时间
	private int blueFreeFreshDay;// 蓝色免费刷新当天天数
	private int blueFreeFreshTimes;// 蓝色当天刷新次数

	private Date purpleFreshTime;// 紫色刷新时间
	private int purpleFreeFreshDay;// 紫色免费刷新当天天数
	private int purpleFreeFreshTimes;// 紫色当天刷新次数
    
	/**
	 * @author liuzhigang
	 * @param type
	 * @return
	 * 获取刷新剩余时间
	 * 单位秒
	 */
	public int getFreshResidualTime(int type){
		switch(type){
		case FRESH_TYPE_WHITE:
			return (int)(FRESH_INTERVAL_WHITE-(System.currentTimeMillis()-whiteFreshTime.getTime())/1000);
		case FRESH_TYPE_GREEN:
			return (int)(FRESH_INTERVAL_GREEN-(System.currentTimeMillis()-greenFreshTime.getTime())/1000);
		case FRESH_TYPE_BLUE:
			return (int)(FRESH_INTERVAL_BLUE-(System.currentTimeMillis()-blueFreshTime.getTime())/1000);
		case FRESH_TYPE_PURPLE:
			return (int)(FRESH_INTERVAL_PURPLE-(System.currentTimeMillis()-purpleFreshTime.getTime())/1000);
		default:
			return 1000;
		}
	}
	/**
	 * @author liuzhigang
	 * @param type
	 * @return
	 * 获取当天指定类型的刷新次数
	 */
	public int getFreeFreshTimes(int type){
		switch(type){
		case FRESH_TYPE_WHITE:
			return whiteFreeFreshTimes;
		case FRESH_TYPE_GREEN:
			return greenFreeFreshTimes;
		case FRESH_TYPE_BLUE:
			return blueFreeFreshTimes;
		case FRESH_TYPE_PURPLE:
			return purpleFreeFreshTimes;
		default:
			return -1;
		}

	}
	/**
	 * @author liuzhigang
	 * @param type
	 * @return
	 * 获取指定祭坛的刷新时间
	 */
	public Date getFreshTime(int type){
		switch(type){
		case FRESH_TYPE_WHITE:
			return whiteFreshTime;
		case FRESH_TYPE_GREEN:
			return greenFreshTime;
		case FRESH_TYPE_BLUE:
			return blueFreshTime;
		case FRESH_TYPE_PURPLE:
			return purpleFreshTime;
		default:
			return new Date();
		}
	}
	/**
	 * @author liuzhigang
	 * @param type
	 * @return 免费刷新
	 */
	public boolean freeFresh(int type) {
		boolean isCan = isCanFreeFresh(type);
		if (isCan) {
			Calendar now = Calendar.getInstance();
			int days = now.get(Calendar.DAY_OF_YEAR);
			switch (type) {
			case FRESH_TYPE_WHITE:
				whiteFreshTime.setTime(System.currentTimeMillis());
				if (whiteFreeFreshDay == days) {
					whiteFreeFreshTimes++;
				} else {
					whiteFreeFreshTimes = 1;
				}
				whiteFreeFreshDay = days;
				break;
			case FRESH_TYPE_GREEN:
				greenFreshTime.setTime(System.currentTimeMillis());
				if (greenFreeFreshDay == days) {
					greenFreeFreshTimes++;
				} else {
					greenFreeFreshTimes = 1;
				}
				greenFreeFreshDay = days;
				break;
			case FRESH_TYPE_BLUE:
				blueFreshTime.setTime(System.currentTimeMillis());
				if (blueFreeFreshDay == days) {
					blueFreeFreshTimes++;
				} else {
					blueFreeFreshTimes = 1;
				}
				blueFreeFreshDay = days;
				break;
			case FRESH_TYPE_PURPLE:
				purpleFreshTime.setTime(System.currentTimeMillis());
				purpleFreeFreshDay = days;
				purpleFreeFreshTimes = 1;
				break;
			default:
				isCan = false;
			}
		}
		return isCan;
	}

	/**
	 * @author liuzhigang
	 * @param type
	 * @return 是否能够免费刷新
	 */
	public boolean isCanFreeFresh(int type) {
		boolean isCan = true;
		Calendar now = Calendar.getInstance();
		int days = now.get(Calendar.DAY_OF_YEAR);
		switch (type) {
		case FRESH_TYPE_WHITE:
			if (now.getTime().getTime() - whiteFreshTime.getTime() >= FRESH_INTERVAL_WHITE
					* 1000) {
				if (days == whiteFreeFreshDay) {
					if (whiteFreeFreshTimes >= FRESH_TIMES_WHITE) {
						isCan = false;
					}
				}
			} else {// 不满足最小间隔
				isCan = false;
			}
			break;
		case FRESH_TYPE_GREEN:
			if (now.getTime().getTime() - greenFreshTime.getTime() >= FRESH_INTERVAL_GREEN
					* 1000) {
				if (days == greenFreeFreshDay) {
					if (greenFreeFreshTimes >= FRESH_TIMES_GREEN) {
						isCan = false;
					}
				}
			} else {
				isCan = false;
			}
			break;
		case FRESH_TYPE_BLUE:
			if (now.getTime().getTime() - blueFreshTime.getTime() >= FRESH_INTERVAL_BLUE
					* 1000) {
				if (days == blueFreeFreshDay) {
					if (blueFreeFreshTimes >= FRESH_TIMES_BLUE) {
						isCan = false;
					}
				}
			} else {
				isCan = false;
			}
			break;
		case FRESH_TYPE_PURPLE:
			if (now.getTime().getTime() - purpleFreshTime.getTime() >= FRESH_INTERVAL_PURPLE
					* 1000) {
				// 3天刷新一次
			} else {
				isCan = false;
			}
			break;
		default:
			isCan = false;
		}
		return isCan;
	}

	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());

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

	public Date getWhiteFreshTime() {
		return whiteFreshTime;
	}

	public void setWhiteFreshTime(Date whiteFreshTime) {
		this.whiteFreshTime = whiteFreshTime;
	}

	public int getWhiteFreeFreshDay() {
		return whiteFreeFreshDay;
	}

	public void setWhiteFreeFreshDay(int whiteFreeFreshDay) {
		this.whiteFreeFreshDay = whiteFreeFreshDay;
	}

	public int getWhiteFreeFreshTimes() {
		return whiteFreeFreshTimes;
	}

	public void setWhiteFreeFreshTimes(int whiteFreeFreshTimes) {
		this.whiteFreeFreshTimes = whiteFreeFreshTimes;
	}

	public Date getGreenFreshTime() {
		return greenFreshTime;
	}

	public void setGreenFreshTime(Date greenFreshTime) {
		this.greenFreshTime = greenFreshTime;
	}

	public int getGreenFreeFreshDay() {
		return greenFreeFreshDay;
	}

	public void setGreenFreeFreshDay(int greenFreeFreshDay) {
		this.greenFreeFreshDay = greenFreeFreshDay;
	}

	public int getGreenFreeFreshTimes() {
		return greenFreeFreshTimes;
	}

	public void setGreenFreeFreshTimes(int greenFreeFreshTimes) {
		this.greenFreeFreshTimes = greenFreeFreshTimes;
	}

	public Date getBlueFreshTime() {
		return blueFreshTime;
	}

	public void setBlueFreshTime(Date blueFreshTime) {
		this.blueFreshTime = blueFreshTime;
	}

	public int getBlueFreeFreshDay() {
		return blueFreeFreshDay;
	}

	public void setBlueFreeFreshDay(int blueFreeFreshDay) {
		this.blueFreeFreshDay = blueFreeFreshDay;
	}

	public int getBlueFreeFreshTimes() {
		return blueFreeFreshTimes;
	}

	public void setBlueFreeFreshTimes(int blueFreeFreshTimes) {
		this.blueFreeFreshTimes = blueFreeFreshTimes;
	}

	public Date getPurpleFreshTime() {
		return purpleFreshTime;
	}

	public void setPurpleFreshTime(Date purpleFreshTime) {
		this.purpleFreshTime = purpleFreshTime;
	}

	public int getPurpleFreeFreshDay() {
		return purpleFreeFreshDay;
	}

	public void setPurpleFreeFreshDay(int purpleFreeFreshDay) {
		this.purpleFreeFreshDay = purpleFreeFreshDay;
	}

	public int getPurpleFreeFreshTimes() {
		return purpleFreeFreshTimes;
	}

	public void setPurpleFreeFreshTimes(int purpleFreeFreshTimes) {
		this.purpleFreeFreshTimes = purpleFreeFreshTimes;
	}

}
