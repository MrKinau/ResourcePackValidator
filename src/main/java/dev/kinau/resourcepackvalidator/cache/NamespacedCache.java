package dev.kinau.resourcepackvalidator.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Accessors(fluent = true)
@Getter
public abstract class NamespacedCache<Data> {

    private boolean parsingErrorOccurred;
    private final String fileSuffix;
    private final OverlayNamespace namespace;
    private final Multimap<FileUtils.Directory, Data> cache = HashMultimap.create();

    public NamespacedCache(OverlayNamespace namespace, String fileSuffix) {
        this.namespace = namespace;
        this.fileSuffix = fileSuffix;
        initCache();
    }

    protected abstract boolean isValid(FileUtils.Directory directory);

    protected abstract Data loadFile(File file) throws Exception;

    private void initCache() {
        for (FileUtils.Directory rpDir : FileUtils.Directory.values()) {
            if (!isValid(rpDir)) continue;
            List<File> dirs = new ArrayList<>();
            File baseDir = rpDir.getFile(namespace);
            if (baseDir.exists() && baseDir.isDirectory())
                dirs.add(baseDir);

            dirs.forEach(dir -> {
                FileUtils.getFiles(dir).forEach(file -> {
                    if (file == null) return;
                    if (!file.getName().endsWith(fileSuffix)) return;
                    try {
                        Data data = loadFile(file);
                        cache.put(rpDir, data);
                    } catch (Exception ex) {
                        this.parsingErrorOccurred = true;
                        log.error("Could not parse file {}", file.getPath(), ex);
                    }
                });
            });
        }
    }
}
