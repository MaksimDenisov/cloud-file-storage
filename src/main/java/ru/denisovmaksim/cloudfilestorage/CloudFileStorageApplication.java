package ru.denisovmaksim.cloudfilestorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@Slf4j
public class CloudFileStorageApplication {
    private static final String DEFAULT = " Default value";

    public static void main(String[] args) {
        if (System.getenv().getOrDefault("APP_ENV", "dev").equals("dev")) {
            log.debug("Environment Variables:");
            log.debug("JDBC_ROOT      = {}",
                    System.getenv().getOrDefault("JDBC_ROOT", DEFAULT));
            log.debug("JDBC_PASSWORD  = {}",
                    System.getenv().getOrDefault("JDBC_PASSWORD", DEFAULT));
            log.debug("JDBC_URL       = {}",
                    System.getenv().getOrDefault("JDBC_URL", DEFAULT));
            log.debug("REDIS_HOST     = {}",
                    System.getenv().getOrDefault("REDIS_HOST", DEFAULT));
            log.debug("REDIS_PORT     = {}",
                    System.getenv().getOrDefault("REDIS_PORT", DEFAULT));
            log.debug("REDIS_PASSWORD = {}",
                    System.getenv().getOrDefault("REDIS_PASSWORD", DEFAULT));
            log.debug("MINIO_URL      = {}",
                    System.getenv().getOrDefault("MINIO_URL", DEFAULT));
            log.debug("MINIO_USER     = {}",
                    System.getenv().getOrDefault("MINIO_USER", DEFAULT));
            log.debug("MINIO_PASSWORD = {}",
                    System.getenv().getOrDefault("MINIO_PASSWORD", DEFAULT));
            log.debug("MINIO_ROOT_USER     = {}",
                    System.getenv().getOrDefault("MINIO_ROOT_USER", DEFAULT));
            log.debug("MINIO_ROOT_PASSWORD = {}",
                    System.getenv().getOrDefault("MINIO_ROOT_PASSWORD", DEFAULT));
        }
        new SpringApplicationBuilder(CloudFileStorageApplication.class)
                .profiles(System.getenv().getOrDefault("APP_ENV", "dev"))
                .run(args);
    }
}
