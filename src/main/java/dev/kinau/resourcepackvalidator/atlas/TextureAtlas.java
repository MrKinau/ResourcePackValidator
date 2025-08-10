package dev.kinau.resourcepackvalidator.atlas;

import com.google.gson.Gson;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@Slf4j
public class TextureAtlas {

    private AtlasData data;

    public TextureAtlas(File rootDir, Gson gson) {
        try {
            File atlasDir = FileUtils.Directory.ATLASES.getFile(rootDir, "minecraft");
            File blocksAtlas = new File(atlasDir, "blocks.json");
            if (blocksAtlas.exists()) {
                this.data = gson.fromJson(new FileReader(blocksAtlas), AtlasData.class);
            } else {
                this.data = new AtlasData();
            }
            data.sources().add(new AtlasSource("directory", "item", "", null, null, null));
            data.sources().add(new AtlasSource("directory", "block", "", null, null, null));
        } catch (Exception ex) {
            log.error("Could not load blocks atlas", ex);
        }
    }

    public boolean isPartOfAtlas(OverlayNamespace namespace, File file) {
        if (data == null) return false;
        return data.sources().stream().anyMatch(atlasSource -> atlasSource.isInAtlas(namespace, file));
    }

    @Getter
    @Accessors(fluent = true)
    @ToString
    public static class AtlasData {
        private final List<AtlasSource> sources = new ArrayList<>();
    }

    @Getter
    @Accessors(fluent = true)
    @ToString
    public static class AtlasFilterType {
        private String namespace;
        private String path;
    }

    //TODO: Add "filter" and "unstitch" type and use inherited classes
    @Getter
    @Accessors(fluent = true)
    @ToString
    @AllArgsConstructor
    public static class AtlasSource {
        private String type;

        private String source;
        private String prefix;

        private String resource;
        private String sprite;

        private AtlasFilterType pattern;

        public boolean isInAtlas(OverlayNamespace namespace, File file) {
            String path = file.getPath();
            String namespacePath = namespace.getAssetsDir().getPath();
            path = path.replace(namespacePath, "").replace("/textures", "");
            if (path.startsWith("/"))
                path = path.substring(1);

            String type = this.type;
            if (type.startsWith("minecraft:"))
                type = type.substring(10);

            if (type.equals("single")) {
                if (resource != null) {
                    String resource = resource().replace("minecraft:", "");
                    if (!resource.endsWith(".png"))
                        resource = resource + ".png";
                    return resource.equals(path);
                } else {
                    return false;
                }
            } else if (type.equals("directory")) {
                if (source != null) {
                    String resource = source().replace("minecraft:", "");
                    if (resource.startsWith(File.separator))
                        resource = resource.substring(1);
                    if (!resource.endsWith(File.separator))
                        resource = resource + File.separator;
                    return path.startsWith(resource);
                } else {
                    return false;
                }
            } else if (type.equals("filter")) {
                // TODO filter atlas type
                return false;
            }
            log.warn("Detected unknown texture atlas type: {}, ignoring it", this.type);
            return false;
        }
    }
}
