package com.wanbang.youyibang.emkt.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * MyWebSocketHandler
 * WebSocket处理器，处理websocket连接相关
 * @author zhengkai.blog.csdn.net
 * @date 2019-06-12
 */

public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static AtomicInteger connections = new AtomicInteger();

    public MyWebSocketHandler(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            System.out.println("当前的连接总数为：" +connections.get());
          log.info("当前的连接总数为：" +connections.get());
        }, 0, 5, TimeUnit.SECONDS);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端建立连接，通道开启！");
        SocketAddress socketAddress = ctx.channel().localAddress();
        String s = socketAddress.toString();
        log.info("客户端ip地址：{}",s);
        //每次过来一个新连接就对连接数加一
        connections.incrementAndGet();
//        System.out.println("当前的连接总数为：" +connections.get());
        //添加到channelGroup通道组
        MyChannelHandlerPool.channelGroup.add(ctx.channel());
    }

    /**
     * 工程出现异常的时候调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端断开连接，通道关闭！");
        //每次断开链接时对连接数减一
        connections.decrementAndGet();
//        System.out.println("当前的连接总数为：" +connections.get());
        //添加到channelGroup 通道组
        MyChannelHandlerPool.channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //首次连接是FullHttpRequest，处理参数 by zhengkai.blog.csdn.net
        if (null != msg && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            Map paramMap=getUrlParams(uri);
            log.info("接收到的参数是："+ JSON.toJSONString(paramMap));
            //如果url包含参数，需要处理
            if(uri.contains("?")){
                String newUri=uri.substring(0,uri.indexOf("?"));
                System.out.println(newUri);
                request.setUri(newUri);
            }

        }else if(msg instanceof TextWebSocketFrame){
            //正常的TEXT消息类型
            TextWebSocketFrame frame=(TextWebSocketFrame)msg;
            log.info("客户端收到服务器数据：" +frame.text());
            sendAllMessage(frame.text());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame obj) throws Exception {
//        FullHttpResponse response = new DefaultFullHttpResponse(
//                io.netty.handler.codec.http.HttpVersion.HTTP_1_1, OK);
//        response.headers().set("Content-Type", "text/plain"); //返回是字符串
//
//
//        //允许跨域访问
//        response.headers().set("Access-Control-Allow-Origin","*");
//        response.headers().set("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
//        response.headers().set("Access-Control-Allow-Methods","GET, POST, PUT,DELETE");
//
//        response.headers().set("Content-Length",
//                response.content().readableBytes());
//        response.headers().set("Connection", HttpHeaderValues.KEEP_ALIVE);
//        ctx.write(response);
//        ctx.flush();
//
//        FullHttpRequest fullHttpRequest=(FullHttpRequest) obj;
//        // 将GET, POST所有请求参数转换成Map对象
//        Map<String, String> parmMap = new RequestParser(fullHttpRequest).parse();
//        System.out.println(parmMap);
    }

    private void sendAllMessage(String message){
        //收到信息后，群发给所有channel
        MyChannelHandlerPool.channelGroup.writeAndFlush( new TextWebSocketFrame(message));
    }

    private static Map getUrlParams(String url){
        Map<String,String> map = new HashMap<>();
        url = url.replace("?",";");
        if (!url.contains(";")){
            return map;
        }
        if (url.split(";").length > 0){
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr){
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key,value);
            }
            return  map;

        }else{
            return map;
        }
    }
}