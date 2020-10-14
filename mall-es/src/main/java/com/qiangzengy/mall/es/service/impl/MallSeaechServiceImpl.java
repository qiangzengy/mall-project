package com.qiangzengy.mall.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.to.es.SkuEsModel;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.es.config.ESConfig;
import com.qiangzengy.mall.es.constant.EsConstant;
import com.qiangzengy.mall.es.feign.ProductFeignService;
import com.qiangzengy.mall.es.service.MallSearchService;
import com.qiangzengy.mall.es.vo.AttrResponseVo;
import com.qiangzengy.mall.es.vo.SearchParam;
import com.qiangzengy.mall.es.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MallSeaechServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {

        // 1。构建查询的DSL语句
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 2。执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, ESConfig.COMMON_OPTIONS);
            // 3。将响应数据分装成指定结果
            SearchResult result = buildSearchResult(response, searchParam);
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
    private SearchResult buildSearchResult(SearchResponse response,SearchParam searchParam) {
        SearchResult result = new SearchResult();
        // 返回的所有结果
        SearchHits hits = response.getHits();
        List<SkuEsModel> list = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(source, SkuEsModel.class);
                if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                list.add(esModel);
            }
        }
        result.setModels(list);
        Aggregations aggregations = response.getAggregations();
        // set分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(Long.valueOf(bucket.getKeyAsString()));
            ParsedLongTerms catalog_agg_name = bucket.getAggregations().get("catalog_agg_name");
            if (Objects.nonNull(catalog_agg_name)){
                List<? extends Terms.Bucket> buckets1 = catalog_agg_name.getBuckets();
                catalogVo.setCatalogName(buckets1.get(0).getKeyAsString());
            }
            catalogVos.add(catalogVo);
        }
        // set品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brand_aggBuckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brand_aggBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(Long.valueOf(bucket.getKeyAsString()));
            // 图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            // 名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandImg(brand_name_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }

        // set属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attr_id_aggBuckets = attr_id_agg.getBuckets();
        for (Terms.Bucket attr_id_aggBucket : attr_id_aggBuckets) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //id
            String attr_id = attr_id_aggBucket.getKeyAsString();
            attrVo.setAttrId(Long.parseLong(attr_id));
            //名字
            ParsedStringTerms attr_name_agg = attr_id_aggBucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //value 可能存在多个
            ParsedStringTerms attr_value_agg = attr_id_aggBucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }

        //总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //总页码
        int pageNums = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : (int) (total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(pageNums);
        //当前页码
        result.setPageNum(searchParam.getPageNum());

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i < pageNums; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = searchParam.getAttrs().stream().map(item -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] arry = item.split("_");
                navVo.setNavValue(arry[1]);
                R r = productFeignService.attrInfo(Long.parseLong(arry[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo rData = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(rData.getAttrName());
                } else {
                    navVo.setNavName(" ");
                }
                String encode = null;
                try {
                    encode = URLEncoder.encode(item, "UTF-8");
                    encode = encode.replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = searchParam.getQueryUrl().replace("&attrs=" + encode, "");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }

        return result;
    }

    /**
     * 构建搜索条件结果
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        // 构建检索条件
        /**
         * 1。查询模糊匹配，过滤（属性、分类、价格区间、品牌、库存）
         * 2。排序、分页、高亮
         * 3。聚合分析
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1。must->模糊匹配
        if (StringUtils.isNotEmpty(searchParam.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        // 1.2。bool -> filter
        // 三级分类id
        if (searchParam.getCatelog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatelog3Id()));
        }
        // 名牌
        if(searchParam.getBrandId()!=null&&searchParam.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }

        // 库存
        boolQuery.filter(QueryBuilders.termsQuery("hasStock",searchParam.getHasStock()==1));

        // 属性
        if(searchParam.getAttrs()!=null&&searchParam.getAttrs().size()>0){
            // attrs=1_5寸:8寸&attrs=2_16G:32G
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder boolQuery2=QueryBuilders.boolQuery();
                // attr=1_5寸:8寸
                String[] arr=attr.split("_");
                String attrId=arr[0];
                String [] attrV=arr[1].split(":");
                boolQuery2.must(QueryBuilders.termsQuery("attrs.attrId",attrId));
                boolQuery2.must(QueryBuilders.termsQuery("attrs.attrValue",attrV));
                // 每一个都要查询一遍
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery2, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        // 价格区间
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
        // 查询条件
        searchSourceBuilder.query(boolQuery);
        // 2.1。排序
        if (StringUtils.isNotEmpty(searchParam.getSort())){
            //sort=hotScore_asc/desc
            String sort=searchParam.getSort();
            String [] arr=sort.split("_");
            searchSourceBuilder.sort(arr[0], arr[1].equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }
        // 2.2。分页
        int from=(searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 2.3。高亮
        if(StringUtils.isNotEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color : red'>");
            highlightBuilder.preTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // 聚合分析
        // 3.1品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(10);
        // 子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        // 3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(10);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);
        // 3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 子聚合,id
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        // 子子聚合,name
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // value
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);
       return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);
    }
}
