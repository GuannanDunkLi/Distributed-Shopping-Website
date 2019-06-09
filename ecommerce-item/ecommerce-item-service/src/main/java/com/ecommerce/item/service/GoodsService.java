package com.ecommerce.item.service;

import com.ecommerce.common.dto.CartDto;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.mapper.SkuMapper;
import com.ecommerce.item.mapper.SpuDetailMapper;
import com.ecommerce.item.mapper.SpuMapper;
import com.ecommerce.item.mapper.StockMapper;
import com.ecommerce.item.pojo.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        PageHelper.startPage(page, rows);
        // Filtering
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // Filtering by title
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // Filtering by saleable
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // Sorting
        example.setOrderByClause("Last_update_time DESC");
        // Querying
        List<Spu> spus = spuMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(spus)) {
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // Analyze the name of the category and brand
        loadCategoryAndBrandName(spus);
        // Parse the paged result and return
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        return new PageResult<>(pageInfo.getTotal(), spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // Deal with category name
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            // Deal with brand name
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        // Add spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // Add spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        saveSkuAndStock(spu);
        // send mq message
        amqpTemplate.convertAndSend("item.insert", spu.getId());
    }

    private void saveSkuAndStock(Spu spu) {
        int count;
        List<Stock> stockList = new ArrayList<>();
        // Add sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if (count != 1){
                throw new EException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            // Add stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }

        stockMapper.insertList(stockList);
    }

    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail == null){
            throw new EException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> list = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(list)){
            throw new EException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        loadStockInSku(list);
        return list;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null){
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        // Query sku
        List<Sku> skulist = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skulist)){
            skuMapper.delete(sku);
            List<Long> ids = skulist.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        // edit spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // edit detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // Add sku ang stock
        saveSkuAndStock(spu);
        // send mq message
        amqpTemplate.convertAndSend("item.update", spu.getId());
    }

    public Spu querySpuById(Long id) {
        // Query spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // Query sku
        spu.setSkus(querySkuBySpuId(id));
        // Query detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    public List<Sku> querySkuBySkuIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // Query stock
        loadStockInSku(skus);
        return skus;
    }

    private void loadStockInSku(List<Sku> skus) {
        for (Sku s : skus) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock == null) {
                throw new EException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }
    }

    @Transactional
    public void decreaseStock(List<CartDto> cartDtos) {
        for (CartDto cartDto : cartDtos) {
            int count = stockMapper.decreaseStock(cartDto.getSkuId(), cartDto.getNum());
            if (count != 1) {
                throw new EException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}