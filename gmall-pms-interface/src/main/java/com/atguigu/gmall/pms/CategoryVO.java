package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {

    private List<CategoryEntity> subs;
}
