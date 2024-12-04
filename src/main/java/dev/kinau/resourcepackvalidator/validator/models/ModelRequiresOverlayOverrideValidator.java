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

    private final List<Requirement> requirements = new ArrayList<>();

    public ModelRequiresOverlayOverrideValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
        loadRequirements();
    }

    private void loadRequirements() {
        JsonArray required = configValue("required", new JsonArray());
        required.forEach(element -> {
            if (!element.isJsonObject()) return;
            if (element.getAsJsonObject().has("path") && element.getAsJsonObject().has("overlays")) {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(element.getAsJsonObject().get("path").getAsString());
                List<String> overlays = element.getAsJsonObject().getAsJsonArray("overlays").asList().stream().map(JsonElement::getAsString).toList();
                List<ReplacementRule> replacementRules = new ArrayList<>();
                if (element.getAsJsonObject().has("replacements") && element.getAsJsonObject().get("replacements").isJsonArray()) {
                    for (JsonElement ruleElement : element.getAsJsonObject().getAsJsonArray("replacements")) {
                        if (!ruleElement.isJsonObject()) continue;
                        JsonObject ruleObject = ruleElement.getAsJsonObject();
                        if (!ruleObject.has("path") || !ruleObject.get("path").isJsonPrimitive()) continue;
                        if (!ruleObject.has("replacement") || !ruleObject.get("replacement").isJsonPrimitive()) continue;
                        replacementRules.add(new ReplacementRule(ruleObject.get("path").getAsString(), ruleObject.get("replacement").getAsString()));
                    }
                }
                requirements.add(new Requirement(pathMatcher, overlays, replacementRules));
            }
        });
    }

    @Override
    protected boolean defaultEnabled() {
        return false;
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject data) {
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
                    File updatedFile = new File(requirement.applyRules(context.value()));
                    File requiredFile = FileUtils.getFileInOverlay(namespace, updatedFile);
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
        public final List<ReplacementRule> replacementRules;

        public String applyRules(File file) {
            if (replacementRules == null) return file.getPath();
            String path = file.getPath();
            for (ReplacementRule replacementRule : replacementRules) {
                path = path.replace(replacementRule.path, replacementRule.replacement);
            }
            return path;
        }
    }

    @RequiredArgsConstructor
    @ToString
    class ReplacementRule {
        public final String path;
        public final String replacement;
    }
}
