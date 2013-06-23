package server;

import org.apache.log4j.Logger;

public class ServerConfigurationNew {
	private static Logger logger=Logger.getLogger(ServerConfigurationNew.class);
	//服务器内部名称
		public static String id;
		//端口号
		public static int port;
		//服务器名称
		public static String serverName;
		//登录公告
		public static String anounncement;
		//策划数据目录
		public static String resDir;
		//玩家等级上限
		public static int maxLevel;
		//人数上限，默认为9999
		public static int players;
		//是否开放注册
		public static boolean canRegister;
		//游戏名称，用于提交
		public static String subgame;
		public static boolean debug;
		public static boolean mailEnable;
		//收件人列表
		public static String mailList;
		//SMTP地址
		public static String mailHost;
		//邮箱用户名
		public static String mailUser;
		//邮箱密码
		public static String mailPassword;
		public static String localIp;
		//绝对路径参考
		public static String absolutePathRef;
		
		public static void setValue(String key,String value){
			if(key.equals("id")){
				id=value;
			}else if(key.equals("port")){
				port=Integer.parseInt(value);
			}else if(key.equals("serverName")){
				serverName=value;
			}else if(key.equals("anounncement")){
				anounncement=value;
			}else if(key.equals("resDir")){
				resDir=value;
			}else if(key.equals("maxLevel")){
				maxLevel=Integer.parseInt(value);
			}else if(key.equals("players")){
				players=Integer.parseInt(value);
			}else if(key.equals("canRegister")){
				canRegister=Boolean.parseBoolean(value);
			}else if(key.equals("subgame")){
				subgame=value;
			}else if(key.equals("debug")){
				debug=Boolean.parseBoolean(value);
			}else if(key.equals("mailEnable")){
				mailEnable=Boolean.parseBoolean(value);
			}else if(key.equals("mailList")){
				mailList=value;
			}else if(key.equals("mailHost")){
				mailHost=value;
			}else if(key.equals("mailUser")){
				mailUser=value;
			}else if(key.equals("mailPassword")){
				mailPassword=value;
			}else if(key.equals("localIp")){
				localIp=value;
			}else if(key.equals("absolutePathRef")){
				absolutePathRef=value;
			}else{
				logger.error("无效的配置文件数据:"+key);
			}
		}
}
