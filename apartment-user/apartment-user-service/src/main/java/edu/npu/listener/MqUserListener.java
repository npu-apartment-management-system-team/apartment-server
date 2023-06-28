package edu.npu.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.doc.UserDoc;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.util.RedisClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.EsConstants.USER_INDEX;
import static edu.npu.common.RedisConstants.*;

/**
 * @author : [wangminan]
 * @description : [承接Canal消息转发的后端服务]
 */
@Slf4j
@Component
public class MqUserListener {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedisClient redisClient;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LoginAccountMapper loginAccountMapper;

    // 负责执行新线程上其他任务的线程池
    private static final ExecutorService cachedThreadPool =
        Executors.newFixedThreadPool(
            // 获取系统核数
            Runtime.getRuntime().availableProcessors()
        );

    /**
     * 接收由canal转发的数据库变动信息
     * 在收到消息通知后需要进行解析 并将
     * @param msg canal传入的信息
     * JSON格式 样例如下
    {
        "data": [
            {
                "id": "1",
                "login_account_id": "1",
                "department_id": "1",
                "bed_id": null,
                "name": "王旻安",
                "personal_id": "12554684512",
                "personal_card_url": "https://wangminan-files.oss-cn-hongkong.aliyuncs.com/default/%E8%BA%AB%E4%BB%BD%E8%AF%81.png",
                "face_id": "42686726",
                "face_url": "https://wangminan-files.oss-cn-hongkong.aliyuncs.com/default/%E7%99%BD%E5%BA%95%E8%AF%81%E4%BB%B6%E7%85%A7.jpg",
                "alipay_id": "2088722003402347",
                "email": "wangminan0815@hotmail.com",
                "sex": "1",
                "is_cadre": "0",
                "status": "0",
                "pay_type": "0",
                "network_enabled": "0",
                "is_deleted": "0"
            }
        ],
        "database": "apartment_system",
        "es": 1687872748000,
        "id": 177,
        "isDdl": false,
        "mysqlType": {
            "id": "bigint",
            "login_account_id": "bigint",
            "department_id": "bigint",
            "bed_id": "bigint",
            "name": "varchar(15)",
            "personal_id": "varchar(18)",
            "personal_card_url": "varchar(512)",
            "face_id": "varchar(128)",
            "face_url": "varchar(512)",
            "alipay_id": "varchar(64)",
            "email": "varchar(256)",
            "sex": "int",
            "is_cadre": "tinyint(1)",
            "status": "int",
            "pay_type": "int",
            "network_enabled": "tinyint(1)",
            "is_deleted": "int"
        },
        "old": [
            {
                "sex": "2"
            }
        ],
        "pkNames": [
            "id"
        ],
        "sql": "",
        "sqlType": {
            "id": -5,
            "login_account_id": -5,
            "department_id": -5,
            "bed_id": -5,
            "name": 12,
            "personal_id": 12,
            "personal_card_url": 12,
            "face_id": 12,
            "face_url": 12,
            "alipay_id": 12,
            "email": 12,
            "sex": 4,
            "is_cadre": -6,
            "status": 4,
            "pay_type": 4,
            "network_enabled": -6,
            "is_deleted": 4
        },
        "table": "user",
        "ts": 1687872748536,
        "type": "UPDATE"
        }
     */
    @RabbitListener(queues = "mysql.user.queue")
    public void listenUserQueue(String msg) {
        log.info("消费者接收到mysql.user.queue的消息：【" + msg + "】");
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(msg);
            // 我们需要两个线程 同时在ElasticSearch与Redis中进行操作。
            if (!jsonNode.get("database").asText().equals("apartment_system")) {
                log.info("接收到非本项目数据库的消息,db:{}",
                        jsonNode.get("database").asText());
                return;
            } else if (!jsonNode.get("table").asText().equals("user")) {
                // 由于用的是fanout 所以确实存在这种可能 我们一个业务需要对应一个listener
                log.info("接收到非本业务消息,table:{}",
                        jsonNode.get("table").asText());
                return;
            }
            // ok 是我们要处理的内容
            if (jsonNode.get("type").asText().equals("INSERT")) {
                log.info("开始处理新增User的同步");
                handleInsert(jsonNode);
            } else if (jsonNode.get("type").asText().equals("UPDATE")) {
                log.info("开始处理更新User的同步");
                handleUpdate(jsonNode);
            } else if (jsonNode.get("type").asText().equals("DELETE")) {
                log.info("开始处理删除User的同步");
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
        User user = extractUserFromJsonNode(jsonNode);
        log.info("收到user:{}的新增消息", user);
        // ElasticSearch缓存预热 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始保存user:{}到ES",user);
            UserDoc userDoc = new UserDoc(user);
            IndexResponse indexResponse;
            try {
                indexResponse = elasticsearchClient.index(index ->
                        index.index(USER_INDEX)
                                .id(String.valueOf(user.getId()))
                                .document(userDoc)
                );
            } catch (IOException e) {
                log.error("新增用户失败,userDoc:{}, error:{}", userDoc, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES保存失败");
            }
            if (indexResponse == null) {
                log.error("新增用户失败,userDoc:{}", userDoc);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES保存失败");
            }
            log.info("新增用户成功");
        });
        // Redis缓存预热
        redisClient.setWithLogicalExpire(
                CACHE_USER_KEY, user.getId(), user, CACHE_USER_TTL, TimeUnit.MINUTES);
    }

