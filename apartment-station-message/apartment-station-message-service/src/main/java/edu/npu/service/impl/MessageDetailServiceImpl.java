package edu.npu.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.SendMessageDto;
import edu.npu.entity.MessageDetail;
import edu.npu.entity.MessageReceiving;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.MessageDetailMapper;
import edu.npu.mapper.MessageReceivingMapper;
import edu.npu.service.MessageDetailService;
import edu.npu.vo.GetMessageDetailVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangminan
 * @description 针对表【message_detail(消息详情表)】的数据库操作Service实现
 * @createDate 2023-07-02 13:57:20
 */
@Service
@Slf4j
public class MessageDetailServiceImpl extends ServiceImpl<MessageDetailMapper, MessageDetail>
        implements MessageDetailService {

    @Resource
    private MessageReceivingMapper messageReceivingMapper;

    @Resource
    private UserServiceClient userServiceClient;


    @Override
    public R sendMessage(SendMessageDto sendMessageDto) {
        if (userServiceClient.getAdminById(Long.valueOf(sendMessageDto.senderAdminId())) == null) {
            throw new ApartmentException("senderAdminId[" + sendMessageDto.senderAdminId() + "]不存在");
        }

        MessageDetail messageDetail = new MessageDetail();
        messageDetail.setSenderAdminId(Long.valueOf(sendMessageDto.senderAdminId()));
        messageDetail.setMessage(sendMessageDto.message());


        DateTime dateTime = new DateTime();
        messageDetail.setCreateTime(dateTime);

        boolean success = save(messageDetail);

        Long messageDetailId = messageDetail.getId();


        if (success && messageDetailId != null) {
            for (String receiverAdminId : sendMessageDto.receiverAdminIds()) {
                if (userServiceClient.getAdminById(Long.valueOf(receiverAdminId)) == null) {
                    throw new ApartmentException("receiverAdminId[" + receiverAdminId + "]不存在");
                }
                MessageReceiving messageReceiving = new MessageReceiving();
                messageReceiving.setMessageDetailId(messageDetailId);
                messageReceiving.setReceiverAdminId(Long.valueOf(receiverAdminId));
                int s1 = messageReceivingMapper.insert(messageReceiving);
                if (0 == s1) {
                    throw new ApartmentException("senderId[" + sendMessageDto.senderAdminId() + "]的messageReceiving关联数据存储失败");
                }
            }
            for (String receiverUserId : sendMessageDto.receiverUserIds()) {
                if (userServiceClient.getUserById(Long.valueOf(receiverUserId)) == null) {
                    throw new ApartmentException("receiverUserId[" + receiverUserId + "]不存在");
                }
                MessageReceiving messageReceiving = new MessageReceiving();
                messageReceiving.setMessageDetailId(messageDetailId);
                messageReceiving.setReceiverUserId(Long.valueOf(receiverUserId));
                int s2 = messageReceivingMapper.insert(messageReceiving);
                if (0 == s2) {
                    throw new ApartmentException("senderId[" + sendMessageDto.senderAdminId() + "]的messageReceiving关联数据存储失败");
                }
            }
            return R.ok("senderId[" + sendMessageDto.senderAdminId() + "]message存储成功");
        } else {
            log.error("senderId[" + sendMessageDto.senderAdminId() + "]的messageDetail存储失败");
            return R.error("senderId[" + sendMessageDto.senderAdminId() + "]的messageDetail存储失败");
        }

    }


    @Override
    public R getMessageDetail(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsWithdrawn, 0)
                .eq(MessageDetail::getIsDeleted, 0));
        if (messageDetail == null) {
            log.error("id[{}]的信息不存在或已删除", id);
            return R.error("id[" + id + "]的信息不存在或已删除");
        }

        List<MessageReceiving> messageReceivingList = messageReceivingMapper.selectList(new LambdaQueryWrapper<MessageReceiving>()
                .eq(MessageReceiving::getMessageDetailId, id));
        for (MessageReceiving messageReceiving : messageReceivingList) {
            if (messageReceiving.getReceiverAdminId() != null && userServiceClient.getAdminById(messageReceiving.getReceiverAdminId()) == null) {
                messageReceivingList.remove(messageReceiving);
            }
            if (messageReceiving.getReceiverUserId() != null && userServiceClient.getUserById(messageReceiving.getReceiverUserId()) == null) {
                messageReceivingList.remove(messageReceiving);
            }
        }

        GetMessageDetailVo getMessageDetailVo = new GetMessageDetailVo(messageDetail, messageReceivingList);

        return R.ok().put("result", getMessageDetailVo);
    }

    @Override
    public R withdrawMessage(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsDeleted, 0));

        if (messageDetail == null) {
            log.error("id[{}]的信息不存在或已删除", id);
            return R.error("id[" + id + "]的信息不存在或已删除");
        }
        if (messageDetail.getIsWithdrawn() == 1) {
            log.error("id[{}]的信息已经撤回", id);
            return R.error("id[" + id + "]的信息已经撤回");
        }

        List<Long> deleteList = messageReceivingMapper.selectList(new LambdaQueryWrapper<MessageReceiving>()
                .eq(MessageReceiving::getMessageDetailId, id)
                .eq(MessageReceiving::getIsDeleted, 0)
        ).stream().map(MessageReceiving::getId).toList();

        int delete = messageReceivingMapper.deleteBatchIds(deleteList);
        if (0 == delete) {
            log.error("id[{}]的信息与message_receiving表关联的信息删除失败", id);
            return R.error("id[" + id + "]的信息与message_receiving表关联的信息删除失败");
        }

        messageDetail.setIsWithdrawn(1);
        boolean success = updateById(messageDetail);

        if (success) {
            return R.ok("id[" + id + "]的信息撤回成功");
        }
        throw new ApartmentException("id[" + id + "]的信息撤回失败");
    }

    @Override
    public R deleteMessage(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsDeleted, 0));
        if (messageDetail == null) {
            log.error("id[{}]的信息不存在或已删除", id);
            return R.error("id[" + id + "]的信息不存在或已删除");
        }

        messageDetail.setIsDeleted(1);
        boolean success = updateById(messageDetail);

        if (success) {
            return R.ok("id[" + id + "]的信息删除成功");
        }
        return R.error("id[" + id + "]的信息删除失败");

    }


}




