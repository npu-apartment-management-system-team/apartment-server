package edu.npu;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : [wangminan]
 * @description : [实体管理系统启动类]
 */
@SpringBootApplication
@EnableFeignClients(basePackages={"edu.npu.feignClient"})
public class ManagementApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        LogFactory.useCustomLogging(StdOutImpl.class);
        SpringApplication.run(ManagementApplication.class, args);
    }
}
