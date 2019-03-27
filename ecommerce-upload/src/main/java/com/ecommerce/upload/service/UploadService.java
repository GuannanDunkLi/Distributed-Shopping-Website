package com.ecommerce.upload.service;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.upload.config.UploadProperties;
import com.ecommerce.upload.web.UploadController;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties uploadProperties;

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public String upload(MultipartFile file) {
        try {
            // 1、图片信息校验
            // 1)校验文件类型
            String type = file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(type)) {
                logger.info("上传失败，文件类型不匹配：{}", type);
                throw new EException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2)校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                logger.info("上传失败，文件内容不符合要求");
                throw new EException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2、保存图片
            // 2.1 上传到FDS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            // 2.2、拼接图片地址
            String url = uploadProperties.getBaseUrl() + storePath.getFullPath();
            // 2.3、返回路径
            return url;
        } catch (Exception e) {
            logger.error("上传文件失败!", e);
            throw new EException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}