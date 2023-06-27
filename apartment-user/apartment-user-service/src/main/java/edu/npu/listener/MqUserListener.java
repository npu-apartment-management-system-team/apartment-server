package edu.npu.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Slf4j
@Component
public class MqUserListener {

    @Resource
    private ObjectMapper objectMapper;

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
    public void listenSimpleQueue(String msg) {
        log.info("消费者接收到mysql.user.queue的消息：【" + msg + "】");
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(msg);
            // 我们需要两个线程 同时在ElasticSearch与Redis中进行操作。
        } catch (JsonProcessingException e) {
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "消息解析失败");
        }
        log.info("消费者处理消息成功！");
    }
}
