package com.heima.wemedia.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {


    /**
     * 查询所有频道
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }


    /**
     * 分页显示文章频道
     * @param channelDto
     * @return
     */
    @Override
    public ResponseResult pageList(ChannelDto channelDto) {
        // 参数检查
        channelDto.checkParam();
        // 定义分页和查询语句
        LambdaQueryWrapper<WmChannel> queryWrapper = new LambdaQueryWrapper<>();
        IPage page = new Page(channelDto.getPage(), channelDto.getSize());
        // 查询name
        if(StringUtils.isNotBlank(channelDto.getName())) {
            queryWrapper.like(WmChannel::getName, channelDto.getName());
        }
        queryWrapper.orderByDesc(WmChannel::getCreatedTime);
        IPage ipage = page(page, queryWrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(channelDto.getPage(), channelDto.getSize(), (int) ipage.getTotal());
        pageResponseResult.setData(ipage.getRecords());
        // 返回数据
        return pageResponseResult;
    }

    /**
     * 保存文章频道
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult saveOne(AdChannel adChannel) {
        // 检查参数
        if(adChannel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmChannel wmChannel = new WmChannel();
        BeanUtil.copyProperties(adChannel, wmChannel);
        wmChannel.setCreatedTime(new Date());
        wmChannel.setIsDefault(true);
        // 新增文章频道
        save(wmChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 修改文章频道
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult updateOne(AdChannel adChannel) {
        // 检查参数
        if(adChannel == null || adChannel.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 修改文章频道
        WmChannel wmChannel = new WmChannel();
        BeanUtil.copyProperties(adChannel, wmChannel);
        updateById(wmChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 根据id删除文章频道
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteById(Integer id) {
        // 检查参数
        if(id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 删除文章频道
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}