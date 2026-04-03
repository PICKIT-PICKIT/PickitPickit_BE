package PickitPickit.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PickitPickit API")
                        .description("인형뽑기 & 가챠샵 정보 서비스 백엔드 API 명세서")
                        .version("v1.0.0")
                );
    }
}
