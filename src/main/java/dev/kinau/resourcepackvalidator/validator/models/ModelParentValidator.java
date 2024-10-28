package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ModelParentValidator extends FileContextValidator<JsonObject, JsonObject> {

    public ModelParentValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonObject data) {
        if (!data.has("parent")) return skip();
        JsonElement parent = data.get("parent");
        if (!parent.isJsonPrimitive()) return failedError("Model defined parent which is not a JsonPrimitive {} at {}", parent, context.value().getPath());
        if (!parent.getAsJsonPrimitive().isString()) return failedError("Model defined parent which is not a String {} at {}", parent, context.value().getPath());
        if (!FileUtils.modelExists(context.namespace(), parent.getAsString(), job.assetDictionary()))
            return failedError("Model defined parent which does not exist {} at {} ", parent.getAsString(), context.value().getPath());
        return success(data);
    }
}
