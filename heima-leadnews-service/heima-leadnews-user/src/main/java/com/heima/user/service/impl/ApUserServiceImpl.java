package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.vos.ApUserVo;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.MD5Utils;
import com.heima.utils.threadlocal.AppThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    /**
     * 登陆
     * @param loginDto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto loginDto) {
        // 登陆成功（根据客户id 生产jwt令牌
        if(StringUtils.isNotBlank(loginDto.getPhone()) && StringUtils.isNotBlank(loginDto.getPassword())) {
            ApUser user = getOne(Wrappers.lambdaQuery(ApUser.class).eq(ApUser::getPhone, loginDto.getPhone()));
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
