package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded cover images từ 2 vị trí:
        // 1. src/main/resources/static/uploads (dev mode)
        // 2. uploads/ (production / JAR mode)
        Path devPath = Paths.get("src/main/resources/static/uploads").toAbsolutePath();
        Path prodPath = Paths.get("uploads").toAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "file:" + devPath + "/",
                    "file:" + prodPath + "/",
                    "classpath:/static/uploads/"
                );
    }
}
