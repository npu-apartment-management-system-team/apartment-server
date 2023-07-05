package edu.npu.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import edu.npu.entity.Application;
import edu.npu.entity.PaymentDepartment;
import edu.npu.entity.PaymentUser;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.UPLOAD_FILE_KEY_PREFIX;

/**
 * @author : [wangminan]
 * @description : [向阿里云OSS存储与拉取]
 */
@Component
@Slf4j
public class OssUtil {

    @Resource
    private OSS oss;

    @Value("${var.aliyun-oss.endpoint}")
    private String endPoint;

    @Value("${var.aliyun-oss.bucketName}")
    private String bucketName;

    @Value("${var.aliyun-oss.baseUrl}")
    private String baseUrl;

    private static final String FAILED_GENERATE_VARIATION_LIST_MSG = "生成历史变动表失败";

    private static final String FAILED_GENERATE_WITHHOLD_LIST_MSG = "生成部门代扣表失败";

    private static final String FAILED_GENERATE_CHARGE_LIST_MSG = "生成职工自收表失败";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";


    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

    // 阿里云的声明周期操作不支持根据访问时间删除文件 所以只能手动删除

    /**
     * 向Oss上传文件
     * @param baseDir 文件夹名 以apartment/开头 外部单位相关apartment/department/
     *                房建段相关apartment/center/
     * @param file 文件
     * @return 是否上传成功
     */
    public String uploadFile(String baseDir, File file){
        try{
            oss.putObject(bucketName,baseDir+file.getName(),file);
            // 设定文件访问权限为公共读
            oss.setObjectAcl(bucketName,
                    baseDir+file.getName(),
                    CannedAccessControlList.PublicRead);
            return baseUrl + "/"  + baseDir + file.getName();
        } catch (OSSException e){
            log.error("文件:{}上传失败",file.getName());
        }
        return null;
    }

