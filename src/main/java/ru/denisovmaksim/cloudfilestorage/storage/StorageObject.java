package ru.denisovmaksim.cloudfilestorage.storage;

import java.io.InputStream;

public record StorageObject(String path, InputStream stream) {
}
