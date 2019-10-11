package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponse implements Serializable {

    //品牌 此时vo对象中的id字段保留（不用写） name就是“品牌” value: [{id:100,name:华为},{id:101,name:小米}]
    private SearchResponseAttrVO brand;
    private SearchResponseAttrVO catelog;//分类
    //所有商品的顶头显示的筛选属性
    private List<SearchResponseAttrVO> attrs = new ArrayList<>();

    //检索出来的商品信息
    private List<GoodsVO> products = new ArrayList<>();

    private Long total;//总记录数
    private Integer pageSize;//每页显示的内容
    private Integer pageNum;//当前页面


}
