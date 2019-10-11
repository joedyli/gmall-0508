package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVO {

    private String groupName;

    private List<ProductAttrValueEntity> attrs;
}
