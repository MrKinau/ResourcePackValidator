package dev.kinau.resourcepackvalidator.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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

        public File getFile(Namespace namespace) {
            return new File(namespace.getFile(), path);
        }

        public File getFile(File rootDir, String namespace) {
            return getFile(getNamespace(rootDir, namespace));
        }
    }

    public static File getNamespace(File rootDir, String namespace) {
        return new File(getAssetsDir(rootDir), namespace);
    }

    public static File getAssetsDir(File rootDir) {
        return new File(rootDir, "assets");
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

    public static boolean fileExists(Directory directory, Namespace defaultNamespace, File rootDir, String relPath, String suffix, Predicate<String> isVanilla) {
        File filesDir = directory.getFile(defaultNamespace);
        File file = new File(filesDir, relPath.replace("/", File.separator) + suffix);
        if (relPath.contains(":")) {
            String[] parts = relPath.split(":");
            if (parts.length > 1) {
                String namespace = parts[0];
                String path = parts[1];
                file = new File(directory.getFile(rootDir, namespace), path.replace("/", File.separator) + suffix);
            }
        }
        if (!file.exists()) {
            return isVanilla.test(relPath);
        }
        return true;
    }

    public static boolean textureExists(Namespace defaultNamespace, File rootDir, String relPath) {
        return fileExists(Directory.TEXTURES, defaultNamespace, rootDir, relPath, ".png", FileUtils::isVanillaTexture);
    }

    public static boolean modelExists(Namespace defaultNamespace, File rootDir, String relPath) {
        return relPath.startsWith("builtin/") || fileExists(Directory.MODELS, defaultNamespace, rootDir, relPath, ".json", FileUtils::isVanillaModel);
    }

    public static boolean isVanillaTexture(String texturePath) {
        if (texturePath.startsWith("minecraft:"))
            texturePath = texturePath.substring(10);
        return FileUtils.class.getClassLoader().getResource("vanilla/textures/" + texturePath + ".png") != null;
    }

    public static boolean isVanillaModel(String modelPath) {
        if (modelPath.startsWith("minecraft:"))
            modelPath = modelPath.substring(10);
        return FileUtils.class.getClassLoader().getResource("vanilla/models/" + modelPath + ".json") != null;
    }
}
