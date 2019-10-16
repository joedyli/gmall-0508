package com.atguigu.gmall.sms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleVO {

    // 促销类型
    private String type;

    // 促销的描述
    private String desc;
}
