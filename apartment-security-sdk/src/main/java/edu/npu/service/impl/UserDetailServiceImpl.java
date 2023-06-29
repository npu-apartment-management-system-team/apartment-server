package edu.npu.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.LoginAccount;
import edu.npu.exception.ApartmentException;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import static edu.npu.common.RedisConstants.HASH_LOGIN_ACCOUNT_KEY;
import static edu.npu.common.RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
public class UserDetailServiceImpl {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Bean
    public UserDetailsService userDetailsService(){
        // 从Redis中获取用户信息
        return username -> {
            try {
                return new AccountUserDetails(
                    objectMapper.readValue(
                    stringRedisTemplate
                        .opsForHash()
                        .entries(LOGIN_ACCOUNT_KEY_PREFIX + username)
                        .get(HASH_LOGIN_ACCOUNT_KEY).toString(), LoginAccount.class)
                );
            } catch (JsonProcessingException e) {
                throw new ApartmentException("用户不存在");
            }
        };
    }
}
