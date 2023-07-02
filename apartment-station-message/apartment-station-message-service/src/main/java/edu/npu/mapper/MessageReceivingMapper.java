package edu.npu.mapper;

import edu.npu.entity.MessageReceiving;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【message_receiving(消息接收表。)】的数据库操作Mapper
* @createDate 2023-07-02 13:57:20
* @Entity edu.npu.entity.MessageReceiving
*/
@Mapper
public interface MessageReceivingMapper extends BaseMapper<MessageReceiving> {

}




