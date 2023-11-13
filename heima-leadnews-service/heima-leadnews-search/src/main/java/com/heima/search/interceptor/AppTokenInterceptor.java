package com.heima.search.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.threadlocal.AppThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class AppTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        得到header中的信息
        String userId = request.getHeader("userId");
        log.info("appT userId = {}", userId);
        Optional<String> optional = Optional.ofNullable(userId);
        if(optional.isPresent()){
            //把用户id存入threadloacl中
            ApUser apUser = new ApUser();
            apUser.setId(Integer.valueOf(userId));
            AppThreadLocal.setApUser(apUser);
            log.info("AppTokenFilter设置用户信息到threadlocal中... {}", AppThreadLocal.getApUser());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        AppThreadLocal.clean();
    }
}
