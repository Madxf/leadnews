package com.heima.wemedia.controller.v1;

import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/channel")
@Api(tags = "自媒体文章频道相关接口")
public class WmChannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    @ApiOperation("文章频道列表")
    public ResponseResult list() {
        return wmChannelService.findAll();
    }

    @ApiOperation("分页显示文章频道")
    @PostMapping("/list")
    public ResponseResult pageList(@RequestBody ChannelDto channelDto) {
        return wmChannelService.pageList(channelDto);
    }

    @ApiOperation("新增文章频道")
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannel adChannel) {
        return wmChannelService.saveOne(adChannel);
    }

    @ApiOperation("修改文章频道")
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel adChannel) {
        return wmChannelService.updateOne(adChannel);
    }

    @ApiOperation("删除文章频道")
    @GetMapping("/del/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        return wmChannelService.deleteById(id);
    }


}
