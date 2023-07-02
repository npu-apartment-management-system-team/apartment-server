package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.ProcessingApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wangminan
 * @description 针对表【processing_application(正在进行中的申请表)】的数据库操作Mapper
 * @createDate 2023-07-02 11:10:29
 * @Entity edu.npu.entity.ProcessingApplication
 */
@Mapper
public interface ProcessingApplicationMapper extends BaseMapper<ProcessingApplication> {

    @Select("SELECT * FROM processing_application " +
            "WHERE id % #{shardTotal} = #{shardIndex} limit #{count}")
    List<ProcessingApplication> getListByShardIndex(
            @Param("shardIndex") Long shardIndex,
            @Param("shardTotal") int shardTotal,
            @Param("count") int count);
}




