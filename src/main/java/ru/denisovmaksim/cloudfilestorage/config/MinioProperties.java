package ru.denisovmaksim.cloudfilestorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record MinioProperties(String bucket) {
}
