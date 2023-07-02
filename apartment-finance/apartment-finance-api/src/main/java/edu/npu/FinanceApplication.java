package edu.npu;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [财务人员与缴费服务启动类]
 */
@SpringBootApplication
public class FinanceApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        LogFactory.useCustomLogging(StdOutImpl.class);
        SpringApplication.run(FinanceApplication.class, args);
    }
}
