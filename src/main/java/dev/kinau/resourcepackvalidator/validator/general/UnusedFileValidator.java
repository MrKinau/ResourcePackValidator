package dev.kinau.resourcepackvalidator.validator.general;

import com.google.gson.JsonElement;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.Tuple;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.JsonElementWithFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class UnusedFileValidator extends Validator<ValidationJob, EmptyValidationContext, Object> {

    @Override
    protected ValidationResult<Object> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        long start = System.currentTimeMillis();
        Set<Tuple<File, String>> allFileNames = FileUtils.getFiles(job.rootDir()).stream()
                .filter(file -> !file.getPath().contains(File.separatorChar + "shaders" + File.separatorChar))
                .filter(file -> !file.getPath().contains(File.separatorChar + "font" + File.separatorChar))
                .filter(file -> !file.getPath().contains(File.separatorChar + "lang" + File.separatorChar))
                .filter(file -> !file.getPath().endsWith(".md"))
                .filter(file -> {
                    if (!file.getPath().contains(File.separatorChar + "minecraft" + File.separatorChar))
                        return true;
                    if (file.getPath().endsWith(".json")) {
                        String[] parts = file.getPath().split(File.separatorChar + "minecraft" + File.separatorChar + "models" + File.separatorChar);
                        if (parts.length < 2)
                            return true;
                        String relPathModel = parts[1].substring(0, parts[1].length() - 5);
                        if (FileUtils.isVanillaModel(relPathModel))
                            return false;
                    } else if (file.getPath().endsWith(".png")) {
                        String[] parts = file.getPath().split(File.separatorChar + "minecraft" + File.separatorChar + "textures" + File.separatorChar);
                        if (parts.length < 2)
                            return true;
                        String relPathModel = parts[1].substring(0, parts[1].length() - 4);
                        if (FileUtils.isVanillaTexture(relPathModel))
                            return false;
                    }
                    return true;
                })
                .map(file -> {
                    String name = file.getName();
                    if (name.contains("."))
                        return new Tuple<>(file, name.substring(0, name.indexOf(".")));
                    return new Tuple<>(file, name);
                })
                .filter(tuple -> !tuple.a().getName().endsWith("_e.png") && !tuple.a().getName().endsWith(".mcmeta"))
                .collect(Collectors.toSet());

        List<String> rawJsonFiles = job.jsonCache().values().stream()
                .flatMap(namespaceJsonCache -> namespaceJsonCache.cache().values().stream())
                .map(JsonElementWithFile::element)
                .map(JsonElement::toString)
                .toList();

        Set<File> unusedFiles = allFileNames.parallelStream()
                .filter(s -> rawJsonFiles.stream().noneMatch(s1 -> s1.contains(s.b())))
                .map(Tuple::a)
                .collect(Collectors.toSet());

        log.debug("Unused detection took {}ms", System.currentTimeMillis() - start);

        unusedFiles.stream().map(File::getPath).forEach(s -> {
            failedWarning("Found unused file at {}", s);
        });

        return success();
    }
}
