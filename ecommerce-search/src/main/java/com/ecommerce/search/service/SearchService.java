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
import org.elasticsearch.index.query.BoolQueryBuilder;
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

        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new EException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());

        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new EException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // search field 'all'
        String all = spu.getTitle() + " " + StringUtils.join(categoryNames, " ") + " " + brand.getName();

        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new EException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        Set<Long> priceSet = new HashSet<>();
        // Processing sku field
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku:skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ",")); //sku中有多个图片，只展示第一张
            skus.add(map);

            priceSet.add(sku.getPrice());
        }
        // Query specification parameters, which are divided into general specification parameters and special specification parameters.
        List<SpecParam> params = specClient.queryParamList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new EException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }

        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);

        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);

        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});

        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param:params){
            String key = param.getName();
            Object value = "";

            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
                if (param.getNumeric()) {
                    // segment method
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                value = specialSpec.get(param.getId());
            }

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

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "other";
        // Save value segment
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = "Greater than"+segs[0] + p.getUnit();
                } else if (begin == 0) {
                    result = "Less than"+segs[1] + p.getUnit();
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

        if (StringUtils.isBlank(key)) {
            return null;
        }

        int page = request.getPage() - 1;
        int size = request.getSize();

        // Create a query builder
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // Filter results
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // Pagination
        queryBuilder.withPageable(PageRequest.of(page, size));
        // Search filtering
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        // Aggregate category and branding
        String categoryAggName = "category_agg";
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // Parsing paged results
        long totalElements = goods.getTotalElements();
        int totalPages = goods.getTotalPages();
        List<Goods> content = goods.getContent();

        Aggregations aggregations = goods.getAggregations();
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAggName));
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // if the product category exists and the number is 1, the specification parameter aggregation can be performed.
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(totalElements, totalPages, content, categories, brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // Create a Boolean query
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));

        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();

            if(!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs."+key+".keyword";
            }
            String value = entry.getValue();
            queryBuilder.filter(QueryBuilders.termQuery(key, value));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // Query the specification parameters that need to be aggregated
        List<SpecParam> specParams = specClient.queryParamList(null, cid, true);
        // Aggregation
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }

        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // Parsing result
        Aggregations aggregations = result.getAggregations();
        for (SpecParam specParam:specParams) {
            String name = specParam.getName();
            StringTerms terms = aggregations.get(name);

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

    public void createOrUpdateIndex(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}