package com.qiangzengy.mall.product;

import com.qiangzengy.mall.product.entity.BrandEntity;
import com.qiangzengy.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
   public void contextLoads() {

        BrandEntity entity=new BrandEntity();
        entity.setName("123");
        brandService.save(entity);
        System.out.println("成功");

    }

}
