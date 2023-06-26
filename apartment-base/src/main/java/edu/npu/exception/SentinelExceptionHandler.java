package edu.npu.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [Sentinel 异常处理]
 */
@Component
public class SentinelExceptionHandler implements BlockExceptionHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        String msg = "未知异常";
        ResponseCodeEnum respondCodeEnum = ResponseCodeEnum.TOO_MANY_REQUESTS;

        if (e instanceof FlowException) {
            msg = "请求被限流了";
        } else if (e instanceof ParamFlowException) {
            msg = "请求被热点参数限流";
        } else if (e instanceof DegradeException) {
            msg = "请求被降级了";
        } else if (e instanceof AuthorityException) {
            msg = "没有权限访问";
            respondCodeEnum = ResponseCodeEnum.ACCESS_BLOCKED_BY_SENTINEL;
        }

        response.setContentType("application/json;charset=utf-8");
        response.setStatus(respondCodeEnum.getStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(
                R.error(respondCodeEnum, msg)
        ));
    }
}
