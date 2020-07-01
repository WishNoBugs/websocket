package com.wanbang.youyibang.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint("/websocket/{name}")
public class WebSocket{
    private Session session;//与某个客户端连接对话，需要通过它来给客户端发送消息
    private String name;//当前连接客户端的标识

    private static ConcurrentHashMap<String,WebSocket> webSocketSet = new ConcurrentHashMap<>();
    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "name") String name){
        this.session = session;
        this.name = name;
        webSocketSet.put(name,this);
        log.info("[WebSocket] 连接成功，当前连接人数为：={}",webSocketSet.size());
    }

    @OnClose
    public void onClose(){
        webSocketSet.remove(this.name);
        log.info("[WebSocket] 推出成功，当前连接人数为：={}",webSocketSet.size());
    }
    @OnMessage
    public void OnMessage(String message){
        log.info("[WebSocket] 收到消息：{}",message);
        //自定义转发,定义传过来的数据格式为name|message
//        if (message.contains("|")){
//            String name = message.split("\\|")[0];
//            singletonOut(message, name);
//        }else {
            fanout(message);
//        }
    }
    /**
     * 当通信发生异常：打印错误日志
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void singletonOut(String message, String name) {
        try {
            webSocketSet.get(name).session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void fanout(String message) {
        for(String name:webSocketSet.keySet()){
            singletonOut(message, name);
        }
    }
}
