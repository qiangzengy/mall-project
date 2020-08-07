package com.qiangzengy.mall.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qiangzengy.mall.es.config.ESConfig;
import lombok.Data;
import lombok.ToString;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MallEsApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 测试存储数据
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-search.html
     * @throws IOException
     */
    @Test
    void createDataEs() throws IOException {

        System.out.println("====="+restHighLevelClient);

        //1。创建IndexRequest对象
        IndexRequest request = new IndexRequest("posts");
        request.id("1");
        User user=new User();
        user.setName("张三");
        user.setAge(18);
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        //request.source("name","zhangsan","age",26);
        //同步
        IndexResponse index = restHighLevelClient.index(request, ESConfig.COMMON_OPTIONS);
        /*ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {

            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        //异步
        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, listener);
*/

        System.out.println("data====>"+index);
    }

    @Data
    class User{
        private String name;
        private Integer age;
    }

    /**
     * 测试检索请求
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-search.html
     */
    @Test
    void serachData() throws IOException {


        //1。创建SeachRequest
        SearchRequest searchRequest = new SearchRequest();
        //2。指定索引
        searchRequest.indices("bank");
        //大多数搜索参数已添加到中SearchSourceBuilder
        //3。指定检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //3。1构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","Holmes Lane"));
        //3.2 聚合 年龄
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("ageAgg")
                .field("age").size(10);
        searchSourceBuilder.aggregation(aggregation);
        //薪资
        AvgAggregationBuilder avg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(avg);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //4。将添加SearchSourceBuilder到中SeachRequest。
        searchRequest.source(searchSourceBuilder);
        System.out.println("searchSourceBuilder="+searchSourceBuilder.toString());

        //5。执行
        SearchResponse search = restHighLevelClient.search(searchRequest, ESConfig.COMMON_OPTIONS);

        //6。分析结果
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit data : hits1) {
            String sourceAsString = data.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("account data:"+account);


        }
        System.out.println("search data ="+search.toString());

    }

    @Data
    @ToString
    static class Account{
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }


}
