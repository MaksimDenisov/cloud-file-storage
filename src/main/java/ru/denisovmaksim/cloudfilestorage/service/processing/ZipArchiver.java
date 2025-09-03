package ru.denisovmaksim.cloudfilestorage.service.processing;

import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipArchiver {
    public ByteArrayOutputStream getByteArrayOutputStream(List<StorageObject> fileObjects, String basePath) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (StorageObject object : fileObjects) {
                String objectPath = object.path().replaceFirst(Pattern.quote(basePath), "");
                ZipEntry zipEntry = new ZipEntry(objectPath);
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream objectInputStream = object.stream();
                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
        return byteArrayOutputStream;
    }
}

