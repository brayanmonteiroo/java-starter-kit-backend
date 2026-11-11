package com.brayanmonteiro.starter.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

/**
 * Base de integração com containers singleton (não usa {@code @Container}
 * para não derrubar Postgres/Redis/Rabbit entre classes de teste).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;
    static final RabbitMQContainer RABBIT;
    static final GenericContainer<?> REDIS;

    static {
        assumeDockerAvailable();
        POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
                .withDatabaseName("java_starter_test")
                .withUsername("starter")
                .withPassword("secret");
        RABBIT = new RabbitMQContainer("rabbitmq:4-management-alpine");
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:8-alpine")).withExposedPorts(6379);
        POSTGRES.start();
        RABBIT.start();
        REDIS.start();
    }

    private static void assumeDockerAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
        } catch (Throwable ex) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Docker indisponível para Testcontainers: " + ex.getMessage());
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("app.jwt.secret", () -> "test-secret-key-with-at-least-thirty-two-characters");
        registry.add("app.seed.enabled", () -> "true");
        registry.add("app.seed.admin-email", () -> "admin@java-starter.test");
        registry.add("app.seed.admin-password", () -> "password");
        registry.add("app.frontend-url", () -> "http://localhost:4200");
        registry.add("app.cors-origins", () -> "http://localhost:4200");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> 1025);
    }

    @Autowired
    protected TestRestTemplate rest;

    protected static final String ADMIN_EMAIL = "admin@java-starter.test";
    protected static final String ADMIN_PASSWORD = "password";

    /** Spring Boot 4 renomeou UNPROCESSABLE_ENTITY → UNPROCESSABLE_CONTENT (ainda HTTP 422). */
    protected static final HttpStatus UNPROCESSABLE = HttpStatus.UNPROCESSABLE_CONTENT;

    protected HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));
        return headers;
    }

    protected HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    protected Map<String, Object> loginAsAdmin() {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> login(String email, String password) {
        ResponseEntity<Map> response = rest.postForEntity(
                "/api/login",
                Map.of("email", email, "password", password),
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Login falhou: " + response.getStatusCode() + " body=" + response.getBody());
        }
        return response.getBody();
    }

    protected String adminAccessToken() {
        return String.valueOf(loginAsAdmin().get("accessToken"));
    }

    protected <T> ResponseEntity<T> get(String path, HttpHeaders headers, Class<T> type) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), type);
    }

    protected <T> ResponseEntity<T> get(String path, HttpHeaders headers, ParameterizedTypeReference<T> type) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), type);
    }

    protected <T> ResponseEntity<T> post(String path, Object body, HttpHeaders headers, Class<T> type) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), type);
    }

    protected <T> ResponseEntity<T> put(String path, Object body, HttpHeaders headers, Class<T> type) {
        return rest.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers), type);
    }

    protected ResponseEntity<Void> delete(String path, HttpHeaders headers) {
        return rest.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

    protected boolean isJson(ResponseEntity<?> response) {
        MediaType contentType = response.getHeaders().getContentType();
        return contentType != null && (
                MediaType.APPLICATION_JSON.includes(contentType)
                        || MediaType.APPLICATION_PROBLEM_JSON.includes(contentType)
                        || ("application".equals(contentType.getType()) && contentType.getSubtype().contains("json"))
        );
    }
}
