package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.LoginAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author wangminan
* @description 针对表【login_account(登录账号)】的数据库操作Mapper
* @createDate 2023-06-26 21:19:29
* @Entity edu.npu.entity.LoginAccount
*/
@Mapper
public interface LoginAccountMapper extends BaseMapper<LoginAccount> {

    @Select("select 1")
    int initDb();
}




