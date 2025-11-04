package ru.denisovmaksim.cloudfilestorage.service.processing;


import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ImageResizer {

    public InputStream shrinkIfWiderThan(InputStream source, String outputFormat,
                                         int targetWidth,
                                         ByteArrayOutputStream outputStream) throws IOException {
        BufferedImage original = ImageIO.read(source);
        if (original.getWidth() <= targetWidth) {
            ImageIO.write(original, outputFormat, outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        Thumbnails.of(original)
                .width(targetWidth)
                .keepAspectRatio(true)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
