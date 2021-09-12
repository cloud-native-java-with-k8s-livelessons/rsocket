package cnj.client;

import io.rsocket.SocketAcceptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
public class ClientApplication {

	@Bean
	SocketAcceptor socketAcceptor(
		RSocketStrategies strategies,
		HealthController controller) {
		return RSocketMessageHandler.responder(strategies, controller);
	}

	@Bean
	RSocketRequester rSocketRequester(
		SocketAcceptor acceptor,
		RSocketRequester.Builder builder) {
		return builder
			.rsocketConnector(connector -> connector.acceptor(acceptor))
			.tcp("localhost", 8181);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> client(RSocketRequester client) {
		return args ->
			client
				.route("hello.{name}")
				.data(new GreetingRequest("RSocket"))
				.retrieveFlux(GreetingResponse.class)
				.subscribe(System.out::println);
	}

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
		System.in.read();
	}

}

@Controller
class HealthController {

	@MessageMapping("health")
	Flux<ClientHealthState> health() {
		var stream = Stream.generate(() -> new ClientHealthState(Math.random()  > .3));
		return Flux.fromStream(stream).delayElements(Duration.ofSeconds(1));
	}
}

@Data
//@AllArgsConstructor
@NoArgsConstructor
class ClientHealthState {

	public ClientHealthState(boolean healthy) {
		this.healthy = healthy;
		System.out.println("health: " + healthy );
	}

	private boolean healthy;
}

// DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
	private String message;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
	private String name;
}
