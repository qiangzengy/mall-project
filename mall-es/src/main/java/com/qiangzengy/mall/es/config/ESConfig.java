package com.qiangzengy.mall.es.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * elasticsearch：
 * 1。导入依赖
 * 2。编写配置
 */
@Configuration
public class ESConfig {

    @Value("${mall.elasticsearch.ip_address}")
    private String ipAddress;

    public static final RequestOptions COMMON_OPTIONS;

    static {

        /*
        RequestOptions此类包含应在相同的应用许多请求之间共享的请求的部件。
        您可以创建一个单例实例，并在所有请求之间共享它
         */
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);//添加所有请求所需的任何标头
//        builder.setHttpAsyncResponseConsumerFactory(//自定义响应使用者
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    /*RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http"),
                    new HttpHost("localhost", 9201, "http")));*/
    @Bean
    public RestHighLevelClient instanceES(){

        RestClientBuilder builder;
        builder=RestClient.builder(
                new HttpHost(ipAddress, 9200, "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

}
