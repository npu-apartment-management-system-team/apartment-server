package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.*;
import edu.npu.feignClient.ApplicationServiceClient;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.mapper.PaymentUserMapper;
import edu.npu.service.PaymentCenterService;
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
 * @Author : Yu
 * @Date : 2023/7/3
 * @description : 房间公寓段财务人员service实现类
 */
@Slf4j
@Service
public class PaymentCenterServiceImpl implements PaymentCenterService {

    @Resource
    private PaymentDepartmentMapper paymentDepartmentMapper;

    @Resource
    private PaymentUserMapper paymentUserMapper;

    @Resource
    private OssUtil ossUtil;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private ApplicationServiceClient applicationServiceClient;

    @Resource
    private UserServiceClient userServiceClient;


    private static final String FAILED_GENERATE_VARIATION_LIST_MSG = "生成历史变动表失败";

    private static final String FAILED_GENERATE_WITHHOLD_LIST_MSG = "生成部门代扣表失败";

    private static final String FAILED_GENERATE_CHARGE_LIST_MSG = "生成职工自收表失败";

    private static final String BASE_DIR = "apartment/center/";

    private static final String TOTAL = "total";

    private static final String LIST = "list";

    private static final  String RESULT = "result";

    /**
     * 查看历史变动表
     *
     * @param queryDto 分页查询Dto
     * @return R 历史变动表
     */
    @Override
    public R getVariationList(QueryDto queryDto) {

        Page<Application> page = applicationServiceClient
                .getApplicationPageForQuery(
                        queryDto.pageNum(), queryDto.pageSize(),
                        queryDto.beginTime(), queryDto.departmentId()
                );

        Map<String, Object> result = Map.of(
                TOTAL, page.getTotal(),
                LIST, page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 下载历史变动表
     *
     * @param downloadQueryDto 下载查询Dto
     * @return R 下载地址url
     */
    @Override
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {

        // 根据条件查询历史变动表
        List<Application> variationList = applicationServiceClient
                .getApplicationListForDownload(
                        downloadQueryDto.beginTime(),
                        downloadQueryDto.departmentId());

        // 生成EXCEL 存储到OSS
        String url = ossUtil.downloadVariationList(variationList, downloadQueryDto.beginTime(), downloadQueryDto.departmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);

    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param queryDto 外部单位代扣缴费列表查询Dto
     * @return R 单位代扣缴费情况表
     */
    @Override
    public R getWithholdList(QueryDto queryDto) {

        IPage<PaymentDepartment> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        /*
        构建wrapper
         */
        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, queryDto.beginTime());
        }

        if (queryDto.departmentId() != null) {
            wrapper.eq(PaymentDepartment::getDepartmentId, queryDto.departmentId());
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
     * @param downloadQueryDto 下载Dto
     * @return R 代扣表url
     */
    @Override
    public R downloadWithholdList(DownloadQueryDto downloadQueryDto) {

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        /*
        构建wrapper
         */
        if(downloadQueryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, downloadQueryDto.beginTime());
        }
        if(downloadQueryDto.departmentId() != null) {
            wrapper.eq(PaymentDepartment::getDepartmentId, downloadQueryDto.departmentId());
        }

        wrapper.orderByDesc(PaymentDepartment::getCreateTime);

        List<PaymentDepartment> withholdList = paymentDepartmentMapper.selectList(wrapper);

        //调用ossUtil中的方法下载
        String url = ossUtil.downloadWithholdList(withholdList, downloadQueryDto.beginTime(), downloadQueryDto.departmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_WITHHOLD_LIST_MSG);

    }

    /**
     * 查看自收缴费情况
     *
     * @param queryDto 缴费列表查询Dto
     * @return R 自收缴费列表
     */
    @Override
    public R getChargeList(QueryDto queryDto) {
        IPage<PaymentUser> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

        /*
        构建wrapper
         */
        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, queryDto.beginTime());
        }

        if (queryDto.departmentId() != null) {
            Long departmentId = queryDto.departmentId();
            wrapper.inSql(PaymentUser::getUserId, "select id from user where department_id = '"+ departmentId +"'");
        }

        wrapper.orderByDesc(PaymentUser::getCreateTime);

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
     * @param downloadQueryDto 下载Dto
     * @return R 自收表url
     */
    @Override
    public R downloadChargeList(DownloadQueryDto downloadQueryDto) {
        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

        //根据参数构建wrapper
        if(downloadQueryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, downloadQueryDto.beginTime());
        }
        if(downloadQueryDto.departmentId() != null) {
            Long departmentId = downloadQueryDto.departmentId();
            wrapper.inSql(PaymentUser::getUserId, "select id from user where department_id = '"+ departmentId +"'");
        }

        wrapper.orderByDesc(PaymentUser::getCreateTime);

        List<PaymentUser> chargeList = paymentUserMapper.selectList(wrapper);

        //调用ossUtil中的方法下载
        String url = ossUtil.downloadChargeList(chargeList, downloadQueryDto.beginTime(), downloadQueryDto.departmentId(), BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put(RESULT, url) : R.error(FAILED_GENERATE_CHARGE_LIST_MSG);
    }


}
