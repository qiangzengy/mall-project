package com.qiangzengy.mall.order.web;

import com.qiangzengy.mall.order.service.OrderService;
import com.qiangzengy.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * 结算
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model){

      OrderConfirmVo confirmVo= orderService.confirmOrder();
      model.addAttribute("orderConfirmData",confirmVo);

        //展示订单信息
        return "confirm";
    }
}