    private void handleUpdate(JsonNode jsonNode) {
        User user = extractUserFromJsonNode(jsonNode);
        log.info("收到user:{}的更新消息", user);
        // ElasticSearch缓存更新 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始更新user:{}到ES",user);
            UserDoc userDoc = new UserDoc(user);
            UpdateRequest<UserDoc, UserDoc> updateRequest =
                    new UpdateRequest.Builder<UserDoc, UserDoc>()
                            .index(USER_INDEX)
                            .id(String.valueOf(user.getId()))
                            .doc(userDoc)
                            .upsert(userDoc)
                            .build();
            UpdateResponse<UserDoc> updateResponse;
            // 3.发送请求
            try {
                updateResponse =
                        elasticsearchClient.update(updateRequest, UserDoc.class);
            } catch (IOException e) {
                log.error("更新用户失败,userDoc:{}, error:{}", userDoc, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES更新失败");
            }
            if (updateResponse == null) {
                log.error("更新用户失败,userDoc:{}", userDoc);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES更新失败");
            }
            log.info("更新用户成功");
        });
        // Redis缓存更新 先删除再重建
        stringRedisTemplate.delete(CACHE_USER_KEY + user.getId());
        redisClient.setWithLogicalExpire(
                CACHE_USER_KEY, user.getId(), user, CACHE_USER_TTL, TimeUnit.MINUTES);
        // 还需要查登录信息 如果有则同步更新
        LoginAccount loginAccount =
                loginAccountMapper.selectById(user.getLoginAccountId());
        // 更新map中的HASH_LOGIN_ACCOUNT_KEY指向的value
        try {
            stringRedisTemplate.opsForHash()
                    .put(LOGIN_ACCOUNT_KEY_PREFIX + loginAccount.getUsername(),
                            HASH_LOGIN_ACCOUNT_KEY,
                            objectMapper.writeValueAsString(loginAccount));
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "JSON序列化失败");
        }
    }

    private void handleDelete(JsonNode jsonNode) {
        User user = extractUserFromJsonNode(jsonNode);
        // 删除ElasticSearch缓存 新线程
        cachedThreadPool.execute(() -> {
            log.info("开始删除user:{}到ES",user);
            DeleteRequest deleteRequest = new DeleteRequest.Builder()
                    .index(USER_INDEX)
                    .id(String.valueOf(user.getId()))
                    .build();
            DeleteResponse deleteResponse;
            // 3.发送请求
            try {
                deleteResponse =
                        elasticsearchClient.delete(deleteRequest);
            } catch (IOException e) {
                log.error("删除用户失败,userDoc:{}, error:{}", user, e.getMessage());
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES删除失败");
            }
            if (deleteResponse == null) {
                log.error("删除用户失败,userDoc:{}", user);
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "ES删除失败");
            }
            log.info("删除用户成功");
        });
        // 删除Redis缓存
        stringRedisTemplate.delete(CACHE_USER_KEY + user.getId());
    }

    private User extractUserFromJsonNode(JsonNode jsonNode) {
        try {
            return objectMapper
                    .treeToValue(jsonNode.get("data").get(0), User.class);
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "JSON解析失败");
        }
    }
}
