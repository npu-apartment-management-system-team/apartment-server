package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.SendMessageDto;
import edu.npu.entity.MessageDetail;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangminan
 * @description 针对表【message_detail(消息详情表)】的数据库操作Service
 * @createDate 2023-07-02 13:57:20
 */
public interface MessageDetailService extends IService<MessageDetail> {

    @Transactional(rollbackFor = Exception.class)
    R sendMessage(SendMessageDto sendMessageDto);

    @Transactional(rollbackFor = Exception.class)
    R getMessageDetail(String id);

    @Transactional(rollbackFor = Exception.class)
    R withdrawMessage(String id);

    @Transactional(rollbackFor = Exception.class)
    R deleteMessage(String id);
}
