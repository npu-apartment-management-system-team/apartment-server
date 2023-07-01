package edu.npu.feignClient.fallback;

import edu.npu.dto.AddFaceDto;
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
        return new AuthServiceClient() {
            @Override
            public String addFace(AddFaceDto addFaceDto) {
                log.error("添加人脸失败,face:{}, 原因:{}", addFaceDto.faceUrl(), cause.getMessage());
                return null;
            }

            @Override
            public boolean deleteFaceEntity(String entityId) {
                log.error("删除人脸实体失败,entityId:{}, 原因:{}", entityId, cause.getMessage());
                return false;
            }

            @Override
            public boolean deleteFace(String faceId) {
                log.error("删除人脸失败,faceId:{}, 原因:{}", faceId, cause.getMessage());
                return false;
            }
        };
    }
}
