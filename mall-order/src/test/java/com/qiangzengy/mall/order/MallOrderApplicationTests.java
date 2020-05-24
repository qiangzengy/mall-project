package com.qiangzengy.mall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallOrderApplicationTests {

    //@Autowired
    //AmqpAdmin amqpAdmin;
    @Test
    void contextLoads() {
        //DirectExchange exchange=new DirectExchange("hello-word",true,false);
        //amqpAdmin.declareExchange(exchange);
    }

}
