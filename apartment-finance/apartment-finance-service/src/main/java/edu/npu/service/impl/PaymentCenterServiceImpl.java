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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @Author: Yu
 * @Date: 2023/7/3
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    private static final String FAILED_GENERATE_VARIATION_LIST_MSG = "生成历史变动表失败";

    private static final String BASE_DIR = "apartment/center/";

    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

    /**
     * 查看历史变动表
     *
     * @param queryDto 分页查询Dto
     * @return R
     */
    @Override
    public R getVariationList(QueryDto queryDto) {

        Page<Application> page = applicationServiceClient
                .getApplicationPageForQuery(
                        queryDto
                );

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 下载历史变动表
     *
     * @param downloadQueryDto 下载查询Dto
     * @return R
     */
    @Override
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {

        // 根据条件查询历史变动表 生成EXCEL 存储到OSS
        List<Application> variationList = applicationServiceClient
                .getApplicationListForDownload(
                        downloadQueryDto.beginTime(),
                        downloadQueryDto.departmentId());
//        variationList = applicationMapper.selectList(
//                getApplicationLambdaQueryWrapper(downloadQueryDto.beginTime(), downloadQueryDto.departmentId()));

//        try (
//                // 创建workbook SXSSFWorkbook默认100行缓存
//                Workbook workbook = new SXSSFWorkbook()
//        ) {
//            // 创建sheet
//            Sheet sheet = workbook.createSheet("职工住宿历史变动表");
//            // 创建表头
//            sheet.createRow(0).createCell(0).setCellValue("变动申请ID");
//            sheet.getRow(0).createCell(1).setCellValue("职工ID");
//            sheet.getRow(0).createCell(2).setCellValue("变动类型");
//            sheet.getRow(0).createCell(3).setCellValue("变动申请url");
//            sheet.getRow(0).createCell(4).setCellValue("变动申请时间");
//            sheet.getRow(0).createCell(5).setCellValue("变动完成时间");
//            // 开始插入数据
//            for (Application tmpVariation : variationList) {
//                // 获取当前行
//                int lastRowNum = sheet.getLastRowNum();
//                // 创建行
//                sheet.createRow(lastRowNum + 1);
//                // 创建列
//                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(tmpVariation.getId());
//                sheet.getRow(lastRowNum + 1).createCell(1).setCellValue(tmpVariation.getUserId());
//                sheet.getRow(lastRowNum + 1).createCell(2).setCellValue(tmpVariation.getType());
//                sheet.getRow(lastRowNum + 1).createCell(3).setCellValue(tmpVariation.getFileUrl());
//                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(tmpVariation.getCreateTime());
//                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(tmpVariation.getUpdateTime());
//
//            }
//            File file = File.createTempFile(
//                    "职工住宿历史变动表_" + downloadQueryDto.beginTime(),
//                    ".xlsx"
//            );
//            String url = uploadFileToOss(workbook, file);

        String url = ossUtil.downloadVariationList(variationList, downloadQueryDto, BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);

    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param queryDto 外部单位代扣缴费列表查询Dto
     * @return R
     */
    @Override
    public R getWithholdList(QueryDto queryDto) {

        IPage<PaymentDepartment> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        if (queryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, queryDto.beginTime());
        }

        if (queryDto.departmentId() != null) {
            wrapper.eq(PaymentDepartment::getDepartmentId, queryDto.departmentId());
        }

        wrapper.orderByDesc(PaymentDepartment::getDepartmentId);
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
     * @param id 外部单位id
     * @return R
     */
    @Override
    public R getWithholdDetailById(Long id) {

        Map<String, Object> resultMap = new HashMap<>();
        //Map<String, Object> payment = new HashMap<>();

        /*
        获取外部单位
         */
        PaymentDepartment paymentDepartment = paymentDepartmentMapper.selectById(id);
        Department department = managementServiceClient.getDepartmentById(
                paymentDepartment.getDepartmentId());

        if (department == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "外部单位不存在");
        }

        //封装payment信息
//        payment.put("createTime", paymentDepartment.getCreateTime());
//        payment.put("price", paymentDepartment.getPrice());
//        payment.put("hasPaid", paymentDepartment.getHasPaid());
//        payment.put("payTime", paymentDepartment.getPayTime());

        resultMap.put("department", department);
        resultMap.put("payment", paymentDepartment);
        return R.ok().put("result", resultMap);

    }

    /**
     * 下载外部单位代扣表
     *
     * @param downloadQueryDto 下载Dto
     * @return R
     */
    @Override
    public R downloadWithholdList(DownloadQueryDto downloadQueryDto) {

        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();

        if(downloadQueryDto.beginTime() != null) {
            wrapper.ge(PaymentDepartment::getCreateTime, downloadQueryDto.beginTime());
        }
        if(downloadQueryDto.departmentId() != null) {
            wrapper.eq(PaymentDepartment::getDepartmentId, downloadQueryDto.departmentId());
        }
        wrapper.orderByDesc(PaymentDepartment::getDepartmentId);
        wrapper.orderByDesc(PaymentDepartment::getCreateTime);

        List<PaymentDepartment> withholdList = paymentDepartmentMapper.selectList(wrapper);

        //调用ossUtil中的方法下载
        String url = ossUtil.downloadWithholdList(withholdList, downloadQueryDto, BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);

    }

    /**
     * 查看自收缴费情况
     *
     * @param queryDto 缴费列表查询Dto
     * @return R
     */
    @Override
    public R getChargeList(QueryDto queryDto) {
        IPage<PaymentUser> page = new Page<>(
                queryDto.pageNum(), queryDto.pageSize());

        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();

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
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id 缴费记录ID
     * @return R
     */
    @Override
    public R getChargeDetailById(Long id) {
        Map<String, Object> resultMap = new HashMap<>();
        //Map<String, Object> payment = new HashMap<>();

        /*
        获取自收缴费用户
         */
        PaymentUser paymentUser = paymentUserMapper.selectById(id);
        User user = userServiceClient.getUserById(
                paymentUser.getUserId());

        if (user == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "缴费用户不存在");
        }

        //封装payment信息
//        payment.put("createTime", paymentUser.getCreateTime());
//        payment.put("price", paymentUser.getPrice());
//        payment.put("status", paymentUser.getStatus());
//        payment.put("type", paymentUser.getType());

        resultMap.put("user", user);
        resultMap.put("payment", paymentUser);
        return R.ok().put("result", resultMap);
    }

    /**
     * 下载自收表
     *
     * @param downloadQueryDto 下载Dto
     * @return R
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
        String url = ossUtil.downloadChargeList(chargeList, downloadQueryDto, BASE_DIR);

        return StringUtils.hasText(url) ?
                R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
    }


    /*
     * 设置wrapper
     *
     * @param beginTime    开始时间
     * @param departmentId 部门ID
     * @return R
     */
    /*private LambdaQueryWrapper<PaymentDepartment> getPaymentLambdaQueryWrapper(Date beginTime, Long departmentId) {
        LambdaQueryWrapper<PaymentDepartment> wrapper = new LambdaQueryWrapper<>();
        boolean hasQuery = false;

        if (beginTime != null) {
            hasQuery = true;
            wrapper.ge(PaymentDepartment::getCreateTime, beginTime);
        }

        if (departmentId != null) {
            hasQuery = true;
            wrapper.inSql(Application::getUserId, "select id from user where department_id = ${queryDto.departmentId()}");
        }

        if (!hasQuery) {
            return null;
        }

        wrapper.orderByDesc(Application::getCreateTime);
        return wrapper;
    }*/

    /**
     * 向OSS上传文件
     *
     * @param workbook excel文件
     * @param file     本地生成的文件
     * @return 上传后回传地址
     */
//    private String uploadFileToOss(Workbook workbook, File file) {
//        try (
//                FileOutputStream fileOutputStream = new FileOutputStream(file)
//        ) {
//            // 保存到OSS 参数为一个File
//            workbook.write(fileOutputStream);
//            // 上传到OSS
//            String url = ossUtil.uploadFile(PaymentCenterServiceImpl.BASE_DIR, file);
//            if (StringUtils.hasText(url)) {
//                // 上传成功
//                log.info("上传到OSS成功,file:{}", file.getName());
//                cachedThreadPool.execute(() -> {
//                    String currentDate =
//                            new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(new Date());
//                    stringRedisTemplate.opsForList()
//                            .leftPush(
//                                    UPLOAD_FILE_KEY_PREFIX + currentDate,
//                                    file.getName()
//                            );
//                    // 设置整个LIST的过期时间
//                    stringRedisTemplate.expire(
//                            UPLOAD_FILE_KEY_PREFIX + currentDate,
//                            2,
//                            TimeUnit.DAYS
//                    );
//                });
//                // 删除临时文件
//                file.deleteOnExit();
//                return url;
//
//            } else {
//                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "上传到OSS失败");
//            }
//        } catch (IOException e) {
//            log.error("上传到OSS失败,file:{}", file.getName());
//            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "上传到OSS失败");
//        }
//    }
}
