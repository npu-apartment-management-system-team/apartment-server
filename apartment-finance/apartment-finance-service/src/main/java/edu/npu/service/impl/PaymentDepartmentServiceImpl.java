package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.*;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.ApplicationServiceClient;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.service.PaymentDepartmentService;
import edu.npu.util.OssUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
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
public class PaymentDepartmentServiceImpl extends ServiceImpl<PaymentDepartmentMapper, PaymentDepartment>
    implements PaymentDepartmentService{

    @Resource
    private PaymentDepartmentMapper paymentDepartmentMapper;

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
     * @return
     */
    @Override
    public R getVariationList(AccountUserDetails accountUserDetails,
                              QueryDto queryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        Page<Application> page = applicationServiceClient
                .getApplicationPageForQuery(
                        queryDto, admin.getDepartmentId()
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
     * @return
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
//        variationList = applicationMapper.selectList(
//                getApplicationLambdaQueryWrapper(downloadQueryDto.beginTime(), downloadQueryDto.departmentId()));

        String url = ossUtil.downloadVariationList(variationList, downloadQueryDto, BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param queryDto
     * @return
     */
    @Override
    public R getWithholdList(AccountUserDetails accountUserDetails, QueryDto queryDto) {

        Admin admin = extractAdmin(accountUserDetails);

        IPage<PaymentDepartment> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();
        //boolean hasQuery = false;
        wrapper.eq(PaymentDepartment::getDepartmentId, queryDto.departmentId());
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
     * @return
     */
    @Override
    public R getWithholdDetailById(AccountUserDetails accountUserDetails, Long id) {

        Admin admin = extractAdmin(accountUserDetails);

        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> payment = new HashMap<>();

        /*
        获取外部单位
         */
        PaymentDepartment paymentDepartment = paymentDepartmentMapper.selectById(id);
        Department department = managementServiceClient.getDepartmentById(
                admin.getDepartmentId());

        //封装payment信息
        payment.put("createTime", paymentDepartment.getCreateTime());
        payment.put("price", paymentDepartment.getPrice());
        payment.put("hasPaid", paymentDepartment.getHasPaid());
        payment.put("payTime", paymentDepartment.getPayTime());

        resultMap.put("department", department);
        resultMap.put("payment", payment);
        return R.ok().put("result", resultMap);
    }

    /**
     * 下载外部单位代扣表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadWithholdList(DownloadQueryDto downloadQueryDto) {
        return null;
    }

    /**
     * 查看自收缴费情况
     *
     * @param queryDto
     * @return
     */
    @Override
    public R getChargeList(QueryDto queryDto) {
        return null;
    }

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id
     * @return
     */
    @Override
    public R getChargeDetailById(Long id) {
        return null;
    }

    /**
     * 下载自收表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadChargeList(DownloadQueryDto downloadQueryDto) {
        return null;
    }


    private Admin extractAdmin(AccountUserDetails accountUserDetails) {
        Admin admin = userServiceClient.getAdminByLoginAccountId(accountUserDetails.getId());
        if (admin == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的外部单位管理员不存在");
        }
        return admin;
    }
}




