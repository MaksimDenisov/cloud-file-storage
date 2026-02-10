package ru.denisovmaksim.cloudfilestorage.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface DataAccessor {
    /**
     * Downloads a single object from storage.
     *
     * @param userId the ID of the user
     * @param path   the path to the object
     * @return the object along with its data stream
     */
    StorageObject getObject(Long userId, String path);

    /**
     * Retrieves a byte range of an object from MinIO as an input stream.
     *
     * @param userId        user identifier
     * @param path          relative object path
     * @param rangeStart    starting byte offset
     * @param contentLength number of bytes to read
     * @return input stream of the requested object range
     */
    InputStream getRangeOfObject(Long userId, String path, Long rangeStart, Long contentLength);

    /**
     * Downloads all objects (recursively) under the specified path.
     *
     * @param userId the ID of the user
     * @param path   the folder path
     * @return a list of file objects with their data streams
     */
    List<StorageObject> getObjects(Long userId, String path);

    /**
     * Uploads a file to the specified path.
     *
     * @param userId the ID of the user
     * @param path   the folder path where the file will be stored
     * @param file   the multipart file to upload
     */
    void saveObject(Long userId, String path, MultipartFile file);

    /**
     * Copies a single object from one path to another.
     *
     * @param userId   the ID of the user
     * @param srcPath  the source path
     * @param destPath the destination path
     */
    void copyOneObject(Long userId, String srcPath, String destPath);

    /**
     * Copies all objects (recursively) from one directory to another.
     *
     * @param userId   the ID of the user
     * @param srcPath  the source directory
     * @param destPath the target directory
     */
    int copyObjects(Long userId, String srcPath, String destPath);

    /**
     * Deletes one object under the specified path, including the folder itself.
     *
     * @param userId the ID of the user
     * @param path   the path to delete
     */
    void deleteOneObject(Long userId, String path);

    /**
     * Deletes all objects under the specified path, including the folder itself.
     *
     * @param userId the ID of the user
     * @param path   the folder path to delete
     */
    void deleteObjects(Long userId, String path);
}
