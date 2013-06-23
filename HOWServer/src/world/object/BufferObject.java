package world.object;

import org.apache.log4j.Logger;

import util.binreader.BuffData;
import util.binreader.SkillData;

public class BufferObject {
	private static Logger logger=Logger.getLogger(BufferObject.class);
	private int skillID;
	private BuffData data;
	private int times = 0;// 已使用的次数，可以为
    public BufferObject(int skillID,BuffData data,int currentRounds){
    	this.skillID=skillID;
    	this.data=data;
    	this.times = data.times;
    }
    public void reduceRounds() {
		times--;
	}

	public int getRounds() {
		return times;
	}
	public BuffData getBuffData(){
		return data;
	}
	/**
	 * @author liuzg
	 * @param one
	 * 执行当前buff或debuff处理
	 */
	public void execute(Creature one,int currentRounds){
		if(isValid(currentRounds)==false){
			return;
		}
		int buff=1;
		SkillData skillData=SkillData.getSkill(skillID);
		if(skillData==null){
			return;
		}
		if(skillData.getSkillSpecialType()==SkillData.ATTACKBUFFER){
			buff=-1;
		}
		if(skillData.getSkillSpecialType()==SkillData.ASSISTBUFFER){
			buff=1;
		}
		double value=data.type;
		logger.debug(one.name+"buffer影响值:"+value+",type="+buff+",attrType="+data.type+",SkillID="+skillID+",skillName="+skillData.name);
		switch(data.type){
//		case BuffData.ATTR_TYPE_SHIELD://影响防护
//			one.addCurrentShield((one.shield*data.targetAttrPara+value)*buff);
//			one.addBufferValue(BuffData.ATTR_TYPE_SHIELD, (one.getAllShield()*data.targetAttrPara+value)*buff);
//			break;
//		case BuffData.ATTR_TYPE_POWER://影响动力
//			one.addCurrentPower((one.power*data.targetAttrPara+value)*buff);
//			one.currentPower=(one.power*value)*buff;
//			one.addBufferValue(BuffData.ATTR_TYPE_POWER, (one.getAllPower()*data.targetAttrPara+value)*buff);
//		break;
//		case BuffData.ATTR_TYPE_ATTACK://影响攻击
//			one.currentAttack=(one.attack*value)*buff;
//			one.addCurrentAttack((one.attack*data.targetAttrPara+value)*buff);
//			one.addBufferValue(BuffData.ATTR_TYPE_ATTACK, (one.getAllAttack()*data.targetAttrPara+value)*buff);
//			break;
//		case BuffData.ATTR_TYPE_HANDLEUSE://影响操控
//			one.currentHandleUse=(one.currentHandleUse*value)*buff;
//			one.addCurrentHandleUse((one.currentHandleUse*data.targetAttrPara+value)*buff);
//			one.addBufferValue(BuffData.ATTR_TYPE_HANDLEUSE, (one.getAllHandleUse()*data.targetAttrPara+value)*buff);
//			break;
//		case BuffData.ATTR_TYPE_REACTION://影响反映
//			one.currentReaction=(one.currentReaction*value)*buff;
//			one.addCurrentReaction((one.currentReaction*data.targetAttrPara+value)*buff);
//			one.addBufferValue(BuffData.ATTR_TYPE_REACTION, (one.getAllReaction()*data.targetAttrPara+value)*buff);
//			break;
//		case BuffData.ATTR_TYPE_SPEED://影响速度
//			one.currentSpeed+=(one.speed*value)*buff;
//			one.addCurrentSpeed((one.speed*data.targetAttrPara+value)*buff);
//			one.addBufferValue(BuffData.ATTR_TYPE_SPEED, (one.getAllSpeed()*data.targetAttrPara+value)*buff);
//			break;
		}
	}
	/**
	 * @author liuzg
	 * @return
	 * 当前buff是否有效
	 */
	public boolean isValid(int currentRounds){
//		if(data.targetAttrRounds==BuffData.ATTR_ROUNDS_TIME_LEN&&(currentRounds-buffStartTime)>=(data.targetRoundsOut*1000)){
//			//超过buff时长
//			return false;
//		}
//		if(data.targetAttrType==BuffData.ATTR_TYPE_BEATTACK&&data.targetAttrValue>0&&beAttackTimes<=0){
//			//防护次数无效
//			return false;
//		}
//		if(data.targetAttrRounds==BuffData.ATTR_ROUNDS_TIMES&&data.targetRoundsOut>0&&rounds<=0){
//			//攻击回合次数无效
//			return false;
//		}
		return true;
	}
}
