package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    @GetMapping("ums/member/query")
    public Resp<MemberEntity> query(@RequestParam("username")String username, @RequestParam("password") String password) throws IllegalAccessException;

    @GetMapping("ums/memberreceiveaddress/{userId}")
    public Resp<List<MemberReceiveAddressEntity>> queryAddressByUserid(@PathVariable("userId")Long userId);

    @GetMapping("ums/member/info/{id}")
    public Resp<MemberEntity> info(@PathVariable("id") Long id);
}
