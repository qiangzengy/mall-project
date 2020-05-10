package com.qiangzengy.mall.es.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    @Bean
    public RestHighLevelClient instanceES(){

        RestClientBuilder builder;

        builder=RestClient.builder(
                new HttpHost("192.168.3.42", 9200, "http"));

        RestHighLevelClient client = new RestHighLevelClient(builder);

        return client;

    }

}
