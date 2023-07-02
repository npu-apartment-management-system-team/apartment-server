package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.MessageDetail;
import edu.npu.mapper.MessageDetailMapper;
import edu.npu.service.MessageDetailService;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【message_detail(消息详情表)】的数据库操作Service实现
* @createDate 2023-07-02 13:57:20
*/
@Service
public class MessageDetailServiceImpl extends ServiceImpl<MessageDetailMapper, MessageDetail>
    implements MessageDetailService{

}




