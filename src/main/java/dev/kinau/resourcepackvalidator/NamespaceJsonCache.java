package dev.kinau.resourcepackvalidator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.Namespace;
import dev.kinau.resourcepackvalidator.validator.context.JsonElementWithFile;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@Accessors(fluent = true)
@Getter
public class NamespaceJsonCache {

    private boolean parsingErrorOccurred;
    private final Namespace namespace;
    private final Multimap<FileUtils.Directory, JsonElementWithFile> cache = HashMultimap.create();

    public NamespaceJsonCache(Namespace namespace) {
        this.namespace = namespace;
        initCache();
    }

    private void initCache() {
        for (FileUtils.Directory rpDir : FileUtils.Directory.values()) {
            if (!rpDir.containsJson()) continue;
            File dir = rpDir.getFile(namespace);
            if (!dir.exists()) continue;

            FileUtils.getFiles(dir).forEach(file -> {
                if (file == null) return;
                if (!file.getName().endsWith(".json")) return;
                try {
                    JsonElement element = JsonParser.parseReader(new FileReader(file));
                    cache.put(rpDir, new JsonElementWithFile(file, element));
                } catch (IOException | JsonParseException ex) {
                    this.parsingErrorOccurred = true;
                    log.error("Could not parse file {}", file.getPath(), ex);
                }
            });
        }
    }
}
