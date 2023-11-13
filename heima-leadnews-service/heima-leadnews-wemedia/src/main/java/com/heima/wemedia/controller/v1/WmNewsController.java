package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@Api(tags = "自媒体文章相关接口")
@Slf4j
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    @ApiOperation("wemedia端分页显示文章")
    public ResponseResult list(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.pageList(dto);
    }

    @PostMapping("/submit")
    @ApiOperation("文章发布")
    public ResponseResult submit(@RequestBody WmNewsDto dto) {
        return wmNewsService.submit(dto);
    }

    @PostMapping("/down_or_up")
    @ApiOperation("文章上/下架")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto) {
        log.info("文章上/下架");
        return wmNewsService.downOrUp(dto);
    }

    @PostMapping("/list_vo")
    @ApiOperation("admin端分页显示文章")
    public ResponseResult pageVoList(@RequestBody NewsAuthDto newsAuthDto) {
        return wmNewsService.pageVoList(newsAuthDto);
    }

    @GetMapping("/one_vo/{id}")
    @ApiOperation("admin端根据id获取文章详情")
    public ResponseResult getById(@PathVariable Integer id) {
        return wmNewsService.getOneById(id);
    }

    @ApiOperation("驳回审核")
    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto newsAuthDto) {
        return wmNewsService.authFail(newsAuthDto);
    }

    @ApiOperation("通过审核")
    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDto newsAuthDto) {
        return wmNewsService.authPass(newsAuthDto);
    }
}

