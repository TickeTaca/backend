package com.ticketaca.global.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트용 Testcontainers 설정.
 *
 * Spring Boot 3.1+의 @ServiceConnection을 사용하여
 * 컨테이너 접속 정보가 자동으로 Spring 프로퍼티에 주입된다.
 * (spring.datasource.url, spring.data.redis.host, spring.kafka.bootstrap-servers 등)
 *
 * 동일한 Spring Context를 공유하는 테스트들은 컨테이너를 재사용한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));
    }

    @Bean
    @ServiceConnection
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis:7-alpine"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka:3.9.0");
    }
}
