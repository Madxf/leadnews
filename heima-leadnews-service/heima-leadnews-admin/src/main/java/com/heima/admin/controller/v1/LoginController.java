package com.heima.admin.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.admin.dtos.LoginDto;
import com.heima.admin.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/login")
@RestController
@Slf4j
@Api(tags = "admin端用户界面接口", value = "admin端用户界面接口")
public class LoginController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 登陆
     * @param loginDto
     * @return
     */
    @ApiOperation("用户登陆")
    @PostMapping("/in")
    public ResponseResult login(@RequestBody LoginDto loginDto) {
        return adminUserService.login(loginDto);
    }

}
