package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApAuthService extends IService<ApUserRealname> {

    /**
     * 用户审核列表分页查询
     * @param authDto
     * @return
     */
    ResponseResult pageList(AuthDto authDto);

    /**
     * 驳回审核
     * @param authDto
     * @return
     */
    ResponseResult authFail(AuthDto authDto);

    /**
     * 通过审核
     * @param authDto
     * @return
     */
    ResponseResult authPass(AuthDto authDto);
}
