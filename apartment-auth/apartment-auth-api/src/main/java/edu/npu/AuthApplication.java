package edu.npu;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [Auth模块启动类]
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        LogFactory.useCustomLogging(StdOutImpl.class);
        SpringApplication.run(AuthApplication.class, args);
    }
}
