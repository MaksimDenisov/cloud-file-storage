package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.denisovmaksim.cloudfilestorage.service.ExplorerService;
import ru.denisovmaksim.cloudfilestorage.service.StreamService;
import ru.denisovmaksim.cloudfilestorage.util.FilesUtil;

@RestController
@AllArgsConstructor
public class StreamController {
    private final StreamService streamService;

    private final ExplorerService explorerService;

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamAudio(@RequestParam()
                                                             String filepath,
                                                             @RequestHeader(value = "Range", required = false)
                                                             String rangeHeader) {
        Long fileLength = explorerService.getSize(filepath);

        long rangeStart = 0;
        long rangeEnd = fileLength - 1;
        if (rangeHeader != null) {
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        }

        StreamingResponseBody body = streamService.getRange(filepath, rangeStart, rangeEnd);

        long contentLength = rangeEnd - rangeStart + 1;
        String contentType = FilesUtil.detectMimeType(filepath);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        headers.set(HttpHeaders.CONTENT_RANGE,
                "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);

        return new ResponseEntity<>(
                body,
                headers,
                rangeHeader == null ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT
        );
    }
}
