package edu.npu.service;

import edu.npu.dto.FaceVerificationDto;
import edu.npu.vo.R;

public interface FaceService {

    boolean addFaceEntity(String entityId);

    String addFace(String entityId, String faceUrl);

    boolean deleteFaceEntity(String entityId);

    boolean deleteFace(String entityId);

    String getEntityIdByFace(String faceUrl);

    R personIdVerification(FaceVerificationDto faceVerificationDto);
}
