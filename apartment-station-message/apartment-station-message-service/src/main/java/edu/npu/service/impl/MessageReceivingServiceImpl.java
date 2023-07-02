package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.MessageReceiving;
import edu.npu.service.MessageReceivingService;
import edu.npu.mapper.MessageReceivingMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【message_receiving(消息接收表。)】的数据库操作Service实现
* @createDate 2023-07-02 13:57:20
*/
@Service
public class MessageReceivingServiceImpl extends ServiceImpl<MessageReceivingMapper, MessageReceiving>
    implements MessageReceivingService{

}




