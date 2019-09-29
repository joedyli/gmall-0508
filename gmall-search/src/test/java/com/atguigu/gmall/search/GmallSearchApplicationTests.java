package com.atguigu.gmall.search;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Test
    public void test() throws IOException {

        Index index = new Index.Builder(new User("li4", 29, "123456")).index("user").type("info").id("1").build();

        DocumentResult result = this.jestClient.execute(index);
        System.out.println(result.toString());
    }

    @Test
    public void test2() throws IOException {

        Get action = new Get.Builder("user", "1").build();
        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result.getSourceAsObject(User.class).toString());
    }

    @Test
    public void test3() throws IOException {

        String source = "{\n" +
                "  \"query\": {\n" +
                "    \"match_all\": {}\n" +
                "  }\n" +
                "}";
        Search search = new Search.Builder(source).addIndex("user").addType("info").build();
        SearchResult result = this.jestClient.execute(search);
        System.out.println(result.getSourceAsObjectList(User.class, false).toString());
        result.getHits(User.class).forEach(hit -> System.out.println(hit.source));
    }

    @Test
    public void test4() throws IOException {
        User user = new User("wang5", 20, null);
        Map<String, User> map = new HashMap<>();
        map.put("doc", user);
        Update action = new Update.Builder(map).index("user").type("info").id("1").build();
        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result.toString());
    }

    @Test
    public void test5() throws IOException {

        Delete action = new Delete.Builder("1").index("user").type("info").build();
        DocumentResult result = this.jestClient.execute(action);
        System.out.println(result);
    }

    @Test
    public void contextLoads() {

    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User{
    private String name;
    private Integer age;
    private String password;
}
