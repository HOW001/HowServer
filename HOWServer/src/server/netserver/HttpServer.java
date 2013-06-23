/**
 * Copyright: Copyright (c) 2007
 * <br>
 * Company: Digifun
 * <br>
 * Date: 2008-8-4
 */
package server.netserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;


import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.netserver.codec.HttpDecoder;
import server.netserver.codec.HttpEncoder;
import server.netserver.codec.CodecFactory;


/**
 * Description:Http监听端口<br>
 * 
 * @author Insunny
 * @version 0.1
 */
public class HttpServer extends GameServerPortListener {
    @Override
    public void start (int port) throws Exception {
    	ProtocolDecoder decoder = new HttpDecoder();
    	ProtocolEncoder encoder = new HttpEncoder();
        this.port = port;
        handler = new HttpIoHandler();
        acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
        LoggingFilter log=new LoggingFilter();
        log.setMessageReceivedLogLevel(LogLevel.INFO);
        log.setMessageSentLogLevel(LogLevel.INFO);
        acceptor.getFilterChain().addLast("log",log);//日志过滤
        //过滤黑名单IP
        InetAddress add=InetAddress.getByName("192.168.1.188");
        BlacklistFilter blackIP=new BlacklistFilter();
        blackIP.setBlacklist(new InetAddress[]{add});
        acceptor.getFilterChain().addLast("blackIP", blackIP);//过滤黑名单IP
//        acceptor.getFilterChain().addLast("connect_interval", new ConnectionThrottleFilter(1000));//1秒之内重复连接将被过滤
        acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CodecFactory(decoder,encoder)));
        acceptor.setHandler(handler);
        acceptor.getSessionConfig().setSoLinger(-1);
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("HttpServer is listenig at port " + port);
    }

}
