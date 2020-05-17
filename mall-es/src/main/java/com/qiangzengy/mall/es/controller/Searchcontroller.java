package com.qiangzengy.mall.es.controller;

import com.qiangzengy.mall.es.service.MallSearchService;
import com.qiangzengy.mall.es.vo.SearchParam;
import com.qiangzengy.mall.es.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Searchcontroller {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        searchParam.setQueryUrl(queryString);
        SearchResult result =mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
