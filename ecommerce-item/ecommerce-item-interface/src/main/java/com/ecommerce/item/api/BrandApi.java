package com.ecommerce.item.api;

import com.ecommerce.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {
    /*
     * 根据商品品牌id查询品牌
     * @param: id
     * @return ResponseEntity<Brand>
     * @author dunklee
     */
    @GetMapping("brand/{id}")
    Brand queryBrandById(@PathVariable("id") Long id);

    /*
     * 根据商品品牌id列表查询品牌
     * @param: ids
     * @return ResponseEntity<List<Brand>>
     * @author dunklee
     */
    @GetMapping("brand/list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
