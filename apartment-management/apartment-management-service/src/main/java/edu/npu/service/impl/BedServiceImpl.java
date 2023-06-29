package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Bed;
import edu.npu.service.BedService;
import edu.npu.mapper.BedMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【bed(床位表)】的数据库操作Service实现
* @createDate 2023-06-29 09:13:20
*/
@Service
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed>
    implements BedService{

}




