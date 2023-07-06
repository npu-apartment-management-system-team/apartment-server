package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Apartment;
import edu.npu.entity.Bed;
import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.mapper.DepartmentMapper;
import edu.npu.service.ApartmentService;
import edu.npu.service.BedService;
import edu.npu.service.RoomService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [feign远程调用接口]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private BedService bedService;

    @Resource
    private RoomService roomService;

    @Resource
    private ApartmentService apartmentService;

    @GetMapping("/department")
    public Department getDepartmentById(@RequestParam(value = "id") Long id) {
        return departmentMapper.selectById(id);
    }

    @GetMapping("/room")
    public Room getRoomById(@RequestParam(value = "id") Long id) {
        return roomService.getById(id);
    }

    @GetMapping("/bed")
    public Bed getBedById(@RequestParam(value = "id") Long id) {
        return bedService.getById(id);
    }

    @PutMapping("/bed")
    public boolean updateBed(@RequestBody Bed bed) {
        return bedService.updateById(bed);
    }

    @GetMapping("/bed/list")
    public List<Bed> getBedListByRoomId(@RequestParam(value = "roomId") Long roomId) {
        return bedService.list(new LambdaQueryWrapper<Bed>()
            .eq(Bed::getRoomId, roomId));
    }

    @GetMapping("/department/list/shard")
    public List<Department> getListByShardIndex(
            @RequestParam(value = "shardIndex") Long shardIndex,
            @RequestParam(value = "shardTotal") Integer shardTotal) {
        return departmentMapper.getListByShardIndex(shardIndex, shardTotal);
    }

    @GetMapping("/room/bedId")
    public Room getRoomByBedId(@RequestParam(value = "bedId") Long bedId) {
        Bed bed = bedService.getById(bedId);
        return roomService.getById(bed.getRoomId());
    }

    @GetMapping("/apartment")
    Apartment getApartmentByAdminId(@RequestParam(value = "adminId") Long adminId) {
        return apartmentService.getOne(
                new LambdaQueryWrapper<Apartment>().eq(
                        Apartment::getForemanAdminId, adminId
                )
        );
    }

    @GetMapping("/bedsByApartment")
    public List<Bed> getBedsByApartmentId(
            @RequestParam(value = "apartmentId") Long apartmentId
    ) {
        List<Room> rooms = roomService.list(
                new LambdaQueryWrapper<Room>().eq(Room::getApartmentId, apartmentId)
        );

        List<Bed> beds = new ArrayList<>();

        for (Room room : rooms) {
            beds.addAll(bedService.list(
                    new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, room.getId())
            ));
        }
        return beds;
    }
}
