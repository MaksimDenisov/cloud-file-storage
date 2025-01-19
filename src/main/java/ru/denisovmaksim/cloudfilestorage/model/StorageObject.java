package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.Path;
import java.nio.file.Paths;


@RequiredArgsConstructor
@Getter
@ToString
public class StorageObject {

    private final String path;

    @Setter
    private long size;

    public boolean isFolder() {
        return path.endsWith("/");
    }

    public String getName() {
        Path path = Paths.get(this.path);
        return path.getFileName().toString();
    }
}
