package com.qiangzengy.mall.product.web;

import com.qiangzengy.mall.product.entity.CategoryEntity;
import com.qiangzengy.mall.product.entity.vo.Catalog2Vo;
import com.qiangzengy.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){


        //1.查出所有的1级分类
        List<CategoryEntity> categoryEntities=categoryService.getLevel1Category();

        model.addAttribute("categorys",categoryEntities);


        return "index";
    }


    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        Map<String,List<Catalog2Vo>> catelogMap=categoryService.getCatalogJson();
        return catelogMap;

    }

}
