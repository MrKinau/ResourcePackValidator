package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModelRequiresOverlayOverrideValidator extends FileContextValidator<JsonObject, JsonObject> {

    private List<Requirement> requirements = new ArrayList<>();

    public ModelRequiresOverlayOverrideValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
        JsonObject data = config.get(getClass().getSimpleName());
        if (data == null) return;
        JsonArray required = data.getAsJsonArray("required");
        required.forEach(element -> {
            if (!element.isJsonObject()) return;
            if (element.getAsJsonObject().has("path") && element.getAsJsonObject().has("overlays")) {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(element.getAsJsonObject().get("path").getAsString());
                List<String> overlays = element.getAsJsonObject().getAsJsonArray("overlays").asList().stream().map(JsonElement::getAsString).toList();
                requirements.add(new Requirement(pathMatcher, overlays));
            }
        });
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject data) {
        if (context.namespace().getOverlay() != null) return skip();
        boolean failed = false;
        for (Requirement requirement : requirements) {
            if (requirement.path.matches(context.value().toPath())) {
                for (String requiredOverlay : requirement.overlays) {
                    Optional<OverlayNamespace> optNamespace = job.namespaces().stream()
                            .filter(overlayNamespace -> overlayNamespace.getOverlay() != null)
                            .filter(overlayNamespace -> overlayNamespace.getOverlay().getName().equals(requiredOverlay))
                            .filter(overlayNamespace -> overlayNamespace.getNamespaceName().equals(context.namespace().getNamespaceName()))
                            .findAny();
                    if (optNamespace.isEmpty())
                        return failedError("Could not find overlay {} although it is required", requiredOverlay);
                    OverlayNamespace namespace = optNamespace.get();
                    File requiredFile = FileUtils.getFileInOverlay(namespace, context.value());
                    if (requiredFile == null || !requiredFile.exists()) {
                        failed = true;
                        String path = requiredFile != null ? requiredFile.getPath() : "some unresolved location";
                        failedError("Required override is not present for {}. This file is required to be overridden in overlay {} at {}", context.value().getPath(), requiredOverlay, path);
                    }
                }
            }
        }
        return failed ? failedError() : success(data);
    }

    @RequiredArgsConstructor
    @ToString
    class Requirement {
        public final PathMatcher path;
        public final List<String> overlays;
    }
}
