package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.cache.AssetDictionary;
import dev.kinau.resourcepackvalidator.cache.NamespaceJsonCache;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ModelTextureReferencesResolvableValidator extends FileContextValidator<JsonObject, Map<String, String>> {

    private static final int MAX_DEPTH = 10;

    public ModelTextureReferencesResolvableValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Map<String, String>> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        Set<ModelPathWithSource> children = getRealChildren(job, context);
        if (children == null)
            return failedError("Model parent depth is higher than {} at {}", MAX_DEPTH, context.value().getPath());
        if (children.isEmpty())
            children.add(new ModelPathWithSource(false, FileUtils.getRelPath(context, FileUtils.Directory.MODELS)));

        Map<String, String> textureReferences = new HashMap<>();
        boolean failed = false;
        for (ModelPathWithSource child : children) {
            if (!validateTextureData(child, job, context, textureReferences))
                failed = true;
        }

        if (failed)
            return failedError();

        return success(textureReferences);
    }

    private Set<ModelPathWithSource> getCustomChildren(ValidationJob job, FileContext context) {
        String relPath = FileUtils.getRelPath(context, FileUtils.Directory.MODELS);
        return getCustomChildren(relPath, job, context);
    }

    private Set<ModelPathWithSource> getCustomChildren(String relPath, ValidationJob job, FileContext context) {
        Set<ModelPathWithSource> customChildren = new HashSet<>();

        for (NamespaceJsonCache jsonCache : job.jsonCache().values()) {
            if (!jsonCache.namespace().getNamespaceName().equals(context.namespace().getNamespaceName())) continue;
            customChildren.addAll(jsonCache.cache().get(FileUtils.Directory.MODELS).stream()
                    .filter(jsonElementWithFile -> jsonElementWithFile.element().isJsonObject())
                    .filter(jsonElementWithFile -> {
                        JsonObject modelObj = jsonElementWithFile.element().getAsJsonObject();
                        if (!modelObj.has("parent") || !modelObj.get("parent").isJsonPrimitive()) return false;
                        String parent = modelObj.getAsJsonPrimitive("parent").getAsString();
                        return parent.equals(relPath) || parent.equals(context.namespace().getNamespaceName() + ":" + relPath);
                    })
                    .map(jsonElementWithFile -> FileUtils.getRelPath(jsonElementWithFile.file(), FileUtils.Directory.MODELS, context.namespace().getNamespaceName()))
                    .map(s -> new ModelPathWithSource(false, s))
                    .collect(Collectors.toSet()));
        }

        return customChildren;
    }

    // real children = children, which do not act as a parent for some other model
    private Set<ModelPathWithSource> getRealChildren(ValidationJob job, FileContext context) {
        Set<ModelPathWithSource> realChildren = new HashSet<>();
        Set<ModelPathWithSource> children = getCustomChildren(job, context);
        for (int i = 0; i < MAX_DEPTH; i++) {
            Set<ModelPathWithSource> hasChildren = new HashSet<>();
            for (ModelPathWithSource child : children) {
                Set<ModelPathWithSource> childsChildren = getCustomChildren(child.path(), job, context);
                if (childsChildren.isEmpty()) {
                    childsChildren = job.assetDictionary().getChildren(child.path(), context.namespace().getNamespaceName() + ":" + child.path()).stream()
                            .map(s -> new ModelPathWithSource(true, s))
                            .collect(Collectors.toSet());
                    if (childsChildren.isEmpty()) {
                        realChildren.add(child);
                        continue;
                    }
                }
                hasChildren.addAll(childsChildren);
            }
            children = hasChildren;

            if (children.isEmpty()) break;
            if (i == MAX_DEPTH - 1) return null;
        }
        return realChildren;
    }

    private boolean validateTextureData(ModelPathWithSource model, ValidationJob job, FileContext context, Map<String, String> textureReferences) {
        Map<String, TextureReferenceWithSource> textureData = new HashMap<>();
        List<ModelWithSource> parents = getAllParents(model, job, context);

        if (parents == null) return false;
        for (ModelWithSource parent : parents) {
            textureData.putAll(getTextureData(parent));
        }

        textureData.forEach((s, textureReferenceWithSource) -> {
            textureReferences.put(s, textureReferenceWithSource.referenceValue());
        });


        if (!detectReferenceChain(textureData, model, context))
            return false;

        Set<TextureReferenceWithSource> references = textureData.values().stream().filter(s -> s.referenceValue().startsWith("#")).collect(Collectors.toSet());
        if (references.isEmpty()) return true;

        boolean failed = false;
        for (TextureReferenceWithSource reference : references) {
            if (!textureData.containsKey(reference.referenceValue().substring(1))) {
                // a child is already failing, do not fail twice for the parent again
                if (!model.path().equals(FileUtils.getRelPath(context, FileUtils.Directory.MODELS)) && !reference.vanilla()) continue;
                failedError("Texture reference {} is missing at {} ({})", reference.referenceValue(), context.namespace().getNamespaceName() + File.separator + FileUtils.Directory.MODELS.getPath() + File.separator + model.path(), context.value().getPath());
                failed = true;
            }
        }

        return !failed;
    }

    private ModelWithSource getModelObject(String relPath, ValidationJob job, FileContext context) {
        Optional<JsonObject> optObj = Optional.empty();
        for (NamespaceJsonCache jsonCache : job.jsonCache().values()) {
            if (!jsonCache.namespace().getNamespaceName().equals(context.namespace().getNamespaceName())) continue;
            optObj = jsonCache.cache().get(FileUtils.Directory.MODELS).stream()
                    .filter(jsonElementWithFile -> {
                        return FileUtils.getRelPath(jsonElementWithFile.file(), FileUtils.Directory.MODELS, context.namespace().getNamespaceName()).equals(relPath)
                                || (context.namespace().getNamespaceName() + ":" + FileUtils.getRelPath(jsonElementWithFile.file(), FileUtils.Directory.MODELS, context.namespace().getNamespaceName())).equals(relPath);
                    })
                    .map(jsonElementWithFile -> jsonElementWithFile.element().getAsJsonObject())
                    .findAny();
            if (optObj.isPresent()) break;
        }
        if (optObj.isPresent())
            return new ModelWithSource(false, optObj.get());
        AssetDictionary.Asset asset = job.assetDictionary().getAsset(
                        "minecraft" + File.separator +
                        FileUtils.Directory.MODELS.getPath() + File.separator +
                        relPath + ".json"
        );
        if (asset == null) return null;

        JsonElement jsonAsset = job.gson().toJsonTree(asset);
        if (jsonAsset.isJsonObject()) return new ModelWithSource(true, jsonAsset.getAsJsonObject());

        return null;
    }

    private List<ModelWithSource> getAllParents(ModelPathWithSource model, ValidationJob job, FileContext context) {
        String relPath = model.path();
        ModelWithSource modelData = getModelObject(relPath, job, context);
        if (modelData == null) {
            failedError("Model {} could not be found during validation of {}", relPath, context.value().getPath());
            return null;
        }
        List<ModelWithSource> parents = new ArrayList<>();
        for (int i = 0; i < MAX_DEPTH; i++) {
            parents.add(0, modelData);
            if (modelData.modelObject().has("parent") && modelData.modelObject().get("parent").isJsonPrimitive()) {
                String parent = modelData.modelObject().getAsJsonPrimitive("parent").getAsString();
                if (parent.startsWith("builtin/")) break;
                modelData = getModelObject(FileUtils.stripNamespace(parent), job, context);
                if (modelData == null) {
                    failedError("Parent model {} for {}:{} could not be found at {}", parent, context.namespace().getNamespaceName(), relPath, context.value().getPath());
                    return null;
                }
                continue;
            }
            break;
        }
        return parents;
    }

    private Map<String, TextureReferenceWithSource> getTextureData(ModelWithSource modelData) {
        Map<String, TextureReferenceWithSource> textureData = new HashMap<>();

        if (modelData.modelObject().has("textures") && modelData.modelObject().get("textures").isJsonObject()) {
            JsonObject textures = modelData.modelObject().getAsJsonObject("textures");
            for (String key : textures.keySet()) {
                textureData.put(key, new TextureReferenceWithSource(modelData.vanilla(), textures.getAsJsonPrimitive(key).getAsString()));
            }
        }
        return textureData;
    }

    private boolean detectReferenceChain(Map<String, TextureReferenceWithSource> textureData, ModelPathWithSource model, FileContext context) {
        for (TextureReferenceWithSource textureField : textureData.values()) {
            // a child is already failing, do not fail twice for the parent again
            if (!model.path().equals(FileUtils.getRelPath(context, FileUtils.Directory.MODELS)) && !textureField.vanilla()) continue;

            List<String> visited = new ArrayList<>();

            TextureReferenceWithSource currTextureField = textureField;

            for (int i = 0; i < MAX_DEPTH; i++) {
                if (currTextureField == null) continue;
                String value = currTextureField.referenceValue();
                if (!value.startsWith("#")) continue;
                String reference = value.substring(1);
                if (visited.contains(reference)) {
                    visited.add(reference);
                    String chain = String.join("->", visited);
                    failedError("Reference chain detected {} at {}", chain, context.value().getPath());
                    return false;
                }
                visited.add(reference);
                currTextureField = textureData.get(reference);
                if (i == MAX_DEPTH - 1) {
                    failedError("Reference chain depth is higher than {} at {}", MAX_DEPTH, context.value().getPath());
                    return false;
                }
            }
        }
        return true;
    }

    @Data
    @Accessors(fluent = true)
    private static class ModelPathWithSource {
        private final boolean vanilla;
        private final String path;
    }

    @Data
    @Accessors(fluent = true)
    private static class ModelWithSource {
        private final boolean vanilla;
        private final JsonObject modelObject;
    }

    @Data
    @Accessors(fluent = true)
    private static class TextureReferenceWithSource {
        private final boolean vanilla;
        private final String referenceValue;
    }

}
