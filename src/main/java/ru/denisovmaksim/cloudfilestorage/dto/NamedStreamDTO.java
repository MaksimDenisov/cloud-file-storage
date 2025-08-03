package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@AllArgsConstructor
@Getter
public class NamedStreamDTO {
    private final String name;
    private final long length;
    private final InputStream stream;
}
