package com.wanbang.youyibang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class EmktApplication extends SpringBootServletInitializer {
    private static final Logger log = LoggerFactory.getLogger(EmktApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EmktApplication.class, args);
//        try {
//            new NettyServer(12300,12305).start();
//        } catch (Exception e) {
//            log.error("NettyServerError:{}",e.getMessage());
//        }
        log.info("(♥◠‿◠)ﾉﾞ  EMKT 启动成功   ლ(´ڡ`ლ)ﾞ");
    }



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EmktApplication.class);
    }

}
