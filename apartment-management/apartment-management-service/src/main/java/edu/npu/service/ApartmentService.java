package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.ApartmentDto;
import edu.npu.entity.Apartment;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【apartment(公寓表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface ApartmentService extends IService<Apartment> {

    @Transactional(rollbackFor = Exception.class)
    R addApartment(ApartmentDto apartmentDto);

    @Transactional(rollbackFor = Exception.class)
    R deleteApartment(Long id);

    @Transactional(rollbackFor = Exception.class)
    R updateApartment(Long id, ApartmentDto apartmentDto);
}
