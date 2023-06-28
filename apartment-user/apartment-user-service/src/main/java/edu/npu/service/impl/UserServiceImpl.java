package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.UserListQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.UserService;
import edu.npu.util.JwtTokenProvider;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Service实现
* @createDate 2023-06-27 21:19:32
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private LoginAccountMapper loginAccountMapper;
    @Resource
    private JwtTokenProvider jwtTokenProvider;


    @Override
    public R getUsersInfo(UserListQueryDto userListQueryDto) {
        return null;
    }

    @Override
    public R updateUserInfo(Long id, UserUpdateDto userUpdateDto) {
        User user=this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId,id));
        if (user==null){
            log.error("所需更新的用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需更新的用户不存在");
        }
        int updateById=0;
        LoginAccount loginAccount = loginAccountMapper.selectById(user.getLoginAccountId());
        if(!loginAccount.getUsername().equals(userUpdateDto.username())){
            loginAccount.setUsername(userUpdateDto.username());
            updateById = loginAccountMapper.updateById(loginAccount);
        }
        BeanUtils.copyProperties(userUpdateDto,user);
        boolean userUpdate=this.updateById(user);
        if(userUpdate&&updateById == 1) {
            return R.ok("用户信息更新成功");
        }else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库更新用户信息失败");
        }
    }
}




