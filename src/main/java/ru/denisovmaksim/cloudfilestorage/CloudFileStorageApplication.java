package ru.denisovmaksim.cloudfilestorage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CloudFileStorageApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CloudFileStorageApplication.class)
                .profiles(System.getenv().getOrDefault("APP_ENV", "dev"))
                .run(args);
    }
}
