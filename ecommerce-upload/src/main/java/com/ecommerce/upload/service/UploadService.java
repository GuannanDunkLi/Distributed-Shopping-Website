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
            // 1、check image information
            // 1) Check file type
            String type = file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(type)) {
                logger.info("上传失败，文件类型不匹配：{}", type);
                throw new EException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2) Check content of image
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                logger.info("上传失败，文件内容不符合要求");
                throw new EException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2、store image
            // 2.1 upload to FDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            // 2.2、 url
            String url = uploadProperties.getBaseUrl() + storePath.getFullPath();
            return url;
        } catch (Exception e) {
            logger.error("upload fail!", e);
            throw new EException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}