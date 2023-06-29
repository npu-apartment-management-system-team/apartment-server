package edu.npu.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.doc.ApartmentDoc;
import edu.npu.entity.Apartment;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.util.RedisClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static edu.npu.EsConstants.APARTMENT_INDEX;
import static edu.npu.common.RedisConstants.CACHE_APARTMENT_KEY;
import static edu.npu.common.RedisConstants.CACHE_APARTMENT_TTL;

/**
 * @author : [wangminan]
 * @description : [监听apartment表的变动]
 */
@Slf4j
@Component
public class MqApartmentListener {
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedisClient redisClient;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

    /**
     * 接收canal转发的数据库变动信息
     * 该方法只处理和apartment表有关的变动信息
     * @param msg canal转发的数据库变动信息
     */
    @RabbitListener(queues = "mysql.apartment.queue")
    public void listenApartmentQueue(String msg){
        log.info("消费者接收到mysql.apartment.queue的信息:{}", msg);
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(msg);
            // 我们需要两个线程 同时在ElasticSearch与Redis中进行操作。
            if (!jsonNode.get("database").asText().equals("apartment_system")) {
                log.info("接收到非本项目数据库的消息,db:{}",
                        jsonNode.get("database").asText());
                return;
            } else if (!jsonNode.get("table").asText().equals("apartment")) {
                // 由于用的是fanout 所以确实存在这种可能 我们一个业务需要对应一个listener
                log.info("接收到非本业务消息,table:{}",
                        jsonNode.get("table").asText());
                return;
            }
            // ok 是我们要处理的内容
            // ok 是我们要处理的内容
            if (jsonNode.get("type").asText().equals("INSERT")) {
                log.info("开始处理新增apartment的同步");
                handleInsert(jsonNode);
            } else if (jsonNode.get("type").asText().equals("UPDATE")) {
                log.info("开始处理更新apartment的同步");
                handleUpdate(jsonNode);
            } else if (jsonNode.get("type").asText().equals("DELETE")) {
                log.info("开始处理删除apartment的同步");
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
        Apartment apartment = extractApartmentFromJsonNode(jsonNode);
        log.info("收到apartment:{}的新增消息", apartment);
        // ElasticSearch缓存预热 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始保存apartment:{}到ES",apartment);
            ApartmentDoc apartmentDoc = new ApartmentDoc(apartment);
            IndexResponse indexResponse;
            try {
                indexResponse = elasticsearchClient.index(index ->
                        index.index(APARTMENT_INDEX)
                                .id(String.valueOf(apartment.getId()))
                                .document(apartmentDoc)
                );
            } catch (IOException e) {
                log.error("新增公寓失败,apartmentDoc:{}, error:{}", apartmentDoc, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES保存失败");
            }
            if (indexResponse == null) {
                log.error("新增公寓失败,apartmentDoc:{}", apartmentDoc);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES保存失败");
            }
            log.info("新增公寓成功");
        });
        // Redis缓存预热
        redisClient.setWithLogicalExpire(
                CACHE_APARTMENT_KEY, apartment.getId(), apartment, CACHE_APARTMENT_TTL, TimeUnit.MINUTES);
    }

    private void handleUpdate(JsonNode jsonNode) {
        Apartment apartment = extractApartmentFromJsonNode(jsonNode);
        log.info("收到apartment:{}的更新消息", apartment);
        // ElasticSearch缓存更新 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始更新apartment:{}到ES",apartment);
            ApartmentDoc apartmentDoc = new ApartmentDoc(apartment);
            UpdateRequest<ApartmentDoc, ApartmentDoc> updateRequest =
                    new UpdateRequest.Builder<ApartmentDoc, ApartmentDoc>()
                            .index(APARTMENT_INDEX)
                            .id(String.valueOf(apartment.getId()))
                            .doc(apartmentDoc)
                            .upsert(apartmentDoc)
                            .build();
            UpdateResponse<ApartmentDoc> updateResponse;
            // 3.发送请求
            try {
                updateResponse =
                        elasticsearchClient.update(updateRequest, ApartmentDoc.class);
            } catch (IOException e) {
                log.error("更新公寓失败,apartmentDoc:{}, error:{}", apartmentDoc, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES更新失败");
            }
            if (updateResponse == null) {
                log.error("更新公寓失败,apartmentDoc:{}", apartmentDoc);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES更新失败");
            }
            log.info("更新公寓成功");
        });
        // Redis缓存更新 先删除再重建
        stringRedisTemplate.delete(CACHE_APARTMENT_KEY + apartment.getId());
        redisClient.setWithLogicalExpire(
                CACHE_APARTMENT_KEY, apartment.getId(), apartment,
                CACHE_APARTMENT_TTL, TimeUnit.MINUTES);
    }

    private void handleDelete(JsonNode jsonNode) {
        Apartment apartment = extractApartmentFromJsonNode(jsonNode);
        // 删除ElasticSearch缓存 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始删除apartment:{}到ES",apartment);
            DeleteRequest deleteRequest = new DeleteRequest.Builder()
                    .index(APARTMENT_INDEX)
                    .id(String.valueOf(apartment.getId()))
                    .build();
            DeleteResponse deleteResponse;
            // 3.发送请求
            try {
                deleteResponse =
                        elasticsearchClient.delete(deleteRequest);
            } catch (IOException e) {
                log.error("删除公寓失败,apartmentDoc:{}, error:{}", apartment, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES删除失败");
            }
            if (deleteResponse == null) {
                log.error("删除公寓失败,apartmentDoc:{}", apartment);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES删除失败");
            }
            log.info("删除公寓成功");
        });
        // 删除Redis缓存
        stringRedisTemplate.delete(CACHE_APARTMENT_KEY + apartment.getId());
    }

    private Apartment extractApartmentFromJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .treeToValue(jsonNode.get("data").get(0), Apartment.class);
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "JSON解析失败");
        }
    }
}
