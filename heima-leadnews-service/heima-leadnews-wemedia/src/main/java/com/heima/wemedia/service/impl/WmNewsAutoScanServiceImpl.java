package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.baiduyun.ImageScan;
import com.heima.common.baiduyun.TextScan;
import com.heima.common.exception.CustomException;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {


    @Autowired
    private WmNewsMapper wmNewsMapper;


    /**
     * 文章内容和图片自动审核
     * @param id
     */
//    @GlobalTransactional // 由于该方法中存在远程调用，因此需要添加分布式事务锁
    @Transactional
    @Async // 异步操作
    public void autoScanWmNews(Long id) {
        // 查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        // 检查参数
        if(wmNews == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 检查状态
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            log.info("开始审核--------begin");
            // 提取文章内容文本与图片
            Map<String, Object> map = extractTextAndImage(wmNews);
            // 自管理敏感词过滤
            boolean sensitiveIsPass = handleSensitiveScan((String)map.get("content"), wmNews);
            if(!sensitiveIsPass) return;
            // 审核内容文本
            boolean textIsPass = scanContentText((String)map.get("content"), wmNews);
            if(!textIsPass) return;
            // 审核图片
            boolean imageIsPass = scanContentImage((List<String>) map.get("image"), wmNews);
            if(!imageIsPass) return;
            //  审核成功，保存app端的相关数据（远程调用文章微服务
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)) {
                throw new RuntimeException("app端文章保存失败");
            }
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功");
        }
    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;
        // 获取所有敏感词（查库
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().
                                                                        select(WmSensitive::getSensitives));
        List<String> stringList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        // 初始化敏感词库（构建"敏感词自动机")
        SensitiveWordUtil.initMap(stringList);
        // 返回是否含有敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(!map.isEmpty()) {
            flag = false;
            updateWmNews(wmNews, (short) 2, "存在违规内容：" + map);
        }
        return flag;
    }


    @Autowired
    private IArticleClient articleClient; // 远程调用文章微服务
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 保存app端相关数据（远程调用文章微服务
     * @param wmNews
     * @return
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        // 填充属性
        BeanUtils.copyProperties(wmNews, dto);
        //文章的布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel != null){
            dto.setChannelName(wmChannel.getName());
        }
        //作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser != null){
            dto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if(wmNews.getArticleId() != null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;
    }


    @Autowired
    private ImageScan imageScan;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private Tess4jClient tess4jClient;
    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean scanContentImage(List<String> images, WmNews wmNews) {
        boolean flag = true;
        if(images == null || images.isEmpty()) return flag;
        // 将图片下载并转为二进制数组格式，再有baiduyunAPI进行自动审核
        List<byte[]> imageContent = new ArrayList<>();
        // 去重
        images = images.stream().distinct().collect(Collectors.toList());
        for(String image: images) {
            byte[] bytes = fileStorageService.downLoadFile(image);
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
                String result = tess4jClient.doOCR(bufferedImage);
                boolean isSensitivePass = handleSensitiveScan(result, wmNews);
                if(!isSensitivePass) return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            imageContent.add(bytes);
        }
        byte[] result = byteArrayList2ByteArray(imageContent);
        Map map = imageScan.scanImage(result);
        if(map != null) {
            Integer conclusionType = (Integer)map.get("conclusionType");
            if(conclusionType == 2) { // 不合规
                updateWmNews(wmNews, (short) 2, "存在违规内容");
                flag = false;
            } else if(conclusionType == 3) { // 需要人工审核
                updateWmNews(wmNews, (short) 3, "存在疑似违规内容");
                flag = false;
            }
        }
        return flag;
    }

    /**
     * 将List<byte[]> --> byte[]
     * @param imageContent
     * @return
     */
    private static byte[] byteArrayList2ByteArray(List<byte[]> imageContent) {
        int totalLength = 0;
        // 计算所有字节数组的总长度
        for (byte[] byteArray : imageContent) {
            totalLength += byteArray.length;
        }
        // 创建一个新的字节数组，用于保存所有字节数组的内容
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        // 将所有字节数组拷贝到结果数组中
        for (byte[] byteArray : imageContent) {
            System.arraycopy(byteArray, 0, result, currentIndex, byteArray.length);
            currentIndex += byteArray.length;
        }
        return result;
    }

    /**
     * 更新文章状态以及原因
     * @param wmNews
     * @param stauts
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, Short stauts, String reason) {
        wmNews.setStatus(stauts);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    @Autowired
    private TextScan textScan;
    /**
     * 审核内容文本
     * @param content
     * @param wmNews
     * @return
     */
    private boolean scanContentText(String content, WmNews wmNews) {
        boolean flag = true;
        if(StringUtils.isBlank(content)) return flag;
        Map map = textScan.scanContent(content);
        if(map != null) {
            Integer conclusionType = (Integer)map.get("conclusionType");
            if(conclusionType == 2) { // 不合规
                updateWmNews(wmNews, (short) 2, "存在违规内容");
                flag = false;
            } else if(conclusionType == 3) { // 需要人工审核
                updateWmNews(wmNews, (short) 3, "存在疑似违规内容");
                flag = false;
            }
        }
        return flag;

    }


    /**
     * 提取文章内容文本与图片(需要包含 标题 以及封面图片）
     * @param wmNews
     * @return
     */
    private Map<String, Object> extractTextAndImage(WmNews wmNews) {
        if(StringUtils.isBlank(wmNews.getContent())) return new HashMap<>();
        String content = wmNews.getContent();
        List<Map> maps = JSON.parseArray(content, Map.class);
        StringBuffer stringBuffer = new StringBuffer();
        List<String> images = new ArrayList<>();
        // 内容
        for (Map map : maps) {
            // 文本
            if(map.get("type").equals("text")) {
                stringBuffer.append(map.get("value"));
            }
            // 图片
            if(map.get("type").equals("image")) {
                images.add((String) map.get("value"));
            }
        }
        // 封面和标题
        stringBuffer.append(wmNews.getTitle());
        if(wmNews.getImages() != null) {
            images.addAll(Arrays.asList(wmNews.getImages().split(",")));
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("content", stringBuffer.toString());
        returnMap.put("images", images);
        return returnMap;
    }
}
