package server.cmds;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import server.netserver.DataPackEntry;

import world.World;

public class CMDThread implements Runnable {
	public static final int COUNT = 50;
	private CmdParser parser;
	private Logger logger = Logger.getLogger(CMDThread.class);
	private LinkedBlockingQueue<Command> queue;
	public String threadName;
	public static long perNum = 0;// 每分钟接收的信息数量
	/*
	 * 最后一次处理信息的时间
	 */
	public static long LastProcessTime=0;
	
	public CMDThread(CmdParser parser) {
		this.parser = parser;
		queue = new LinkedBlockingQueue<Command>();
	}

	public void addCommand(Command command) {
		queue.add(command);
	}

	/**
	 * @author lzg------2011-6-1 处理所有CP队列操作
	 */
	public void run() {
		long currentTime = System.currentTimeMillis();
		int type = 0;
//		long count=0;
		while (World.running()) {
			try {
				if (System.currentTimeMillis() - LastProcessTime > 1000 * 60) {
					LastProcessTime = System.currentTimeMillis();
					logger.info("CMDThread正在执行......");
					logger.info("接收线程每分钟处理信息:" + perNum + "条!");
					perNum = 0;
				}
				Command cmd = queue.take();
				perNum++;
				if (cmd != null && cmd.session != null) {
					try {
						currentTime = System.currentTimeMillis();
						type = CmdParser.getType(cmd.command);
						parser = CmdDispatch.getInstance().parsers[type];
						if (parser == null) {
							logger.error("无法在CmdDipatch中找到CP解析器:type=" + type);
							continue;
						}
						if (cmd.connType == DataPackEntry.CONN_TYPE_HTTP) {
							parser.parseForHttp(cmd.session, cmd.command,
									cmd.data);
						} else {
							parser.parse(cmd.session, cmd.command, cmd.data);
						}
						if (System.currentTimeMillis() - currentTime > 500) {
							logger.info("命令:"
									+ Integer.toHexString(cmd.command)
									+ ",执行用时:"
									+ (System.currentTimeMillis() - currentTime));
						}
					} catch (Exception e) {
						logger.error("解析命令异常:", e);
					}
				}
//				count++;
//				if(count>10000){
//					count=0;
//					Thread.sleep(100);
//				}
			} catch (Exception e) {
				logger.error("CMD Parser" + parser.getType() + " qsize:"
						+ queue.size(), e);
			}

		}
		logger.info(parser.getClass().getName() + "线程停止!");
	}
}
