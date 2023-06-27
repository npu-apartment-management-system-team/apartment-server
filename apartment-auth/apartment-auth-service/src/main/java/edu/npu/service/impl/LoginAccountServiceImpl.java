package edu.npu.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.RedisConstants;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.AlipayLoginCallbackDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.Admin;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.AdminMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.FaceService;
import edu.npu.service.LoginAccountService;
import edu.npu.util.JwtTokenProvider;
import edu.npu.util.RsaUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.*;
import static edu.npu.util.RegexPatterns.EMAIL_REGEX;

/**
 * @author wangminan
 * @description 针对表【login_account(登录账号)】的数据库操作Service实现
 * @createDate 2023-06-26 21:19:29
 */
@Service
@Slf4j
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
        implements LoginAccountService, UserDetailsService {

    public static final String REGISTER_FAILED_MSG = "注册失败,请检查用户名是否重复";

    public static final String TOKEN = "token";

    @Resource
    private AuthenticationManager authenticationManager;

    @Value("${var.rsa.private-key}")
    private String privateKey;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private FaceService faceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R registerUser(UserRegisterDto userRegisterDto) {
        if (StringUtils.hasText(userRegisterDto.email()) &&
                !userRegisterDto.email().matches(EMAIL_REGEX)){
            // 正则表达式匹配
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "邮箱格式不正确");
        }
        if (userRegisterDto.sex() == null ||
                userRegisterDto.isCadre() == null ||
                userRegisterDto.departmentId() == null) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "参数不完整");
        }
        // 开始处理注册信息
        // 调用阿里云人脸人体存储人脸到数据库 以手机号作为entityId
        boolean addFaceEntity = faceService.addFaceEntity(userRegisterDto.username());
        if (!addFaceEntity){
            return R.error(ResponseCodeEnum.SERVER_ERROR, "人脸实体注册失败");
        }
        String faceId = faceService.addFace(userRegisterDto.username(), userRegisterDto.faceUrl());
        if (!StringUtils.hasText(faceId)){
            return R.error(ResponseCodeEnum.SERVER_ERROR, "人脸上传失败");
        }
        // 添加到loginAccount表
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setUsername(userRegisterDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, userRegisterDto.password())));
        loginAccount.setRole(RoleEnum.USER.getValue());
        boolean saveLoginAccount = this.save(loginAccount);
        if (!saveLoginAccount){
            return R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
        }
        // 将用户信息添加到用户表
        User user = new User();
        user.setFaceId(faceId);
        BeanUtils.copyProperties(userRegisterDto, user);
        int insert = userMapper.insert(user);
        return insert == 1 ? R.ok("注册成功") :
                R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
    }

    @Override
    public R login(UserLoginDto userLoginDto) {
        try{
            LoginAccount loginAccount =
                    this.getOne(
                            new LambdaQueryWrapper<LoginAccount>()
                                    .eq(LoginAccount::getUsername, userLoginDto.username())
                    );
            if (loginAccount == null) {
                return R.error(ResponseCodeEnum.FORBIDDEN, "登录失败,请检查用户名");
            }
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginAccount.getUsername(),
                                    RsaUtil.decrypt(privateKey, userLoginDto.password()),
                                    loginAccount.getAuthorities()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Map<String, Object> result = genTokenWithLoginAccount(loginAccount);
            return R.ok(result);
        } catch (Exception e) {
            log.error("登录失败,请检查密码", e);
            return R.error(ResponseCodeEnum.SERVER_ERROR, "登录失败");
        }
    }

    @Override
    public String handleAlipayLogin(AlipayLoginCallbackDto alipayLoginCallbackDto) {
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(alipayLoginCallbackDto.auth_code());
        AlipaySystemOauthTokenResponse tokenResponse;
        log.info(alipayClient.toString());
        try {
            tokenResponse = alipayClient.execute(tokenRequest);
        } catch (AlipayApiException e) {
            throw new ApartmentException(e.getMessage());
        }
        if (tokenResponse.isSuccess()) {
            String accessToken = tokenResponse.getAccessToken();
            AlipayUserInfoShareRequest alipayIdRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse alipayIdResponse;
            try {
                alipayIdResponse = alipayClient.execute(alipayIdRequest, accessToken);
            } catch (AlipayApiException e) {
                throw new ApartmentException(e.getMessage());
            }
            if (alipayIdResponse.isSuccess()) {
                // 支付宝的工作完成了 现在轮到我们的工作
                // 查数据库 看是否有alipayId和返回值匹配的用户
                // 照理说alipay的ID是要unique的 这个沙箱应用只给俩号 所以对不起 做不到
                User user = userMapper.selectOne(
                        new QueryWrapper<User>().lambda()
                                .eq(User::getAlipayId, alipayIdResponse.getUserId()));
                if (user == null) {
                    log.error("支付宝ID: {} 未绑定用户", alipayIdResponse.getUserId());
                    return "redirect:https://apartment-client.wangminan.me/#/oauth/alipay/failure";
                }
                // 查询LoginAccount
                LoginAccount loginAccount = this.getById(user.getLoginAccountId());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loginAccount,
                                null,
                                loginAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                Map<String, Object> result = genTokenWithLoginAccount(loginAccount);
                log.info("支付宝登录成功, 用户: {}, token: {}",
                        loginAccount.getUsername(), result.get(TOKEN));
                // 拼接URL
                return "redirect:https://apartment-client.wangminan.me/#/oauth/alipay/success" +
                        "?token=" +
                        result.get(TOKEN) +
                        "&id=" +
                        result.get("id");
            } else {
                log.error("调用获取支付宝ID接口失败, resp: {}", alipayIdResponse);
            }
        } else {
            log.error("调用获取支付宝AK接口失败, resp: {}", tokenResponse);
        }
        return "redirect:https://apartment-client.wangminan.me/#/oauth/alipay/failure";
    }

    @Override
    public R logout(LoginAccount loginAccount) {
        // 将token从redis中删除 然后返回即可
        stringRedisTemplate.delete(
                LOGIN_ACCOUNT_KEY_PREFIX + loginAccount.getUsername());
        return R.ok();
    }

    @Override
    public R loginByPhone(CheckSmsCodeDto checkSmsCodeDto) {
        String phone = checkSmsCodeDto.phone();
        String code = checkSmsCodeDto.code();
        String cachedCode =
                stringRedisTemplate.opsForValue()
                        .get(RedisConstants.SMS_CODE_PREFIX + phone);
        if (cachedCode == null){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "验证码已过期");
        } else {
            if (cachedCode.equals(code)){
                LoginAccount loginAccount = this.getOne(
                        new LambdaQueryWrapper<LoginAccount>()
                                .eq(LoginAccount::getUsername, phone));
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loginAccount,
                                null,
                                loginAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                Map<String, Object> result =
                        genTokenWithLoginAccount(loginAccount);
                return R.ok(result);
            } else {
                return R.error(ResponseCodeEnum.FORBIDDEN, "验证码错误");
            }
        }
    }

    private Map<String, Object> genTokenWithLoginAccount(LoginAccount loginAccount) {
        String username = loginAccount.getUsername();
        String token = jwtTokenProvider.generateToken(loginAccount);
        // token放入redis 用一个hash结构存储token和loginAccount的json字符串形式
        try {
            stringRedisTemplate.opsForHash().put(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    HASH_TOKEN_KEY, token);
            stringRedisTemplate.opsForHash().put(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    HASH_LOGIN_ACCOUNT_KEY, objectMapper.writeValueAsString(loginAccount));
            // 设置过期时间LOGIN_ACCOUNT_EXPIRE_TTL
            stringRedisTemplate.expire(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    LOGIN_ACCOUNT_EXPIRE_TTL,
                    TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw new ApartmentException("loginAccount序列化失败");
        }
        // 组织返回结果
        Map<String, Object> result = new HashMap<>();
        if (loginAccount.getRole() == RoleEnum.USER.getValue()) {
            // 需要去查user表 拿到id
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().lambda()
                            .eq(User::getLoginAccountId, loginAccount.getId()));
            result.put("user", user);
        } else {
            // 查admin表
            Admin admin = adminMapper.selectOne(
                    new QueryWrapper<Admin>().lambda()
                            .eq(Admin::getLoginAccountId, loginAccount.getId()));
            result.put("admin", admin);
        }
        result.put("role", loginAccount.getRole());
        result.put(TOKEN, token);
        return result;
    }

    private boolean tryLock() {
        Boolean flag =
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(LOGIN_LOCK_KEY, "1", 1, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        LoginAccount loginAccount = this.getOne(
                new LambdaQueryWrapper<LoginAccount>()
                        .eq(LoginAccount::getUsername, username));
        if (loginAccount == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return loginAccount;
    }
}




