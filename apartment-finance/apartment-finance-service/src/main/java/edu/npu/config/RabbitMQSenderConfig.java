package edu.npu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQSenderConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        // 获取RabbitTemplate对象 每个RabbitTemplate都只能有一个ReturnCallback
        RabbitTemplate rabbitTemplate =
                applicationContext.getBean(RabbitTemplate.class);
        // 配置ReturnsCallback 原本的ReturnCallback已弃用
        rabbitTemplate.setReturnsCallback(
            returnedMessage -> {
                // 判断是否是延迟消息 延迟消息状态码312 不属于失败消息
                Integer receivedDelay =
                    returnedMessage.getMessage()
                        .getMessageProperties().getReceivedDelay();
                if (receivedDelay != null && receivedDelay > 0) {
                    // 是一个延迟消息，忽略这个错误提示
                    return;
                }
                // 记录日志
                log.error("消息发送到队列失败，响应码:{}, 失败原因:{}, " +
                                "交换机:{}, 路由key:{}, 消息:{}",
                    returnedMessage.getReplyCode(),
                    returnedMessage.getReplyText(),
                    returnedMessage.getExchange(),
                    returnedMessage.getRoutingKey(),
                    new String(returnedMessage.getMessage().getBody())
                );
                // 如果有需要的话，重发消息
            }
        );
    }
}
