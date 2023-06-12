package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModelHasAnyTextureValidator extends FileContextValidator<JsonObject, JsonObject> {

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        if (!modelObj.has("textures"))
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not present) at {}", context.value().getPath());
        if (!modelObj.get("textures").isJsonObject())
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not an object) at {}", context.value().getPath());
        if (modelObj.getAsJsonObject("textures").isEmpty())
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures object is empty) at {}", context.value().getPath());
        return success(modelObj.getAsJsonObject("textures"));
    }

    private ValidationResult<JsonObject> failOrSkip(FileContext context, ValidationJob job, JsonObject data, String warning, Object... args) {
        if (hasParent(context, job, data))
            return failedSilent();
        return failedWarning(warning, args);
    }

    private boolean hasParent(FileContext context, ValidationJob job, JsonObject obj) {
        if (obj.has("parent") && obj.get("parent").isJsonPrimitive() && obj.getAsJsonPrimitive("parent").isString()) {
            String parent = obj.getAsJsonPrimitive("parent").getAsString();
            return FileUtils.modelExists(context.namespace(), job.rootDir(), parent);
        }
        return false;
    }

}
