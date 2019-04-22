package com.ecommerce.item.api;

import com.ecommerce.item.pojo.SpecGroup;
import com.ecommerce.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    /*
     * 查询规格参数集合
     * @param: gid
     * @param: cid
     * @param: searching
     * @return List<SpecParam>
     * @author dunklee
     */
    @GetMapping("spec/params")
    List<SpecParam> queryParamList(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /*
     * 根据分类cid3查询规格参数详细信息
     * @param: cid
     * @return java.util.List<com.ecommerce.item.pojo.SpecGroup>
     * @author dunklee
     */
    @GetMapping("spec/group")
    List<SpecGroup> queryAllByCid(@RequestParam("cid") Long cid);
}
