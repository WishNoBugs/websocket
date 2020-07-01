package com.wanbang.youyibang.emkt.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class NettyServerListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(NettyServerListener.class);
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(contextRefreshedEvent.getApplicationContext().getParent() == null){
            //自己的NettyServer
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new NettyServer(12301,12303).start();
                    } catch (Exception e) {
                        log.error("NettyServerError:{}",e.getMessage());
                    }
                }
            }).start();
        }
    }
}
