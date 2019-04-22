package com.ecommerce.item.service;

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

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 开始分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索字段过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // 默认排序
        example.setOrderByClause("Last_update_time DESC");
        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        // 判断
        if (CollectionUtils.isEmpty(spus)) {
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 解析分类和品牌的名称
        loadCategoryAndBrandName(spus);
        // 解析分页结果并返回
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        return new PageResult<>(pageInfo.getTotal(), spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
//            categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(category -> category.getName()).reduce((s1,s2) -> s1+s2); // 字符串拼接会造成内存里存在大量字符串
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        saveSkuAndStock(spu);
    }

    private void saveSkuAndStock(Spu spu) {
        int count;// 定义库存集合，方便下面批量新增库存
        List<Stock> stockList = new ArrayList<>();
        // 新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if (count != 1){
                throw new EException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        // 批量新增库存
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
        // 查询库存
        for (Sku s :list) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if(stock == null){
                throw new EException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }
        return list;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null){
            throw new EException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        // 查询sku
        List<Sku> skulist = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skulist)){
            // 删除sku和stock
            skuMapper.delete(sku);
            List<Long> ids = skulist.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        // 修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1){
            throw new EException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 新增sku和stocke
        saveSkuAndStock(spu);
    }
}