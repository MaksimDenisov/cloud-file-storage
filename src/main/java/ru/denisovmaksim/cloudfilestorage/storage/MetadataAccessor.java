package ru.denisovmaksim.cloudfilestorage.storage;

import java.util.List;
import java.util.Optional;

public interface MetadataAccessor {
    /**
     * Creates an empty folder object in the storage for the specified user and path.
     *
     * @param userId the ID of the user
     * @param path   the logical path to be created
     */
    void createPath(Long userId, String path);

    /**
     * Checks whether at least one object exists in the specified path for the given user.
     *
     * @param userId the ID of the user whose storage space is being checked
     * @param path   the virtual path to check for object existence
     * @return {@code true} if at least one object exists at the specified path; {@code false} otherwise
     */
    boolean exist(Long userId, String path);

    Optional<StorageObjectInfo> getOne(Long userId, String path);

    /**
     * Retrieves metadata about the objects (files and folders) at the given path.
     *
     * @param userId the ID of the user
     * @param path   the logical user path
     * @return a list of storage object information if present
     */
    Optional<List<StorageObjectInfo>> listObjectInfo(Long userId, String path);

    /**
     * Retrieves metadata about the objects (files and folders) containing query substring in path.
     *
     * @param userId the ID of the user
     * @param query  Searching substring
     * @return a list of storage object information if present
     */
    List<StorageObjectInfo> findObjectInfosBySubstring(Long userId, String path, String query);
}
