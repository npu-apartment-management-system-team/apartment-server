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

        Page<Bed> bedPage = new Page<>(bedPageQueryDto.pageNum(), bedPageQueryDto.pageSize());

        List<Long> roomIdList = null;
        if (bedPageQueryDto.apartmentId() != null) {
            roomIdList = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                            .eq(Room::getApartmentId, bedPageQueryDto.apartmentId()))
                    .stream().map(Room::getId).toList();
        }

        bedPage = this.baseMapper.selectPage(bedPage, new LambdaQueryWrapper<Bed>()
                .eq(bedPageQueryDto.roomId() != null, Bed::getRoomId, bedPageQueryDto.roomId())
                .eq(bedPageQueryDto.isInUse() != null, Bed::getIsInUse, bedPageQueryDto.isInUse())
                .like(!bedPageQueryDto.query().isBlank(), Bed::getName, bedPageQueryDto.query())
                .in(roomIdList != null, Bed::getRoomId, roomIdList));

        Map<String, Object> resultMap = Map.of("list", bedPage.getRecords(), "total", bedPage.getTotal());
        return R.ok().put("result", resultMap);
    }

    @Override
    public R getBedById(BedQueryDto bedQueryDto) {
        return bedMapper.selectById(bedQueryDto.id()) == null ?
                R.error("查询失败！") :
                R.ok().put("result", bedMapper.selectById(bedQueryDto.id()));
    }

    @Override
    public R addBed(AddBedDto addBedDto) {
        Bed bed = new Bed();
        BeanUtils.copyProperties(addBedDto, bed);
        return bedMapper.insert(bed) == 1 ?
                R.ok("添加成功!") : R.error("添加失败!");
    }

    @Override
    public R updateBed(Long id, UpdateBedDto updateBedDto) {
        Bed bed = bedMapper.selectById(id);
        BeanUtils.copyProperties(updateBedDto, bed);
        bed.setId(id);
        return bedMapper.updateById(bed) == 1 ?
                R.ok("修改成功!") : R.error("修改失败!");
    }

    @Override
    public R deleteBed(Long id) {
        return bedMapper.deleteById(id) == 1 ?
                R.ok("删除成功!") : R.error("删除失败!");
    }


}




