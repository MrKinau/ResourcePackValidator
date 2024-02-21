package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.Map;

@Slf4j
public class ModelHasAnyTextureValidator extends FileContextValidator<JsonObject, JsonObject> {

    public ModelHasAnyTextureValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        if (!modelObj.has("textures"))
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not present) at {}", context.value().getPath());
        if (!modelObj.get("textures").isJsonObject())
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures attribute not an object) at {}", context.value().getPath());
        if (modelObj.getAsJsonObject("textures").isEmpty())
            return failOrSkip(context, job, modelObj, "No textures specified in model (textures object is empty) at {}", context.value().getPath());
        JsonObject textures = modelObj.getAsJsonObject("textures");
        if (textures.keySet().stream().allMatch(s -> s.equals("particle"))) {
            ValidationResult<JsonObject> result = failOrIgnore(context, job, modelObj, "No textures specified in model (textures object only contains particles, although this maybe works, it's kinda illegal) at {}", context.value().getPath());
            if (result != null) return result;
        }
        return success(modelObj);
    }

    private ValidationResult<JsonObject> failOrSkip(FileContext context, ValidationJob job, JsonObject data, String warning, Object... args) {
        if (hasParent(context, job, data))
            return skip();
        return failedError(warning, args);
    }

    private ValidationResult<JsonObject> failOrIgnore(FileContext context, ValidationJob job, JsonObject data, String warning, Object... args) {
        if (!hasParent(context, job, data))
            return failedError(warning, args);
        return null;
    }

    private boolean hasParent(FileContext context, ValidationJob job, JsonObject obj) {
        if (obj.has("parent") && obj.get("parent").isJsonPrimitive() && obj.getAsJsonPrimitive("parent").isString()) {
            String parent = obj.getAsJsonPrimitive("parent").getAsString();
            return FileUtils.modelExists(context.namespace(), parent, job.assetDictionary());
        }
        return false;
    }

    @Override
    protected Level defaultFailedLogLevel() {
        return Level.WARN;
    }
}
