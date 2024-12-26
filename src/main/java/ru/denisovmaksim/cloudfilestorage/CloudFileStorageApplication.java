package ru.denisovmaksim.cloudfilestorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@Slf4j
public class CloudFileStorageApplication {
    public static void main(String[] args) {
        if (System.getenv().getOrDefault("APP_ENV", "dev").equals("dev")) {
            log.info("Environment Variables:");
            log.info("JDBC_ROOT      = {}",
                    System.getenv().getOrDefault("JDBC_ROOT", " Default value"));
            log.info("JDBC_PASSWORD  = {}",
                    System.getenv().getOrDefault("JDBC_PASSWORD", " Default value"));
            log.info("JDBC_URL       = {}",
                    System.getenv().getOrDefault("JDBC_URL", " Default value"));
            log.info("REDIS_HOST     = {}",
                    System.getenv().getOrDefault("REDIS_HOST", " Default value"));
            log.info("REDIS_PORT     = {}",
                    System.getenv().getOrDefault("REDIS_PORT", " Default value"));
            log.info("REDIS_PASSWORD = {}",
                    System.getenv().getOrDefault("REDIS_PASSWORD", " Default value"));
            log.info("MINIO_URL      = {}",
                    System.getenv().getOrDefault("MINIO_URL", " Default value"));
            log.info("MINIO_USER     = {}",
                    System.getenv().getOrDefault("MINIO_USER", " Default value"));
            log.info("MINIO_PASSWORD = {}",
                    System.getenv().getOrDefault("MINIO_PASSWORD", " Default value"));
        }
        new SpringApplicationBuilder(CloudFileStorageApplication.class)
                .profiles(System.getenv().getOrDefault("APP_ENV", "dev"))
                .run(args);
    }
}
