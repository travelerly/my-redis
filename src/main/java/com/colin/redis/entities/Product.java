package com.colin.redis.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author colin
 * @create 2021-05-24 08:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品价格
     */
    private Integer price;

    /**
     * 产品详情
     */
    private String detail;

}
