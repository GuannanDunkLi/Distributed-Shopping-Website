package com.ecommerce.search.repository;

import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.Spu;
import com.ecommerce.search.client.GoodsClient;
import com.ecommerce.search.pojo.Goods;
import com.ecommerce.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void testCreateIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page = 1; // 从第一页开始，别很es搞混了
        int rows = 100;
        int size = 0;
        do {
            // 查询spu
            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = spuPageResult.getItems(); // 当前页spu集合
            if (CollectionUtils.isEmpty(spuList)) {
                break;
            }
            // 构建成goods
            List<Goods> goods = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            // 存入索引库
            goodsRepository.saveAll(goods);
            // 翻页
            page++;
            size = spuList.size();
        } while (size == 100);
    }
}