package cnj.client;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;

import java.util.Map;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ClientApplication.class, args);
		Thread.sleep(10 * 1000);
	}

	@Bean
	ApplicationRunner requestResponse(RSocketRequester rSocketRequester) {
		return args -> {
			var replyStream = rSocketRequester
				.route("hello.{name}", "RSocket")
				.retrieveMono(new ParameterizedTypeReference<Map<String, String>>() {
				});
			var reply = replyStream.block();
			System.out.println("hello.{name}=" + reply);
		};
	}

	@Bean
	ApplicationRunner stream(RSocketRequester rSocketRequester) {
		return args -> {
			var replyStream = rSocketRequester
				.route("hellos")
				.retrieveFlux(new ParameterizedTypeReference<Map<String, String>>() {
				});
			replyStream.subscribe(r -> System.out.println("hellos=" + r));
			;
		};
	}

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
		return builder.tcp("localhost", 8181);
	}
}
