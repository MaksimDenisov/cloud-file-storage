package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;


@Getter
@Builder
@ToString
public class StorageObject {

    private final String path;

    private final String name;

    private final StorageObjectType type;

    private final ZonedDateTime lastModified;

    @Setter
    private Long size;


    public boolean isFolder() {
        return type == StorageObjectType.FOLDER;
    }

    public static class StorageObjectBuilder {
        public StorageObjectBuilder name(String name) {
            this.name = name;
            type = (name.endsWith("/"))
                    ? StorageObjectType.FOLDER : StorageObjectType.UNKNOWN_FILE;
            if (name.endsWith("/")) {
                this.name = name.replaceFirst(".$", "");
            }
            return this;
        }
    }
}
