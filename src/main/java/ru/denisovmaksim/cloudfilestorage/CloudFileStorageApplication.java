package ru.denisovmaksim.cloudfilestorage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CloudFileStorageApplication {
    public static void main(String[] args) {
        System.out.println(System.getenv().get("JDBC_URL"));
        new SpringApplicationBuilder(CloudFileStorageApplication.class)
                .profiles(System.getenv().getOrDefault("APP_ENV", "dev"))
                .run(args);
    }
}
