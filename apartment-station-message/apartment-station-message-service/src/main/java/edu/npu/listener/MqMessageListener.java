package edu.npu.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            } else if (!jsonNode.get("table").asText().equals("message_detail")) {
                // 由于用的是fanout 所以确实存在这种可能 我们一个业务需要对应一个listener
                log.info("接收到非本业务消息,table:{}",
                        jsonNode.get("table").asText());
                return;
            }
            // ok 是我们要处理的内容
            if (jsonNode.get("type").asText().equals("INSERT")) {
                log.info("开始处理新增Message的同步");
                handleInsert(jsonNode);
            } else if (jsonNode.get("type").asText().equals("UPDATE")) {
                log.info("开始处理更新Message的同步");
                // handleUpdate(jsonNode);
            } else if (jsonNode.get("type").asText().equals("DELETE")) {
                log.info("开始处理删除Message的同步");
                // handleDelete(jsonNode);
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
        MessageDetail messageDetail = extractMessageFromJsonNode(jsonNode);
        log.info("收到Message:{}的更新消息,开始同步消息到ES", messageDetail);
        cachedThreadPool.execute(() -> {
            log.info("开始保存Message:{}到ES", messageDetail);

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

            MessageDoc messageDoc = MessageDoc.builder()
                    .id(messageDetail.getId())
                    .message(messageDetail.getMessage())
                    .createTime(messageDetail.getCreateTime())
                    .isWithdraw(messageDetail.getIsWithdrawn())
                    .senderAdminId(messageDetail.getSenderAdminId())
                    .senderAdminName(admin.getName())
                    .receiverIds(receiverIds)
                    .build();
        });
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
