package ru.denisovmaksim.cloudfilestorage.storage.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class StorageObjectAssert
        extends AbstractAssert<StorageObjectAssert, StorageObject> {
    protected StorageObjectAssert(StorageObject actual) {
        super(actual, StorageObjectAssert.class);
    }

    public static StorageObjectAssert assertThat(StorageObject actual) {
        return new StorageObjectAssert(actual);
    }

    public StorageObjectAssert hasPath(String expectedPath) {
        isNotNull();
        if (!actual.path().equals(expectedPath)) {
            failWithMessage("Expected path to be <%s> but was <%s>",
                    expectedPath, actual.path());
        }
        return this;
    }

    public StorageObjectAssert matchContent(byte[] expectedBytes) throws IOException {
        isNotNull();
        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            if (!Arrays.equals(actualBytes, expectedBytes)) {
                failWithActualExpectedAndMessage(
                        actualBytes,
                        expectedBytes,
                        "File content mismatch (size %d bytes)",
                        actualBytes.length
                );
            }
        }
        return this;
    }
}
