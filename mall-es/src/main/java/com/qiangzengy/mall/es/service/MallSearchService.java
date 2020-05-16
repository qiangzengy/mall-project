package com.qiangzengy.mall.es.service;

import com.qiangzengy.mall.es.vo.SearchParam;
import com.qiangzengy.mall.es.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
