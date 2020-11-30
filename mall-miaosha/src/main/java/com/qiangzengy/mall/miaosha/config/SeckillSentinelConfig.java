package com.qiangzengy.mall.miaosha.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.qiangzengy.common.utils.R;
import org.springframework.context.annotation.Configuration;


/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/30
 */

@Configuration
public class SeckillSentinelConfig {

    public SeckillSentinelConfig() {

        WebCallbackManager.setUrlBlockHandler((httpServletRequest, httpServletResponse, e) -> {
            R error = R.error(50000,"请求流量过大");
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.getWriter().write(JSON.toJSONString(error));
        });

    }
}
