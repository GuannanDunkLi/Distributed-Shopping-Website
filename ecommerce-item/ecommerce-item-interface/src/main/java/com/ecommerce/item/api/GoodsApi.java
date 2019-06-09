package com.ecommerce.item.api;

import com.ecommerce.common.dto.CartDto;
import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.Sku;
import com.ecommerce.item.pojo.Spu;
import com.ecommerce.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    /*
     * Query spu information by pagination
     * @param: page
     * @param: rows
     * @param: saleable
     * @param: key
     * @return ResponseEntity<PageResult<Spu>>
     * @author dunklee
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    /*
     * Check the product details according to the spu id
     * @param: spuId
     * @return ResponseEntity<SpuDetail>
     * @author dunklee
     */
    @GetMapping("spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id") Long spuId);

    /*
     * Query all sku according to spu id
     * @param: spuId
     * @return ResponseEntity<List<Sku>>
     * @author dunklee
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    /*
     * Query spu information based on spu id
     * @param: id
     * @return Spu
     * @author dunklee
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    /*
     * Query all sku according to sku's id collection
     * @param: spuId
     * @return ResponseEntity<List<Sku>>
     * @author dunklee
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuBySkuIds(@RequestParam("ids") List<Long> ids);

    /*
     * Decrease the number of stock
     * @param: cartDtos
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDto> cartDtos);
}
