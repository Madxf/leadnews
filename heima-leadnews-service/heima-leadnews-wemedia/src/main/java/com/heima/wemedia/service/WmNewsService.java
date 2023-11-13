package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {
    /**
     * 文章列表分页查询
     * @param dto
     * @return
     */
    ResponseResult pageList(WmNewsPageReqDto dto);

    /**
     * 文章发布
     * @param dto
     * @return
     */
    ResponseResult submit(WmNewsDto dto);

    /**
     * 文章上/下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);

    /**
     * admin端分页显示文章
     * @param newsAuthDto
     * @return
     */
    ResponseResult pageVoList(NewsAuthDto newsAuthDto);

    /**
     * admin端根据id获取文章详情
     * @param id
     * @return
     */
    ResponseResult getOneById(Integer id);

    /**
     * 驳回审核
     * @param authDto
     * @return
     */
    ResponseResult authFail(NewsAuthDto newsAuthDto);

    /**
     * 通过审核
     * @param authDto
     * @return
     */
    ResponseResult authPass(NewsAuthDto newsAuthDto);
}
