package com.qiangzengy.mall.cart.interceptor;

import com.qiangzengy.common.constant.AuthConstant;
import com.qiangzengy.common.constant.CartConstant;
import com.qiangzengy.common.vo.MemberRespVo;
import com.qiangzengy.mall.cart.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 需要添加拦截器，在MallWebConfig里面实现的
 * 拦截器作用：
 * 在执行目标方法之前，判断用户登陆状态，并封装传递给controller目标
 */
public class CartInterceptor implements HandlerInterceptor {

    //ThreadLocal保存的是UserInfoTo的数据
    public static ThreadLocal<UserInfoTo> threadLocal=new ThreadLocal<>();

    /**
     * 重写preHandle() 方法，在目标方法执行之前拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();
        //1。获取session
        HttpSession session = request.getSession();
        //2.从session获取当前登陆的用户
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (member!=null){
            //用户登陆
            userInfoTo.setUserId(member.getId());
        }

        //获取user-key，从cookie中获取
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
            //没有临时用户
            if (StringUtils.isEmpty(userInfoTo.getUserKey())){
                String uuid= UUID.randomUUID().toString();
                userInfoTo.setUserKey(uuid);
            }
        }
        //在目标方法执行之前，将userInfoTo数据放入threadLocal里面
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 在业务执行之后，需要让浏览器保存cookie
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_EX_TIME);
            response.addCookie(cookie);
        }

    }
}
