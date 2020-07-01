package com.wanbang.youyibang.emkt.handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyServer Netty服务器配置
 * @author zhengkai.blog.csdn.net
 * @date 2019-06-12
 */
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup();//负责新客户端的接入

    private EventLoopGroup group = new NioEventLoopGroup();//负责处理连接客户端的网络请求

    private ServerBootstrap sb = new ServerBootstrap();

    private int beginPort = 0;
    private int endPort = 0;


    public NettyServer(int beginPort, int endPort) {
        this.beginPort = beginPort;
        this.endPort = endPort;
    }

    public void start() throws Exception {
        try {
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            sb.group(group, bossGroup) // 绑定线程池
                    .channel(NioServerSocketChannel.class) // 指定使用的channel
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("收到新连接");
                            //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                            ch.pipeline().addLast(new HttpServerCodec());
                            //以块的方式来写的处理器
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new HttpObjectAggregator(8192));
                            ch.pipeline().addLast(new MyWebSocketHandler());
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536 * 10));
                        }
                    });

            for (int i = beginPort;i<=endPort;i++) {
                final int port = i;
                sb.localAddress(i);// 绑定监听端口
                ChannelFuture cf = sb.bind().sync(); // 服务器异步创建绑定
                log.info(NettyServer.class + " 启动正在监听： " + cf.channel().localAddress());
                if(i == endPort) {
                    cf.channel().closeFuture().sync(); // 关闭服务器通道
                }
            }
        } finally {
            group.shutdownGracefully().sync(); // 释放线程池资源
            bossGroup.shutdownGracefully().sync();
        }
    }
}
