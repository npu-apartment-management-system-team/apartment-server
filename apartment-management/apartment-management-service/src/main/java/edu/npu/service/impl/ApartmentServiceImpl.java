package edu.npu.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoDistanceSort;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.doc.ApartmentDoc;
import edu.npu.dto.ApartmentDto;
import edu.npu.dto.ApartmentPageQueryDto;
import edu.npu.entity.Apartment;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.ApartmentMapper;
import edu.npu.service.ApartmentService;
import edu.npu.util.RedisClient;
import edu.npu.vo.ApartmentPageResultVo;
import edu.npu.vo.R;
import edu.npu.vo.SimpleApartmentVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.EsConstants.APARTMENT_INDEX;
import static edu.npu.common.RedisConstants.*;

/**
 * @author wangminan
 * @description 针对表【apartment(公寓表)】的数据库操作Service实现
 * @createDate 2023-06-29 09:13:20
 */
@Service
public class ApartmentServiceImpl extends ServiceImpl<ApartmentMapper, Apartment>
        implements ApartmentService {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private RedisClient redisClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addApartment(ApartmentDto apartmentDto) {
        Apartment apartment = new Apartment();
        BeanUtils.copyProperties(apartmentDto, apartment);
        boolean save = this.save(apartment);
        return save ? R.ok() :
            R.error(ResponseCodeEnum.SERVER_ERROR, "添加公寓失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteApartment(Long id) {
        boolean remove = this.removeById(id);
        return remove ? R.ok() :
            R.error(ResponseCodeEnum.SERVER_ERROR, "删除公寓失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateApartment(Long id, ApartmentDto apartmentDto) {
        Apartment apartment = getById(id);
        if (apartment == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "您希望改动的公寓不存在");
        } else {
            BeanUtils.copyProperties(apartmentDto, apartment);
            boolean update = this.updateById(apartment);
            return update ? R.ok() :
                R.error(ResponseCodeEnum.SERVER_ERROR, "更新公寓失败");
        }
    }

    @Override
    public R getApartmentList(ApartmentPageQueryDto apartmentPageQueryDto) {
        SearchRequest searchRequest = buildBasicQuery(apartmentPageQueryDto);
        try {
            SearchResponse<ApartmentDoc> searchResponse =
                    elasticsearchClient.search(searchRequest, ApartmentDoc.class);
            return resolveRestResponse(searchResponse);
        } catch (IOException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "用户信息查询失败");
        }
    }

    @Override
    public SearchRequest buildBasicQuery(ApartmentPageQueryDto pageQueryDto) {
        Query query = formBoolQuery(pageQueryDto);
        // 2.2.分页
        int page = pageQueryDto.pageNum();
        int size = pageQueryDto.pageSize();
        // 拼装
        if (pageQueryDto.latitude() == null || pageQueryDto.longitude() == null) {
            return new SearchRequest.Builder()
                .index(APARTMENT_INDEX)
                .query(query)
                .from((page - 1) * size)
                .size(size)
                .build();
        }
        return new SearchRequest.Builder()
            .index(APARTMENT_INDEX)
            .query(query)
            .from((page - 1) * size)
            .size(size)
            .sort(
                // 经纬度距离排序
                new SortOptions.Builder()
                    .geoDistance(
                        new GeoDistanceSort
                            .Builder()
                            .field("location")
                            .location(
                                new GeoLocation.Builder().latlon(
                                    new LatLonGeoLocation.Builder()
                                        .lat(pageQueryDto.latitude())
                                        .lon(pageQueryDto.longitude())
                                        .build()
                            ).build())
                            .build()
                    )
                    .build()
            ).build();
    }

    private static Query formBoolQuery(ApartmentPageQueryDto pageQueryDto) {
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
        // 2. 拼装
        return new BoolQuery.Builder()
            .must(keywordQuery)
            .build()._toQuery();
    }

    @Override
    public R resolveRestResponse(SearchResponse<ApartmentDoc> response) {
        ApartmentPageResultVo pageResult = handlePageResponse(response);
        R r = new R();
        r.put("code", ResponseCodeEnum.SUCCESS.getValue());
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.data());
        result.put("total", pageResult.total());
        return r.put("result", result);
    }

    @Override
    public R getApartmentSimpleList() {
        List<SimpleApartmentVo> simpleApartmentVoList =
            list().stream().map(
                apartment ->
                    new SimpleApartmentVo(apartment.getId(), apartment.getName())
            ).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("list", simpleApartmentVoList);
        return R.ok(result);
    }

    @Override
    public R getApartmentDetail(Long id) {
        Apartment apartment = redisClient.queryWithLogicalExpire(
                CACHE_APARTMENT_KEY, String.valueOf(id), Apartment.class,
                this::getById, CACHE_APARTMENT_TTL, TimeUnit.MINUTES,
                LOCK_APARTMENT_KEY
        );
        return R.ok().put("result", apartment);
    }

    private ApartmentPageResultVo handlePageResponse(SearchResponse<ApartmentDoc> response) {
        // 4.1 获取数据
        List<Hit<ApartmentDoc>> hits = response.hits().hits();
        // 4.1.总条数
        long total = 0;
        if (response.hits().total() != null) {
            total = response.hits().total().value();
        }
        List<ApartmentDoc> apartmentDocs = hits.stream().map(Hit::source).toList();
        return new ApartmentPageResultVo(total, apartmentDocs);
    }
}




