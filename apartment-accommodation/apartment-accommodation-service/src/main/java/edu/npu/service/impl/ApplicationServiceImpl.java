package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Application;
import edu.npu.service.ApplicationService;
import edu.npu.mapper.ApplicationMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【application(申请表)】的数据库操作Service实现
* @createDate 2023-06-29 21:18:43
*/
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
    implements ApplicationService{

}




