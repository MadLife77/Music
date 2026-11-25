package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件到 Minio
     *
     * @param file   文件
     * @param folder 文件夹
     * @return 可访问的 URL
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成唯一文件名
            String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 获取文件流
            InputStream inputStream = file.getInputStream();

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回可访问的 URL
            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + "：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 空 URL 直接跳过
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                return;
            }
            // 解析 URL，获取文件路径（兼容 localhost 与 127.0.0.1 两种端点写法）
            String filePath = fileUrl;
            String prefix = endpoint + "/" + bucketName + "/";
            if (filePath.startsWith(prefix)) {
                filePath = filePath.substring(prefix.length());
            } else {
                int idx = filePath.indexOf("/" + bucketName + "/");
                if (idx >= 0) {
                    filePath = filePath.substring(idx + bucketName.length() + 2);
                }
            }
            if (filePath.trim().isEmpty()) {
                return;
            }

            // 容错：先判断对象是否存在，不存在则视为已删除，直接返回。
            // 这样当数据库里残留坏链（桶中对象缺失）时，上传新头像/封面仍能正常进行，
            // 不会因删除一个不存在的旧对象而抛出异常导致整个上传接口失败。
            try {
                minioClient.statObject(
                        io.minio.StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .build()
                );
            } catch (io.minio.errors.ErrorResponseException ere) {
                if ("NoSuchKey".equals(ere.errorResponse().code())
                        || "NoSuchBucket".equals(ere.errorResponse().code())) {
                    return;
                }
                throw ere;
            }

            // 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );

        } catch (Exception e) {
            // 删除失败仅记录日志，不阻断上传/更新主流程
            System.err.println("[WARN] deleteFile ignored error for " + fileUrl + ": " + e.getMessage());
        }
    }
}
