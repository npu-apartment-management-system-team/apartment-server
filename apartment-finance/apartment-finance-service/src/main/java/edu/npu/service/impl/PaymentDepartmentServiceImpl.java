package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.*;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.ApplicationServiceClient;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.mapper.PaymentUserMapper;
import edu.npu.service.PaymentDepartmentService;
import edu.npu.util.OssUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author wangminan
* @description 外部单位财务人员Service实现
* @createDate 2023-07-02 16:45:55
*/
@Service
@Slf4j
public class PaymentDepartmentServiceImpl extends ServiceImpl<PaymentDepartmentMapper, PaymentDepartment>
    implements PaymentDepartmentService{

    @Resource
    private PaymentDepartmentMapper paymentDepartmentMapper;

    @Resource
    private PaymentUserMapper paymentUserMapper;

    @Resource
    private ApplicationServiceClient applicationServiceClient;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private OssUtil ossUtil;

    private static final String FAILED_GENERATE_VARIATION_LIST_MSG = "生成历史变动表失败";

    private static final String BASE_DIR = "apartment/department/";

    private static final String TOTAL = "total";

    private static final String LIST = "list";

    private static final  String RESULT = "result";

    /**
     * 查看历史变动表
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 分页查询Dto
     * @return R 历史变动表
     */
    @Override
    public R getVariationList(AccountUserDetails accountUserDetails,
                              QueryDto queryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        Page<Application> page = applicationServiceClient
                .getApplicationPageForQuery(
                        queryDto.pageNum(), queryDto.pageSize(),
                        queryDto.beginTime(), admin.getDepartmentId()

                );


        Map<String, Object> result = Map.of(
                TOTAL, page.getTotal(),
                LIST, page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 下载历史变动表
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载查询Dto
     * @return R 下载地址url
     */
    @Override
    public R downloadVariationList(AccountUserDetails accountUserDetails,
                                   DownloadQueryDto downloadQueryDto) {

        //获取admin
        Admin admin = extractAdmin(accountUserDetails);
        // 根据条件查询历史变动表
        List<Application> variationList = applicationServiceClient
                .getApplicationListForDownload(
                        downloadQueryDto.beginTime(),
                        admin.getDepartmentId());

        // 调用ossUtil方法生成EXCEL 存储到OSS
        String url = ossUtil.downloadVariationList(variationList, downloadQueryDto.beginTime(), admin.getDepartmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 单位代扣缴费列表查询Dto
     * @return R 单位代扣缴费情况表
     */
    @Override
    public R getWithholdList(AccountUserDetails accountUserDetails, QueryDto queryDto) {

        //获取admin
        Admin admin = extractAdmin(accountUserDetails);

        IPage<PaymentDepartment> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        //构建wrapper
        wrapper.eq(PaymentDepartment::getDepartmentId, admin.getDepartmentId());
        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, queryDto.beginTime());
        }

        if (queryDto.status() != null) {
            wrapper.eq(PaymentDepartment::getHasPaid, queryDto.status());
        }

        wrapper.orderByDesc(PaymentDepartment::getCreateTime);

        page = paymentDepartmentMapper.selectPage(page, wrapper);

        Map<String, Object> result = Map.of(
                TOTAL, page.getTotal(),
                LIST, page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 查看某条代扣缴费具体情况
     *
     * @param id 该条代扣缴费id
     * @return R 该条代扣缴费具体情况
     */
    @Override
    public R getWithholdDetailById(Long id) {

        Map<String, Object> resultMap = new HashMap<>();

        /*
        获取外部单位
         */
        PaymentDepartment paymentDepartment = paymentDepartmentMapper.selectById(id);
        Department department = managementServiceClient.getDepartmentById(
                paymentDepartment.getDepartmentId());

        if (department == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "外部单位不存在");
        }

        resultMap.put("department", department);
        resultMap.put("payment", paymentDepartment);
        return R.ok().put(RESULT, resultMap);
    }

    /**
     * 下载外部单位代扣表
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载Dto
     * @return R 代扣表url
     */
    @Override
    public R downloadWithholdList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {

        //获取admin
        Admin admin = extractAdmin(accountUserDetails);
        /*
        按条件查询list
         */
        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        if(downloadQueryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, downloadQueryDto.beginTime());
        }

        wrapper.eq(PaymentDepartment::getDepartmentId, admin.getDepartmentId());
        wrapper.orderByDesc(PaymentDepartment::getCreateTime);

        List<PaymentDepartment> withholdList = paymentDepartmentMapper.selectList(wrapper);

        //调用ossUtil中的方法下载
        String url = ossUtil.downloadWithholdList(withholdList, downloadQueryDto.beginTime(), admin.getDepartmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);

    }

    /**
     * 查看自收缴费情况
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 缴费列表查询Dto
     * @return R 自收缴费列表
     */
    @Override
    public R getChargeList(AccountUserDetails accountUserDetails, QueryDto queryDto) {

        //获取admin
        Admin admin = extractAdmin(accountUserDetails);

        IPage<PaymentUser> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

        //构建wrapper
        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, queryDto.beginTime());
        }

        Long departmentId = admin.getDepartmentId();
        wrapper.inSql(PaymentUser::getUserId, "select id from user where department_id = '"+ departmentId +"'");

        wrapper.orderByDesc(PaymentUser::getCreateTime);

        //查询
        page = paymentUserMapper.selectPage(page, wrapper);

        Map<String, Object> result = Map.of(
                TOTAL, page.getTotal(),
                LIST, page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id 缴费记录ID
     * @return R 该条自收缴费具体内容
     */
    @Override
    public R getChargeDetailById(Long id) {

        Map<String, Object> resultMap = new HashMap<>();

        /*
        获取自收缴费用户
         */
        PaymentUser paymentUser = paymentUserMapper.selectById(id);
        User user = userServiceClient.getUserById(
                paymentUser.getUserId());

        if (user == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "缴费用户不存在");
        }

        resultMap.put("user", user);
        resultMap.put("payment", paymentUser);
        return R.ok().put(RESULT, resultMap);
    }

    /**
     * 下载自收表
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载Dto
     * @return R 自收表url
     */
    @Override
    public R downloadChargeList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

        //根据参数构建wrapper
        if(downloadQueryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, downloadQueryDto.beginTime());
        }

        Long departmentId = admin.getDepartmentId();
        wrapper.inSql(PaymentUser::getUserId, "select id from user where department_id = '"+ departmentId +"'");

        wrapper.orderByDesc(PaymentUser::getCreateTime);

        List<PaymentUser> chargeList = paymentUserMapper.selectList(wrapper);

        //调用ossUtil中的方法下载
        String url = ossUtil.downloadChargeList(chargeList, downloadQueryDto.beginTime(), admin.getDepartmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }

    /**
     * 外部单位填写必要信息以确认住宿费用代扣
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param id 外部单位缴费表id
     * @param chequeId 支票id
     * @return R 设置结果
     */
    @Override
    public R postChequeId(AccountUserDetails accountUserDetails, Long id, String chequeId) {

        Admin admin = extractAdmin(accountUserDetails);

        //根据id找paymentDepartment
        PaymentDepartment paymentDepartment = paymentDepartmentMapper.selectById(id);

        if (paymentDepartment == null) {
            log.error("外部单位缴费表不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "外部单位缴费表不存在");
        }

        if (!Objects.equals(admin.getDepartmentId(), paymentDepartment.getDepartmentId())) {
            log.error("外部单位管理员只有本单位相关权限！");
            return R.error(ResponseCodeEnum.FORBIDDEN, "权限不足");
        }

        //设置chequeId
        paymentDepartment.setChequeId(chequeId);

        int isUpdate = paymentDepartmentMapper.updateById(paymentDepartment);
        return isUpdate == 1 ? R.ok("外部单位缴费表支票ID设置成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, "支票ID设置失败");
    }

    /**
     * 获取登录的admin
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @return admin 登录的admin
     */
    private Admin extractAdmin(AccountUserDetails accountUserDetails) {
        Admin admin = userServiceClient.getAdminByLoginAccountId(accountUserDetails.getId());
        if (admin == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的外部单位管理员不存在");
        }
        return admin;
    }
}
