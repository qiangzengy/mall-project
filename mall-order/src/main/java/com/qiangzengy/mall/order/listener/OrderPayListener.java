package com.qiangzengy.mall.order.listener;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */

import com.qiangzengy.mall.order.vo.PayAsyncVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 支付宝支付成功异步回调
 */

@RestController
@RequestMapping("/order/pay")
public class OrderPayListener {

    @RequestMapping("/aliPayCallback")
    public String aliPayCallback(@RequestBody PayAsyncVo payAsyncVo){
        return "success";
    }

}
