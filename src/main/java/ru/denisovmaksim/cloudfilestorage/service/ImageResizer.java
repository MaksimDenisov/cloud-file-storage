package ru.denisovmaksim.cloudfilestorage.service;


import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class ImageResizer {
    public InputStream resizeKeepAspectRatioAndStream(InputStream source, int targetWidth) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(source)
                .width(targetWidth)
                .keepAspectRatio(true)
                .outputFormat("jpg")
                .toOutputStream(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
