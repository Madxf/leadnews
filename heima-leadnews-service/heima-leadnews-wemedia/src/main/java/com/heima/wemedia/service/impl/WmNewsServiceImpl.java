package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.AuthConstants;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import com.heima.wemedia.threadlocal.WmThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    /**
     * 文章列表分页查询
     * @param dto
     * @return
     */
    @Override
    public ResponseResult pageList(WmNewsPageReqDto dto) {
        // 检查参数
        dto.checkParam();
        // 分页
        IPage iPage = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 状态查询
        if(dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        // 频道查询
        if(dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        // 发布时间范围查询
        if(dto.getBeginPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }
        // 关键字模糊查询
        if(dto.getKeyword() != null) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }
        // 是否为当前用户
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocal.getWmUser().getId());
        // 按时间倒序
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);
        // 返回数据
        IPage page = page(iPage, lambdaQueryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 文章发布
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submit(WmNewsDto dto) {
        // 参数判断
        if(dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 保存或修改文章
        //   属性拷贝
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        //   图片格式 list --> string
        if(dto.getImages() != null && !dto.getImages().isEmpty()) {
            String image = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(image);
        }
        //   如果当前封面类型为自动 -1
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        // 判断文章是否为草稿，若为草稿，直接结束本方法
        if(wmNews.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        // 插入文章内容图片与素材的关系
        //    获取文章内容的图片url
        List<String> images = extractImageUrls(dto.getContent());
        //    保存文章内容图片与素材的关系
        saveRelativeInfoForContent(images, wmNews.getId());
        // 插入文章封面图片与素材的关系
        saveRelativeInfoForCover(dto, wmNews, images);

        // 审核文章 （异步）
        // todo  后续通过JUC进行优化
        try {
            Thread.sleep(500);
//            wmNewsAutoScanService.autoScanWmNews(wmNews.getId().longValue());
            wmNewsTaskService.addNewsToTask(wmNews.getId(), dto.getPublishTime());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1，如果内容图片大于等于1，小于3  单图  type 1
     * 2，如果内容图片大于等于3  多图  type 3
     * 3，如果内容没有图片，无图  type 0
     *
     * 第二个功能：保存封面图片与素材的关系
     * @param dto
     * @param wmNews
     * @param images
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> images) {
        List<String> coverImages = dto.getImages();
        // 如果当前封面类型为自动，则设置封面类型的数据
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            int size = images.size();
            if(size >= 3) {
                // 多图
                coverImages = images.stream().limit(3).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
            } else if(size >= 1) {
                // 单图
                coverImages = images.stream().limit(1).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            } else {
                // 无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            // 修改文章
            if(coverImages != null && !coverImages.isEmpty()) {
                wmNews.setImages(StringUtils.join(coverImages, ","));
            }
            updateById(wmNews);
        }
        //  保存封面图片与素材的关系
        if(coverImages != null && !coverImages.isEmpty()) {
            saveRelativeInfo(coverImages, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 保存文章内容图片与素材的关系
     * @param images
     * @param id
     */
    private void saveRelativeInfoForContent(List<String> images, Integer id) {
        saveRelativeInfo(images, id, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    /**
     * 保存文章(**)图片与素材的关系
     * @param images
     * @param id
     * @param type
     */
    private void saveRelativeInfo(List<String> images, Integer id, Short type) {
        // 判断素材是否为空
        if(images == null || images.isEmpty()) return;
        // 将images 的类型转为 List<Integer>
        List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, images));
        // 判断素材是否失效
        //     查询出的素材为空
        if(wmMaterials.isEmpty()) {
            throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
        }
        //      查询出的素材与传入素材不等
        if(wmMaterials.size() != images.size()) {
            throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
        }
        List<Integer> idList = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
        // 批量保存
        wmNewsMaterialMapper.saveRelations(idList, id, type);
    }


    /**
     * 提取内容中的图片url
     * @param content
     * @return
     */
    private List<String> extractImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if(map.get("type").equals("image")) {
                String url = (String) map.get("value");
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //补全属性
        wmNews.setUserId(WmThreadLocal.getWmUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架
        if(wmNews.getId() == null) {
            // 新增文章
            save(wmNews);
        } else {
            // 修改文章
            //      删除文章图片与素材的关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 文章上/下架
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        // 校验参数
        if(dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数无效");
        }
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数无效");
        }

        if(dto.getEnable() < 0 || dto.getEnable() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数无效");

        }
        // 修改状态
        update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable, dto.getEnable()).eq(WmNews::getId, dto.getId()));
        // 向article端发送消息
        if(wmNews.getArticleId() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("articleId", wmNews.getArticleId());
            map.put("enable", wmNews.getEnable());
            kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, JSON.toJSONString(map));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * admin端分页显示文章
     * @param newsAuthDto
     * @return
     */
    @Override
    public ResponseResult pageVoList(NewsAuthDto newsAuthDto) {
        // 检查参数
        newsAuthDto.checkParam();
        // new 分页和查询对象
        IPage iPage = new Page(newsAuthDto.getPage(), newsAuthDto.getSize());
        LambdaQueryWrapper<WmNews> queryWrapper = new LambdaQueryWrapper<>();
        // 根据标题搜索（模糊查询）
        if(StringUtils.isNotBlank(newsAuthDto.getTitle())) {
            queryWrapper.like(WmNews::getTitle, newsAuthDto.getTitle());
        }
        // 根据文章状态搜索
        if(newsAuthDto.getStatus() != null) {
            List<Integer> tmp = Arrays.asList(0, 1, 2, 3, 4, 8, 9);
            if(!tmp.contains(newsAuthDto.getStatus())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
            }
            queryWrapper.eq(WmNews::getStatus, newsAuthDto.getStatus());
        }
        queryWrapper.orderByDesc(WmNews::getCreatedTime);
        // 返回数据
        IPage page = page(iPage, queryWrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(newsAuthDto.getPage(), newsAuthDto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    /**
     * admin端根据id获取文章详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult getOneById(Integer id) {
        if(id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(id);
        return ResponseResult.okResult(wmNews);
    }

    /**
     * 驳回审核
     * @param newsAuthDto
     * @return
     */
    @Override
    public ResponseResult authFail(NewsAuthDto newsAuthDto) {
        // 检查参数
        newsAuthDto.checkParam();
        // 检查id
        if(newsAuthDto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews =
                getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, newsAuthDto.getId()));
        // 检查状态(只有等待人工审核的文章才能被驳回审核)
        if(wmNews == null || wmNews.getStatus() == null || !wmNews.getStatus().equals(WmNewsConstants.ARTIFICIAL_SCAN)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        }
        // 更新状态
        wmNews.setStatus(WmNewsConstants.SCAN_FAIL);
        updateById(wmNews);
        // 返回参数
        return pageVoList(newsAuthDto);
    }

    /**
     * 通过审核
     *
     * @param newsAuthDto
     * @return
     */
    @Override
    public ResponseResult authPass(NewsAuthDto newsAuthDto) {
        // 检查参数
        newsAuthDto.checkParam();
        // 检查id
        if(newsAuthDto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews =
                getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, newsAuthDto.getId()));
        // 检查状态(只有等待人工审核的文章才能被驳回审核)
        if(wmNews == null || wmNews.getStatus() == null || !wmNews.getStatus().equals(WmNewsConstants.ARTIFICIAL_SCAN)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        }
        // 更新状态
        wmNews.setStatus(WmNewsConstants.SCAN_FAIL);
        updateById(wmNews);
        // 返回参数
        return pageVoList(newsAuthDto);
    }
}
