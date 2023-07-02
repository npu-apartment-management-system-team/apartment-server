package edu.npu.feignClient;

import edu.npu.feignClient.fallback.AuthServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "auth-api",
        path = "/auth/face",
        fallbackFactory = AuthServiceClientFallbackFactory.class)
public interface AuthServiceClient {
    @GetMapping("/entityId")
    String getEntityIdByFace(
            @RequestParam(value = "faceUrl") String faceUrl);
}
