package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SpuAttributeValueVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Test
    public void test11(){
        System.out.println(this.gmallWmsClient.queryWareSkuBySkuId(49l));
    }

    @Test
    public void importData() {


        Long pageNum = 1l;
        Long pageSize = 100l;

        do {
            // 构建分页查询条件
            QueryCondition condition = new QueryCondition();
            condition.setPage(pageNum);
            condition.setLimit(pageSize);
            // 执行分页查询
            Resp<List<SpuInfoEntity>> pageVoResp = this.gmallPmsClient.querySpuByPageAndSale(condition);
            List<SpuInfoEntity> spuInfoEntities = pageVoResp.getData();

            // 如果当前页的数据为空，直接退出方法
            if (CollectionUtils.isEmpty(spuInfoEntities)) {
                return;
            }

            spuInfoEntities.forEach(spuInfoEntity -> {
                Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (!CollectionUtils.isEmpty(skuInfoEntities)) {

                    skuInfoEntities.forEach(skuInfoEntity -> {
                        GoodsVO goodsVO = new GoodsVO();
                        // sku的基本信息
                        goodsVO.setId(skuInfoEntity.getSkuId());
                        goodsVO.setName(skuInfoEntity.getSkuTitle());
                        goodsVO.setSort(0);
                        goodsVO.setSale(0);
                        goodsVO.setPrice(skuInfoEntity.getPrice());
                        goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());

                        // 品牌相关信息
                        Resp<BrandEntity> brandResp = this.gmallPmsClient.brandInfo(skuInfoEntity.getBrandId());
                        BrandEntity brandEntity = brandResp.getData();
                        goodsVO.setBrandId(brandEntity.getBrandId());
                        goodsVO.setBrandName(brandEntity.getName());

                        // 分类相关信息
                        Resp<CategoryEntity> categoryResp = this.gmallPmsClient.categoryInfo(skuInfoEntity.getCatalogId());
                        CategoryEntity categoryEntity = categoryResp.getData();
                        goodsVO.setProductCategoryId(categoryEntity.getCatId());
                        goodsVO.setProductCategoryName(categoryEntity.getName());

                        // 库存信息
                        Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                        List<WareSkuEntity> wareSkuEntities = wareResp.getData();
                        goodsVO.setStock(0l);
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            wareSkuEntities.forEach(wareSkuEntity -> {
                                // 只要有一个仓库有该sku的库存，就可以搜索出来
                                if (wareSkuEntity.getStock() > 0) {
                                    goodsVO.setStock(100l);
                                }
                            });
                        }

                        // 设置搜索属性
                        Resp<List<ProductAttrValueEntity>> attrResp = this.gmallPmsClient.queryBySpuId(spuInfoEntity.getId());
                        List<ProductAttrValueEntity> productAttrValueEntities = attrResp.getData();
                        if (!CollectionUtils.isEmpty(productAttrValueEntities)) {
                            List<SpuAttributeValueVO> attrValueList = productAttrValueEntities.stream().map(productAttrValueEntity -> {
                                SpuAttributeValueVO spuAttributeValueVO = new SpuAttributeValueVO();
                                spuAttributeValueVO.setSpuId(productAttrValueEntity.getSpuId());
                                spuAttributeValueVO.setValue(productAttrValueEntity.getAttrValue());
                                spuAttributeValueVO.setName(productAttrValueEntity.getAttrName());
                                spuAttributeValueVO.setId(productAttrValueEntity.getId());
                                spuAttributeValueVO.setAttrId(productAttrValueEntity.getAttrId());
                                return spuAttributeValueVO;
                            }).collect(Collectors.toList());
                            goodsVO.setAttrValueList(attrValueList);
                        }

                        Index action = new Index.Builder(goodsVO).index("goods1").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                        try {
                            this.jestClient.execute(action);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            // 获取当前的记录数，直到记录数不为100
            pageSize = (long) spuInfoEntities.size();
            pageNum++; // 下一页
        } while (pageSize == 100); // 只要当前页的记录数还有100条，就继续遍历

    }


    @Test
    public void contextLoads() throws IOException {

        Index index = new Index.Builder(new User("li4", 21, "123456")).index("user").type("info").id("2").build();

        DocumentResult result = this.jestClient.execute(index);
        System.out.println(result.toString());
    }

    @Test
    public void test() throws IOException {
        User user = new User();
        user.setAge(40);
        Map<String, User> map = new HashMap<>();
        map.put("doc", user);

        Update action = new Update.Builder(map).index("user").type("info").id("1").build();

        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result);
    }

    @Test
    public void testQuery() throws IOException {

        Get action = new Get.Builder("user", "1").build();
        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result.getSourceAsObject(User.class).toString());
    }

    @Test
    public void testSearch() throws IOException {

        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"match_all\": {}\n" +
                "  }\n" +
                "}";

        Search action = new Search.Builder(query).addIndex("user").addType("info").build();
        SearchResult result = this.jestClient.execute(action);
        result.getSourceAsObjectList(User.class, false).forEach(System.out::println);
        result.getHits(User.class).forEach(hit -> {
            System.out.println(hit.source);
        });
    }

    @Test
    public void testDelete() throws IOException {

        Delete action = new Delete.Builder("3").index("user").type("info").build();
        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result.toString());
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private String name;
    private Integer age;
    private String password;
}
