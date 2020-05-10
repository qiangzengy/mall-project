package com.qiangzengy.mall.es;

import com.qiangzengy.mall.es.config.ESConfig;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class MallEsApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;


    //测试向es存储数据
    @Test
    void createDataEs() throws IOException {

        //1。创建IndexRequest对象
        IndexRequest request = new IndexRequest("posts");
        request.id("1");
        request.source("name","zhangsan","age",26);
        IndexResponse response= restHighLevelClient.index(request, ESConfig.COMMON_OPTIONS);
        System.out.println(request);

    }

}
