package app.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import app.interceptor.JwtAuthenticationInterceptor;
import app.utils.ImageUtils;

@Configuration
public class SpringWebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/user/**", "/live-chat");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(ImageUtils.getUploadDir()).toAbsolutePath().normalize();
        String uploadDir = uploadPath.toString();
        registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");

    }

}
