package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.ApartmentDto;
import edu.npu.entity.Apartment;
import edu.npu.mapper.ApartmentMapper;
import edu.npu.service.ApartmentService;
import edu.npu.vo.R;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【apartment(公寓表)】的数据库操作Service实现
* @createDate 2023-06-29 09:13:20
*/
@Service
public class ApartmentServiceImpl extends ServiceImpl<ApartmentMapper, Apartment>
    implements ApartmentService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addApartment(ApartmentDto apartmentDto) {
        Apartment apartment = new Apartment();
        BeanUtils.copyProperties(apartmentDto, apartment);
        boolean save = this.save(apartment);
        return save ? R.ok() : R.error(ResponseCodeEnum.SERVER_ERROR, "添加公寓失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteApartment(Long id) {
        boolean remove = this.removeById(id);
        return remove ? R.ok() : R.error(ResponseCodeEnum.SERVER_ERROR, "删除公寓失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateApartment(Long id, ApartmentDto apartmentDto) {
        Apartment apartment = getById(id);
        if (apartment == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "您希望改动的公寓不存在");
        } else {
            System.out.println(apartmentDto);
            BeanUtils.copyProperties(apartmentDto, apartment);
            boolean update = this.updateById(apartment);
            return update ? R.ok() : R.error(ResponseCodeEnum.SERVER_ERROR, "更新公寓失败");
        }
    }
}




