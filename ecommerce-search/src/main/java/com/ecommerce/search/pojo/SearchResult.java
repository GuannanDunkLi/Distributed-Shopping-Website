package com.ecommerce.search.pojo;

import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.Brand;
import com.ecommerce.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

/*
 * 搜索结果类
 * @param: null
 * @return
 * @author dunklee
 */
@Data
public class SearchResult extends PageResult<Goods> {
    private List<Category> categories;
    private List<Brand> brands;
    private List<Map<String, Object>> specs;

    public SearchResult() {}

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}