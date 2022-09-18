package springbootmicroservicesapigatewaydragon.springbootmicroservicesapigatewaydragon.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springbootmicroservicesapigatewaydragon.springbootmicroservicesapigatewaydragon.filters.AuthFilterPersonalizado;

@Configuration
public class GatewayConfig {
    /*
     * Especificara las reglas para el gateway
     *
     * */

    @Autowired
    private AuthFilterPersonalizado authFilterPersonalizado;

    @Bean
    @Profile("localhost-NoEureka")
    public RouteLocator configLocalNoEureka(RouteLocatorBuilder builder) {


        // Gateway simple sin Eureka ya que la redireccion se hace a traves de la url
        return builder.routes()
                // Especificando las paths y el redirect al microservicio
                .route(
                        r -> r.path("/api/v1/dragon-ball/**")
                                .uri("http://localhost:8082")
                )
                .route(
                        r -> r.path("/api/v1/gameofthrones/**")
                                .uri("http://localhost:8083")
                )
                .build();
    }

    @Bean
    @Profile("localhost-ConEureka")
    public RouteLocator configLocalEureka(RouteLocatorBuilder builder) {

        return builder.routes()
                // Especificando las paths y el nombre de la app que esta registrado en Eureka
                .route(
                        r -> r.path("/api/v1/dragon-ball/**")
                                .uri("lb://dragon-ball")
                )
                .route(
                        r -> r.path("/api/v1/gameofthrones/**")
                                .uri("lb://game-of-thrones-client2")
                )
                .build();
    }

    @Bean
    @Profile("localhost-ConEureka-CircuitBreaker")
    public RouteLocator configLocalEurekaCB(RouteLocatorBuilder builder) {

        return builder.routes()
                // Especificando las paths y el nombre de la app que esta registrado en Eureka
                .route(
                        r -> r.path("/api/v1/dragon-ball/**")
                                .filters(f -> f.filter(this.authFilterPersonalizado))// Añadimos el filtro personalizado para verificar el token
                                .uri("lb://dragon-ball")
                )
                .route(
                        r -> r.path("/api/v1/gameofthrones/**")
                                // Añadiendo el filtro por si falla que use el failover
                                .filters(f -> {
                                    f.circuitBreaker(
                                            c -> c.setName("failOverGameofthrones")
                                                    .setFallbackUri("forward:/api/v1/fallover/characters")); // Uri que redireccionara si falla

                                    // Añadimos el filtro personalizado para verificar el
                                    f.filter(this.authFilterPersonalizado);
                                    return f;
                                })
                                .uri("lb://game-of-thrones-client2") // Si no falla cargara el microservicio
                )
                .route(
                        r -> r.path("/api/v1/fallover/**")
                                .uri("lb://failover-client3")
                )
                .route( // regitrando la ruta de authorization
                        r -> r.path("/auth/**")
                                .uri("lb://microservice-auth")
                )



                .build();
    }

}
