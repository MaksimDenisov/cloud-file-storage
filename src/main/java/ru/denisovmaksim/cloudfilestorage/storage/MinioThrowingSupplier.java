package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.errors.MinioException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@FunctionalInterface
interface MinioThrowingSupplier<T> {
    T get() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;
}
