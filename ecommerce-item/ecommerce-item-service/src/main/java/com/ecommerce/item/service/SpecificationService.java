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
        // 查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // 查询当前分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        // 先把规格参数变成map，map的key是规格组的id，map的值是组下规格参数
        Map<Long, List<SpecParam>> params = new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!params.containsKey(specParam.getGroupId())) {
                // 这个组id在params中不存在，新增一个list
                params.put(specParam.getGroupId(), new ArrayList<>());
            }
            params.get(specParam.getGroupId()).add(specParam);
        }
        // 填充specParams到specGroup中去
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(params.get(specGroup.getId()));
        }
        return specGroups;
    }
}