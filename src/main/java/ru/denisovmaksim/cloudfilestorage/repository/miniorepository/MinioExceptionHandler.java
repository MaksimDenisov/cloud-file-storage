package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import ru.denisovmaksim.cloudfilestorage.exceptions.FileStorageException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
class MinioExceptionHandler {

    public static <T> T getWithHandling(MinioThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e.getMessage());
            throw new FileStorageException(e);
        }
    }

    public static void executeWithHandling(MinioThrowingRunnable supplier) {
        try {
            supplier.run();
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e.getMessage());
            throw new FileStorageException(e);
        }
    }
}