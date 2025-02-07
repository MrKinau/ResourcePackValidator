package dev.kinau.resourcepackvalidator.cache;

import com.google.gson.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// On Update:
//
// There is a cmd line argument you can use to generate the assetscache
//
// 1. libraries/com/mojang/minecraft/%ver%/assets | find . -type f > files.txt
// 2. ./minecraft » minecraft
// 3. .mcassetsroot » deleted
// 4. merge with old data
// 5. cat files.txt | sort | uniq > files2.txt
// 6. (.+) » "$1",
// 7. paste into json
// 8. minify
@Slf4j
public class AssetDictionary {

    private static final Asset EMPTY_ASSET = new Asset(null, null);
    private static final Gson GSON = new Gson();

    private final Map<String, Asset> assets = new HashMap<>();

    // TODO: Distinguish resource pack versions?
    public AssetDictionary load() {
        log.debug("Loading vanilla assets…");
        assets.clear();
        try (InputStream stream = AssetDictionary.class.getClassLoader().getResourceAsStream("vanillaassets.json")) {
            if (stream == null) throw new IllegalArgumentException("vanillaassets.json is null");
            JsonElement root = JsonParser.parseReader(new InputStreamReader(stream));
            if (!root.isJsonObject()) throw new IllegalArgumentException("root is not JsonObject");
            JsonObject rootObject = root.getAsJsonObject();
            if (!rootObject.has("files") || !rootObject.get("files").isJsonArray())
                throw new IllegalArgumentException("root has no files array");
            JsonArray files = rootObject.getAsJsonArray("files");
            for (JsonElement assetElement : files) {
                if (assetElement.isJsonPrimitive()) {
                    assets.put(assetElement.getAsString(), EMPTY_ASSET);
                    continue;
                }
                if (assetElement.isJsonObject()) {
                    JsonObject assetObject = assetElement.getAsJsonObject();
                    if (assetObject.isEmpty()) continue;
                    String key = assetObject.keySet().iterator().next();
                    try {
                        Asset asset = GSON.fromJson(assetObject.getAsJsonObject(key), Asset.class);
                        assets.put(key, asset);
                    } catch (JsonSyntaxException e) {
                        assets.put(key, EMPTY_ASSET);
                    }
                }
            }
            log.debug("Loaded {} vanilla assets", assets.size());
        } catch (IOException | IllegalArgumentException ex) {
            log.error("Could not read vanilla assets", ex);
        }
        return this;
    }

    public JsonObject createAssets(File file) {
        Path rootPath = file.toPath();

        JsonObject rootObject = new JsonObject();
        JsonArray files = new JsonArray();
        try (Stream<Path> fileTree = Files.walk(rootPath)) {
            fileTree.filter(path -> !path.toFile().isDirectory())
                    .filter(path -> !path.toFile().getName().equals(".mcassetsroot"))
                    .forEach(path -> {
                        JsonObject assetObject = null;
                        if (path.toFile().getName().endsWith(".json")) {
                            try {
                                JsonElement jsonElement = JsonParser.parseReader(new FileReader(path.toFile()));
                                if (jsonElement != null && jsonElement.isJsonObject()) {
                                    JsonObject fullObject = jsonElement.getAsJsonObject();
                                    assetObject = new JsonObject();
                                    if (fullObject.has("parent")) {
                                        assetObject.add("parent", fullObject.get("parent"));
                                    }
                                    if (fullObject.has("textures")) {
                                        assetObject.add("textures", fullObject.get("textures"));
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        String relPath = path.toString().substring(rootPath.toString().length() + 1);
                        if (assetObject == null || assetObject.isEmpty()) {
                            files.add(relPath);
                        } else {
                            JsonObject dataObject = new JsonObject();
                            dataObject.add(relPath, assetObject);
                            files.add(dataObject);
                        }
                    });
        } catch (IOException ex) {
            log.error("Could not create assets", ex);
        }
        rootObject.add("files", files);
        return rootObject;
    }

    public boolean contains(String asset) {
        return assets.containsKey(asset);
    }

    public Set<String> getChildren(String... asset) {
        if (asset == null || asset.length == 0)
            return Collections.emptySet();
        return assets.entrySet().stream()
                .filter(entry -> {
                    for (String s : asset) {
                        if (s.equals(entry.getValue().parent()))
                            return true;
                    }
                    return false;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @Accessors(fluent = true)
    public static class Asset {
        @Nullable private String parent;
        @Nullable private Map<String, String> textures;
    }
}
