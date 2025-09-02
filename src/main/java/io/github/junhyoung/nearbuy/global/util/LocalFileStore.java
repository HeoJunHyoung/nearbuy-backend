package io.github.junhyoung.nearbuy.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class LocalFileStore implements FileStore{

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public String storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);

        File directory = new File(fileDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        // 클라이언트가 접근할 URL 경로를 반환 (전체 경로가 아님)
        return "/images/" + storeFileName;
    }

    @Override
    public List<String> storeFiles(List<MultipartFile> multipartFiles) throws IOException {

        List<String> storeFileResult = new ArrayList<>();

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return storeFileResult;
        }

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile)); // storeFile() 메서드 내부 호출
            }
        }
        return storeFileResult;
    }

    public String getFullPath(String filename) {
        return fileDir + filename.replace("/images/", "");
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

}
