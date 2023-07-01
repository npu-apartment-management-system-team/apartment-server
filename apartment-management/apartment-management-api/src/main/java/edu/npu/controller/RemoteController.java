package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Bed;
import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.service.BedService;
import edu.npu.service.DepartmentService;
import edu.npu.service.RoomService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [feign远程调用接口]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private BedService bedService;

    @Resource
    private RoomService roomService;

    @GetMapping("/department")
    public Department getDepartmentById(@RequestParam(value = "id") Long id) {
        return departmentService.getById(id);
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
}
