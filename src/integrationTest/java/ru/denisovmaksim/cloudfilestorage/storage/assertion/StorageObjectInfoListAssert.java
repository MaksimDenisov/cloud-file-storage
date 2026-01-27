package ru.denisovmaksim.cloudfilestorage.storage.assertion;

import org.assertj.core.api.AbstractAssert;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;

import java.util.List;


// TODO Use StorageObjectInfoAssert
public class StorageObjectInfoListAssert extends AbstractAssert<StorageObjectInfoListAssert, List<StorageObjectInfo>> {

    protected StorageObjectInfoListAssert(List<StorageObjectInfo> actual) {
        super(actual, StorageObjectInfoListAssert.class);
    }

    public static StorageObjectInfoListAssert assertThat(List<StorageObjectInfo> actual) {
        return new StorageObjectInfoListAssert(actual);
    }

    public StorageObjectInfoListAssert containsExactlyPaths(String... paths) {
        isNotNull();

        List<String> actualPaths = actual.stream()
                .map(StorageObjectInfo::getPath)
                .toList();

        for (String path : paths) {
            if (!actualPaths.contains(path)) {
                failWithMessage("Expected list to contain path <%s> but it was not found. Actual paths: %s",
                        path, actualPaths);
            }
        }

        if (actualPaths.size() != paths.length) {
            failWithMessage("Expected list to contain exactly <%d> elements %s but was <%d> %s",
                    paths.length, List.of(paths), actualPaths.size(), actualPaths);
        }

        return this;
    }
}
