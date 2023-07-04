package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.AddBedDto;
import edu.npu.dto.BedPageQueryDto;
import edu.npu.dto.BedQueryDto;
import edu.npu.dto.UpdateBedDto;
import edu.npu.entity.Bed;
import edu.npu.entity.Room;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.BedMapper;
import edu.npu.mapper.RoomMapper;
import edu.npu.service.BedService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangminan
 * @description 针对表【bed(床位表)】的数据库操作Service实现
 * @createDate 2023-06-29 09:13:20
 */
@Service
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed>
        implements BedService {

    @Resource
    private BedMapper bedMapper;

    @Resource
    private RoomMapper roomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getBedList(BedPageQueryDto bedPageQueryDto) {
        try {
            List<Bed> bedList = new ArrayList<>();
            boolean hasQuery = false;
            if (bedPageQueryDto.apartmentId() != null) {
                hasQuery = true;
                List<Room> roomList =
                        roomMapper.selectList(new LambdaQueryWrapper<Room>()
                                .eq(Room::getApartmentId, bedPageQueryDto.apartmentId()));
                bedList = list(new LambdaQueryWrapper<Bed>()
                        .in(Bed::getRoomId, roomList.stream().map(Room::getId).toArray()));
            } else if (bedPageQueryDto.roomId() != null) {
                hasQuery = true;
                bedList = list(new LambdaQueryWrapper<Bed>()
                        .eq(Bed::getRoomId, bedPageQueryDto.roomId()));
            }

            IPage<Bed> page = new Page<>(
                    bedPageQueryDto.pageNum(), bedPageQueryDto.pageSize());
            LambdaQueryWrapper<Bed> queryWrapper = new LambdaQueryWrapper<>();

            if (bedPageQueryDto.query() != null) {
                hasQuery = true;
                queryWrapper.like(Bed::getName, bedPageQueryDto.query());
            }
            if (bedPageQueryDto.apartmentId() != null ||
                    bedPageQueryDto.roomId() != null) {
                hasQuery = true;
                queryWrapper.in(Bed::getId, bedList.stream().map(Bed::getId).toArray());
            }
            if (bedPageQueryDto.isInUse() != null) {
                hasQuery = true;
                queryWrapper.eq(Bed::getIsInUse, bedPageQueryDto.isInUse());
            }
            if (hasQuery) {
                bedMapper.selectPage(page, queryWrapper);
            } else {
                bedMapper.selectPage(page, null);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", page.getRecords());
            resultMap.put("total", page.getTotal());
            return R.ok().put("result", resultMap);
        } catch (Exception e) {
            throw new ApartmentException("查询管理员列表失败！");
        }
    }

    @Override
    public R getBedById(BedQueryDto bedQueryDto) {
        return bedMapper.selectById(bedQueryDto.id()) == null ?
                R.error("查询失败！") :
                R.ok().put("result", bedMapper.selectById(bedQueryDto.id()));
    }

    @Override
    public R addBed(AddBedDto addBedDto) {
        Bed bed=new Bed();
        BeanUtils.copyProperties(addBedDto,bed);
        return bedMapper.insert(bed) == 1 ?
                R.ok("添加成功!") : R.error("添加失败!");
    }

    @Override
    public R updateBed(Long id, UpdateBedDto updateBedDto) {
        Bed bed = bedMapper.selectById(id);
        BeanUtils.copyProperties(updateBedDto,bed);
        bed.setId(id);
        return bedMapper.updateById(bed) == 1 ?
                R.ok("修改成功!") : R.error("修改失败!");
    }

    @Override
    public R deleteBed(Long id) {
        bedMapper.deleteById(id);
        return bedMapper.deleteById(id) == 1 ?
                R.ok("删除成功!") : R.error("删除失败!");
    }


}




