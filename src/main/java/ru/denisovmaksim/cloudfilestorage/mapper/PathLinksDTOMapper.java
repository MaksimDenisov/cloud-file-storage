package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.LinkDTO;

import java.util.ArrayList;
import java.util.List;

public class PathLinksDTOMapper {
    private static final String PATH_SEPARATOR = "/";

    public static List<LinkDTO> toChainLinksFromPath(String path) {
        List<LinkDTO> links = new ArrayList<>();
        String currentItemPath = "";
        for (String dir : path.split(PATH_SEPARATOR)) {
            currentItemPath = currentItemPath + dir + PATH_SEPARATOR;
            links.add(new LinkDTO(currentItemPath, dir));
        }
        return links;
    }
}
