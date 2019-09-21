package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan("com.atguigu.gmall.pms.dao")
@EnableSwagger2
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
