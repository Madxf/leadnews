package com.heima.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.AuthConstants;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApAuthMapper;
import com.heima.user.service.ApAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ApAuthServiceImpl extends ServiceImpl<ApAuthMapper, ApUserRealname> implements ApAuthService {

    /**
     * 用户审核列表分页查询
     * @param authDto
     * @return
     */
    @Override
    public ResponseResult pageList(AuthDto authDto) {
        // 检查参数
        authDto.checkParam();
        // new 分页以及查询对象
        IPage iPage = new Page(authDto.getPage(), authDto.getSize());
        LambdaQueryWrapper<ApUserRealname> queryWrapper = new LambdaQueryWrapper<>();
        // 查询状态
        if(authDto.getStatus() != null) {
            List<Integer> tmp = Arrays.asList(0, 1, 2, 9);
            if(!tmp.contains(authDto.getStatus())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
            }
            queryWrapper.eq(ApUserRealname::getStatus, authDto.getStatus());
        }
        queryWrapper.orderByDesc(ApUserRealname::getCreatedTime);
        // 返回数据
        IPage page = page(iPage, queryWrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(authDto.getPage(), authDto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }


    /**
     * 驳回审核
     * @param authDto
     * @return
     */
    @Override
    public ResponseResult authFail(AuthDto authDto) {
        // 检查参数
        authDto.checkParam();
        PageResponseResult pageResponseResult = new PageResponseResult(authDto.getPage(), authDto.getSize(), 0);
        // 检查id
        if(authDto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        }
        ApUserRealname userRealname =
                getOne(Wrappers.<ApUserRealname>lambdaQuery().eq(ApUserRealname::getId, authDto.getId()));
        // 检查状态(只有待审核的用户才能被驳回审核)
        if(userRealname == null || userRealname.getStatus() == null || userRealname.getStatus() != 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        }
        // 更新状态
        userRealname.setStatus(AuthConstants.SCAN_FAIL);
        updateById(userRealname);
        // 返回参数
        return pageList(authDto);
    }

    /**
     * 通过审核
     * @param authDto
     * @return
     */
    @Override
    public ResponseResult authPass(AuthDto authDto) {
        // 检查参数
        authDto.checkParam();
        PageResponseResult pageResponseResult = new PageResponseResult(authDto.getPage(), authDto.getSize(), 0);
        // 检查id
        if(authDto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname userRealname =
                getOne(Wrappers.<ApUserRealname>lambdaQuery().eq(ApUserRealname::getId, authDto.getId()));
        // 检查状态(只有待审核的用户才能被通过审核)
        if(userRealname == null || userRealname.getStatus() == null || userRealname.getStatus() != 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 更新状态
        userRealname.setStatus(AuthConstants.SCAN_SUCCESS);
        updateById(userRealname);
        // 返回参数
        return pageList(authDto);
    }
}
