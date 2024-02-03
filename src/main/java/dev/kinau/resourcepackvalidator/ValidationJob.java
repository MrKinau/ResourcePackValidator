package dev.kinau.resourcepackvalidator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kinau.resourcepackvalidator.atlas.TextureAtlas;
import dev.kinau.resourcepackvalidator.cache.AssetDictionary;
import dev.kinau.resourcepackvalidator.cache.NamespaceJsonCache;
import dev.kinau.resourcepackvalidator.cache.NamespaceTextureCache;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.McMetaFile;
import dev.kinau.resourcepackvalidator.utils.Overlay;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import dev.kinau.resourcepackvalidator.validator.ValidatorRegistry;
import dev.kinau.resourcepackvalidator.validator.data.font.FontProviderFactory;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Accessors(fluent = true)
@Getter
public class ValidationJob {

    private final File rootDir;
    private final ValidatorRegistry registry;
    private final McMetaFile mcMetaFile;
    private final List<OverlayNamespace> namespaces;
    private final List<Overlay> overlays;
    private final Map<OverlayNamespace, NamespaceJsonCache> jsonCache = new HashMap<>();
    private final Map<OverlayNamespace, NamespaceTextureCache> textureCache = new HashMap<>();
    private final Map<OverlayNamespace, TextureAtlas> textureAtlas = new HashMap<>();
    private final AssetDictionary assetDictionary;
    private final FontProviderFactory fontProviderFactory;

    public ValidationJob(File rootDir, ValidatorRegistry registry) {
        this.rootDir = rootDir;
        this.registry = registry;
        this.mcMetaFile = readMcMeta();
        this.overlays = loadOverlays();
        this.namespaces = loadNamespaces();
        namespaces.forEach(namespace -> {
            jsonCache.put(namespace, new NamespaceJsonCache(namespace));
            textureCache.put(namespace, new NamespaceTextureCache(namespace));
            textureAtlas.put(namespace, new TextureAtlas(namespace));
        });
        this.assetDictionary = new AssetDictionary().load();
        this.fontProviderFactory = new FontProviderFactory();
    }

    private List<OverlayNamespace> getNamespaces(Overlay overlay) {
        List<OverlayNamespace> namespaces = new ArrayList<>();
        File rootOrOverlayDir = Optional.ofNullable(overlay).map(Overlay::getFile).orElse(rootDir);
        File assetsDir = FileUtils.getAssetsDir(rootOrOverlayDir);
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            File[] files = assetsDir.listFiles();
            if (files != null) {
                namespaces.addAll(Arrays.stream(files)
                        .filter(File::isDirectory)
                        .map(file -> new OverlayNamespace(rootOrOverlayDir, file, overlay, new ArrayList<>()))
                        .toList());
            }
        }
        return namespaces;
    }

    private void fillUnderlyingNamespaces(List<OverlayNamespace> namespaces) {
        namespaces.forEach(overlayNamespace -> {
            if (overlayNamespace.getOverlay() == null) return;
            List<Overlay> underlyingOverlays = new ArrayList<>();
            for (int i = overlays().size() - 1; i >= 0; i--) {
                Overlay overlay = overlays().get(i);
                if (overlay == overlayNamespace.getOverlay()) break;
                underlyingOverlays.add(overlay);
            }
            overlayNamespace.getUnderlyingOverlays().addAll(underlyingOverlays.stream()
                    .map(overlay -> namespaces.stream().filter(overlayNamespace1 -> overlayNamespace1.getOverlay() == overlay).findAny())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());
            namespaces.stream().filter(overlayNamespace1 -> overlayNamespace1.getOverlay() == null).findAny().ifPresent(rootNamespace -> {
                overlayNamespace.getUnderlyingOverlays().add(rootNamespace);
            });
        });
    }

    private List<OverlayNamespace> loadNamespaces() {
        log.debug("Loading namespaces…");
        if (rootDir == null) return Collections.emptyList();
        List<OverlayNamespace> namespaces = getNamespaces(null);
        overlays().forEach(overlay -> {
            namespaces.addAll(getNamespaces(overlay));
        });
        fillUnderlyingNamespaces(namespaces);

        log.debug("Found namespace(s): {}", namespaces.stream().map(OverlayNamespace::getName).collect(Collectors.joining(", ")));
        return namespaces;
    }

    private List<Overlay> loadOverlays() {
        if (mcMetaFile == null) return Collections.emptyList();
        if (!mcMetaFile.getOverlays().isEmpty())
            log.debug("Found overlay(s): {}", mcMetaFile.getOverlays().stream().map(Overlay::getName).collect(Collectors.joining(", ")));
        return mcMetaFile.getOverlays();
    }

    // no validation yet, just used for obtaining overlays
    private McMetaFile readMcMeta() {
        log.debug("Loading mcmeta file…");
        if (rootDir == null) return null;
        File metaFile = new File(rootDir, "pack.mcmeta");
        if (!metaFile.exists() || !metaFile.isFile()) return null;
        try {
            JsonElement element = JsonParser.parseReader(new FileReader(metaFile));
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            McMetaFile mcMetaFile = new McMetaFile(metaFile, new ArrayList<>());
            if (root.has("overlays") && root.get("overlays").isJsonObject()) {
                JsonObject overlays = root.getAsJsonObject("overlays");
                if (overlays.has("entries") && overlays.get("entries").isJsonArray()) {
                    JsonArray overlayEntries = overlays.getAsJsonArray("entries");
                    overlayEntries.forEach(overlayEntry -> {
                        if (overlayEntry.isJsonObject() && overlayEntry.getAsJsonObject().has("directory") && overlayEntry.getAsJsonObject().get("directory").isJsonPrimitive()) {
                            File file = new File(rootDir, overlayEntry.getAsJsonObject().getAsJsonPrimitive("directory").getAsString());
                            if (!file.exists() || !file.isDirectory()) return;
                            mcMetaFile.getOverlays().add(new Overlay(file));
                        }
                    });
                }
            }
            return mcMetaFile;
        } catch (Exception ex) {
            log.error("Could not parse pack.mcmeta", ex);
        }
        return null;
    }

    public boolean validate() {
        log.debug("Starting validation…");
        return registry.validate(this)
                && jsonCache.values().stream().noneMatch(NamespaceJsonCache::parsingErrorOccurred);
    }

}
