package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class ProductAttrValueVO extends ProductAttrValueEntity {

    private List<Object> valueSelected;

    // 重写setAttrValue，接受valueSelected数据
    public void setAttrValue(List<Object> valueSelected) {

        if (CollectionUtils.isEmpty(valueSelected)){
            return ;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
