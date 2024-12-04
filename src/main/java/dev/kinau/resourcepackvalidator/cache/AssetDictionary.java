package dev.kinau.resourcepackvalidator.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
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

    private final Set<String> assets = new HashSet<>();

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
            assets.addAll(files.asList().stream().map(JsonElement::getAsString).toList());
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
                        files.add(path.toString().substring(rootPath.toString().length() + 1));
                    });
        } catch (IOException ex) {
            log.error("Could not create assets", ex);
        }
        rootObject.add("files", files);
        return rootObject;
    }

    public boolean contains(String asset) {
        return assets.contains(asset);
    }
}
