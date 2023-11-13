package com.heima.model.admin.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginDto {

    /**
     * 手机号
     */
    @ApiModelProperty(value = "姓名", required = true)
    private String name;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
