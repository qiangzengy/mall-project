package com.qiangzengy.mall.order.interceptor;

import com.qiangzengy.common.constant.AuthConstant;
import com.qiangzengy.mall.order.vo.MemberResVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResVo> loginUser= new ThreadLocal<>();


    /**
     * 拦截器：
     * 目标请求到达之前做一个前置拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /**
         *  路径匹配/order/order/status/**，就放行，无需登陆
         */
        String uri=request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**",uri);
        if (match){
            return true;
        }

        /**
         * 判断是否登陆，只有登陆了才可以访问
         */

        //1.获取登陆用户
        MemberResVo attribute = (MemberResVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if(attribute!=null){
            //将用户信息set到ThreadLocal中
            loginUser.set(attribute);
            return true;
        }else {
            //没有登陆，就重定向到登陆页面
            request.getSession().setAttribute("msg","请先进行登陆");
            response.sendRedirect("http://auth.gulimall.com");
            return false;
        }
    }
}
