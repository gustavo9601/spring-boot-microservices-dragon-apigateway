package springbootmicroservicesapigatewaydragon.springbootmicroservicesapigatewaydragon.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import springbootmicroservicesapigatewaydragon.springbootmicroservicesapigatewaydragon.models.dto.TokenDto;

@Component
public class AuthFilterPersonalizado implements GatewayFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilterPersonalizado.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    // En caso de que ocurra un error lo atrapara, obtiene el estado y lo setea a la respuesta
    public Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    /*
     * Filtro que en cada peticion verificara que el usuario envie el token
     * */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("Ejecutando filtro de autorizacion");

        // Revisa si la peticion tiene en el header el autorization
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return this.onError(exchange, HttpStatus.BAD_REQUEST);
        }
        // Extrae el token del header, de la posicion 0
        String authToken = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        String[] chunks = authToken.split(" ");

        log.info("Token capturado: {}", authToken);
        log.info("chunks: {}", chunks);

        // Verificando que si exista el token en el formato correcto
        if (chunks.length != 2 || !chunks[0].equals("Bearer")) {
            return this.onError(exchange, HttpStatus.BAD_REQUEST);
        }

        // microservice-auth // Nombre del servicio registrado en el eureka server
        return this.webClientBuilder.build()
                .post().uri("http://microservice-auth/auth/check-token?token=" + chunks[1]).retrieve() // redirecciona a la url
                .bodyToMono(TokenDto.class).map(t -> { // Mapea la respuesta a un TokenDto
                    return exchange;
                }).flatMap(chain::filter);
    }
}
