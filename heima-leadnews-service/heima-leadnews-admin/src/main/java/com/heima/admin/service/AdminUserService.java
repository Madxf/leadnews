package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.admin.dtos.LoginDto;

public interface AdminUserService extends IService<AdUser> {
    /**
     * 登陆
     * @param loginDto
     * @return
     */
    ResponseResult login(LoginDto loginDto);

}
