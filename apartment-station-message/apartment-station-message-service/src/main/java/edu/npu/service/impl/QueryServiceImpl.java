package edu.npu.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.doc.MessageDoc;
import edu.npu.dto.BasicQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.Admin;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.service.QueryService;
import edu.npu.vo.PageResultVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.npu.common.EsConstants.MESSAGE_INDEX;

/**
 * @author : [wangminan]
 * @description : [站内信搜索功能实现类]
 */
@Service
public class QueryServiceImpl implements QueryService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Override
    public R querySenderOutbox(AccountUserDetails accountUserDetails,
                               BasicQueryDto queryDto) {
        if (accountUserDetails == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户不存在,请先登录");
        }
        SearchRequest searchRequest = buildBasicQuery(
                accountUserDetails, queryDto, true);
        try {
            SearchResponse<MessageDoc> searchResponse =
                    elasticsearchClient.search(searchRequest, MessageDoc.class);
            return resolveRestResponse(searchResponse);
        } catch (IOException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "用户信息查询失败");
        }
    }

    @Override
    public R queryReceiverInbox(AccountUserDetails accountUserDetails, BasicQueryDto queryDto) {
        if (accountUserDetails == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户不存在,请先登录");
        }
        SearchRequest searchRequest = buildBasicQuery(
                accountUserDetails, queryDto, false);
        try {
            SearchResponse<MessageDoc> searchResponse =
                    elasticsearchClient.search(searchRequest, MessageDoc.class);
            return resolveRestResponse(searchResponse);
        } catch (IOException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "用户信息查询失败");
        }
    }

    private SearchRequest buildBasicQuery(AccountUserDetails accountUserDetails,
                                          BasicQueryDto pageQueryDto,
                                          Boolean isOutBox) {
        Query query = formBoolQuery(accountUserDetails, pageQueryDto, isOutBox);
        // 2.2.分页
        int page = pageQueryDto.pageNum();
        int size = pageQueryDto.pageSize();
        // 拼装
        return new SearchRequest.Builder()
                .index(MESSAGE_INDEX)
                .query(query)
                .from((page - 1) * size)
                .size(size)
                .build();
    }

    private Query formBoolQuery(AccountUserDetails accountUserDetails,
                                BasicQueryDto pageQueryDto,
                                Boolean isOutBox) {
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
        // 2. 用户信息
        Query loginAccountQuery;
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue()) {
            // 只能是用户了 只要搜receiverIds字段的数组中是否包含
            User user =
                    userServiceClient.getUserByLoginAccountId(
                            accountUserDetails.getId());
            loginAccountQuery = new MatchQuery.Builder()
                    .field("receiverIds").query(user.getId())
                    .build()._toQuery();
        } else {
            // 除了receiverIds字段的数组还需要检索senderAdminId是否包含
            Admin admin =
                    userServiceClient.getAdminByLoginAccountId(
                            accountUserDetails.getId());
            if (isOutBox) {
                loginAccountQuery = new BoolQuery.Builder()
                        .should(new MatchQuery.Builder()
                                .field("receiverIds").query(admin.getId())
                                .build()._toQuery())
                        .should(new MatchQuery.Builder()
                                .field("senderAdminId").query(admin.getId())
                                .build()._toQuery())
                        .build()._toQuery();
            } else {
                loginAccountQuery = new MatchQuery.Builder()
                        .field("receiverIds").query(admin.getId())
                        .build()._toQuery();
            }
        }
        if (loginAccountQuery == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL,
                    "查询不到对应用户或管理员");
        }
        // 3. 拼装
        return new BoolQuery.Builder()
                .must(keywordQuery)
                .must(loginAccountQuery)
                .build()._toQuery();
    }

    private R resolveRestResponse(SearchResponse<MessageDoc> response) {
        PageResultVo pageResult = handlePageResponse(response);
        R r = new R();
        r.put("code", ResponseCodeEnum.SUCCESS.getValue());
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.data());
        result.put("total", pageResult.total());
        return r.put("result", result);
    }

    private PageResultVo handlePageResponse(SearchResponse<MessageDoc> response) {
        // 4.1 获取数据
        List<Hit<MessageDoc>> hits = response.hits().hits();
        // 4.1.总条数
        long total = 0;
        if (response.hits().total() != null) {
            total = response.hits().total().value();
        }
        List<MessageDoc> messageDocs = hits.stream().map(Hit::source).toList();
        return new PageResultVo(total, messageDocs);
    }
}
