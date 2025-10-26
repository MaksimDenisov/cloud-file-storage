package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Deprecated
@Import(IntegrationTestConfiguration.class)
class MinioMetadataAccessorTest {

    private static final Long USER_ID = 1L;
    private static final String BUCKET = "user-files";

    @BeforeEach
    void cleanUp() throws Exception {
        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET)
                .recursive(true)
                .build());

        for (Result<Item> object : objects) {
            String objectName = object.get().objectName();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET).object(objectName).build());
        }
    }

    @Autowired
    private MinioClient minioClient;


    @Autowired
    private MinioMetadataAccessor minioMetadataAccessor;





    @Test
    void getStorageObjectsByNotExistPath() {
        assertFalse(minioMetadataAccessor.isExist(USER_ID, "not-exist-path"));
        assertFalse(minioMetadataAccessor.listObjectInfo(USER_ID, "not-exist-path").isPresent());
    }

    @Test
    void getStorageObjectsByRoot() {
        assertTrue(minioMetadataAccessor.listObjectInfo(USER_ID, "").isPresent());
    }


}
