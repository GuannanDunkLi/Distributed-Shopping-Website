package com.ecommerce.item.web;

import com.ecommerce.common.vo.PageResult;
import com.ecommerce.item.pojo.Sku;
import com.ecommerce.item.pojo.Spu;
import com.ecommerce.item.pojo.SpuDetail;
import com.ecommerce.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

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
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) {
        return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, saleable, key));
    }

    /*
     * 商品新增
     * @param: spu
     * @return ResponseEntity<Void>
     * @author dunklee
     * @date 2019/3/28
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
     * 根据spu的id查询商品详情
     * @param: spuId
     * @return ResponseEntity<SpuDetail>
     * @author dunklee
     * @date 2019/3/28
     */
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id") Long spuId) {
        return ResponseEntity.ok(goodsService.queryDetailById(spuId));
    }

    /*
     * 根据spu的id查询所有sku
     * @param: spuId
     * @return ResponseEntity<List<Sku>>
     * @author dunklee
     * @date 2019/3/28
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long spuId) {
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }

    /*
     * 商品修改
     * @param: spu
     * @return ResponseEntity<Void>
     * @author dunklee
     * @date 2019/3/28
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}