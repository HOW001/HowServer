package db.model;

import common.Logger;

import db.service.IDManager;
import util.binreader.HeroData;
import world.object.Creature;
/**
 * 
 * @author liuzhigang
 * 英雄
 */
public class Hero extends Creature implements DataBaseEntry {
	private static Logger logger=Logger.getLogger(Hero.class);
	private int version;
	private int holder;//拥有者
	
	private Hero(){
		this(TYPE_HERO);
	}
	private Hero(byte type) {
		super(type);
	}
    public static Hero create(int code){
    	Hero hero=new Hero();
    	hero.setId(IDManager.getInstance().getCurrentHeroID());
    	hero.setCode(code);
    	return hero;
    }
	@Override
	public void initDBEntry(Player p) {
		this.setHolder(p.getId());

	}

	@Override
	protected void generateGameID() {
		gameID = type << 28;
		gameID += generated;
	}
	public int getHolder() {
		return holder;
	}
	public void setHolder(int holder) {
		this.holder = holder;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	@Override
	public int isHaveAttackSkill() {
		// TODO Auto-generated method stub
		return 0;
	}
	public HeroData getData(){
		HeroData data=HeroData.getHeroData(code);
		if(data==null){
			logger.error("无法找到英雄数据:"+code);
			data=HeroData.getHeroData(10001);
		}
		return data;
	}
	@Override
	public int isHaveShieldSkill() {
		// TODO Auto-generated method stub
		return 0;
	}
}
