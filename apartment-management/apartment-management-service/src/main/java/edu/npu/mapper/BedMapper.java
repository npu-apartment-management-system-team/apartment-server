package edu.npu.mapper;

import edu.npu.entity.Bed;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.Mapping;

/**
* @author wangminan
* @description 针对表【bed(床位表)】的数据库操作Mapper
* @createDate 2023-06-29 09:13:20
* @Entity edu.npu.entity.Bed
*/
@Mapper
public interface BedMapper extends BaseMapper<Bed> {
}




