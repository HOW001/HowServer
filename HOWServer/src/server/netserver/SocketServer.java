
package server.netserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;


import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.filter.firewall.ConnectionThrottleFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.ServerEntrance;
import server.netserver.codec.CodecFactory;
import server.netserver.codec.SocketDecoder;
import server.netserver.codec.SocketEncoder;

/**
 * Description:SocketServer的启动类，负责启动和配置与Socket类型的服务器<br>
 * @author liuzg
 */
public class SocketServer extends GameServerPortListener {

    @Override
    public void start (int port) throws Exception {
        this.port = port;
        handler = new SocketIoHandler();
        ProtocolDecoder decoder = new SocketDecoder();
    	ProtocolEncoder encoder = new SocketEncoder();
    	//处理器个数+1
    	int cpuCount=Runtime.getRuntime().availableProcessors() + 1;
    	logger.info("*********本机的处理器个数为:"+(cpuCount-1)+"**********");
    	
        acceptor = new NioSocketAcceptor(cpuCount);
        acceptor.getSessionConfig().setReadBufferSize(2<<12);//4K
        acceptor.getSessionConfig().setMaxReadBufferSize(2<<18);//256K
        acceptor.getSessionConfig().setReceiveBufferSize(2<<18);//256K
        acceptor.getSessionConfig().setSendBufferSize(2<<21);//2<<21 2M
        acceptor.getSessionConfig().setTcpNoDelay(true);//设为非延迟发送
//        acceptor.setBacklog(ServerEntrance.serverConfiguration.getPlayersValue()+100);//设置最大连接数
        
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,30);//session闲置时间
        acceptor.getSessionConfig().setReaderIdleTime(30);
        acceptor.getSessionConfig().setWriterIdleTime(30);
        LoggingFilter log=new LoggingFilter();
        log.setMessageReceivedLogLevel(LogLevel.INFO);
        log.setMessageSentLogLevel(LogLevel.INFO);
        acceptor.getFilterChain().addLast("log",log);//日志过滤
        //过滤黑名单IP
        InetAddress add=InetAddress.getByName("192.168.1.188");
        BlacklistFilter blackIP=new BlacklistFilter();
        blackIP.setBlacklist(new InetAddress[]{add});
        acceptor.getFilterChain().addLast("blackIP", blackIP);//过滤黑名单IP
//        acceptor.getFilterChain().addLast("connect_interval", new ConnectionThrottleFilter(1000));//30秒之内重复连接将被过滤
        acceptor.getFilterChain().addLast("protocol",new ProtocolCodecFilter(new CodecFactory(decoder,encoder)));
        acceptor.setHandler(handler);
        acceptor.getSessionConfig().setSoLinger(-1);
        acceptor.bind(new InetSocketAddress(port));
        logger.info("SocketServer is listenig at port " + port);
    }
}
