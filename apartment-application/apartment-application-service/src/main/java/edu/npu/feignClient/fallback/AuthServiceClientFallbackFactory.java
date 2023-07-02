package edu.npu.feignClient.fallback;

import edu.npu.feignClient.AuthServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [人脸服务降级工厂]
 */
@Component
@Slf4j
public class AuthServiceClientFallbackFactory implements FallbackFactory<AuthServiceClient> {
    @Override
    public AuthServiceClient create(Throwable cause) {
        return faceUrl -> {
            log.error("调用人脸服务识别人脸失败,faceUrl:{},原因:{}", faceUrl, cause);
            return null;
        };
    }
}
