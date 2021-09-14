package com.example.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@SpringBootApplication
class ServiceApplication

fun main(args: Array<String>) {
    runApplication<ServiceApplication>(*args)
}

@Configuration
class SecurityConfiguration {

    @Bean
    fun authorization(security: RSocketSecurity) =
        security
            .simpleAuthentication(Customizer.withDefaults())
            .authorizePayload {
                it.anyExchange().authenticated()
            }
            .build()

    @Bean
    fun authenticationPrincipalMessageHandler(rs: RSocketStrategies): RSocketMessageHandler {
        val rmh = RSocketMessageHandler()
        rmh.argumentResolverConfigurer.addCustomResolver(
            AuthenticationPrincipalArgumentResolver()
        )
        rmh.rSocketStrategies = rs
        return rmh
    }


    @Bean
    fun authentication() = MapReactiveUserDetailsService(
        User.withDefaultPasswordEncoder().username("jlong").password("pw")
            .roles("USER").build()
    )
}

@Controller
class GreetingsRSocketController {

    @MessageMapping("hello")
    fun request(@AuthenticationPrincipal user: Mono<UserDetails>) =
        user
            .map { it.username }
            .map { mapOf("greetings" to "Hello, ${it}!") }


}