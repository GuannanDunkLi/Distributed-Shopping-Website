package com.ecommerce.search.service;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.utils.JsonUtils;
import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.*;
import com.ecommerce.search.client.BrandClient;
import com.ecommerce.search.client.CategoryClient;
import com.ecommerce.search.client.GoodsClient;
import com.ecommerce.search.client.SpecificationClient;
import com.ecommerce.search.pojo.Goods;
import com.ecommerce.search.pojo.SearchRequest;
import com.ecommerce.search.pojo.SearchResult;
import com.ecommerce.search.repository.GoodsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new EException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new EException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 搜索字段
        String all = spu.getTitle() + " " + StringUtils.join(categoryNames, " ") + " " + brand.getName();
        // 查询sku集合
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new EException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        // 价格集合
        Set<Long> priceSet = new HashSet<>();
        // 对sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku:skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ",")); //sku中有多个图片，只展示第一张
            skus.add(map);
            // 查询价格集合
            priceSet.add(sku.getPrice());
        }
        // 查询规格参数，规格参数中分为通用规格参数和特有规格参数
        List<SpecParam> params = specClient.queryParamList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new EException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        // 查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);
        // 获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        // 获取特殊规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        // 规格参数
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param:params){
            // 规格名称
            String key = param.getName();
            Object value = "";
            // 判断是否是通用规格
            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
                // 判断是否是数值类型
                if (param.getNumeric()) {
                    // 处理成段
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                value = specialSpec.get(param.getId());
            }
            // 存入map
            specs.put(key, value);
        }

        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setAll(all);
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setPrice(priceSet);
        goods.setSkus(JsonUtils.toString(skus));
        goods.setSpecs(specs);
        return goods;
    }

    // copy
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        // 判断是否有搜索条件，如果没有，直接返回null，不允许搜索全部商品
        if (StringUtils.isBlank(key)) {
            return null;
        }

        int page = request.getPage() - 1;
        int size = request.getSize();

        // 创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 对结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 搜索过滤
        QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key);
        queryBuilder.withQuery(basicQuery);
        // 聚合分类和品牌
        String categoryAggName = "category_agg";
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 查询
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // 解析分页结果
        long totalElements = goods.getTotalElements(); // 总条数
        int totalPages = goods.getTotalPages(); // 总页数
        List<Goods> content = goods.getContent();
        // 解析聚合结果
        Aggregations aggregations = goods.getAggregations();
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAggName));
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 商品分类存在且数量为1， 可以进行规格参数聚合
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(totalElements, totalPages, content, categories, brands,specs);
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 查询需要聚合的规格参数
        List<SpecParam> specParams = specClient.queryParamList(null, cid, true);
        // 聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        // 获取结果
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // 解析结果
        Aggregations aggregations = result.getAggregations();
        for (SpecParam specParam:specParams) {
            String name = specParam.getName();
            StringTerms terms = aggregations.get(name);
            // 准备map
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Category> categories = categoryClient.queryCategoryByIds(ids);
        return categories;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Brand> brands = brandClient.queryBrandByIds(ids);
        return brands;
    }
}