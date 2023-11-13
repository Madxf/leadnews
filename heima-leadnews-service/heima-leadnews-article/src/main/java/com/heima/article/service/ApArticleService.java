package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

import java.util.List;

public interface ApArticleService extends IService<ApArticle> {
    /**
     * 加载首页
     * @param dto
     * @return
     */
    ResponseResult load(ArticleHomeDto dto, Short type);

    /**
     * app端保存文章
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);
}
