package com.heima.model.admin.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class AdChannel {

    private Integer id;

    /**
     * 频道名称
     */
    private String name;

    /**
     * 频道描述
     */
    private String description;

    /**
     * 是否默认频道
     * 1：默认     true
     * 0：非默认   false
     */
    private Boolean isDefault;

    /**
     * 是否启用
     * 1：启用   true
     * 0：禁用   false
     */
    private Boolean status;

    /**
     * 默认排序
     */
    private Integer ord;

    /**
     * 创建时间
     */
    private Date createdTime;
}
