package com.heima.search.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.dtos.HistorySearchDto;
import com.heima.search.service.ApUserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/history")
@Slf4j
public class ApUserSearchController {

    @Autowired
    private ApUserSearchService apUserSearchService;

    @PostMapping("/load")
    public ResponseResult findUserSearch() {
        log.info("load");
        return apUserSearchService.findSearch();
    }

    @PostMapping("/del")
    public ResponseResult delUserSearch(@RequestBody HistorySearchDto dto) {
        log.info("del");
        return apUserSearchService.delUserSearch(dto);
    }
}
