package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bouncycastle.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传
 */
@Api(tags = "文件上传控制器")
@RequestMapping("/admin/product")
@RestController
public class FileuploadController {


    @Autowired
    FileUploadService fileUploadService;

    /**
     * 文件上传功能
     * 1、前端把文件流放到哪里了？我们该怎么拿到？
     *     Post请求数据在请求体（包含了文件[流]）
     * 如何接：
     * @RequestParam("file")MultipartFile file
     * @RequestPart("file")MultipartFile file: 专门处理文件的
     *
     * 各种注解接不通位置的请求数据
     * @RequestParam: 无论是什么请求 接请求参数； 用一个Pojo把所有数据都接了
     * @RequestPart： 接请求参数里面的文件项
     * @RequestBody： 接请求体中的所有数据 (json转为pojo)
     * @PathVariable: 接路径上的动态变量
     * @RequestHeader: 获取浏览器发送的请求的请求头中的某些值
     * @CookieValue： 获取浏览器发送的请求的Cookie值
     * - 如果多个就写数据，否则就写单个对象
     *
     *
     * @return
     */
    @ApiOperation(value = "文件上传")
    @PostMapping("/fileUpload")
    public Result fileupload(@RequestPart("file")MultipartFile file) throws Exception {

        //收到前端的文件流，上传给Minio。并返回这个文件在Minio中的存储地址。
        //以后用这个地址访问，数据库保存的也是这个地址
        String url = fileUploadService.upload(file);

        return Result.ok(url);
    }


    /**
     *     <form action="http://localhost:9000/admin/product/reg"
     *             enctype="multipart/form-data" method="post">
     *         头像：<input type="file" name="header" multiple/> <br/>
     *         生活照：<input type="file" name="shz"/> <br/>
     *         身份证：<input type="file" name="sfz"/> <br/>
     *         用户名：<input name="username"/> <br/>
     *         密码：<input name="password"/><br/>
     *         邮箱：<input name="email"/><br/>
     *         爱好： 篮球<input name="ah" type="checkbox" value="篮球"/> ，
     *         足球<input name="ah" type="checkbox" value="足球"/>
     *         <button>注册</button>
     *     </form>
     * @param username
     * @param password
     * @param email
     * @param header
     * @param sfz
     * @param shz
     * @param ah
     * @param cache
     * @param jsessionid
     * @return
     */
    @PostMapping("/reg")
    public Result hahaah(@RequestParam("username")String username,
                         @RequestParam("password")String password,
                         @RequestParam("email")String email,
                         @RequestPart("header")MultipartFile[] header,
                         @RequestPart("sfz")MultipartFile sfz,
                         @RequestPart("shz")MultipartFile shz,
                         @RequestParam("ah")String[] ah,
                         @RequestHeader("Cache-Control") String cache,
                         @CookieValue("jsessionid") String jsessionid){
        //1、用户名，密码，邮箱
        Map<String,Object> result = new HashMap<>();
        result.put("用户名：",username);
        result.put("密码：",password);
        result.put("邮箱：",email);

        result.put("头像文件大小？",header.length);
        result.put("生活照文件大小？",sfz.getSize());
        result.put("身份证文件大小？",shz.getSize());
        result.put("爱好", Arrays.asList(ah));
        result.put("cache",cache);

        return Result.ok(result);
    }
}
