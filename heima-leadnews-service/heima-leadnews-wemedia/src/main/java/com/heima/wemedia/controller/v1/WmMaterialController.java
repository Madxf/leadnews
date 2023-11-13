package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
@Api(tags = "自媒体素材管理接口")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;


    @PostMapping("/upload_picture")
    @ApiOperation("图片素材上传")
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    @ApiOperation("图片素材列表")
    public ResponseResult listPictures(@RequestBody WmMaterialDto dto) {
        return wmMaterialService.listPicture(dto);
    }
}
