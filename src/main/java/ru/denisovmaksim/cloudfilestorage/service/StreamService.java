package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.io.IOException;
import java.io.InputStream;

@Service
@AllArgsConstructor
public class StreamService {
    private final MinioFileStorage fileStorage;
    private final SecurityService securityService;

    public StreamingResponseBody getRange(@ValidPath(PathType.FILEPATH) String filepath,
                                          long rangeStart, long rangeEnd) {
        Long userId = securityService.getAuthUserId();

        final long start = rangeStart;
        final long end = rangeEnd;
        final int chunkSize = 16 * 1024; // 16 KB
        return outputStream -> {
            long current = start;
            while (current <= end) {
                long bytesToRead = Math.min(chunkSize, end - current + 1);
                try (InputStream chunkStream = fileStorage.getRangeOfObject(userId, filepath, current, bytesToRead)) {
                    chunkStream.transferTo(outputStream);
                } catch (IOException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Connection reset by peer")) {
                        break;
                    } else {
                        throw e;
                    }
                }
                current += bytesToRead;
            }
        };
    }
}
