package server.cmds;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.log4j.Logger;

import db.model.Player;
import server.ServerEntrance;
import server.netserver.DataPackEntry;
//import server.netserver.SessionAttributeEntry;
import util.Bits;
//import util.EncryptUtil;
import world.World;

/**
 * 命令分发器
 *
 */
public class CmdDispatch {	
	private Logger logger = Logger.getLogger(CmdDispatch.class);
	public CmdParser[] parsers = new CmdParser[CmdParser.TOTLE_TYPE_NUMBER];
//	private CMDThread threads=new CMDThread(); 
	private CMDThread[] threads = new CMDThread[CmdParser.TOTLE_TYPE_NUMBER]; 
	public static CmdDispatch instance=null;
	/*
	 * 最后一次收到信息的时间
	 */
	public static long LastReceiveTime=0;
	public static CmdDispatch getInstance() {
		if(instance == null){
			instance = new CmdDispatch();
			CmdParser.createAllCmdParser();//同时生成各种解析器
//			ServerEntrance.runThread(instance.threads);
		}
		return instance;
	}
	
	/**
	 * 添加命令解析器
	 * @param parser
	 */
	protected void addParser(CmdParser parser){
		if(parser == null){
			return;
		}
		int index = parser.getType();
		if(index < 0 || index >= CmdParser.TOTLE_TYPE_NUMBER){
			return;
		}
		if(parsers[index] == null){
			parsers[index] = parser;
			threads[index] = new CMDThread(parser);	
			ServerEntrance.runThread(threads[index]);
		} 
	}
	
	public void parseCMDForHttp(NioSession session,int command,byte[] bytes){
		Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
		if(player!=null){
			player.lastLinkTickTime=System.currentTimeMillis();
		}
		command=Bits.getInt(bytes, 0);
		int type = CmdParser.getType(command);
         if (threads != null&&CmdDispatch.getInstance().parsers[type]!=null) {
             threads[type].addCommand(new Command(session, command,DataPackEntry.CONN_TYPE_HTTP, bytes));
         } else {
             logger.error("无法找到命令解析器：0x" + Integer.toHexString(command));
             if(player!=null){
            	player.sendResult("无法找到命令解析器：0x" + Integer.toHexString(command));
             }
         }
	}
    private static ConcurrentHashMap<Integer,Integer> commandTimes=new ConcurrentHashMap<Integer,Integer>();
    private static long lastPrintTime=0;
	/**
	 * 解析命令
	 * @param handler
	 * @param bytes
	 */
	public void parseCMD(NioSession session,byte[] bytes) {
		try {
			if(session.isClosing()){
				return;
			}	
			Player player = session.getAttribute(Player.PLAYERKEY)!=null&&session.getAttribute(Player.PLAYERKEY) instanceof Player ?(Player)session.getAttribute(Player.PLAYERKEY):null;
			if(player!=null){
				player.lastLinkTickTime=System.currentTimeMillis();
			}
			int start=0;
			int command = Bits.getInt(bytes,0);
			if(commandTimes.get(command)!=null){
				commandTimes.put(command,commandTimes.get(command)+1);
			}else{
				commandTimes.put(command,1);
			}
			start+=4;
			if(World.players.size()<10){
				logger.info(session+"服务器收到命令:"+Integer.toHexString(command));
			}
			if(!isValidCommand(command)){
				  logger.error(session + "发送了错误的命令:0x" + Integer.toHexString(command));
				  logger.error(session+"被服务器主动关闭201210241427");
				  logger.error(session+"收到错误命令,关闭session...");
				  session.close();
				  return;
			}
			boolean isToServer = false;
			byte[] data=new byte[bytes.length-start];
			System.arraycopy(bytes,start,data,0,data.length);    
			logger.info(session+"开始放入接收处理线程:"+Integer.toHexString(command));
			if (isToServer == false) {
			    int type = CmdParser.getType(command);
			    if (threads!= null&&CmdDispatch.getInstance().parsers[type]!=null) {
			    	LastReceiveTime=System.currentTimeMillis();
			        threads[type].addCommand(new Command(session, command, DataPackEntry.CONN_TYPE_SOCKET,data));
			    } else {
			        logger.error("无法找到命令解析器：0x" + Integer.toHexString(command));
			        if(player!=null){
			        	player.sendResult("无法找到命令解析器：0x" + Integer.toHexString(command));
			        }
			    }
			}
			if(System.currentTimeMillis()-lastPrintTime>1000*60*10){
				logger.info("各个命令执行次数:");
				for(int cmd:commandTimes.keySet()){
					logger.info("cmd:=0x"+Integer.toHexString(cmd)+",times="+commandTimes.get(cmd));
				}
				commandTimes.clear();
				lastPrintTime=System.currentTimeMillis();
			}
		} catch (Exception e) {
			logger.error("解析命令时出现异常:",e);
		}
	}
	
	/**
	 * 检查是否是合法命令
	 * @param command
	 * @return
	 */
	private boolean isValidCommand(int command){
		return CmdParser.isClientCommand(command);
	}
	/**
	 * @author liuzg
	 * 重新启动一次线程
	 */
	public void restartThread(){
//		ServerEntrance.runThread(threads);
	}
}
