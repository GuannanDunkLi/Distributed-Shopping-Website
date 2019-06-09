package com.ecommerce.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Data
@Table(name = "tb_sku")
public class Sku {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long spuId;
    private String title;
    private String images; // Multiple images will be separated by commas
    private Long price;
    private String ownSpec; // Key-value pairs for special specifications
    private String indexes; // Indexes of special specifications of goods
    private Boolean enable;
    private Date createTime;
    private Date lastUpdateTime;

    @Transient
    private int stock;
}
