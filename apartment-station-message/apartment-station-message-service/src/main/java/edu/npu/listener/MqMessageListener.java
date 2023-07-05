package edu.npu.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.doc.MessageDoc;
import edu.npu.entity.Admin;
import edu.npu.entity.MessageDetail;
import edu.npu.entity.MessageReceiving;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.MessageDetailMapper;
import edu.npu.mapper.MessageReceivingMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static edu.npu.common.EsConstants.MESSAGE_INDEX;

/**
 * @author : [wangminan]
 * @description : [消息队列监听器]
 */
@Slf4j
@Component
public class MqMessageListener {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private MessageDetailMapper messageDetailMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private MessageReceivingMapper messageReceivingMapper;

    // 负责执行新线程上其他任务的线程池
    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

    @RabbitListener(queues = "mysql.message.queue")
    public void listenMessageQueue(String msg) {
        log.info("消费者接收到mysql.user.queue的消息：【" + msg + "】");
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(msg);
            // 我们需要两个线程 同时在ElasticSearch与Redis中进行操作。
            if (!jsonNode.get("database").asText().equals("apartment_system")) {
                log.info("接收到非本项目数据库的消息,db:{}",
                        jsonNode.get("database").asText());
                return;
            } else if (!jsonNode.get("table").asText().equals("message_detail") &&
                    !jsonNode.get("table").asText().equals("message_receiving")
            ) {
                // 由于用的是fanout 所以确实存在这种可能 我们一个业务需要对应一个listener
                log.info("接收到非本业务消息,table:{}",
                        jsonNode.get("table").asText());
                return;
            }
            // ok 是我们要处理的内容
            if (jsonNode.get("table").asText().equals("message_receiving") &&
                    jsonNode.get("type").asText().equals("INSERT")) {
                log.info("开始处理新增Message的同步");
                handleInsert(jsonNode);
            } else if (jsonNode.get("table").asText().equals("message_detail") &&
                    jsonNode.get("type").asText().equals("UPDATE")) {
                log.info("开始处理更新Message的同步");
                handleUpdate(jsonNode);
            } else if (jsonNode.get("table").asText().equals("message_detail") &&
                    jsonNode.get("type").asText().equals("DELETE")) {
                log.info("开始处理删除Message的同步");
                handleDelete(jsonNode);
            } else {
                log.info("接收到非常规业务类型,type:{}",
                        jsonNode.get("type").asText());
                return;
            }
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "消息解析失败");
        }
        log.info("消费者处理消息成功！");
    }

    private void handleInsert(JsonNode jsonNode) {
        MessageReceiving messageReceiving = extractMessageReceivingFromJsonNode(jsonNode);
        log.info("收到Message:{}的保存消息,开始同步消息到ES", messageReceiving);
        cachedThreadPool.execute(() -> {
            log.info("开始保存Message:{}到ES", messageReceiving);

            MessageDetail messageDetail = messageDetailMapper.selectById(
                    messageReceiving.getMessageDetailId()
            );

            log.info("开始保存增量信息");
            updateWithMessageDetail(messageDetail);
            log.info("新增消息成功");
        });
    }

    private void handleUpdate(JsonNode jsonNode) {
        MessageDetail messageDetail = extractMessageFromJsonNode(jsonNode);
        log.info("收到Message:{}的更新消息,开始同步消息到ES", messageDetail);
        cachedThreadPool.execute(() -> {
            log.info("开始更新Message:{}到ES", messageDetail);
            if (messageDetail.getIsDeleted() == 1) {
                // 其实这是一条删除消息
                handleDelete(jsonNode);
                return;
            }

            updateWithMessageDetail(messageDetail);
        });
        log.info("更新消息成功");
    }

    private void updateWithMessageDetail(MessageDetail messageDetail) {
        MessageDoc messageDoc = getMessageDocFromMessageDetail(messageDetail);

        UpdateRequest<MessageDoc, MessageDoc> updateRequest =
                new UpdateRequest.Builder<MessageDoc, MessageDoc>()
                        .index(MESSAGE_INDEX)
                        .id(String.valueOf(messageDetail.getId()))
                        .doc(messageDoc)
                        .upsert(messageDoc)
                        .build();
        UpdateResponse<MessageDoc> updateResponse;
        // 3.发送请求
        try {
            updateResponse =
                    elasticsearchClient.update(updateRequest, MessageDoc.class);
        } catch (IOException e) {
            log.error("更新消息失败,messageDoc:{}, error:{}", messageDoc, e.getMessage());
            throw new ApartmentException(
                    ApartmentError.UNKNOWN_ERROR, "ES更新消息失败");
        }
        if (updateResponse == null) {
            log.error("更新消息失败,messageDoc:{}", messageDoc);
            throw new ApartmentException(
                    ApartmentError.UNKNOWN_ERROR, "ES更新消息失败");
        }
    }

    private void handleDelete(JsonNode jsonNode) {
        MessageDetail messageDetail = extractMessageFromJsonNode(jsonNode);
        log.info("收到Message:{}的删除消息,开始同步消息到ES", messageDetail);
        cachedThreadPool.execute(() -> {
           log.info("开始从ES删除Message:{}", messageDetail);
            DeleteRequest deleteRequest = new DeleteRequest.Builder()
                    .index(MESSAGE_INDEX)
                    .id(String.valueOf(messageDetail.getId()))
                    .build();
            DeleteResponse deleteResponse;
            // 3.发送请求
            try {
                deleteResponse =
                        elasticsearchClient.delete(deleteRequest);
            } catch (IOException e) {
                log.error("删除消息失败,messageDetail:{}, error:{}",
                        messageDetail, e.getMessage());
                throw new ApartmentException(
                        ApartmentError.UNKNOWN_ERROR, "ES删除消息失败");
            }
            if (deleteResponse == null) {
                log.error("删除消息失败,messageDetail:{}", messageDetail);
                throw new ApartmentException(
                        ApartmentError.UNKNOWN_ERROR, "ES删除消息失败");
            }
            log.info("删除消息成功");
        });
    }

    private MessageDoc getMessageDocFromMessageDetail(MessageDetail messageDetail) {
        List<MessageReceiving> messageReceiving =
                messageReceivingMapper.selectList(
                        new LambdaQueryWrapper<MessageReceiving>()
                                .eq(MessageReceiving::getMessageDetailId,
                                        messageDetail.getId())
                );
        List<Long> receiverIds = new ArrayList<>();
        for (MessageReceiving message : messageReceiving) {
            if (message.getReceiverAdminId() != null) {
                receiverIds.add(message.getReceiverAdminId());
            } else {
                receiverIds.add(message.getReceiverUserId());
            }
        }
        Admin admin = userServiceClient
                .getAdminById(messageDetail.getSenderAdminId());

        return MessageDoc.builder()
                .id(messageDetail.getId())
                .message(messageDetail.getMessage())
                .createTime(messageDetail.getCreateTime())
                .isWithdraw(messageDetail.getIsWithdrawn())
                .senderAdminId(messageDetail.getSenderAdminId())
                .senderAdminName(admin.getName())
                .receiverIds(receiverIds)
                .build();
    }

    private MessageReceiving extractMessageReceivingFromJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .treeToValue(jsonNode.get("data").get(0), MessageReceiving.class);
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "JSON解析失败");
        }
    }

    private MessageDetail extractMessageFromJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .treeToValue(jsonNode.get("data").get(0), MessageDetail.class);
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "JSON解析失败");
        }
    }
}
