package edu.npu.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}
