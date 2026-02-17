package ru.denisovmaksim.cloudfilestorage.service.fixture;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;


@TestComponent
public class StorageFixture {

    public static final Long USER_ID = 1L;

    private final StorageDataAccessor dataAccessor;

    private final StorageMetadataAccessor metadataAccessor;

    public StorageFixture(StorageDataAccessor dataAccessor, StorageMetadataAccessor metadataAccessor) {
        this.dataAccessor = dataAccessor;
        this.metadataAccessor = metadataAccessor;
    }

    public void clearAll() {
        Optional<List<StorageObjectInfo>> objects = metadataAccessor.listObjectInfo(USER_ID, "");
        if (objects.isEmpty()) {
            return;
        }
        for (StorageObjectInfo info : objects.get()) {
            if (info.dir()) {
                dataAccessor.deleteObjects(USER_ID, info.path());
            } else {
                dataAccessor.deleteOneObject(USER_ID, info.path());
            }
        }
    }


    public void folder(String path) {
        metadataAccessor.createPath(USER_ID, path);
    }

    public void file(String path, String filename, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile(
                filename,
                filename,
                "text/plain",
                bytes);
        dataAccessor.saveObject(USER_ID, path, file);
    }
}
