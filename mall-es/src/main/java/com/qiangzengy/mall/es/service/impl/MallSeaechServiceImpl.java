package com.qiangzengy.mall.es.service.impl;

import com.qiangzengy.mall.es.config.ESConfig;
import com.qiangzengy.mall.es.constant.EsConstant;
import com.qiangzengy.mall.es.service.MallSearchService;
import com.qiangzengy.mall.es.vo.SearchParam;
import com.qiangzengy.mall.es.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MallSeaechServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {

        //1。构建查询的DSL语句
        SearchRequest searchRequest=buildSearchRequest(searchParam);
        try {
            //2。执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, ESConfig.COMMON_OPTIONS);
            //3。将响应数据分装成指定结果
            SearchResult result=buildSearchResult(response);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 结果数据的封装
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response) {

        return null;
    }

    /**
     * 构建搜索条件结果
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //构建检索条件
        /**
         * 1。查询模糊匹配，过滤（属性、分类、价格区间、品牌、库存）
         * 2。排序、分页、高亮
         * 3。聚合分析
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1。must->模糊匹配
        if (StringUtils.isNotEmpty(searchParam.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        //1.2。bool -> filter
        //三级分类id
        if (searchParam.getCatelog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatelog3Id()));
        }
        //名牌
        if(searchParam.getBrandId()!=null&&searchParam.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }

        //库存
        boolQuery.filter(QueryBuilders.termsQuery("hasStock",searchParam.getHasStock()==1));

        //属性
        if(searchParam.getAttrs()!=null&&searchParam.getAttrs().size()>0){
            //attrs=1_5寸:8寸&attrs=2_16G:32G
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder boolQuery2=QueryBuilders.boolQuery();
                //attr=1_5寸:8寸
                String[] arr=attr.split("_");
                String attrId=arr[0];
                String [] attrV=arr[1].split(":");
                boolQuery2.must(QueryBuilders.termsQuery("attrs.attrId",attrId));
                boolQuery2.must(QueryBuilders.termsQuery("attrs.attrValue",attrV));
                //每一个都要查询一遍
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery2, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //价格区间
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())){

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String price=searchParam.getSkuPrice();

            String [] arry=price.split("_");
            if(arry.length==2){
                rangeQuery.gte(arry[0]).lte(arry[1]);
            }else if(arry.length==1){
                if (price.startsWith("_")){
                    rangeQuery.lte(arry[0]);
                }
                if (price.endsWith("_")) {
                    rangeQuery.gte(arry[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //查询条件
        searchSourceBuilder.query(boolQuery);

        //2.1。排序
        if (StringUtils.isNotEmpty(searchParam.getSort())){
            //sort=hotScore_asc/desc
            String sort=searchParam.getSort();
            String [] arr=sort.split("_");
            searchSourceBuilder.sort(arr[0], arr[1].equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }
        //2.2。分页
        int from=(searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3。高亮
        if(StringUtils.isNotEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color : red'>");
            highlightBuilder.preTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        //聚合分析
        //3.1品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(10);
        //子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        //3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(10);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);
        //3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //子聚合,id
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        //子子聚合,name
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //value
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));

        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);
        new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);
        return null;
    }
}
