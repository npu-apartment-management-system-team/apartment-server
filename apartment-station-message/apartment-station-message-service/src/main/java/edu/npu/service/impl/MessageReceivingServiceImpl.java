package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.RoleEnum;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.MessageDetail;
import edu.npu.entity.MessageReceiving;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.MessageDetailMapper;
import edu.npu.mapper.MessageReceivingMapper;
import edu.npu.service.MessageReceivingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wangminan
 * @description 针对表【message_receiving(消息接收表。)】的数据库操作Service实现
 * @createDate 2023-07-02 13:57:20
 */
@Service
@Slf4j
public class MessageReceivingServiceImpl extends ServiceImpl<MessageReceivingMapper, MessageReceiving>
        implements MessageReceivingService {

    @Resource
    private MessageDetailMapper messageDetailMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    public R getMessageDetail(AccountUserDetails accountUserDetails, String id) {
        MessageDetail messageDetail = messageDetailMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsWithdrawn, 0));
        if (messageDetail == null) {
            log.error("id[{}]的信息不存在或被撤回", id);
            return R.error("id[" + id + "]的信息不存在或被撤回");
        }

        MessageReceiving messageReceiving = ownWaytoGetMessageReceiving(accountUserDetails, id);

        if (messageReceiving == null) {
            log.error("id[{}]的信息与message_receiving表关联的信息不存在或已删除", id);
            return R.error("id[" + id + "]的信息与message_receiving表关联的信息不存在或已删除");
        }
        messageReceiving.setIsAcked(1);
        this.updateById(messageReceiving);
        return R.ok().put("result", messageDetail);

    }



    @Override
    public R deleteMessage(AccountUserDetails accountUserDetails, String id) {
        MessageReceiving messageReceiving = ownWaytoGetMessageReceiving(accountUserDetails, id);
        if (messageReceiving == null) {
            log.error("id[{}]的信息与message_receiving表关联的信息不存在或已删除", id);
            return R.error("id[" + id + "]的信息与message_receiving表关联的信息不存在或已删除");
        }
        messageReceiving.setIsDeleted(1);
        boolean success = this.updateById(messageReceiving);
        if (success) {
            return R.ok("id[" + id + "]的信息与message_receiving表关联的信息删除成功");
        }
        log.error("id[{}]的信息与message_receiving表关联的信息删除失败", id);
        return R.error("id[" + id + "]的信息与message_receiving表关联的信息删除失败");
    }

    private MessageReceiving ownWaytoGetMessageReceiving(AccountUserDetails accountUserDetails, String id) {

        MessageReceiving messageReceiving;
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue()) {
            Long userId = userServiceClient.getUserByLoginAccountId(accountUserDetails.getId()).getId();
            messageReceiving = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageReceiving>()
                    .eq(MessageReceiving::getMessageDetailId, id)
                    .eq(MessageReceiving::getIsDeleted, 0)
                    .eq(MessageReceiving::getReceiverUserId, userId));
        } else {
            Long adminId = userServiceClient.getAdminByLoginAccountId(accountUserDetails.getId()).getId();
            messageReceiving = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageReceiving>()
                    .eq(MessageReceiving::getMessageDetailId, id)
                    .eq(MessageReceiving::getIsDeleted, 0)
                    .eq(MessageReceiving::getReceiverAdminId, adminId));
        }
        return messageReceiving;
    }
}




