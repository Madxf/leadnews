package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/login")
@RestController
@Slf4j
@Api(tags = "app端用户界面接口", value = "app端用户界面接口")
public class ApUserController {

    @Autowired
    private ApUserService apUserService;

    /**
     * 登陆
     * @param loginDto
     * @return
     */
    @ApiOperation("用户登陆")
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto loginDto) {
        return apUserService.login(loginDto);
    }

}
