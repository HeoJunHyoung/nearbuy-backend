package io.github.junhyoung.nearbuy.global.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * 파일 저장을 위한 인터페이스
 * ㄴ 로컬 저장, S3 등 다양한 저장 방식에 대한 확장성 제공
 */
public interface FileStore {
    String storeFile(MultipartFile multipartFile) throws IOException;

    List<String> storeFiles(List<MultipartFile> multipartFiles) throws IOException;

}