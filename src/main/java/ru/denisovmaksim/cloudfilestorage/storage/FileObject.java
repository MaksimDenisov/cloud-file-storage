package ru.denisovmaksim.cloudfilestorage.storage;

import java.io.InputStream;

public record FileObject(String path, long size, InputStream stream) {
}
