package edu.npu.controller;

import edu.npu.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/hello")
    public R hello() {
        log.info("port:{}, contentPath:{}", port, contentPath);
        return R.ok("hello");
    }
}
