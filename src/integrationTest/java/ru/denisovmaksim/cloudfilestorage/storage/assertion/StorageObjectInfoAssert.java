package ru.denisovmaksim.cloudfilestorage.storage.assertion;

import org.assertj.core.api.AbstractAssert;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;



public class StorageObjectInfoAssert
        extends AbstractAssert<StorageObjectInfoAssert, StorageObjectInfo> {

    protected StorageObjectInfoAssert(StorageObjectInfo actual) {
        super(actual, StorageObjectInfoAssert.class);
    }

    public static StorageObjectInfoAssert assertThat(StorageObjectInfo actual) {
        return new StorageObjectInfoAssert(actual);
    }

    public StorageObjectInfoAssert hasPath(String expectedPath) {
        isNotNull();
        if (!actual.getPath().equals(expectedPath)) {
            failWithMessage("Expected path to be <%s> but was <%s>",
                    expectedPath, actual.getPath());
        }
        return this;
    }

    public StorageObjectInfoAssert hasName(String expectedName) {
        isNotNull();
        if (!actual.getName().equals(expectedName)) {
            failWithMessage("Expected name to be <%s> but was <%s>",
                    expectedName, actual.getName());
        }
        return this;
    }

    public StorageObjectInfoAssert isDirectory() {
        isNotNull();
        if (!actual.isDir()) {
            failWithMessage("Expected object to be a directory but it was a file");
        }
        return this;
    }

    public StorageObjectInfoAssert isFile() {
        isNotNull();
        if (actual.isDir()) {
            failWithMessage("Expected object to be a file but it was a directory");
        }
        return this;
    }

    public StorageObjectInfoAssert hasSize(long expectedSize) {
        isNotNull();
        if (actual.getSize() != expectedSize) {
            failWithMessage("Expected size to be <%d> but was <%d>",
                    expectedSize, actual.getSize());
        }
        return this;
    }
}
