package cnj.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

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

@Controller
@Log4j2
class GreetingController {

	@MessageMapping("hello.{name}")
	Flux<GreetingResponse> greet(
		@DestinationVariable String name,
		RSocketRequester clientRSocketConnection
	) {
		return Mono.just(name)
			.map(GreetingRequest::new)
			.flatMapMany(gr -> this.greet(clientRSocketConnection, gr));
	}

	private Flux<GreetingResponse> greet(
		RSocketRequester clientRSocketConnection, GreetingRequest requests) {

		var clientHealth = clientRSocketConnection
			.route("health")
			.retrieveFlux(ClientHealthState.class)
			.filter(chs -> !chs.isHealthy())
			.doOnNext(chs -> log.info("not healthy! "));

		var greetings = Flux
			.fromStream(Stream
				.generate(() -> new GreetingResponse("ni hao " + requests.getName() + " @ " + Instant.now() + "!")))
			.take(100)
			.delayElements(Duration.ofSeconds(1));

		return greetings.takeUntilOther(clientHealth);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ClientHealthState {
	private boolean healthy;
}