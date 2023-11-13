package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmSensitiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
@Api(tags = "敏感词管理相关Api")
@Slf4j
public class WmSensitivityController {

    @Autowired
    private WmSensitiveService wmSensitiveService;

    @ApiOperation("分页查询")
    @PostMapping("/list")
    public ResponseResult pageList(@RequestBody SensitiveDto sensitiveDto) {
        log.info("分页查询");
        return wmSensitiveService.pageList(sensitiveDto);
    }

    @ApiOperation("新增敏感词")
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdSensitive adSensitive) {
        return wmSensitiveService.saveOne(adSensitive);
    }

    @ApiOperation("删除敏感词")
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        return wmSensitiveService.deleteById(id);
    }

    @ApiOperation("修改敏感词")
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdSensitive adSensitive) {
        log.info("update, {}", adSensitive);
        return wmSensitiveService.updateOneById(adSensitive);
    }
}
