package com.ecommerce.item.mapper;

import com.ecommerce.item.pojo.Sku;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface SkuMapper extends Mapper<Sku> {
}
