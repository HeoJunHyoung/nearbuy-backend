package io.github.junhyoung.nearbuy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** URL 요청 시, file.dir 경로에서 파일을 찾아 제공
        // 'file:' 접두사는 로컬 파일 시스템의 경로임을 명시
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileDir);
    }
}