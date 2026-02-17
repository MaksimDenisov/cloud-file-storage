package ru.denisovmaksim.cloudfilestorage.storage;

public record StorageObjectInfo(String path, String name, boolean dir, long size) {
}
