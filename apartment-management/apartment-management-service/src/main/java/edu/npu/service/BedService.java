package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AddBedDto;
import edu.npu.dto.BedPageQueryDto;
import edu.npu.dto.BedQueryDto;
import edu.npu.dto.UpdateBedDto;
import edu.npu.entity.Bed;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【bed(床位表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface BedService extends IService<Bed> {

    R getBedList(BedPageQueryDto bedListQueryDto);

    R getBedById(BedQueryDto bedQueryDto);

    R addBed(AddBedDto addBedDto);

    R updateBed(Long id, UpdateBedDto updateBedDto);

    R deleteBed(Long id);
}
