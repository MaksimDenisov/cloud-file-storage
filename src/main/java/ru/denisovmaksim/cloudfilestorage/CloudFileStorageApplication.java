package ru.denisovmaksim.cloudfilestorage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CloudFileStorageApplication {
    public static void main(String[] args) {
        if (System.getenv().getOrDefault("APP_ENV", "dev").equals("dev")) {
            System.out.printf("JDBC_ROOT      = " + System.getenv().get("JDBC_ROOT") + "\n");
            System.out.printf("JDBC_PASSWORD  = " + System.getenv().get("JDBC_PASSWORD") + "\n");
            System.out.printf("JDBC_URL       = " + System.getenv().get("JDBC_URL") + "\n");
            System.out.printf("REDIS_HOST     = " + System.getenv().get("REDIS_HOST") + "\n");
            System.out.printf("REDIS_PORT     = " + System.getenv().get("REDIS_PORT") + "\n");
            System.out.printf("REDIS_PASSWORD = " + System.getenv().get("REDIS_PASSWORD") + "\n");
            System.out.printf("MINIO_URL      = " + System.getenv().get("MINIO_URL") + "\n");
            System.out.printf("MINIO_USER     = " + System.getenv().get("MINIO_USER") + "\n");
            System.out.printf("MINIO_PASSWORD = " + System.getenv().get("MINIO_PASSWORD") + "\n");
        }
        new SpringApplicationBuilder(CloudFileStorageApplication.class)
                .profiles(System.getenv().getOrDefault("APP_ENV", "dev"))
                .run(args);
    }
}
