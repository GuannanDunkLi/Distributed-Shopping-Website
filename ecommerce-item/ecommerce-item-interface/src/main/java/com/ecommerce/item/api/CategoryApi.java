package com.ecommerce.item.api;

import com.ecommerce.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    /*
     * Query the category list according to the category id list
     * @param: ids
     * @return ResponseEntity<List<Category>>
     * @author dunklee
     */
    @GetMapping("category/list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
}
