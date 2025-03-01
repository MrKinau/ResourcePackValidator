package dev.kinau.resourcepackvalidator.validator.general;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.cache.JsonElementWithFile;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.Tuple;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class UnusedFileValidator extends Validator<ValidationJob, EmptyValidationContext, Object> {

    public UnusedFileValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Object> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        List<PathMatcher> ignoreList = ignoreList();

        long start = System.currentTimeMillis();
        Set<Tuple<File, String>> allFileNames = FileUtils.getFiles(job.rootDir()).stream()
                .filter(file -> ignoreList.stream().noneMatch(pathMatcher -> pathMatcher.matches(file.toPath())))
                .filter(file -> {
                    if (!file.getPath().contains(File.separatorChar + "minecraft" + File.separatorChar))
                        return true;
                    if (file.getPath().endsWith(".json")) {
                        String[] parts = file.getPath().split(File.separatorChar + "minecraft" + File.separatorChar + "models" + File.separatorChar);
                        if (parts.length < 2)
                            return true;
                        String relPathModel = parts[1].substring(0, parts[1].length() - 5);
                        return !FileUtils.isVanillaModel(relPathModel, job.assetDictionary());
                    } else if (file.getPath().endsWith(".png")) {
                        String[] parts = file.getPath().split(File.separatorChar + "minecraft" + File.separatorChar + "textures" + File.separatorChar);
                        if (parts.length < 2)
                            return true;
                        String relPathModel = parts[1].substring(0, parts[1].length() - 4);
                        return !FileUtils.isVanillaTexture(relPathModel, job.assetDictionary());
                    }
                    return true;
                })
                .map(file -> {
                    String name = file.getName();
                    if (name.contains("."))
                        return new Tuple<>(file, name.substring(0, name.indexOf(".")));
                    return new Tuple<>(file, name);
                })
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

        log.debug(logPrefix() + "Unused detection took {}ms", System.currentTimeMillis() - start);

        unusedFiles.stream().map(File::getPath).forEach(s -> {
            failedError("Found unused file at {}", s);
        });

        if (unusedFiles.size() > 0)
            return failedError();
        return success();
    }

    @Override
    protected Level defaultFailedLogLevel() {
        return Level.WARN;
    }
}
