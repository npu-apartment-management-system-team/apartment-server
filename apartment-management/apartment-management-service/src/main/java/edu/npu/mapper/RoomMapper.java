package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【room(房间表)】的数据库操作Mapper
* @createDate 2023-06-29 09:13:20
* @Entity edu.npu.entity.Room
*/
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

}




