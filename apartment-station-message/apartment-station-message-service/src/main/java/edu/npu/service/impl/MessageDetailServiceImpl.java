package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

        MessageDetail messageDetail = new MessageDetail();
        messageDetail.setSenderAdminId(Long.valueOf(sendMessageDto.senderAdminId()));
        messageDetail.setMessage(sendMessageDto.message());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date;
        try {
            date = sdf.parse(String.valueOf(new Date()));
        } catch (ParseException e) {
            throw new ApartmentException("存储信息时时间转换异常");
        }
        messageDetail.setCreateTime(date);

        boolean success = save(messageDetail);
        Long messageDetailID = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getSenderAdminId, sendMessageDto.senderAdminId())
                .eq(MessageDetail::getCreateTime, date)
        ).getId();

        if (success&&messageDetailID!=null) {
            for (String receiverAdminId : sendMessageDto.receiverAdminIds()) {
                MessageReceiving messageReceiving = new MessageReceiving();
                messageReceiving.setMessageDetailId(messageDetailID);
                messageReceiving.setReceiverAdminId(Long.valueOf(receiverAdminId));
                messageReceivingMapper.insert(messageReceiving);
            }
            for (String receiverUserId : sendMessageDto.receiverUserIds()) {
                MessageReceiving messageReceiving = new MessageReceiving();
                messageReceiving.setMessageDetailId(messageDetailID);
                messageReceiving.setReceiverUserId(Long.valueOf(receiverUserId));
                messageReceivingMapper.insert(messageReceiving);
            }
            return R.ok("message存储成功");
        } else {
            log.error("messageDetail存储失败");
            return R.error("messageDetail存储失败");
        }

    }

    @Override
    public R getMessageDetail(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsWithdrawn,0)
                .eq(MessageDetail::getIsDeleted, 0));
        if(messageDetail==null){
            log.error("id[{}]的信息不存在",id);
            return R.error("id["+id+"]的信息不存在");
        }
        List<MessageReceiving> messageReceivingList = messageReceivingMapper.selectList(new LambdaQueryWrapper<MessageReceiving>()
                .eq(MessageReceiving::getMessageDetailId, id));
        GetMessageDetailVo getMessageDetailVo = new GetMessageDetailVo(messageDetail,messageReceivingList);

        return R.ok().put("result",getMessageDetailVo);
    }

    @Override
    public R withdrawMessage(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsDeleted, 0));

        if(messageDetail==null){
            log.error("id[{}]的信息不存在",id);
            return R.error("id["+id+"]的信息不存在");
        }
        if(messageDetail.getIsWithdrawn()==1){
            log.error("id[{}]的信息已经撤回",id);
            return R.error("id["+id+"]的信息已经撤回");
        }

        List<Long> deleteList = messageReceivingMapper.selectList(new LambdaQueryWrapper<MessageReceiving>()
                .eq(MessageReceiving::getMessageDetailId, id)
                .eq(MessageReceiving::getIsDeleted, 0)
        ).stream().map(MessageReceiving::getId).toList();

        int delete = messageReceivingMapper.deleteBatchIds(deleteList);
        if(0==delete){
            log.error("id[{}]的信息与message_receiving表关联的信息删除失败",id);
            return R.error("id["+id+"]的信息与message_receiving表关联的信息删除失败");
        }

        messageDetail.setIsWithdrawn(1);
        boolean success = updateById(messageDetail);

        if(success){
            return R.ok("id["+id+"]的信息撤回成功");
        }
        throw new ApartmentException("id["+id+"]的信息撤回失败");
    }

    @Override
    public R deleteMessage(String id) {
        MessageDetail messageDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getId, id)
                .eq(MessageDetail::getIsDeleted, 0));
        if(messageDetail==null){
            log.error("id[{}]的信息不存在",id);
            return R.error("id["+id+"]的信息不存在");
        }

        messageDetail.setIsDeleted(1);
        boolean success = updateById(messageDetail);

        if(success){
            return R.ok("id["+id+"]的信息删除成功");
        }
        return R.error("id["+id+"]的信息删除失败");

    }

}




