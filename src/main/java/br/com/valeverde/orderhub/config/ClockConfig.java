package br.com.valeverde.orderhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /**
     * Relógio único da aplicação, sempre no fuso da operação (America/Sao_Paulo),
     * independentemente do fuso do servidor.
     */
    @Bean
    public Clock clock(BusinessProperties properties) {
        return Clock.system(ZoneId.of(properties.timezone()));
    }
}
