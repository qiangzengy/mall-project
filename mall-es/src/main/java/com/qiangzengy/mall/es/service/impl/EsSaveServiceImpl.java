package com.qiangzengy.mall.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.qiangzengy.common.to.es.SkuEsModel;
import com.qiangzengy.mall.es.config.ESConfig;
import com.qiangzengy.mall.es.constant.EsConstant;
import com.qiangzengy.mall.es.service.EsSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EsSaveServiceImpl implements EsSaveService {

    @Resource
    private RestHighLevelClient highLevelClient;

    //将数据保存到es中
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        //1。建立索引
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexResponse = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexResponse.id(String.valueOf(skuEsModel.getSkuId()));
            String jsonString = JSON.toJSONString(skuEsModel);
            indexResponse.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexResponse);
        }
        BulkResponse bulk = highLevelClient.bulk(bulkRequest, ESConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();//如果失败，返回true
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
        log.info("商品上架信：{},上架状态：{}", collect, !b);
        return b;
    }




}
