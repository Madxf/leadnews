package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();

    /**
     * 分页显示文章频道
     * @param channelDto
     * @return
     */
    ResponseResult pageList(ChannelDto channelDto);

    /**
     * 保存文章频道
     * @param adChannel
     * @return
     */
    ResponseResult saveOne(AdChannel adChannel);

    /**
     * 修改文章频道
     * @param adChannel
     * @return
     */
    ResponseResult updateOne(AdChannel adChannel);

    /**
     * 根据id删除文章频道
     * @param id
     * @return
     */
    ResponseResult deleteById(Integer id);
}