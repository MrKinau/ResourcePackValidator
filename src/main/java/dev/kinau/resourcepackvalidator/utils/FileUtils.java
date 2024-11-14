package dev.kinau.resourcepackvalidator.utils;

import dev.kinau.resourcepackvalidator.cache.AssetDictionary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileUtils {

    @RequiredArgsConstructor
    @Getter
    public enum Directory {
        ATLASES("atlases", true),
        BLOCKSTATES("blockstates", true),
        FONT("font", true),
        LANG("lang", true),
        MODELS("models", true),
        PARTICLES("particles", true),
        SHADERS("shaders", true),
        TEXTS("texts", true),
        TEXTURES("textures", false),
        SOUNDS("sounds.json", true);

        private final String path;
        @Accessors(fluent = true)
        private final boolean containsJson;

        public File getFile(File namespace) {
            return new File(namespace, path);
        }

        public File getFile(OverlayNamespace namespace) {
            return new File(namespace.getAssetsDir(), path);
        }

        public File getFile(File rootDir, String namespace) {
            return getFile(getNamespace(rootDir, namespace));
        }
    }

    public static File getNamespace(File rootDir, String namespace) {
        return new File(getAssetsDir(rootDir), namespace);
    }

    public static File getAssetsDir(File rootOrOverlayDir) {
        return new File(rootOrOverlayDir, "assets");
    }

    public static List<File> getFiles(File file) {
        List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    files.addAll(getFiles(child));
                }
            }
        } else if (file.isFile())
            files.add(file);
        return files;
    }

    public static File getFileInOverlay(OverlayNamespace namespace, File origin) {
        String[] parts = origin.getAbsolutePath().split("assets" + File.separator + namespace.getNamespaceName() + File.separator);
        if (parts.length <= 1) return null;
        String end = String.join(File.separator, Arrays.copyOfRange(parts, 1, parts.length));
        return new File(namespace.getAssetsDir(), end);
    }

    private static File getFile(Directory directory, OverlayNamespace defaultNamespace, String relPath, String suffix) {
        File filesDir = directory.getFile(defaultNamespace);

        File file = new File(filesDir, relPath.replace("/", File.separator) + suffix);
        if (relPath.contains(":")) {
            String[] parts = relPath.split(":");
            if (parts.length > 1) {
                String namespace = parts[0];
                String path = parts[1];
                file = new File(directory.getFile(defaultNamespace.getOverlayOrRootDir(), namespace), path.replace("/", File.separator) + suffix);
            }
        }
        return file;
    }

    public static File getFile(Directory directory, OverlayNamespace defaultNamespace, String relPath, String suffix, Predicate<String> isVanilla) {
        if (relPath.endsWith(suffix))
            relPath = relPath.substring(0, relPath.length() - suffix.length());
        File file = getFile(directory, defaultNamespace, relPath, suffix);
        if (!file.exists()) {
            for (OverlayNamespace underlyingOverlay : defaultNamespace.getUnderlyingOverlays()) {
                file = getFile(directory, underlyingOverlay, relPath, suffix);
                if (file.exists()) break;
            }
            if (!file.exists()) {
                if (isVanilla.test(relPath))
                    return file;
                return null;
            }
        }
        return file;
    }


    public static boolean fileExists(Directory directory, OverlayNamespace defaultNamespace, String relPath, String suffix, Predicate<String> isVanilla) {
        return getFile(directory, defaultNamespace, relPath, suffix, isVanilla) != null;
    }

    public static File getTextureFile(OverlayNamespace defaultNamespace, String relPath) {
        return getFile(Directory.TEXTURES, defaultNamespace, relPath, ".png", s -> false);
    }

    public static boolean textureExists(OverlayNamespace defaultNamespace, String relPath, AssetDictionary assetDictionary) {
        return fileExists(Directory.TEXTURES, defaultNamespace, relPath, ".png", s -> FileUtils.isVanillaTexture(s, assetDictionary));
    }

    public static boolean modelExists(OverlayNamespace defaultNamespace, String relPath, AssetDictionary assetDictionary) {
        return relPath.startsWith("builtin/") || fileExists(Directory.MODELS, defaultNamespace, relPath, ".json", s -> FileUtils.isVanillaModel(s, assetDictionary));
    }

    public static boolean isVanillaTexture(String texturePath, AssetDictionary assetDictionary) {
        if (texturePath.startsWith("minecraft:"))
            texturePath = texturePath.substring(10);
        if (!texturePath.endsWith(".png"))
            texturePath += ".png";
        texturePath = "minecraft/" + Directory.TEXTURES.getPath() + "/" + texturePath;
        return assetDictionary.contains(texturePath);
    }

    public static boolean isVanillaModel(String modelPath, AssetDictionary assetDictionary) {
        if (modelPath.startsWith("minecraft:"))
            modelPath = modelPath.substring(10);
        if (!modelPath.endsWith(".json"))
            modelPath += ".json";
        modelPath = "minecraft/" + Directory.MODELS.getPath() + "/" + modelPath;
        return assetDictionary.contains(modelPath);
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;
        if (!directory.isDirectory()) {
            Files.delete(directory.toPath());
            return;
        }
        try (Stream<Path> fileTree = Files.walk(directory.toPath())){
            fileTree.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static boolean isArmorModel(File file, OverlayNamespace namespace) {
        if (file == null || !file.exists()) return false;

        File equipmentModels = new File(namespace.getAssetsDir(), "models/equipment");
        if (!equipmentModels.exists()) return false;

        return file.toPath().startsWith(equipmentModels.toPath());
    }
}
