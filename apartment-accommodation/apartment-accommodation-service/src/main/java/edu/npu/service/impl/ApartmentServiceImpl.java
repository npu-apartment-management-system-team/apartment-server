package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Apartment;
import edu.npu.service.ApartmentService;
import edu.npu.mapper.ApartmentMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【apartment(公寓表)】的数据库操作Service实现
* @createDate 2023-06-29 21:18:43
*/
@Service
public class ApartmentServiceImpl extends ServiceImpl<ApartmentMapper, Apartment>
    implements ApartmentService{

}




