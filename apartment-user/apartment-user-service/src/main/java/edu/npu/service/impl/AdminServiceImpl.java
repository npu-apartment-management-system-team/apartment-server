package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AdminDto;
import edu.npu.dto.AdminPageQueryDto;
import edu.npu.entity.Admin;
import edu.npu.entity.LoginAccount;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.AdminMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.service.AdminService;
import edu.npu.util.RsaUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import static edu.npu.util.RegexPatterns.EMAIL_REGEX;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author wangminan
* @description 针对表【admin(管理员)】的数据库操作Service实现
* @createDate 2023-06-27 21:19:31
*/
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    public static final String ADD_FAILED_MSG = "新增失败,请检查用户名是否重复";

    public static final String UPDATE_FAILED_MSG = "修改失败";

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private LoginAccountMapper loginAccountMapper;

    @Value("${var.rsa.private-key}")
    private String privateKey;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 新增管理员账号
     * @param adminDto admin账号信息
     * @return R
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addAdmin(AdminDto adminDto) {

//        if (isAddAdminValid(addAdminDto)){
//            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "新增管理员账号参数不完整");
//        }

        if (StringUtils.hasText(adminDto.email()) &&
                !adminDto.email().matches(EMAIL_REGEX)){
            // 正则表达式匹配
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "邮箱格式不正确");
        }

        // 添加到loginAccount表
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setUsername(adminDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, adminDto.password())));
        loginAccount.setRole(adminDto.role());
        int isLoginAccountSave = loginAccountMapper.insert(loginAccount);
        if (isLoginAccountSave != 1){
            return R.error(ResponseCodeEnum.SERVER_ERROR, ADD_FAILED_MSG);
        }
        // 将管理员信息添加到管理员表
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminDto, admin);
        admin.setLoginAccountId(loginAccount.getId());
        int insert = adminMapper.insert(admin);
        return insert == 1 ? R.ok("管理员新增成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, ADD_FAILED_MSG);
    }

    /**
     * 删除管理员账号
     * @param id 管理员id
     * @return R
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteAdmin(Long id) {

        Admin admin = adminMapper.selectById(id);

        if (admin == null) {
            log.error("所需删除的管理员不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需删除的管理员不存在");
        }

        //从admin表中删除账号
        this.removeById(admin);

        Long loginAccountId = admin.getLoginAccountId();

        //从login_account表中删除账号
        loginAccountMapper.deleteById(loginAccountId);

        //检验是否删除成功
        admin = this.getOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getId, id));
        LoginAccount loginAccount = loginAccountMapper.selectById(loginAccountId);

        if (admin == null && loginAccount == null) {
            return R.ok("账号删除成功");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库删除失败");
        }

    }

    /**
     * 修改管理员账号
     * @param id 管理员id
     * @return 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateAdmin(Long id, AdminDto adminDto) {

        Admin admin = adminMapper.selectById(id);

        if (admin == null) {
            log.error("所需修改的管理员不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需修改的管理员不存在");
        }

        LoginAccount loginAccount = loginAccountMapper.selectById(admin.getLoginAccountId());
        /*
        修改loginAccount
         */
        loginAccount.setUsername(adminDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, adminDto.password())));
        loginAccount.setRole(adminDto.role());

        if (StringUtils.hasText(adminDto.email()) &&
                !adminDto.email().matches(EMAIL_REGEX)){
            // 正则表达式匹配
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "邮箱格式不正确");
        }

        BeanUtils.copyProperties(adminDto, admin);

        int updateLoginAccount = loginAccountMapper.updateById(loginAccount);
        int updateAdmin = adminMapper.updateById(admin);
        return (updateLoginAccount == 1 && updateAdmin == 1) ? R.ok("管理员信息修改成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, UPDATE_FAILED_MSG);

    }

    /**
     * 查询管理员账号列表
     * @param adminPageQueryDto 查询条件
     * @return 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getAdminList(AdminPageQueryDto adminPageQueryDto) {
        try {
            IPage<Admin> page = new Page<>(
                    adminPageQueryDto.pageNum(), adminPageQueryDto.pageSize());

            //query和departmentId共同查询
            if(adminPageQueryDto.departmentId() != null &&
                    adminPageQueryDto.query() != null) {
                adminMapper.selectPage(page, new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getDepartmentId, adminPageQueryDto.departmentId())
                        .like(Admin::getName, adminPageQueryDto.query()));
            } else if(adminPageQueryDto.departmentId() != null) {
                adminMapper.selectPage(page, new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getDepartmentId, adminPageQueryDto.departmentId()));
            } else if(adminPageQueryDto.query() != null) {
                adminMapper.selectPage(page, new LambdaQueryWrapper<Admin>()
                        .like(Admin::getName, adminPageQueryDto.query()));
            } else {
                adminMapper.selectPage(page, null);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", page.getRecords());
            resultMap.put("total", page.getTotal());

            return R.ok().put("result", resultMap);
        } catch (Exception e) {
            throw new ApartmentException("查询管理员列表失败！");
        }

    }

    /**
     * 查询班组长列表
     * @return 班组长列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getForemanList() {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            List<Admin> adminList = adminMapper.selectList(
                    new LambdaQueryWrapper<Admin>().
                            select(Admin::getId, Admin::getName).
                            eq(Admin::getDepartmentId, 0L));
            for (Admin admin : adminList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", admin.getId());
                map.put("name", admin.getName());
                list.add(map);
            }
            resultMap.put("list", list);
            return R.ok().put("result", resultMap);
        } catch (Exception e) {
            throw new ApartmentException("查询班组长列表失败！");
        }
    }
}
