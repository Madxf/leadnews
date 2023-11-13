package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    /**
     * 敏感词分页查询
     * @param sensitiveDto
     * @return
     */
    ResponseResult pageList(SensitiveDto sensitiveDto);

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */
    ResponseResult saveOne(AdSensitive adSensitive);

    /**
     * 根据id删除敏感词
     * @param id
     * @return
     */
    ResponseResult deleteById(Integer id);

    /**
     * 根据id修改敏感词
     * @param adSensitive
     * @return
     */
    ResponseResult updateOneById(AdSensitive adSensitive);
}
