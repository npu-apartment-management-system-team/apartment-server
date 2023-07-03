package edu.npu.listener;

import edu.npu.service.PaymentUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [延迟队列监听器]
 */
@Slf4j
@Component
public class PaymentDelayListener {

    @Resource
    private PaymentUserService paymentUserService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "delay.queue", durable = "true"),
            exchange = @Exchange(name = "delay.direct", delayed = "true"),
            key = "delay"
    ))
    public void listenDelayExchange(String msg) {
        log.info("消费者接收到了delay.queue的延迟消息,msg:{}",msg);
        // 查单业务逻辑
        log.info("开始查询订单号:{}有关的支付业务",msg);
        boolean paymentQuery = paymentUserService.tradeQuery(msg);
        log.info("查询订单号:{}有关的支付业务结束,结果:{}",msg,paymentQuery);
    }
}
