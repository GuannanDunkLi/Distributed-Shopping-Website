package com.ecommerce.item.api;

import com.ecommerce.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {
    /*
     * Query the brand according to the product brand id
     * @param: id
     * @return ResponseEntity<Brand>
     * @author dunklee
     */
    @GetMapping("brand/{id}")
    Brand queryBrandById(@PathVariable("id") Long id);

    /*
     * Query the brand according to the product brand id list
     * @param: ids
     * @return ResponseEntity<List<Brand>>
     * @author dunklee
     */
    @GetMapping("brand/list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
