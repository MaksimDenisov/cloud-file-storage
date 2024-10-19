package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.Result;
import io.minio.messages.Item;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.denisovmaksim.cloudfilestorage.repository.miniorepository.MinioExceptionHandler.getWithHandling;

class MinioItemToStorageObjectMapper {
    static List<StorageObject> toStorageObjects(MinioPath minioPath, Iterable<Result<Item>> resultItems) {
        Map<String, StorageObject> stringStorageObjectMap = new ConcurrentHashMap<>();
        for (Result<Item> resultItem : resultItems) {
            MinioItemDescription itemDescription =
                    getWithHandling(() -> new MinioItemDescription(minioPath, resultItem.get()));
            if (itemDescription.isRootFolder()) {
                continue;
            }
            stringStorageObjectMap.computeIfPresent(itemDescription.getDirectElementName(),
                    (name, object) -> {
                        if (itemDescription.hasOnlyOneChild()) {
                            object.setSize(object.getSize() + 1);
                        }
                        return object;
                    });
            stringStorageObjectMap.putIfAbsent(itemDescription.getDirectElementName(),
                    StorageObject.builder().name(itemDescription.getDirectElementName())
                            .path(itemDescription.getDirectElementPath())
                            .type(itemDescription.getType())
                            .lastModified(itemDescription.getLastModified())
                            .size(itemDescription.hasOnlyOneChild() ? 1L : 0L)
                            .build()
            );
        }
        return stringStorageObjectMap.values()
                .stream()
                .toList();
    }
}