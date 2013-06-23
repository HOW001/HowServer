package server.cmds;

import java.util.Map;

import org.apache.mina.transport.socket.nio.NioSession;

import server.netserver.MsgOutEntry;
import util.ByteArray;
import util.binreader.GameParameterData;
import util.binreader.LivenessListData;
import util.binreader.PromptData;
import util.binreader.GateInfoData;
import world.object.Contest;
import db.model.Liveness;
import db.model.Player;
/**
 * 活跃度
 * @author fengmx
 */
public class LivenessCP extends CmdParser{
	private static final int liveness_sign_init = 0x0001;//请求签到列表
	private static final int liveness_sign_get_item = 0x0002;//领取签到奖励
	private static final int liveness_list = 0x0003;//请求活跃度列表
	private static final int liveness_update = 0x0004;//更新活跃度列表
	private static final int liveness_get_item = 0x0005;//领取活跃度奖励
	
//	public static final int liveness_open_level = 23;
	private final static LivenessCP instance =  new LivenessCP();
	public static LivenessCP getInstance(){
		return instance;
	}
	public LivenessCP() {
		super(TYPE_LIVENESS);
	}
	@Override
	public void parse(NioSession session, int command, byte[] bytes) {
		try {
			ByteArray ba = new ByteArray(bytes);
			Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
			if(player==null){
				return;
			}
			switch (getCommand(command)){
			case liveness_sign_init:
				if(player.getLevel()<GameParameterData.liveness_sign_level){
					PromptData promptData = PromptData.getDataById(259);
					if(promptData==null){
						logger.error("无效的PromptData数据："+259);
						return;
					}
					String msg = UISystemCP.getInstance().getResultMsg(promptData.msg, (int)GameParameterData.liveness_sign_level+"");
					UISystemCP.sendMessageForType(session, promptData.type, msg, promptData.id, new String[]{""});
					return;
				}
				initLivenessMessage(player);
				break;
			case liveness_sign_get_item:
				Liveness liveness = player.getLivenessEntry();
				int days = ba.readInt();
				liveness.addLivenessAward(player, 0, days);
				break;
			case liveness_list:
				if(player.getLevel()<GameParameterData.liveness_list_level){
					PromptData promptData = PromptData.getDataById(260);
					if(promptData==null){
						logger.error("无效的PromptData数据："+260);
						return;
					}
					String msg = UISystemCP.getInstance().getResultMsg(promptData.msg, (int)GameParameterData.liveness_list_level+"");
					UISystemCP.sendMessageForType(session, promptData.type, msg, promptData.id, new String[]{""});
					return;
				}
				initLivenessListMessage(player);
				break;
			case liveness_get_item:
				liveness = player.getLivenessEntry();
				int type = ba.readInt();
				liveness.addLivenessAward(player,1,type);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("商店命令解析异常:",e);
		}
	}

	@Override
	public void parseForHttp(NioSession session, int command, byte[] bytes) {
		
	}
	
	
	/**
	 * 处理活动的活跃度
	 * @param player
	 * @param activityType
	 */
	public void processActivityLiveness(Player player, int activityType){
		if(!isOpen(player)){
			return;
		}
		Liveness liveness = player.getLivenessEntry();
		if(liveness==null){
			return;
		}
		if(activityType<1||activityType>15){
			return;
		}
		int code = LivenessListData.getLivenessListCodeBytype(activityType);
		liveness.changeCompleteTimesByCode(player, code, 1);
	}
	/**
	 * 请求签到列表
	 * @param player
	 */
	private void initLivenessMessage(Player player){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(liveness_sign_init));
			Liveness liveness = player.getLivenessEntry();
			ba.writeShort(liveness.isAwardForSignMap.size());
			for(Map.Entry<Integer, Integer> entry:liveness.isAwardForSignMap.entrySet()){
				ba.writeInt(entry.getValue());
			}
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("请求签到列表异常：", e);
		}
	}
	/**
	 * 更新指定天的签到列表
	 * @param player
	 * @param days
	 */
	public void updateLivenessMessage(Player player,int days){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(liveness_sign_get_item));
			ba.writeInt(days);
			ba.writeInt(1);
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("更新指定天的签到列表异常：", e);
		}
	}
	/**
	 * 请求活跃度列表
	 * @param player
	 */
	public void initLivenessListMessage(Player player){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(liveness_list));
			Liveness liveness = player.getLivenessEntry();
			if(liveness==null){
				return;
			}
			ba.writeInt(liveness.getCurrentLiveness());
			ba.writeByte((byte)liveness.getStateByType(1, 1));
			ba.writeByte((byte)liveness.getStateByType(1, 2));
			ba.writeByte((byte)liveness.getStateByType(1, 3));
			ba.writeByte((byte)liveness.getStateByType(1, 4));
			ba.writeShort(liveness.livenessMap.size());
			for(Map.Entry<Integer, Integer> entry:liveness.livenessMap.entrySet()){
				ba.writeInt(entry.getKey());
				ba.writeInt(entry.getValue());
			}
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("更新指定天的签到列表异常：", e);
		}
	}
	/**
	 * 更新指定编号的完成次数
	 * @param player
	 * @param code
	 */
	public void updateLivenessListMessage(Player player,int code){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(liveness_update));
			Liveness liveness = player.getLivenessEntry();
			if(liveness==null){
				return;
			}
			ba.writeInt(liveness.getCurrentLiveness());
			ba.writeByte((byte)liveness.getStateByType(1, 1));
			ba.writeByte((byte)liveness.getStateByType(1, 2));
			ba.writeByte((byte)liveness.getStateByType(1, 3));
			ba.writeByte((byte)liveness.getStateByType(1, 4));
			ba.writeInt(code);
			ba.writeInt(liveness.getCompleteTimesByCode(code));
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("更新指定天的签到列表异常：", e);
		}
	}
	/**
	 * 更新活跃度领取状态
	 * @param player
	 * @param type
	 */
	public void updateLivenessListState(Player player,int type){
		try {
			ByteArray ba = new ByteArray();
			ba.writeInt(getCmd(liveness_get_item));
			Liveness liveness = player.getLivenessEntry();
			if(liveness==null){
				return;
			}
			ba.writeInt(type);
			ba.writeByte(liveness.getStateByType(1,type));
			sendData(player.getIoSession(), ba);
		} catch (Exception e) {
			logger.error("更新指定天的签到列表异常：", e);
		}
	}
	/**
	 * 活跃度是否开放
	 * @param player
	 */
	public boolean isOpen(Player player){
		if(player.getLevel()<GameParameterData.liveness_list_level){
//			UISystemCP.sendResult(player.getIoSession(), "签到只对23级以上的玩家开放。");
			return false;
		}
		return true;
	}
	private void sendData(NioSession session, ByteArray ba){
		MsgOutEntry om = new MsgOutEntry(session);
		om.flush(ba.toArray());
		om = null;
	}
}
