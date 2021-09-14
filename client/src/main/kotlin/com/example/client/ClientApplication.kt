package com.example.client

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveMono
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata
import org.springframework.util.MimeTypeUtils

@SpringBootApplication
class ClientApplication

fun main(args: Array<String>) {
    runApplication<ClientApplication>(*args)
    Thread.sleep(10_000)
}


@Configuration
class RSocketConfiguration {

    private val credentials = UsernamePasswordMetadata("jlong", "pw")
    private val mimeType =
        MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)

    @Bean
    fun strategiesCustomizer(): RSocketStrategiesCustomizer =
        RSocketStrategiesCustomizer { it.encoder(SimpleAuthenticationEncoder()) }

    @Bean
    fun rsocketRequester(rs: RSocketRequester.Builder) =
        rs
            .setupMetadata(this.credentials, this.mimeType)
            .tcp("localhost", 9191)

    @Bean
    fun runner(rs: RSocketRequester) = ApplicationRunner {
        rs
            .route("hello")
            .metadata(this.credentials, this.mimeType)
            .retrieveMono<Map<String, String>>()
            .subscribe { println(it) }
    }
}

