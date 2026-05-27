package PickitPickit.global.config;

import PickitPickit.global.security.WithdrawnUserBlockInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final WithdrawnUserBlockInterceptor withdrawnUserBlockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(withdrawnUserBlockInterceptor)
                .addPathPatterns("/api/**");
    }
}