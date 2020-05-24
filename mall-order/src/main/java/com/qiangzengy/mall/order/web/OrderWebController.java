package com.qiangzengy.mall.order.web;

import com.qiangzengy.mall.order.service.OrderService;
import com.qiangzengy.mall.order.vo.OrderConfirmVo;
import com.qiangzengy.mall.order.vo.OrderSubmitRespVo;
import com.qiangzengy.mall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * 结算
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

      OrderConfirmVo confirmVo= orderService.confirmOrder();
      model.addAttribute("orderConfirmData",confirmVo);
        //展示订单信息
        return "confirm";
    }


    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderVo,Model model){
        //下单：验证令牌->创建订单->校验价格->锁库存
        //下单成功：去支付页面，选择支付类型
        //下单失败：回到订单确认页重新确认订单信息

        OrderSubmitRespVo respVo=orderService.submitOrder(orderVo);
        if (respVo.getCode()==0){
            model.addAttribute("orderSubmitResp",respVo);
            return "pay";

        }
        return "redirect:http://order.gulimall.com/toTrade";

    }
}
