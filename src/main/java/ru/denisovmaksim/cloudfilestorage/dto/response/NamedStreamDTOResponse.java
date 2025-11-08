package ru.denisovmaksim.cloudfilestorage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@AllArgsConstructor
@Getter
public class NamedStreamDTOResponse {
    private final String name;
    private final long length;
    private final InputStream stream;
}
