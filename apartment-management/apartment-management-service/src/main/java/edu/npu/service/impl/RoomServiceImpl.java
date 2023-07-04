package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.AddRoomDto;
import edu.npu.dto.PutRoomDto;
import edu.npu.entity.Bed;
import edu.npu.entity.Room;
import edu.npu.mapper.BedMapper;
import edu.npu.mapper.RoomMapper;
import edu.npu.service.RoomService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author wangminan
 * @description 针对表【room(房间表)】的数据库操作Service实现
 * @createDate 2023-06-29 09:13:20
 */
@Service
@Slf4j
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room>
        implements RoomService {

    @Resource
    private BedMapper bedMapper;

    @Override
    public R addRoom(AddRoomDto addRoomDto) {
        Room room = this.baseMapper.selectOne(new LambdaQueryWrapper<Room>()
                .eq(Room::getName, addRoomDto.name()));
        if (room != null) {
            return R.error("房间名[" + addRoomDto.name() + "]已存在");
        }
        Room newRoom = new Room();
        BeanUtils.copyProperties(addRoomDto, newRoom);
        newRoom.setApartmentId(Long.valueOf(addRoomDto.apartmentId()));
        boolean success = save(newRoom);
        if (success) {
            log.info("房间[" + addRoomDto.name() + "]添加成功");
            return R.ok("房间[" + addRoomDto.name() + "]添加成功");
        }
        log.error("房间[" + addRoomDto.name() + "]添加失败");
        return R.error("房间[" + addRoomDto.name() + "]添加失败");
    }

    @Override
    public R deleteRoom(Long id) {
        Room room = this.baseMapper.selectOne(new LambdaQueryWrapper<Room>()
                .eq(Room::getId, id));
        if (room == null) {
            log.error("id[" + id + "]的房间不存在");
            return R.error("id[" + id + "]的房间不存在");
        }
        boolean success = remove(new LambdaQueryWrapper<Room>()
                .eq(Room::getId, id));
        if (success) {
            log.info("id[" + id + "]的房间删除成功");
            return R.ok("id[" + id + "]的房间删除成功");
        }
        return R.error("id[" + id + "]的房间删除失败");
    }

    @Override
    public R updateRoom(Long id, PutRoomDto putRoomDto) {
        Room room = getOne(new LambdaQueryWrapper<Room>()
                .eq(Room::getName, putRoomDto.name()));
        if (room == null) {
            return R.error("房间名[" + putRoomDto.name() + "]不存在");
        }
        Room newRoom = new Room();
        BeanUtils.copyProperties(putRoomDto, newRoom);
        newRoom.setId(id);
        boolean success = updateById(newRoom);
        if (success) {
            log.info("房间[" + putRoomDto.name() + "]更新成功");
            return R.ok("房间[" + putRoomDto.name() + "]更新成功");
        }
        log.error("房间[" + putRoomDto.name() + "]更新失败");
        return R.error("房间[" + putRoomDto.name() + "]更新失败");
    }

    @Override
    public R getAllRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String
            isForCadre, Integer type) {
        Page<Room> allRoomPage = new Page<>(pageNum, pageSize);
        allRoomPage = this.baseMapper.selectPage(allRoomPage, new LambdaQueryWrapper<Room>()
                .eq(Room::getApartmentId, apartmentId)
                .eq(!isForCadre.isBlank(), Room::getIsForCadre, isForCadre)
                .eq(type != null, Room::getType, type)
                .like(query != null, Room::getName, query));

        if (allRoomPage.getTotal() > 0) {
            Map<String, Object> result = Map.of("total", allRoomPage.getTotal(), "list", allRoomPage.getRecords());
            return R.ok().put("result", result);
        }
        return R.error("查询的房间暂无数据");
    }

    @Override
    public R getUnreservedRoom(Integer pageNum, Integer pageSize, String apartmentId, String query, String
            isForCadre, Integer type) {
        Page<Room> unreservedPage = new Page<>(pageNum, pageSize);
        unreservedPage = this.baseMapper.selectPage(unreservedPage, new LambdaQueryWrapper<Room>()
                .eq(Room::getApartmentId, apartmentId)
                .eq(Room::getIsReserved, 0)
                .eq(!isForCadre.isBlank(), Room::getIsForCadre, isForCadre)
                .eq(type != null, Room::getType, type)
                .like(query != null, Room::getName, query));

        if (unreservedPage.getTotal() > 0) {
            Map<String, Object> result = Map.of("total", unreservedPage.getTotal(), "list", unreservedPage.getRecords());
            return R.ok().put("result", result);
        }
        return R.error("查询的非保留房间暂无数据");
    }

    @Override
    public R getRoomDetail(Long id) {
        Room room = getOne(new LambdaQueryWrapper<Room>()
                .eq(Room::getId, id));
        if (room != null) {
            List<Bed> beds = bedMapper.selectList(new LambdaQueryWrapper<Bed>()
                    .eq(Bed::getRoomId, id));
            Map<String, Object> result = Map.of("room", room, "beds", beds);
            return R.ok().put("result", result);
        }
        return R.error("查询的房间[" + id + "]不存在");
    }
}




