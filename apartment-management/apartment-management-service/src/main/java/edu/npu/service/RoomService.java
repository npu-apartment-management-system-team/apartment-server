package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AddRoomDto;
import edu.npu.dto.PutRoomDto;
import edu.npu.entity.Room;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【room(房间表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface RoomService extends IService<Room> {


    /**
     * 添加房间
     * @param addRoomDto
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R addRoom(AddRoomDto addRoomDto);

    /**
     *
     * @param id
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R deleteRoom(Long id);

    /**
     * 更新房间信息
     * @param id
     * @param putRoomDto
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R updateRoom(Long id, PutRoomDto putRoomDto);

    /**
     * 获取所有房间
     * @param pageNum
     * @param pageSize
     * @param apartmentId
     * @param query
     * @param isForCadre
     * @param type
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R getAllRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String isForCadre, Integer type);

    /**
     * 获取未保留的房间
     * @param pageNum
     * @param pageSize
     * @param apartmentId
     * @param query
     * @param isForCadre
     * @param type
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R getUnreservedRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String isForCadre, Integer type);

    /**
     * 获取房间详情
     * @param id
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R getRoomDetail(Long id);
}
