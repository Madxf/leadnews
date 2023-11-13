package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.search.dtos.HistorySearchDto;
import org.springframework.scheduling.annotation.Async;

public interface ApUserSearchService {

    /**
     * 保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    void insert(String keyword, Integer userId);

    /**
     * 加载搜索记录
     * @return
     */
    ResponseResult findSearch();

    /**
     * 删除搜索记录
     * @param dto
     * @return
     */
    ResponseResult delUserSearch(HistorySearchDto dto);
}
