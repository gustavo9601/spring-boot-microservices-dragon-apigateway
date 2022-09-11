package springbootmicroservicesapigatewaydragon.springbootmicroservicesapigatewaydragon.configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class GatewayConfig {
    /*
    * Especificara las reglas para el gateway
    *
    * */

    @Bean
    // @Profile("default")
    public RouteLocator configLocalNoEureka(RouteLocatorBuilder builder) {
        return builder.routes().build();
    }


}
