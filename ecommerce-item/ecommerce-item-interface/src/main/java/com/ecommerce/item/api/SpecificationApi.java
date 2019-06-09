package com.ecommerce.item.api;

import com.ecommerce.item.pojo.SpecGroup;
import com.ecommerce.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    /*
     * Query specification parameter set
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
     * Query specification parameter details according to category cid3
     * @param: cid
     * @return java.util.List<com.ecommerce.item.pojo.SpecGroup>
     * @author dunklee
     */
    @GetMapping("spec/group")
    List<SpecGroup> queryAllByCid(@RequestParam("cid") Long cid);
}
