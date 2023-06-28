package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.UserListQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.entity.User;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Service
* @createDate 2023-06-27 21:19:32
*/
public interface UserService extends IService<User> {

    R getUsersInfo(UserListQueryDto userListQueryDto);

    R updateUserInfo(Long id, UserUpdateDto userUpdateDto);

    R deleteUser(Long id);
}
