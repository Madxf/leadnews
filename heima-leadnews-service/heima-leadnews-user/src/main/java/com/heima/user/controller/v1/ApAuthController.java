package com.heima.user.controller.v1;


import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApAuthService;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户审核相关Api")
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class ApAuthController {

    @Autowired
    private ApAuthService apAuthService;

    @ApiOperation("用户审核列表分页查询")
    @PostMapping("/list")
    public ResponseResult pageList(@RequestBody AuthDto authDto) {
        return apAuthService.pageList(authDto);
    }

    @ApiOperation("驳回审核")
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto authDto) {
        return apAuthService.authFail(authDto);
    }

    @ApiOperation("通过审核")
    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto authDto) {
        return apAuthService.authPass(authDto);
    }
}
