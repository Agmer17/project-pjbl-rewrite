package app.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import app.interceptor.AdminInterceptor;
import app.interceptor.JwtAuthenticationInterceptor;
import app.utils.ImageUtils;

@Configuration
public class SpringWebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/user/**", "/live-chat/**", "/live-chat",
                "/admin", "/admin/**");

        registry.addInterceptor(adminInterceptor).addPathPatterns("/admin", "/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(imageUtils.getUploadDir()).toAbsolutePath().normalize();
        String uploadDir = uploadPath.toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");

    }

}
