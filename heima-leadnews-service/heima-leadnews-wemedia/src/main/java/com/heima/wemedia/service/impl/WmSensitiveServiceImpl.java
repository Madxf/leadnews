package com.heima.wemedia.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {

    /**
     * 敏感词分页查询
     * @param sensitiveDto
     * @return
     */
    @Override
    public ResponseResult pageList(SensitiveDto sensitiveDto) {
        sensitiveDto.checkParam();
        // 分页
        IPage iPage =  new Page(sensitiveDto.getPage(), sensitiveDto.getSize());
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        // 查询名字相关
        if(StringUtils.isNotBlank(sensitiveDto.getName())) {
            wrapper.like(WmSensitive::getSensitives, sensitiveDto.getName());
        }
        // 按时间倒序
        wrapper.orderByDesc(WmSensitive::getCreatedTime);
        // 返回数据
        IPage page = page(iPage, wrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(sensitiveDto.getPage(), sensitiveDto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */
    @Override
    public ResponseResult saveOne(AdSensitive adSensitive) {
        if(adSensitive == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmSensitive wmSensitive = new WmSensitive();
        BeanUtil.copyProperties(adSensitive, wmSensitive);
        wmSensitive.setCreatedTime(new Date());
        save(wmSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 根据id删除敏感词
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteById(Integer id) {
        if(id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * 根据id修改敏感词
     * @param adSensitive
     * @return
     */
    @Override
    public ResponseResult updateOneById(AdSensitive adSensitive) {
        if(adSensitive == null || adSensitive.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmSensitive wmSensitive = new WmSensitive();
        BeanUtil.copyProperties(adSensitive, wmSensitive);
        updateById(wmSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
