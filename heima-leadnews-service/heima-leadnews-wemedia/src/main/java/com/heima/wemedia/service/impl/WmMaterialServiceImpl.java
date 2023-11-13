package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.threadlocal.WmThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {

        // 检查参数
        if(multipartFile == null || multipartFile.getSize() == 0L) return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        log.info("curT = {}", Thread.currentThread().getId());

        // 上传到minio
        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String url = null;
        try {
            url = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 保存到数据库
        WmUser wmUser = WmThreadLocal.getWmUser();
        Integer id = wmUser == null? 0: wmUser.getId();
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(id);
        wmMaterial.setUrl(url);
        wmMaterial.setType((short)0);
        wmMaterial.setIsCollection((short)0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        log.info("上传成功");
        // 返回
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 显示收藏图片列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult listPicture(WmMaterialDto dto) {
        // 验证数据参数
        dto.checkParam();
        // 分页
        IPage iPage = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmMaterial> queryWrapper = new LambdaQueryWrapper<>();
        // 是否收藏
        if(dto.getIsCollection() != null || dto.getIsCollection() == 1) {
            queryWrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        // 当前用户下
        queryWrapper.eq(WmMaterial::getUserId, WmThreadLocal.getWmUser().getId());
        // 按时间降序
        queryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        IPage page = page(iPage, queryWrapper);
        // 返回数据
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }
}
