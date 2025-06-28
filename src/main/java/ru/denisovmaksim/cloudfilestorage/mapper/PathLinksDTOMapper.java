package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.LinkDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.PATH_SEPARATOR;

public final class PathLinksDTOMapper {

    private PathLinksDTOMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<LinkDTO> toChainLinksFromPath(String path) {
        List<LinkDTO> links = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String dir : path.split(PATH_SEPARATOR)) {
            builder.append(dir).append(PATH_SEPARATOR);
            links.add(new LinkDTO(builder.toString(), dir));
        }
        return links;
    }
}
