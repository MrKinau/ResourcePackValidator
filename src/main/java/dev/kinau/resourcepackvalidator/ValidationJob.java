package dev.kinau.resourcepackvalidator;

import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.Namespace;
import dev.kinau.resourcepackvalidator.validator.ValidatorRegistry;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

@Slf4j
@Accessors(fluent = true)
@Getter
public class ValidationJob {

    private final File rootDir;
    private final ValidatorRegistry registry;
    private final List<Namespace> namespaces;
    private final Map<Namespace, NamespaceJsonCache> jsonCache = new HashMap<>();

    public ValidationJob(File rootDir, ValidatorRegistry registry) {
        this.rootDir = rootDir;
        this.registry = registry;
        this.namespaces = loadNamespaces();
        namespaces.forEach(namespace -> {
            jsonCache.put(namespace, new NamespaceJsonCache(namespace));
        });
    }

    private List<Namespace> loadNamespaces() {
        log.debug("Loading namespaces…");
        if (rootDir == null) return Collections.emptyList();
        File assetsDir = FileUtils.getAssetsDir(rootDir);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) return Collections.emptyList();
        File[] files = assetsDir.listFiles();
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .map(Namespace::new)
                .toList();
    }

    public void validate() {
        log.debug("Starting validation…");
        registry.validate(this);
    }

}
