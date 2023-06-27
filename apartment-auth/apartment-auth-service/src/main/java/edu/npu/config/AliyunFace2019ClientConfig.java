package edu.npu.config;

import com.aliyun.facebody20191230.Client;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author : [wangminan]
 * @description : [人脸识别用Client]
 */
@Configuration
public class AliyunFace2019ClientConfig {

    @Resource
    private Environment config;

    @Bean
    public Client createFace2019Client() throws Exception {
        Config faceConfig = new Config()
                .setAccessKeyId(config.getProperty("var.aliyun-face.accessKeyId"))
                .setAccessKeySecret(config.getProperty("var.aliyun-face.accessKeySecret"));
        // 访问的域名
        faceConfig.endpoint = config.getProperty("var.aliyun-face.endpoint");
        return new Client(faceConfig);
    }
}
