package com.atguigu.gmall.pms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttrValueVO {

    private Long attrId;

    private String attrName;

    private List<String> values;
}
