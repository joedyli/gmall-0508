package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.Highlighter;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    @Override
    public SearchResponse search(SearchParamVO searchParamVO) throws IOException {

        String query = buildDslQuery(searchParamVO);
        Search action = new Search.Builder(query).addIndex("goods").addType("info").build();
        SearchResult searchResult = this.jestClient.execute(action);
        System.out.println(searchResult);
        SearchResponse searchResponse = parseSearchResult(searchResult);

        // 分页条件，查询条件中具有的属性
        searchResponse.setPageNum(searchParamVO.getPageNum());
        searchResponse.setPageSize(searchParamVO.getPageSize());

        return searchResponse;
    }

    private SearchResponse parseSearchResult(SearchResult searchResult) {
        SearchResponse response = new SearchResponse();
        // 设置总记录数
        response.setTotal(searchResult.getTotal());

        // 当前页的所有记录
        List<SearchResult.Hit<GoodsVO, Void>> hits = searchResult.getHits(GoodsVO.class);
        List<GoodsVO> goodsVOS = hits.stream().map(hit -> {
            GoodsVO goodsVO = hit.source;
            goodsVO.setName(hit.highlight.get("name").get(0));
            return goodsVO;
        }).collect(Collectors.toList());
        response.setProducts(goodsVOS);


        MetricAggregation aggregations = searchResult.getAggregations(); // 获取聚合结果集
        // 品牌
        SearchResponseAttrVO brandVO = new SearchResponseAttrVO();
        brandVO.setProductAttributeId(null);
        brandVO.setName("品牌"); // 页面显示内容

        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandId"); // 从聚合结果集中获取品牌的聚合
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();// 获取聚合中的所有桶

        List<String> brands = buckets.stream().map(bucket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", Long.valueOf(bucket.getKeyAsString()));
            TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandName");
            String name = brandNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", name);
            String brand = JSON.toJSONString(map);
            return brand;
        }).collect(Collectors.toList());
        brandVO.setValue(brands);
        response.setBrand(brandVO);

        // 分类
        SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
        categoryVO.setProductAttributeId(null);
        categoryVO.setName("分类"); // 页面显示内容

        TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryId"); // 从聚合结果集中获取分类的聚合
        List<TermsAggregation.Entry> categoryBuckets = categoryAgg.getBuckets();// 获取聚合中的所有桶

        List<String> categories = categoryBuckets.stream().map(bucket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", Long.valueOf(bucket.getKeyAsString()));
            TermsAggregation categoryNameAgg = bucket.getTermsAggregation("categoryName");
            String name = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", name);
            String category = JSON.toJSONString(map);
            return category;
        }).collect(Collectors.toList());
        categoryVO.setValue(categories);
        response.setCatelog(categoryVO);

        // 规格参数
        ChildrenAggregation aggregation = aggregations.getChildrenAggregation("attrAgg"); // 获取嵌套聚合
        TermsAggregation attrIdAgg = aggregation.getTermsAggregation("attrId"); // 获取attrId聚合
        List<TermsAggregation.Entry> attrBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrVOS = attrBuckets.stream().map(bucket -> {
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId(Long.valueOf(bucket.getKeyAsString())); // 获取attrId聚合中的每个桶的key作为规格属性的id
            TermsAggregation attrNameAgg = bucket.getTermsAggregation("attrName"); // 获取每个attrId聚合的attrName子聚合
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString()); // 获取第一个桶的内容
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValue"); // 获取每个attrId聚合的attrValue子聚合
            List<String> values = attrValueAgg.getBuckets().stream().map(valueBucket -> valueBucket.getKeyAsString()).collect(Collectors.toList());// 获取每个桶中的key
            attrVO.setValue(values);
            return attrVO;
        }).collect(Collectors.toList());

        response.setAttrs(attrVOS);

        return response;
    }

    private String buildDslQuery(SearchParamVO searchParamVO) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1. 构建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isEmpty(keyword)){
            return null;
        }
        // 放查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND));

        // 放过滤条件
        // 放分类过滤
        String[] catelog3 = searchParamVO.getCatelog3();
        if (catelog3 != null && catelog3.length > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("productCategoryId", catelog3));
        }
        // 放品牌
        String[] brands = searchParamVO.getBrand();
        if (brands != null && brands.length > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brands));
        }
        // 放价格
        Integer priceFrom = searchParamVO.getPriceFrom();
        Integer priceTo = searchParamVO.getPriceTo();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        if (priceFrom != null) {
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);

        // 构建规格属性嵌套过滤
        String[] props = searchParamVO.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                // 切割属性过滤条件，以：分割，第一位是id，第二位是value
                String[] attrProps = StringUtils.split(prop, ":");
                if (attrProps != null && attrProps.length == 2) {
                    // id过滤
                    boolQuery.must(QueryBuilders.termQuery("attrValueList.attrId", attrProps[0]));
                    // value过滤
                    String[] values = StringUtils.split(attrProps[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("attrValueList.value", values));
                }
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrValueList", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        // 2. 构建排序条件
        String order = searchParamVO.getOrder();
        if (StringUtils.isNoneBlank(order)){
            String[] orders = StringUtils.split(order, ":");
            if (orders.length == 2) {
                SortOrder sortOrder = StringUtils.equals(orders[1], "asc") ? SortOrder.ASC : SortOrder.DESC;
                switch (orders[0]){
                    case "0":
                        sourceBuilder.sort("_score", sortOrder);
                    case "1":
                        sourceBuilder.sort("sale", sortOrder);
                    case "2":
                        sourceBuilder.sort("price", sortOrder);
                }
            }
        }

        // 3. 分页
        sourceBuilder.from((searchParamVO.getPageNum() - 1) * searchParamVO.getPageSize());
        sourceBuilder.size(searchParamVO.getPageSize());

        // 4. 构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("name").preTags("<p style='color:red'>").postTags("</p>"));

        // 5. 构建聚合条件
        // 构建品牌的聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandId").field("brandId").subAggregation(AggregationBuilders.terms("brandName").field("brandName"));
        sourceBuilder.aggregation(brandAgg);

        // 分类的聚合
        TermsAggregationBuilder categoryAgg = AggregationBuilders.terms("categoryId").field("productCategoryId").subAggregation(AggregationBuilders.terms("categoryName").field("productCategoryName"));
        sourceBuilder.aggregation(categoryAgg);

        // 规格属性的嵌套聚合
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrValueList")
                .subAggregation(AggregationBuilders.terms("attrId").field("attrValueList.attrId")
                        .subAggregation(AggregationBuilders.terms("attrName").field("attrValueList.name"))
                        .subAggregation(AggregationBuilders.terms("attrValue").field("attrValueList.value")));
        sourceBuilder.aggregation(nestedAggregationBuilder);

        return sourceBuilder.toString();
    }

    public static void main(String[] args) throws IOException {
        SearchServiceImpl searchService = new SearchServiceImpl();
        SearchParamVO searchParamVO = new SearchParamVO();
        searchParamVO.setKeyword("尚硅谷");
        searchParamVO.setBrand(new String[]{"2"});
        searchParamVO.setCatelog3(new String[]{"225"});
        searchParamVO.setPriceFrom(2000);
        searchParamVO.setPriceTo(3000);
        searchParamVO.setProps(new String[]{"25:23231", "33:3000"});
        searchParamVO.setOrder("2:desc");
        searchParamVO.setPageNum(2);
        System.out.println(searchService.buildDslQuery(searchParamVO));
//        searchService.search(searchParamVO);
    }

}
