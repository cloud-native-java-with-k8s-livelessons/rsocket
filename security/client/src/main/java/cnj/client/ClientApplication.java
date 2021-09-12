package cnj.client;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

@SpringBootApplication
public class ClientApplication {

	private final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("jlong", "pw");
	private final MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ClientApplication.class, args);
		Thread.sleep(10 * 1000);
	}

	@Bean
	RSocketStrategiesCustomizer strategiesCustomizer() {
		return strategies -> strategies.encoder(new SimpleAuthenticationEncoder());
	}

	@Bean
	ApplicationRunner requestResponse(RSocketRequester rSocketRequester) {
		return args -> {
			var replyStream = rSocketRequester
				.route("hello", "RSocket")
				.metadata(this.credentials, this.mimeType)
				.retrieveMono(new ParameterizedTypeReference<Map<String, String>>() {
				});
			var reply = replyStream.block();
			System.out.println("hello (authenticated)=" + reply);
		};
	}


	@Bean
	RSocketRequester rSocketRequester(
		RSocketRequester.Builder builder) {
		return builder
			.setupMetadata(this.credentials, this.mimeType)
			.tcp("localhost", 8181);
	}
}
