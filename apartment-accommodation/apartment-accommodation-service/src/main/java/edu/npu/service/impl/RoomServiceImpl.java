package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Room;
import edu.npu.service.RoomService;
import edu.npu.mapper.RoomMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【room(房间表)】的数据库操作Service实现
* @createDate 2023-06-29 21:18:43
*/
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room>
    implements RoomService{

}




