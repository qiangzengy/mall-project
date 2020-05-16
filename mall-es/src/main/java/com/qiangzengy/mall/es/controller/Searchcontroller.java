package com.qiangzengy.mall.es.controller;

import com.qiangzengy.mall.es.service.MallSearchService;
import com.qiangzengy.mall.es.vo.SearchParam;
import com.qiangzengy.mall.es.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Searchcontroller {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam){
        SearchResult result =mallSearchService.search(searchParam);
        return "list";
    }
}
