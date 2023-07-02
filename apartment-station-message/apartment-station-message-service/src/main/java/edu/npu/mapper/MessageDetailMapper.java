package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.MessageDetail;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【message_detail(消息详情表)】的数据库操作Mapper
* @createDate 2023-07-02 13:57:20
* @Entity edu.npu.entity.MessageDetail
*/
@Mapper
public interface MessageDetailMapper extends BaseMapper<MessageDetail> {

}




