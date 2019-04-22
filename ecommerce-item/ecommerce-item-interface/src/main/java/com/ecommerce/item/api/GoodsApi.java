package com.ecommerce.item.api;

import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.Sku;
import com.ecommerce.item.pojo.Spu;
import com.ecommerce.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {
    /*
     * 分页查询spu信息
     * @param: page
     * @param: rows
     * @param: saleable
     * @param: key
     * @return ResponseEntity<PageResult<Spu>>
     * @author dunklee
     * @date 2019/3/28
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    /*
     * 根据spu的id查询商品详情
     * @param: spuId
     * @return ResponseEntity<SpuDetail>
     * @author dunklee
     * @date 2019/3/28
     */
    @GetMapping("spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id") Long spuId);

    /*
     * 根据spu的id查询所有sku
     * @param: spuId
     * @return ResponseEntity<List<Sku>>
     * @author dunklee
     * @date 2019/3/28
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);
}
