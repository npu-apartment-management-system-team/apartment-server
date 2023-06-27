package edu.npu.service.impl;

import cn.hutool.core.codec.Base64;
import com.aliyun.facebody20191230.models.*;
import com.aliyun.facebody20200910.models.ExecuteServerSideVerificationRequest;
import com.aliyun.facebody20200910.models.ExecuteServerSideVerificationResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.FaceVerificationDto;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.service.FaceService;
import edu.npu.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : [wangminan]
 * @description : [调用阿里云人脸人体提供服务]
 */
@Service
@Slf4j
public class FaceServiceImpl implements FaceService {

    public static final String FACE_DB_NAME = "apartment_face_db";

    public static final Integer OPERATION_SUCCESS = 200;

    // 都是client 需要使用setter根据类型注入 之后用OSS的时候也要注意有多个client
    private com.aliyun.facebody20191230.Client face2019Client;

    private com.aliyun.facebody20200910.Client face2020Client;

    @Autowired
    public void setFace2019Client(com.aliyun.facebody20191230.Client face2019Client) {
        this.face2019Client = face2019Client;
    }

    @Autowired
    public void setFace2020Client(com.aliyun.facebody20200910.Client face2020Client) {
        this.face2020Client = face2020Client;
    }

    @Override
    public boolean addFaceEntity(String entityId) {
        AddFaceEntityRequest addFaceEntityRequest =
            new AddFaceEntityRequest()
                .setDbName(FACE_DB_NAME)
                .setEntityId(entityId);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            AddFaceEntityResponse addFaceEntityResponse =
                    face2019Client.addFaceEntityWithOptions(addFaceEntityRequest, runtime);
            // 解析结果
            return addFaceEntityResponse.getStatusCode().equals(OPERATION_SUCCESS);
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人脸实体新增服务异常");
        }
    }

    /**
     * 人脸新增
     * @param entityId 人脸实体ID
     * @param faceUrl 人脸图片URL
     * @return 人脸ID
     * 返回示例如下
     {
           "RequestId": "7EF16DC6-8819-5FCF-8477-26EAD194BF23",
           "Data": {
             "FaceId": "42686726",
             "QualitieScore": 99.99743
           }
     }
     */
    @Override
    public String addFace(String entityId, String faceUrl) {
        // 使用上海地区OSS
        AddFaceRequest addFaceRequest =
            new AddFaceRequest()
                .setDbName(FACE_DB_NAME)
                .setEntityId(entityId)
                .setImageUrl(faceUrl)
                .setSimilarityScoreThresholdBetweenEntity(70F)
                .setQualityScoreThreshold(70F);
        if (addFaceRequest.getDbName() == null) {
            return null;
        }
        // 发起请求
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            AddFaceResponse addFaceResponse = face2019Client
                    .addFaceWithOptions(addFaceRequest, runtime);
            // 解析结果
            return addFaceResponse.getStatusCode().equals(OPERATION_SUCCESS) ?
                    addFaceResponse.getBody().getData().getFaceId() : null;
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人脸新增服务异常");
        }
    }

    /**
     * 人脸实体删除
     * @param entityId 人脸实体ID
     * @return 删除结果
     */
    @Override
    public boolean deleteFaceEntity(String entityId) {
        DeleteFaceEntityRequest deleteFaceEntityRequest =
            new DeleteFaceEntityRequest()
                .setDbName(FACE_DB_NAME)
                .setEntityId(entityId);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            DeleteFaceEntityResponse deleteFaceEntityResponse =
                    face2019Client.deleteFaceEntityWithOptions(deleteFaceEntityRequest, runtime);
            // 解析
            return deleteFaceEntityResponse.getStatusCode().equals(OPERATION_SUCCESS);
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人脸实体删除服务异常");
        }
    }

    /**
     * 人脸删除
     * @param faceId 人脸实体ID
     * @return 删除结果
     */
    @Override
    public boolean deleteFace(String faceId) {
        DeleteFaceRequest deleteFaceRequest =
            new DeleteFaceRequest()
                .setDbName(FACE_DB_NAME)
                .setFaceId(faceId);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            DeleteFaceResponse deleteFaceResponse =
                    face2019Client.deleteFaceWithOptions(deleteFaceRequest, runtime);
            // 解析
            return deleteFaceResponse.getStatusCode().equals(OPERATION_SUCCESS);
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人脸删除服务异常");
        }
    }

    /**
     * 人脸查询
     * @param faceUrl 人脸图片URL
     * @return 人脸实体ID
     * 返回示例如下
    /*
     * 返回示例如下
        {
            "RequestId": "6527FF1E-4C5D-54A0-A293-871BBFB8B9F6",
            "Data": {
                "MatchList": [{
                    "FaceItems": [
                        {
                            "EntityId": "wangminan",
                            "FaceId": "42686726",
                            "Score": 0.9999998807907104,
                            "Confidence": 99.99999,
                            "DbName": "apartment_face_db"
                        }
                    ],
                    "QualitieScore": 99.99743,
                    "Location": {
                        "X": 82,
                        "Y": 99,
                        "Height": 157,
                        "Width": 117
                        }
                }]
            }
        }
     */
    @Override
    public String getEntityIdByFace(String faceUrl){
        SearchFaceRequest searchFaceRequest =
                new SearchFaceRequest()
                        .setDbName(FACE_DB_NAME)
                        .setImageUrl(faceUrl)
                        .setLimit(1)
                        .setQualityScoreThreshold(70F);
        // 发起请求
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            SearchFaceResponse searchFaceResponse = face2019Client
                    .searchFaceWithOptions(searchFaceRequest, runtime);
            // 解析结果
            return searchFaceResponse.getStatusCode().equals(OPERATION_SUCCESS) ?
                    searchFaceResponse.getBody().getData().getMatchList().get(0)
                            .getFaceItems().get(0).getEntityId() : null;
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人脸搜索服务异常");
        }
    }

    /**
     * 人证核身
     *
     * @param faceVerificationDto 人证核身信息
     * @return R 核身结果
     */
    @Override
    public R personIdVerification(FaceVerificationDto faceVerificationDto) {
        String name = faceVerificationDto.name();
        String personalId = faceVerificationDto.personalId();
        String faceUrl = faceVerificationDto.faceUrl();
        ExecuteServerSideVerificationRequest verificationRequest=
            new ExecuteServerSideVerificationRequest()
                .setFacialPictureUrl(faceUrl)
                .setCertificateNumber(personalId)
                .setCertificateName(name);
        RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        try {
            ExecuteServerSideVerificationResponse verificationResponse =
                face2020Client.executeServerSideVerificationWithOptions(
                    verificationRequest, headers, runtime);
            // 解析结果
            return verificationResponse.getStatusCode().equals(OPERATION_SUCCESS) ?
                R.ok() : R.error(ResponseCodeEnum.SERVER_ERROR, "人证核身失败");
        } catch (Exception e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "人证核身服务异常");
        }
    }
}
