package edu.npu.controller;

import edu.npu.dto.AddRoomDto;
import edu.npu.dto.PutRoomDto;
import edu.npu.service.RoomService;
import edu.npu.vo.R;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */

@RestController
@Slf4j
@RequestMapping("/room")
public class RoomController {

    @Resource
    private RoomService roomService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R addRoom(@RequestBody AddRoomDto addRoomDto) {
        return roomService.addRoom(addRoomDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoom(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R updateRoom(@PathVariable Long id, @RequestBody PutRoomDto putRoomDto) {
        return roomService.updateRoom(id, putRoomDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getAllRoom(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam String apartmentId,
                        @RequestParam(required = false) String query, @RequestParam(required = false) String isForCadre, @RequestParam(required = false) Integer type) {
        return roomService.getAllRoom(pageNum, pageSize, apartmentId, query, isForCadre, type);
    }

    @GetMapping("/unreserved")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getUnreservedRoom(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam String apartmentId,
                         @RequestParam(required = false) String query, @RequestParam(required = false)  String isForCadre, @RequestParam(required = false)  Integer type){
        return roomService.getUnreservedRoom(pageNum, pageSize, apartmentId, query, isForCadre, type);
    }


    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getRoomDetail(@RequestParam Long id) {
        return roomService.getRoomDetail(id);
    }


}
