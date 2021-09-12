package cnj.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Controller
@SpringBootApplication
public class ServiceApplication {

	// rsc tcp://localhost:8181 --route hello.rsocketx
	@MessageMapping("hello.{name}")
	Mono<Map<String, String>> requestResponse(@DestinationVariable String name) {
		return Mono.just(Collections.singletonMap("greetings", "Hello, " + name + "!"));
	}

	// rsc tcp://localhost:8181 --route hellos --stream
	@MessageMapping("hellos")
	Flux<Map<String, String>> stream() {
		return Flux
			.just("Srinivas", "Zhouyue", "Mario", "Shahram", "Zhen", "Mia", "Valeria")
			.map(name -> Collections.singletonMap("greetings", "Hello, " + name + "!"))
			.delayElements(Duration.ofSeconds(1));
	}

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}
