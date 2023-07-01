package edu.npu.feignClient;

import edu.npu.dto.AddFaceDto;
import edu.npu.feignClient.fallback.AuthServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "auth-api",
        path = "/auth/face",
        fallbackFactory = AuthServiceClientFallbackFactory.class)
public interface AuthServiceClient {
    @PostMapping
    String addFace(@Validated @RequestBody AddFaceDto addFaceDto);

    @DeleteMapping("/entity/{entityId}")
    boolean deleteFaceEntity(
            @PathVariable(value = "entityId") String entityId);

    @DeleteMapping("/{faceId}")
    boolean deleteFace(
            @PathVariable(value = "faceId") String faceId);
}
