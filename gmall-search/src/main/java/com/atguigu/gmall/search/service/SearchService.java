package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;

import java.io.IOException;

public interface SearchService {

    SearchResponse search(SearchParamVO searchParamVO) throws IOException;
}
