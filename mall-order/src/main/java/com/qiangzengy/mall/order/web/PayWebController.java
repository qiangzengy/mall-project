package com.qiangzengy.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.qiangzengy.mall.order.config.AlipayTemplate;
import com.qiangzengy.mall.order.service.OrderService;
import com.qiangzengy.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/21
 */

@Controller
public class PayWebController {


    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @GetMapping("/payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn){

        //获取订单的支付信息
        PayVo payVo=orderService.getOrderPay(orderSn);

        //PayVo payVo=new PayVo();
        try {
            alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

}
