package edu.npu.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.UserPageQueryDto;
import edu.npu.dto.BindAlipayCallbackDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.UserService;
import edu.npu.util.JwtTokenProvider;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static edu.npu.common.EsConstants.USER_INDEX;

import org.springframework.util.StringUtils;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Service实现
* @createDate 2023-06-27 21:19:32
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private LoginAccountMapper loginAccountMapper;
    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private AlipayClient alipayClient;


    /**
     * 获取用户信息列表
     * @param userPageQueryDto 参数
     * @return R
     */
    @Override
    public R getUsersInfo(UserPageQueryDto userPageQueryDto) {
        SearchRequest searchRequest = buildBasicQuery(userPageQueryDto);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateUserInfo(Long id, UserUpdateDto userUpdateDto) {
        User user = this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, id));
        if (user == null) {
            log.error("所需更新的用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需更新的用户不存在");
        }
        int updateById = 0;
        LoginAccount loginAccount = loginAccountMapper.selectById(user.getLoginAccountId());
        if (!loginAccount.getUsername().equals(userUpdateDto.username())) {
            loginAccount.setUsername(userUpdateDto.username());
            updateById = loginAccountMapper.updateById(loginAccount);
        }
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean userUpdate = this.updateById(user);
        if (userUpdate && updateById == 1) {
            return R.ok("用户信息更新成功");
        } else {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "用户信息更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteUser(Long id) {
        User user=this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId,id));
        if (user==null){
            log.error("所需删除的用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需删除的用户不存在");
        }
        boolean userDelete=this.removeById(id);
        int DeleteById = loginAccountMapper.deleteById(id);
        if(userDelete&&DeleteById==1){
            return R.ok("用户信息删除成功");
        }else{
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "用户信息更新失败");
        }
    }

    @Override
    public SearchRequest buildBasicQuery(UserPageQueryDto pageQueryDto) {
        Query query = formBoolQuery(pageQueryDto);
        // 2.2.分页
        int page = pageQueryDto.pageNum();
        int size = pageQueryDto.pageSize();
        // 拼装
        return new SearchRequest.Builder()
                .index(USER_INDEX)
                .query(query)
                .from((page - 1) * size)
                .size(size)
                .build();
    }

    private static Query formBoolQuery(UserPageQueryDto pageQueryDto) {
        Query keywordQuery;
        // 1.关键字
        String keyword = pageQueryDto.query();
        if (StringUtils.hasText(keyword)) {
            keywordQuery = new MatchQuery.Builder()
                    .field("all").query(keyword)
                    .build()._toQuery();
        } else {
            keywordQuery = new MatchAllQuery.Builder()
                    .build()._toQuery();
        }
        // 2. departmentId
        Long departmentId = pageQueryDto.departmentId();
        Query departmentIdQuery = null;
        if (departmentId != null) {
            departmentIdQuery = new MatchQuery.Builder()
                    .field("departmentId").query(departmentId)
                    .build()._toQuery();
        }
        // 3. apartmentId
        Long apartmentId = pageQueryDto.apartmentId();
        Query apartmentIdQuery = null;
        if (apartmentId != null) {
            apartmentIdQuery = new MatchQuery.Builder()
                    .field("apartmentId").query(apartmentId)
                    .build()._toQuery();
        }
        // 3. 拼装
        if (departmentIdQuery != null && apartmentIdQuery != null) {
            return new BoolQuery.Builder()
                    .must(keywordQuery)
                    .must(departmentIdQuery)
                    .must(apartmentIdQuery)
                    .build()._toQuery();
        } else if (departmentId != null) {
            return new BoolQuery.Builder()
                    .must(keywordQuery)
                    .must(departmentIdQuery)
                    .build()._toQuery();
        } else if (apartmentId != null) {
            return new BoolQuery.Builder()
                    .must(keywordQuery)
                    .must(apartmentIdQuery)
                    .build()._toQuery();
        } else {
            return new BoolQuery.Builder()
                    .must(keywordQuery)
                    .build()._toQuery();
        }
    }

    @Override
    public String bindAlipayToUser(BindAlipayCallbackDto bindAlipayCallbackDto) {
        String token = bindAlipayCallbackDto.state();
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(bindAlipayCallbackDto.auth_code());
        AlipaySystemOauthTokenResponse tokenResponse;
        log.info(alipayClient.toString());
        try {
            tokenResponse = alipayClient.execute(tokenRequest);
        } catch (AlipayApiException e) {
            throw new ApartmentException("调用获取支付宝AK接口失败");
        }
        if (tokenResponse.isSuccess()) {
            String accessToken = tokenResponse.getAccessToken();
            AlipayUserInfoShareRequest alipayIdRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse alipayIdResponse;
            try {
                alipayIdResponse = alipayClient.execute(alipayIdRequest, accessToken);
            } catch (AlipayApiException e) {
                throw new ApartmentException("调用获取支付宝ID接口失败");
            }
            if (alipayIdResponse.isSuccess()) {
                // 支付宝的工作完成了 现在需要把支付宝ID和User表的User绑定
                // 因为接口是从支付宝回调来的 所以没有登录状态 需要从state读到的token字段中获取信息
                String username = jwtTokenProvider.extractUsername(token);
                LoginAccount loginAccount =
                        // 直接写 不注入service的loadUserByUsername方法
                        loginAccountMapper.selectOne(
                                new LambdaQueryWrapper<LoginAccount>()
                                        .eq(LoginAccount::getUsername, username));
                User user =
                        this.getOne(
                                new QueryWrapper<User>().lambda()
                                        .eq(User::getLoginAccountId, loginAccount.getId()));
                user.setAlipayId(alipayIdResponse.getUserId());
                // 没有参数 也可以加 但建议前端缓存环境变量
                return this.updateById(user) ?
                        "redirect:" +
                                "https://apartment-client.wangminan.me" +
                                "/#/main/my/bind-alipay/success?token=" + token :
                        "redirect:" +
                                "https://apartment-client.wangminan.me" +
                                "/#/main/my/bind-alipay/failure?token=" + token;
            } else {
                log.error("调用获取支付宝ID接口失败, resp: {}", alipayIdResponse);
            }
        } else {
            log.error("调用获取支付宝AK接口失败, resp: {}", tokenResponse);
        }
        return "redirect:https://apartment-client.wangminan.me/#/main/mybind-alipay/failure";
    }
}




