package world.object;
import org.apache.log4j.Logger;
import util.binreader.MonsterData;
import db.model.Player;

/**
 * 怪物类
 * @see Creature
 * @see Monster
 * @see Player
 * 
 */
public class Monster extends Creature {
	private static Logger logger = Logger.getLogger(Monster.class);

	/**
	 * 继承自父类的构造函数 设为protected是为了子类继承用
	 * 
	 * @param type
	 */
	protected Monster(int type) {
		this();
	}

	/**
	 * 默认的构造函数
	 */
	public Monster() {
		super(TYPE_MOB);
		setState(STATE_RESPAWN);
		generated++;
		generateGameID();
	}
    private MonsterData mobData;
    
    public MonsterData getMobData(){
    	return mobData;
    }

	protected void setData(MonsterData data) {
		if (data == null) {
			return;
		}
		mobData=data;
		setId(data.id);
		code=data.id;
		name = data.name;
		level = 1;
		restoreLife();
	}

	public static Monster createMob(MonsterData data) {
		Monster mob = null;
		mob = new Monster();
		mob.setCode(data.id);
		mob.setData(data);		
		return mob;
	}
	
	/**
	 * 生成怪物ID
	 */
	protected void generateGameID() {
		gameID = type << 28;
		gameID += generated;
	}

	public void tick() {
		super.tick();
	}

	protected void tickLive() {
		super.tickLive();
		if (getState() != STATE_LIVE) {
			return;
		}
	}

	/**
	 * 死亡
	 */
	public void die() {
		super.die();
	}

	public void tickDead() {
		resetVisibleList();
		setState(STATE_RESPAWN);
	}

	/**
	 * @author liuzg 战斗心跳状态
	 */
	public void tickFighting() {
//		restoreLife();
//		setState(STATE_LIVE);
	}


	@Override
	public int isHaveAttackSkill() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int isHaveShieldSkill() {
		// TODO Auto-generated method stub
		return 0;
	}
}
