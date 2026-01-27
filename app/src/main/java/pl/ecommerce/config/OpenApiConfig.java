//Dokumentacja swagger
package pl.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecommerce Platform API")
                        .description("Dokumentacja API dla projektu sklepu internetowego")
                        .version("v1.0"));
    }
}