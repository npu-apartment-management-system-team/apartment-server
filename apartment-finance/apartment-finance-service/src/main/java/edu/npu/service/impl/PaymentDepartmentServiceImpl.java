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

/**
* @author wangminan
* @description 针对表【payment_department(代扣外部单位缴费表)】的数据库操作Service实现
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

    /**
     * 查看历史变动表
     * @param queryDto
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
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 下载历史变动表
     * @param downloadQueryDto
     * @return R
     */
    @Override
    public R downloadVariationList(AccountUserDetails accountUserDetails,
                                   DownloadQueryDto downloadQueryDto) {

        Admin admin = extractAdmin(accountUserDetails);
        // 根据条件查询历史变动表 生成EXCEL 存储到OSS
        List<Application> variationList = applicationServiceClient
                .getApplicationListForDownload(
                        downloadQueryDto.beginTime(),
                        admin.getDepartmentId());

        String url = ossUtil.downloadVariationList(variationList, downloadQueryDto.beginTime(), admin.getDepartmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param queryDto
     * @return R
     */
    @Override
    public R getWithholdList(AccountUserDetails accountUserDetails, QueryDto queryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        IPage<PaymentDepartment> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();
        //boolean hasQuery = false;
        wrapper.eq(PaymentDepartment::getDepartmentId, admin.getDepartmentId());
        if (queryDto.beginTime() != null) {
            //hasQuery = true;
            wrapper.ge(PaymentDepartment::getCreateTime, queryDto.beginTime());
        }

        if (queryDto.status() != null) {
            wrapper.eq(PaymentDepartment::getHasPaid, queryDto.status());
        }

        wrapper.orderByDesc(PaymentDepartment::getCreateTime);

        page = paymentDepartmentMapper.selectPage(page, wrapper);

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 查看某条代扣缴费具体情况
     *
     * @param id
     * @return R
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
        return R.ok().put("result", resultMap);
    }

    /**
     * 下载外部单位代扣表
     *
     * @param downloadQueryDto
     * @return R
     */
    @Override
    public R downloadWithholdList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {

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
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);

    }

    /**
     * 查看自收缴费情况
     *
     * @param queryDto
     * @return R
     */
    @Override
    public R getChargeList(AccountUserDetails accountUserDetails, QueryDto queryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        IPage<PaymentUser> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, queryDto.beginTime());
        }

        Long departmentId = admin.getDepartmentId();
        wrapper.inSql(PaymentUser::getUserId, "select id from user where department_id = '"+ departmentId +"'");

        wrapper.orderByDesc(PaymentUser::getCreateTime);

        page = paymentUserMapper.selectPage(page, wrapper);

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id
     * @return R
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
        return R.ok().put("result", resultMap);
    }

    /**
     * 下载自收表
     *
     * @param downloadQueryDto
     * @return R
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
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }

    /**
     * 外部单位填写必要信息以确认住宿费用代扣
     *
     * @param id
     * @param chequeId
     * @return R
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

        if (admin.getDepartmentId() != paymentDepartment.getDepartmentId()) {
            log.error("外部单位管理员只有本单位相关权限！");
            return R.error(ResponseCodeEnum.FORBIDDEN, "权限不足");
        }

        paymentDepartment.setChequeId(chequeId);

        int isUpdate = paymentDepartmentMapper.updateById(paymentDepartment);
        return isUpdate == 1 ? R.ok("外部缴费表支票ID设置成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, "支票ID设置失败");
    }


    private Admin extractAdmin(AccountUserDetails accountUserDetails) {
        Admin admin = userServiceClient.getAdminByLoginAccountId(accountUserDetails.getId());
        if (admin == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的外部单位管理员不存在");
        }
        return admin;
    }
}
