package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌API
 */


@RequestMapping("/admin/product")
@RestController
public class BaseTrademarkController {


    @Autowired
    BaseTrademarkService baseTrademarkService;


    /**
     * 分页查询所有品牌
     * @param pn
     * @param size
     * @return
     */
    @GetMapping("/baseTrademark/{pn}/{size}")
    public Result baseTrademark(@PathVariable("pn")Long pn,
                                @PathVariable("size")Long size){


        //long current, long size
        Page<BaseTrademark> page =new Page<>(pn,size);

        //分页查询（分页信息、查询到的记录集合）
        Page<BaseTrademark> pageResult = baseTrademarkService.page(page);

        return Result.ok(pageResult);
    }


    /**
     * 根据品牌id获取品牌信息
     * @param id
     * @return
     */
    @GetMapping("/baseTrademark/get/{id}")
    public Result getBaseTrademark(@PathVariable("id")Long id){
        BaseTrademark trademark = baseTrademarkService.getById(id);
        return Result.ok(trademark);
    }


    //{"id":2,"tmName":"华为2","logoUrl":"http://39.99.159.121:9000/gmall/1623030108745png"}
    /**
     * 修改品牌
     * @param trademark
     * @return
     */
    @PutMapping("/baseTrademark/update")
    public Result updatebaseTrademark(@RequestBody BaseTrademark trademark){
        baseTrademarkService.updateById(trademark);
        return Result.ok();
    }

    /**
     * 保存品牌 {"tmName":"哈哈啊哈","logoUrl":"/static/default.jpg"}
     */
    @PostMapping("/baseTrademark/save")
    public Result savebaseTrademark(@RequestBody BaseTrademark trademark){
        baseTrademarkService.save(trademark);
        return Result.ok();
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/baseTrademark/remove/{tid}")
    public Result deletebaseTrademark(@PathVariable("tid")Long tid){
        baseTrademarkService.removeById(tid);
        return Result.ok();
    }

    /**
     * 获取所有品牌
     * @return
     */
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> list = baseTrademarkService.list();
        return Result.ok(list);
    }
}
