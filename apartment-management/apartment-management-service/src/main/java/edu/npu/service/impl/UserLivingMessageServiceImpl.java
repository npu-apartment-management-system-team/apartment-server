package edu.npu.service.impl;

import edu.npu.entity.*;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApartmentMapper;
import edu.npu.mapper.BedMapper;
import edu.npu.mapper.DepartmentMapper;
import edu.npu.mapper.RoomMapper;
import edu.npu.service.UserLivingMessageService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserLivingMessageServiceImpl implements UserLivingMessageService {

    @Resource
    UserServiceClient userServiceClient;

    @Resource
    DepartmentMapper departmentMapper;

    @Resource
    BedMapper bedMapper;

    @Resource
    RoomMapper roomMapper;

    @Resource
    ApartmentMapper apartmentMapper;

    @Override
    public R getUserLivingMessage(AccountUserDetails accountUserDetails) {
        User user = userServiceClient.getUserById(accountUserDetails.getId());
        if (user == null) {
            log.error("id[" + accountUserDetails.getId() + "]的用户不存在");
            return R.error("id[" + accountUserDetails.getId() + "]的用户不存在");
        }

        Map<String, Object> map = new HashMap<>();

        Department department = departmentMapper.selectById(user.getDepartmentId());

        map.put("department", department);

        Bed bed = bedMapper.selectById(user.getBedId());

        if (bed == null) {
            map.put("bed", null);
            map.put("room", null);
            map.put("apartment", null);
            return R.ok("用户信息查询成功").put("result", map);
        }
        map.put("bed", bed);

        Room room = roomMapper.selectById(bed.getRoomId());
        if (room == null) {
            map.put("room", null);
            map.put("apartment", null);
            return R.ok("用户信息查询成功").put("result", map);
        }
        map.put("room", room);

        Apartment apartment = apartmentMapper.selectById(room.getApartmentId());
        if (apartment == null) {
            map.put("apartment", null);
            return R.ok("用户信息查询成功").put("result", map);
        }

        map.put("apartment", apartment);
        return R.ok("用户信息查询成功").put("result", map);
    }
}
