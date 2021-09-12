package cnj.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Controller
@SpringBootApplication
public class ServiceApplication {


	@MessageMapping("hello")
	Mono<Map<String, String>> requestResponse(@AuthenticationPrincipal Mono<UserDetails> user) {
		return user
			.map(UserDetails::getUsername)
			.map(name -> Collections.singletonMap("greetings", "Hello, " + name + "!"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}

@Configuration
class SecurityConfiguration {

	@Bean
	PayloadSocketAcceptorInterceptor interceptor(RSocketSecurity security) {
		return security
			.simpleAuthentication(Customizer.withDefaults())
			.authorizePayload(ap -> ap.anyExchange().authenticated())
			.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder().username("jlong").password("pw").roles("USER").build());
	}

	@Bean
	RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
		var rmh = new RSocketMessageHandler();
		rmh.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
		rmh.setRSocketStrategies(strategies);
		return rmh;
	}
}