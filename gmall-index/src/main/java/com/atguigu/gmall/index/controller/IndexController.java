package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryLvl1Cates(){

        List<CategoryEntity> lvl1Cates = this.indexService.queryLvl1Cates();

        return Resp.ok(lvl1Cates);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<CategoryVO>> queryCatesByPid(@PathVariable("pid")Long pid){
        List<CategoryVO> cates = indexService.queryCatesByPid(pid);

        return Resp.ok(cates);
    }

    @GetMapping("test/lock")
    public Resp<Object> testLock(){

        this.indexService.testLock();

        return Resp.ok(null);
    }

    @GetMapping("test/read")
    public Resp<Object> testread(){

        String msg = this.indexService.testread();

        return Resp.ok(msg);
    }
    @GetMapping("test/write")
    public Resp<Object> testwrite(){

        String msg = this.indexService.testwrite();

        return Resp.ok(msg);
    }

    @GetMapping("test/latch")
    public Resp<Object> testlatch() throws InterruptedException {

        String msg = this.indexService.testlatch();

        return Resp.ok(msg);
    }
    @GetMapping("test/out")
    public Resp<Object> testout(){

        String msg = this.indexService.testout();

        return Resp.ok(msg);
    }

}
