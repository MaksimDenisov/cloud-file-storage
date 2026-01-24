package ru.denisovmaksim.cloudfilestorage.storage;

import org.testcontainers.containers.MinIOContainer;

public final class MinioTestContainer {

    private static final MinIOContainer INSTANCE =
            new MinIOContainer("minio/minio");

    static {
        INSTANCE.start();
    }

    private MinioTestContainer() {
    }

    public static MinIOContainer getInstance() {
        return INSTANCE;
    }
}
