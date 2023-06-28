package edu.npu.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AddAdminDto;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Admin;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.mapper.AdminMapper;
import edu.npu.service.AdminService;
import edu.npu.service.LoginAccountService;
import edu.npu.util.RsaUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import static edu.npu.util.RegexPatterns.EMAIL_REGEX;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
* @author wangminan
* @description 针对表【admin(管理员)】的数据库操作Service实现
* @createDate 2023-06-27 21:19:31
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    public static final String REGISTER_FAILED_MSG = "注册失败,请检查用户名是否重复";

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private LoginAccountService loginAccountService;

    @Value("${var.rsa.private-key}")
    private String privateKey;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 新增管理员账号
     * @param addAdminDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addAdmin(AddAdminDto addAdminDto) {

//        if (isAddAdminValid(addAdminDto)){
//            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "新增管理员账号参数不完整");
//        }

        if (StringUtils.hasText(addAdminDto.email()) &&
                !addAdminDto.email().matches(EMAIL_REGEX)){
            // 正则表达式匹配
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "邮箱格式不正确");
        }

        // 添加到loginAccount表
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setUsername(addAdminDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, addAdminDto.password())));
        loginAccount.setRole(addAdminDto.role());
        boolean saveLoginAccount = loginAccountService.save(loginAccount);
        if (!saveLoginAccount){
            return R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
        }
        // 将用户信息添加到用户表
        Admin admin = new Admin();
        BeanUtils.copyProperties(addAdminDto, admin);
        int insert = adminMapper.insert(admin);
        return insert == 1 ? R.ok("管理员新增成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);


    }

    /**
     * 删除管理员账号
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteAdmin(Integer id) {

        Admin admin = adminMapper.selectById(id);

        if (admin == null) {
            log.error("所需删除的管理员不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需删除的管理员不存在");
        }

        //从admin表中删除账号
        this.removeById(admin);

        Long loginAccountId = admin.getLoginAccountId();

        //从login_accout表中删除账号
        loginAccountService.removeById(loginAccountId);

        //检验是否删除成功
        admin = this.getOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getId, id));
        LoginAccount loginAccount = loginAccountService.getOne(
                new LambdaQueryWrapper<LoginAccount>()
                        .eq(LoginAccount::getId, loginAccountId));
        if (admin == null && loginAccount == null) {
            return R.ok("账号删除成功");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库删除失败");
        }

    }

    /**
     * 修改管理员账号
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateAdmin(Integer id) {
        return null;
    }

    /**
     * 查询管理员账号列表
     * @param pageQueryDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getAdminList(PageQueryDto pageQueryDto) {
        return null;
    }

    /**
     * 查询班组长列表
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getForemanList() {
        return null;
    }


}
