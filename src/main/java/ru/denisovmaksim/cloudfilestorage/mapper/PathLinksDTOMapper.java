package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.LinkDTO;

import java.util.ArrayList;
import java.util.List;

public class PathLinksDTOMapper {
    public static List<LinkDTO> toChainLinksFromPath(String path) {
        List<LinkDTO> links = new ArrayList<>();
        String currentItemPath = "";
        for (String dir : path.split("/")) {
            currentItemPath = currentItemPath + dir + "/";
            links.add(new LinkDTO(currentItemPath, dir));
        }
        return links;
    }
}
