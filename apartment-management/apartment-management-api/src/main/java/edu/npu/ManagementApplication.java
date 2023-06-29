package edu.npu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [实体管理系统启动类]
 */
@SpringBootApplication
@MapperScan("edu.npu.mapper")
public class ManagementApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        SpringApplication.run(ManagementApplication.class, args);
    }
}
