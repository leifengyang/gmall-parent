package com.atguigu.gmall.product.service;

import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 文件上传
     * @param file   需要上传的文件
     * @return 返回文件在minio中的存储地址
     */
    String upload(MultipartFile file) throws Exception;

}
