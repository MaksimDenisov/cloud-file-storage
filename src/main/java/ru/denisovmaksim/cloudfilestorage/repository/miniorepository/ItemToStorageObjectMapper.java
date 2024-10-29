package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.messages.Item;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

class ItemToStorageObjectMapper {
    static StorageObject toStorageObjects(MinioPath minioPath, Item item) {
        String minioName = item.objectName();
        String name = minioName.replace(minioPath.getFullMinioPath(), "");
        StorageObject object = StorageObject.builder()
                .path(minioName.replace(minioPath.getUserFolder(), ""))
                .name(name)
                .build();
        if (!object.isFolder()) {
            object.setSize(item.size());
        }
        return object;
    }
}
