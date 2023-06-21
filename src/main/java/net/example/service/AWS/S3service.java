package net.example.service.AWS;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.example.domain.entity.UserDetailsCustom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3service {

    private final AmazonS3 s3Client;

    @Value("${spring.application.bucket.name}")
    private String bucketName;

    @SneakyThrows
    public void uploadFile(UserDetailsCustom user,
                           String fileName,
                           String extension,
                           MultipartFile multipartFile) {

        var metadata = new ObjectMetadata();
        metadata.setContentType(extension);
        metadata.setContentLength(multipartFile.getSize());

        fileName = buildS3FileName(user, fileName);

        s3Client.putObject(
            bucketName,
            fileName,
            multipartFile.getInputStream(),
            metadata);
    }

    @SneakyThrows
    public S3Object downloadFile(UserDetailsCustom user,
                                 String fileName) {
        return s3Client.getObject(bucketName, buildS3FileName(user, fileName));
    }

    public void deleteFile(UserDetailsCustom user,
                           String fileName) {
        s3Client.deleteObject(bucketName, buildS3FileName(user, fileName));
    }

    public void renameFile(UserDetailsCustom user,
                           String oldName, String newName) {

        var oldS3Name = buildS3FileName(user, oldName);
        var newS3Name = buildS3FileName(user, newName);

        s3Client.copyObject(
            bucketName,
            oldS3Name,
            bucketName,
            newS3Name);

        s3Client.deleteObject(bucketName, oldS3Name);
    }

    private static String buildS3FileName(UserDetailsCustom user, String fileName) {
        return user.getUsername()
            .replaceAll("\\s", "") + "/" + fileName;
    }
}
