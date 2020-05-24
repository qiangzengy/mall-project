package com.qiangzengy.mall.order.controller;

import com.qiangzengy.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class IndexController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/{index}.html")
    public String index(@PathVariable("index") String index ){
        return index;

    }

    @GetMapping("/test.html")
    @ResponseBody
    public String testMq(){
        OrderEntity entity=new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",entity);
        return "ok";

    }

}
