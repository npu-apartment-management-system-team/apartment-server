package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.Application;
import edu.npu.entity.Department;
import edu.npu.entity.PaymentDepartment;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.mapper.DepartmentMapper;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.service.PaymentCenterService;
import edu.npu.util.OssUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

import static edu.npu.common.RedisConstants.UPLOAD_FILE_KEY_PREFIX;
/**
 * @Author: Yu
 * @Date: 2023/7/3
 */
@Slf4j
@Service
public class PaymentCenterServiceImpl implements PaymentCenterService {

    @Resource
    private ApplicationMapper applicationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private PaymentDepartmentMapper paymentDepartmentMapper;

    @Resource
    private OssUtil ossUtil;

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
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getVariationList(UserPayListQueryDto userPayListQueryDto) {

        IPage<Application> page = new Page<>(
                userPayListQueryDto.pageNum(), userPayListQueryDto.pageSize());

//        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
//        boolean hasQuery = false;
//
//        if(userPayListQueryDto.beginTime() != null) {
//            hasQuery = true;
//            wrapper.ge(Application::getCreateTime, userPayListQueryDto.beginTime());
//        }
//
//        if(userPayListQueryDto.departmentId() != null) {
//            hasQuery = true;
//            wrapper.inSql(Application::getUser_id, "select id from user where department_id = ${queryDto.departmentId()}");
//        }
//
//        wrapper.orderByDesc(Application::getCreateTime);
//
//        if (hasQuery) {
//            page = paymentApplicationMapper.selectPage(page, wrapper);
//
//        } else {
//            page = paymentApplicationMapper.selectPage(page, null);
//        }

        page = applicationMapper.selectPage(page, getApplicationLambdaQueryWrapper(
                userPayListQueryDto.beginTime(), userPayListQueryDto.departmentId()));

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    /**
     * 下载历史变动表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {

        // 根据条件查询历史变动表 生成EXCEL 存储到OSS
        List<Application> variationList;
        variationList = applicationMapper.selectList(
                getApplicationLambdaQueryWrapper(downloadQueryDto.beginTime(), downloadQueryDto.departmentId()));

        try (
                // 创建workbook SXSSFWorkbook默认100行缓存
                Workbook workbook = new SXSSFWorkbook()
        ) {
            // 创建sheet
            Sheet sheet = workbook.createSheet("职工住宿历史变动表");
            // 创建表头
            sheet.createRow(0).createCell(0).setCellValue("变动申请ID");
            sheet.getRow(0).createCell(1).setCellValue("职工ID");
            sheet.getRow(0).createCell(2).setCellValue("变动类型");
            sheet.getRow(0).createCell(3).setCellValue("变动申请url");
            sheet.getRow(0).createCell(4).setCellValue("变动申请时间");
            sheet.getRow(0).createCell(5).setCellValue("变动完成时间");
            // 开始插入数据
            for (Application tmpVariation : variationList) {
                // 获取当前行
                int lastRowNum = sheet.getLastRowNum();
                // 创建行
                sheet.createRow(lastRowNum + 1);
                // 创建列
                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(tmpVariation.getId());
                sheet.getRow(lastRowNum + 1).createCell(1).setCellValue(tmpVariation.getUserId());
                sheet.getRow(lastRowNum + 1).createCell(2).setCellValue(tmpVariation.getType());
                sheet.getRow(lastRowNum + 1).createCell(3).setCellValue(tmpVariation.getFileUrl());
                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(tmpVariation.getCreateTime());
                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(tmpVariation.getUpdateTime());

            }
            File file = File.createTempFile(
                    "职工住宿历史变动表_" + downloadQueryDto.beginTime(),
                    ".xlsx"
            );
            String url = uploadFileToOss(workbook, file);

            return StringUtils.hasText(url) ?
                    R.ok().put("result", url) : R.error(FAILED_GENERATE_VARIATION_LIST_MSG);
        } catch (IOException e) {
            throw new ApartmentException(FAILED_GENERATE_VARIATION_LIST_MSG);
        }

    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getWithholdList(UserPayListQueryDto userPayListQueryDto) {
        return null;
    }

    /**
     * 查看某条代扣缴费具体情况
     *
     * @param id
     * @return
     */
    @Override
    public R getWithholdDetailById(Long id) {

        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> payment = new HashMap<>();

        /*
        获取外部单位
         */
        PaymentDepartment paymentDepartment = paymentDepartmentMapper.selectById(id);
        Department department = departmentMapper.selectById(
                paymentDepartment.getDepartmentId());

//        departmentMapper.selectOne(new LambdaQueryWrapper<Department>()
//                .eq(Department::getId, paymentDepartmentMapper.selectById(id).getDepartmentId()));

        if (department == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "外部单位不存在");
        }

        //封装payment信息
        payment.put("price", paymentDepartment.getPrice());
        payment.put("hasPaid", paymentDepartment.getHasPaid());

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
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getChargeList(UserPayListQueryDto userPayListQueryDto) {
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


    /**
     * 设置wrapper
     * @param beginTime
     * @param departmentId
     * @return
     */
    @Nullable
    private LambdaQueryWrapper<Application> getApplicationLambdaQueryWrapper(Date beginTime, Long departmentId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        boolean hasQuery = false;

        if(beginTime != null) {
            hasQuery = true;
            wrapper.ge(Application::getCreateTime, beginTime);
        }

        if(departmentId != null) {
            hasQuery = true;
            wrapper.inSql(Application::getUserId, "select id from user where department_id = ${queryDto.departmentId()}");
        }

        if(!hasQuery) {
            return null;
        }

        wrapper.orderByDesc(Application::getCreateTime);

        return wrapper;
    }

    /**
     * 向OSS上传文件
     * @param workbook
     * @param file
     * @return
     */
    private String uploadFileToOss(Workbook workbook, File file) {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file)
        ) {
            // 保存到OSS 参数为一个File
            workbook.write(fileOutputStream);
            // 上传到OSS
            String url = ossUtil.uploadFile(PaymentCenterServiceImpl.BASE_DIR, file);
            if (StringUtils.hasText(url)) {
                // 上传成功
                log.info("上传到OSS成功,file:{}", file.getName());
                cachedThreadPool.execute(() -> {
                    String currentDate =
                            new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(new Date());
                    stringRedisTemplate.opsForList()
                            .leftPush(
                                    UPLOAD_FILE_KEY_PREFIX + currentDate,
                                    file.getName()
                            );
                    // 设置整个LIST的过期时间
                    stringRedisTemplate.expire(
                            UPLOAD_FILE_KEY_PREFIX + currentDate,
                            2,
                            TimeUnit.DAYS
                    );
                });
                // 删除临时文件
                file.deleteOnExit();
                return url;

            } else {
                throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "上传到OSS失败");
            }
        } catch (IOException e) {
            log.error("上传到OSS失败,file:{}", file.getName());
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "上传到OSS失败");
        }
    }
}
