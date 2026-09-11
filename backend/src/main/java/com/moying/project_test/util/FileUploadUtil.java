package com.moying.project_test.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 墨莹
 * @date 2026/8/6 13:47
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component // 交给Spring管理，@Value才能注入
public class FileUploadUtil {

    @Value("${image_upload.path}")
    private String uploadPath;

    @Value("${image_upload.access-prefix}")
    private String accessPrefix;

    @Value("${image_upload.domain}")
    private String domain;

    // 允许的图片后缀
    private static final String[] ALLOWED_SUFFIX = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    // 最大文件大小 5MB
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    /**
     * 上传相册图片
     * @param file 文件
     * @return 完整访问URL
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        // 文件大小校验
        if(file.getSize() > MAX_SIZE){
            throw new RuntimeException("文件不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        if(originalName == null){
            throw new RuntimeException("文件名获取失败");
        }

        int dotIndex = originalName.lastIndexOf(".");
        if(dotIndex == -1){
            throw new RuntimeException("文件缺少后缀，不是合法图片");
        }
        String suffix = originalName.substring(dotIndex).toLowerCase();

        // 校验后缀
        boolean ok = false;
        for(String s : ALLOWED_SUFFIX){
            if(s.equals(suffix)){
                ok = true;
                break;
            }
        }
        if(!ok){
            throw new RuntimeException("只允许上传 jpg/png/gif/bmp 图片");
        }

        String fileName = UUID.randomUUID() + suffix;

        File targetDir = new File(uploadPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File targetFile = new File(targetDir, fileName);
        file.transferTo(targetFile);

        // 规范拼接url，避免双斜杠
        return domain + accessPrefix + "/" + fileName;
    }
}