    /**
     * 从Oss删除文件 供SchedulerX2定时调用
     * @param baseDir 文件夹名 以apartment/开头 外部单位相关department/sheet/
     *                房建段相关apartment/center/
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteFile(String baseDir, String fileName){
        log.info("收到调度,正在删除文件:{}", fileName);
        try{
            oss.deleteObject(bucketName,baseDir+fileName);
            return true;
        } catch (OSSException e){
            log.error("文件:{}删除失败",fileName);
        }
        return false;
    }


    /**
     * 下载职工住宿历史变动表
     * @param variationList
     * @param beginTime
     * @param departmentId
     * @param baseDir
     * @return
     */
    public String downloadVariationList(List<Application> variationList, Date beginTime, Long departmentId, String baseDir) {
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
                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(tmpVariation.getCreateTime()));
                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(tmpVariation.getUpdateTime()));

            }
            String begintime = "all";
            if (beginTime != null) {
                begintime = new SimpleDateFormat("yyyy-MM").format(beginTime);
            }

            File file = File.createTempFile(
                    "职工住宿历史变动表_" + begintime + "_" + departmentId,
                    ".xlsx"
            );
            String url = uploadFileToOss(workbook, file, baseDir);

            return url;
        } catch (IOException e) {
            throw new ApartmentException(FAILED_GENERATE_VARIATION_LIST_MSG);
        }
    }

    /**
     * 下载单位代扣表
     * @param withholdList
     * @param beginTime
     * @param departmentId
     * @param baseDir
     * @return
     */
    public String downloadWithholdList(List<PaymentDepartment> withholdList, Date beginTime, Long departmentId, String baseDir) {
        try (
                // 创建workbook SXSSFWorkbook默认100行缓存
                Workbook workbook = new SXSSFWorkbook()
        ) {
            // 创建sheet
            Sheet sheet = workbook.createSheet("单位代扣表");
            // 创建表头
            sheet.createRow(0).createCell(0).setCellValue("代扣缴费ID");
            sheet.getRow(0).createCell(1).setCellValue("单位ID");
            sheet.getRow(0).createCell(2).setCellValue("需缴纳金额");
            sheet.getRow(0).createCell(3).setCellValue("创建时间");
            sheet.getRow(0).createCell(4).setCellValue("支付进展");
            sheet.getRow(0).createCell(5).setCellValue("支付时间");
            sheet.getRow(0).createCell(6).setCellValue("铁路内部支票ID");
            // 开始插入数据
            for (PaymentDepartment tmpPaymentDepartment : withholdList) {
                // 获取当前行
                int lastRowNum = sheet.getLastRowNum();
                // 创建行
                sheet.createRow(lastRowNum + 1);
                // 创建列
                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(tmpPaymentDepartment.getId());
                sheet.getRow(lastRowNum + 1).createCell(1).setCellValue(tmpPaymentDepartment.getDepartmentId());
                sheet.getRow(lastRowNum + 1).createCell(2).setCellValue(tmpPaymentDepartment.getPrice());
                sheet.getRow(lastRowNum + 1).createCell(3).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(tmpPaymentDepartment.getCreateTime()));
                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(tmpPaymentDepartment.getHasPaid());
                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(tmpPaymentDepartment.getPayTime()));
                sheet.getRow(lastRowNum + 1).createCell(6).setCellValue(tmpPaymentDepartment.getChequeId());

            }

            String begintime = "all";
            if (beginTime != null) {
                begintime = new SimpleDateFormat("yyyy-MM").format(beginTime);
            }

            File file = File.createTempFile(
                    "单位代扣表_" + begintime + "_" + departmentId,
                    ".xlsx"
            );
            String url = uploadFileToOss(workbook, file, baseDir);

            return url;
        } catch (IOException e) {
            throw new ApartmentException(FAILED_GENERATE_WITHHOLD_LIST_MSG);
        }
    }

    /**
     * 下载自收表
     * @param chargeList
     * @param beginTime
     * @param departmentId
     * @param baseDir
     * @return
     */
    public String downloadChargeList(List<PaymentUser> chargeList, Date beginTime, Long departmentId, String baseDir) {
        try (
                // 创建workbook SXSSFWorkbook默认100行缓存
                Workbook workbook = new SXSSFWorkbook()
        ) {
            // 创建sheet
            Sheet sheet = workbook.createSheet("职工缴费记录表");
            // 创建表头
            sheet.createRow(0).createCell(0).setCellValue("职工缴费ID");
            sheet.getRow(0).createCell(1).setCellValue("职工ID");
            sheet.getRow(0).createCell(2).setCellValue("需缴纳金额");
            sheet.getRow(0).createCell(3).setCellValue("创建时间");
            sheet.getRow(0).createCell(4).setCellValue("支付进展");
            sheet.getRow(0).createCell(5).setCellValue("缴费类别");
            // 开始插入数据
            for (PaymentUser tmpPaymentUser : chargeList) {
                // 获取当前行
                int lastRowNum = sheet.getLastRowNum();
                // 创建行
                sheet.createRow(lastRowNum + 1);
                // 创建列
                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(tmpPaymentUser.getId());
                sheet.getRow(lastRowNum + 1).createCell(1).setCellValue(tmpPaymentUser.getUserId());
                sheet.getRow(lastRowNum + 1).createCell(2).setCellValue(tmpPaymentUser.getPrice());
                sheet.getRow(lastRowNum + 1).createCell(3).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(tmpPaymentUser.getCreateTime()));
                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(tmpPaymentUser.getStatus());
                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(tmpPaymentUser.getType());

            }

            String begintime = "all";
            if (beginTime != null) {
                begintime = new SimpleDateFormat("yyyy-MM").format(beginTime);
            }

            File file = File.createTempFile(
                    "职工缴费表_" + begintime + "_" + departmentId,
                    ".xlsx"
            );
//            System.out.printf("部门ID：" + departmentId);
//            System.out.printf("开始时间：" + beginTime);
            String url = uploadFileToOss(workbook, file, baseDir);

            return url;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApartmentException(FAILED_GENERATE_CHARGE_LIST_MSG);
        }
    }

    /**
     * 上传到Oss
     * @param workbook
     * @param file
     * @param baseDir
     * @return
     */
    private String uploadFileToOss(Workbook workbook, File file, String baseDir) {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file)
        ) {
            // 保存到OSS 参数为一个File
            workbook.write(fileOutputStream);
            // 上传到OSS
            String url = this.uploadFile(baseDir, file);
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
