package com.ecommerce.item.web;

import com.ecommerce.item.pojo.SpecGroup;
import com.ecommerce.item.pojo.SpecParam;
import com.ecommerce.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    // 根据分类id查询规格组
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    /*
     * 查询规格参数集合
     * @param: gid
     * @param: cid
     * @param: searching
     * @return ResponseEntity<List<SpecParam>>
     * @author dunklee
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamList(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching){
        return ResponseEntity.ok(specificationService.queryParamList(gid, cid, searching));
    }

    /*
     * 根据分类cid3查询规格参数详细信息
     * @param: cid
     * @return List<SpecGroup>
     * @author dunklee
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryAllByCid(@RequestParam("cid") Long cid) {
        return ResponseEntity.ok(specificationService.queryAllByCid(cid));
    }
}