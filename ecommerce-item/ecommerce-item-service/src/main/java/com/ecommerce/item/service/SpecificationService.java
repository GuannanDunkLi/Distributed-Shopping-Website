package com.ecommerce.item.service;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.item.mapper.SpecGroupMapper;
import com.ecommerce.item.mapper.SpecParamMapper;
import com.ecommerce.item.pojo.SpecGroup;
import com.ecommerce.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(group);
        if (CollectionUtils.isEmpty(list)){
            throw new EException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if (CollectionUtils.isEmpty(list)){
            throw new EException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryAllByCid(Long cid) {
        // Query specGroups
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // Query the parameters under the current category
        List<SpecParam> specParams = queryParamList(null, cid, null);
        // First change the specification parameter to map,
        // the key of map is the id of the specification group,
        // and the value of map is the specification parameter of the group.
        Map<Long, List<SpecParam>> params = new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!params.containsKey(specParam.getGroupId())) {
                // This group id does not exist in params, add a list
                params.put(specParam.getGroupId(), new ArrayList<>());
            }
            params.get(specParam.getGroupId()).add(specParam);
        }
        // Fill the specParams to the specGroup
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(params.get(specGroup.getId()));
        }
        return specGroups;
    }
}