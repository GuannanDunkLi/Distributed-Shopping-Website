package com.ecommerce.item.web;

import com.ecommerce.item.pojo.Category;
import com.ecommerce.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(Long pid) {
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

    /*
     * Query the category list according to the category id list
     * @param: ids
     * @return ResponseEntity<List<Category>>
     * @author dunklee
     * @date 2019/4/9
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

    @GetMapping("childLists")
    public ResponseEntity<List<Category>> queryChildCategoryListsByIds(@RequestParam("id") Long id){
        return ResponseEntity.ok(categoryService.queryChildCategoryListsById(id));
    }
}
