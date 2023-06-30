package edu.npu;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [住宿流程服务启动类]
 */
@MapperScan("edu.npu.mapper")
@SpringBootApplication
public class AccommodationApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        LogFactory.useCustomLogging(StdOutImpl.class);
        SpringApplication.run(AccommodationApplication.class, args);
    }
}
