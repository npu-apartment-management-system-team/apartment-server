package edu.npu.service;

import edu.npu.dto.AddRoomDto;
import edu.npu.dto.PutRoomDto;
import edu.npu.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【room(房间表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface RoomService extends IService<Room> {
    

    R addRoom(AddRoomDto addRoomDto);

    R deleteRoom(Long id);

    R updateRoom(Long id, PutRoomDto putRoomDto);

    R getAllRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String isForCadre, Integer type);

    R getUnreservedRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String isForCadre, Integer type);

    R getRoomDetail(Long id);
}
