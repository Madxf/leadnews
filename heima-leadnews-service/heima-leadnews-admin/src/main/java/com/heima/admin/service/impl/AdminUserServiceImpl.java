package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdminUserMapper;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.admin.dtos.LoginDto;
import com.heima.admin.service.AdminUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@Slf4j
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdUser> implements AdminUserService {
    /**
     * 登陆
     * @param loginDto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto loginDto) {
        // 登陆成功（根据客户id 生产jwt令牌
        if(StringUtils.isNotBlank(loginDto.getName()) && StringUtils.isNotBlank(loginDto.getPassword())) {
            AdUser user = getOne(Wrappers.lambdaQuery(AdUser.class).eq(AdUser::getName, loginDto.getName()));
            if(user == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST, "用户不存在");
            }
            // 验证密码（采用MD5 + 盐 机制进行加密
            String password = user.getPassword();
            String salt = user.getSalt();
            String pwd = MD5Utils.encode(loginDto.getPassword() + salt);
            if(!pwd.equals(password)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR, "密码错误");
            }
            // 生产jwt令牌并返回
            String token = AppJwtUtil.getToken(user.getId().longValue());
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("token", token);
            user.setSalt("");
            user.setPassword("");
            map.put("user", user);

            // 返回结果
            return ResponseResult.okResult(map);
        } else {
            // 登陆失败（转游客登陆，直接根据 0L 生产jwt令牌
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }

}
