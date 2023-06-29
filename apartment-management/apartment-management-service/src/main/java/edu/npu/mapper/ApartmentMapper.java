package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Apartment;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【apartment(公寓表)】的数据库操作Mapper
* @createDate 2023-06-29 09:13:20
* @Entity edu.npu.entity.Apartment
*/
@Mapper
public interface ApartmentMapper extends BaseMapper<Apartment> {

}




