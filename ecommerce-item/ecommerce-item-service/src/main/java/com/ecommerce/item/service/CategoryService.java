package com.ecommerce.item.service;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.item.mapper.CategoryMapper;
import com.ecommerce.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid) {
        Category c = new Category();
        c.setParentId(pid);
        List<Category> list = categoryMapper.select(c);
        if(CollectionUtils.isEmpty(list)){
            throw new EException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> queryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw new EException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> queryChildCategoryListsById(Long id) {
        Category c = new Category();
        c.setParentId(id);
        List<Category> list = categoryMapper.select(c);
        for (Category category : list) {
            Category c1 = new Category();
            c1.setParentId(category.getId());
            List<Category> childCates = categoryMapper.select(c1);
            category.setChildCates(childCates);
        }
        return list;
    }
}