package edu.npu.controller;

import edu.npu.mapper.LoginAccountMapper;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : [wangminan]
 * @description : [类似于docker中hello world, 提供集群基础状态校验]
 */
@RestController
@Slf4j
public class HelloController {

    @Value("${server.port}")
    private long port;

    @Value("${server.servlet.context-path}")
    private String contentPath;

    @Resource
    private LoginAccountMapper loginAccountMapper;

    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

    @GetMapping("/hello")
    public R hello() {
        cachedThreadPool.execute(() -> {
            int result = loginAccountMapper.initDb();
            log.info("初始化数据库连接, result:{}", result);
        });
        log.info("port:{}, contentPath:{}", port, contentPath);
        return R.ok("hello");
    }
}
