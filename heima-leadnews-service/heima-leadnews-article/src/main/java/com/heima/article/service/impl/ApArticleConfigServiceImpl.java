package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    /**
     * 根据map更新ApArticleConfig
     * @param map
     */
    @Override
    @Transactional
    public void updateByMap(Map map) {
        if(map == null) {
            return;
        }
        Object enable = map.get("enable");
        boolean isDown = true;
        if(enable.equals(0)){
            isDown = false;
        }
        update(Wrappers.<ApArticleConfig>lambdaUpdate().set(ApArticleConfig::getIsDown, isDown)
                            .eq(ApArticleConfig::getArticleId, map.get("articleId")));
    }
}
