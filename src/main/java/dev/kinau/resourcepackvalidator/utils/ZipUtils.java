package dev.kinau.resourcepackvalidator.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

@Slf4j
public class ZipUtils {

    public static Path extractFiles(File zipFile, File targetDirectory) {
        try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:" + zipFile.toURI()), Collections.emptyMap())) {
            fileSystem.getRootDirectories().forEach(rootPath -> {
                try (Stream<Path> fileTree = Files.walk(rootPath)) {
                    fileTree.forEach(path -> {
                        try {
                            if (path.getRoot().equals(path)) return;
                            Files.copy(path, new File(targetDirectory, path.toString()).toPath());
                        } catch (IOException ex) {
                            log.error("Could not copy contents of zip file {}", path, ex);
                        }
                    });
                } catch (IOException ex) {
                    log.error("Could not read contents of zip file", ex);
                }
            });
        } catch (IOException ex) {
            log.error("Could not extract zip file", ex);
        }
        return null;
    }
}
