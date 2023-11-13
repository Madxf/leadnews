package com.heima.model.admin.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class AuthDto extends PageRequestDto {
    private Integer id;

    private String msg;
    /**
     * 审核是否通过
     * 0: 未通过， 1: 通过
     */
    private Integer status;

}
