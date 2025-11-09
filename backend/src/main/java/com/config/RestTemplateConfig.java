package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Устанавливаем таймауты в миллисекундах. Эти методы НЕ устаревшие.
        factory.setConnectTimeout(3000); // 3 секунды на подключение
        factory.setReadTimeout(5000);    // 5 секунд на чтение ответа

        return new RestTemplate(factory);
    }
}