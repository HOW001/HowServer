package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件
 *
 */
public class SMTPSender {
	/**
	 * 默认的类型
	 */
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static String LOCAL_IP ="127.0.0.1";//ServerEntrance.serverConfiguration.getLocalIpValue();
	
	private static final String SMTP_PORT = "25";
	private static String host;
	private static String from;
	private static boolean authentication;
	private static String username;
	private static String password;
	private static ConcurrentHashMap<String, List<String>> mailBuffer = new ConcurrentHashMap<String, List<String>>();
	
	/**
	 * 是否开启
	 */
	private static boolean enabled = false;
	
	public static boolean enblaed(){
		return enabled;
	}
	
	public static void setEnable(boolean b){
		enabled = b;
	}
	/**
	 * 收件人地址列表
	 */
	private static String[] mailList = null;
	public static void initDefault() {
		host = "smtp.163.com";
		from = "car_server_001@163.com";
		authentication = true;
		username = "car_server_001";
		password = "rekoo123456";
		if(LOCAL_IP == null || LOCAL_IP.trim().equals("")){
			LOCAL_IP = "192.168.2.18";
		}
	}
	
	/**
	 * 配置
	 * @param hostStr smtp地址
	 * @param fromStr 发件人地址
	 */
	public static void doConfig(String hostStr,String fromStr){
		host = hostStr;
		from = fromStr;
	}
	
	/**
	 * 配置用户名密码
	 * @param userStr
	 * @param passwordStr
	 */
	public static void doConfigUserPassword(String userStr,String passwordStr){
		username = userStr;
		password = passwordStr;
		if(password == null || password.trim().equals("")){
			authentication = false;
		}
	}
	
	/**
	 * 配置
	 * @param hostStr smtp地址
	 * @param fromStr 发件人地址
	 * @param userStr smtp登录用户名
	 * @param passwordStr smtp登录密码
	 */
	public static void doConfig(String hostStr,String fromStr,String userStr,String passwordStr){
		host = hostStr;
		from = fromStr;
		doConfigUserPassword(userStr, passwordStr);
	}
	
	/**
	 * 在收件人列表中添加email地址
	 * 必须包含“@”符号，并且地址字符串长度必须大于5
	 * @param mail
	 */
	public static void addMailList(String mail){
		if(mail == null){
			return;
		}
		mailList = null;
		mailList = mail.split(";");
	}
	/**
	 * Singleton
	 */
	private final static SMTPSender sender= new SMTPSender();
	
	private SMTPSender(){
		initDefault();
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", SMTP_PORT);
		if (authentication) {
			props.put("mail.smtp.auth", "true");
		} else {
			props.put("mail.smtp.auth", "false");
		}
		props.put("mail.smtp.localhost", LOCAL_IP); 
	}
	
	public static SMTPSender getSender(){
		return sender;
	}
	
	
	/**
	 * 发送一封邮件
	 * @param to 收件人地址
	 * @param subject 主题
	 * @param content 内容
	 * @throws Exception
	 */
	public void sendOneMail(String to,String subject,
			String content) throws Exception {
		if(enabled == false){
			return;
		}
		Session session = Session.getDefaultInstance(System.getProperties(), null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setContent(content, CONTENT_TYPE);
		Transport smtp = null;
		try {
			smtp = session.getTransport("smtp");
			if(authentication){
				smtp.connect(host, username, password);
			}
			else{
				smtp.connect();
			}
			smtp.sendMessage(message, message.getAllRecipients());
			smtp.close();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			
		}
	}
	
	/**
	 * @author liuzg
	 * @param to
	 * @param subject
	 * @param content
	 * @throws Exception
	 * 发送邮件
	 */
	private void sendMails(String[] to,String subject,
			String content) throws Exception {
		if(enabled == false){
			return;
		}
		Session session = Session.getDefaultInstance(System.getProperties(), null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		InternetAddress[] addresses = new InternetAddress[to.length];
		for(int i=0;i<to.length;i++){
			addresses[i] = new InternetAddress(to[i]);
		}
		message.addRecipients(Message.RecipientType.TO, addresses);
		message.setSubject(subject);
		message.setContent(content, CONTENT_TYPE);
		if (authentication) {
			Transport smtp = null;
			try {
//				smtp = session.getTransport("smtp");
//				smtp.connect(host, username, password);
//				smtp.sendMessage(message, message.getAllRecipients());
//				smtp.close();
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				
			}
		} else {
			Transport.send(message);
		}
	}

	/**
	 * 发送邮件
	 * 
	 * @param title
	 * @param content
	 */
	public static void sendMail(String title, String content) {
		try {
			List<String> list = mailBuffer.get(title);
			if (list == null) {//不存在该类型邮件则加入缓存
				list = new ArrayList<String>();
				list.add(content);
				mailBuffer.put(title, list);
			} else {
				boolean isSendMails=false;
				if(title.equals("Login")){//登录100人次后发送邮件
					if(list.size()>=100){
						isSendMails=true;
					}
				}else if(title.equals("upLevel")){//满级人数超过5人发送邮件
					if(list.size()>=5){
						isSendMails=true;
					}
				}else if(list.size()>=10){
					isSendMails=true;
				}
				if (isSendMails) {// 缓存中同类型邮件达到4封时，同新邮件合并一起发送
					StringBuilder sb = new StringBuilder();
					for (String string : list) {
						sb.append(string);
						sb.append("\n");
					}
					sb.append(content);// 将新邮件合并
					getSender().sendMails(mailList, title, sb.toString());
					mailBuffer.remove(title);// 清空该类型邮件
				} else {// 缓存中同类型邮件不足4封，加入缓存。
					list.add(content);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 强制发送缓存中的邮件
	 * 
	 */
	public static void sendMailNow() {
		try {
			for (String title: mailBuffer.keySet()) {
				StringBuilder sb=new StringBuilder();
				for (String content : mailBuffer.get(title)) {
					sb.append(content);
					sb.append("\n");
				}
				getSender().sendMails(mailList, title, sb.toString());
			}
			mailBuffer.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] argv){			
		try {
			SMTPSender sender = SMTPSender.getSender();
			sender.sendOneMail("liuzhigang0532@qq.com", "测试邮件", "测试邮件");
			System.out.println("邮件测试完毕！");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
