package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
final class MinioExceptionHandler {

    private MinioExceptionHandler() {
        throw new UnsupportedOperationException("Utility class");
    }

    static <T> T callWithMinio(MinioThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("MinioClient exception occurred while accessing storage", e);
            throw new FileStorageException(e);
        }
    }

    static void runWithMinio(MinioThrowingRunnable supplier) {
        try {
            supplier.run();
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("MinioClient exception occurred while accessing storage.", e);
            throw new FileStorageException(e);
        }
    }
}
