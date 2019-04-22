package com.ecommerce.item.api;

import com.ecommerce.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    /*
     * 根据分类id列表查询分类列表
     * @param: ids
     * @return ResponseEntity<List<Category>>
     * @author dunklee
     * @date 2019/4/9
     */
    @GetMapping("category/list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
}
