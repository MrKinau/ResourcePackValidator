package dev.kinau.resourcepackvalidator.validator.models.override;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.Map;

public class ModelOverridesExistsValidator extends FileContextValidator<JsonArray, Object> {

    public ModelOverridesExistsValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Object> isValid(ValidationJob job, FileContext context, JsonArray data) {
        boolean failed = false;
        for (JsonElement datum : data) {
            if (!datum.isJsonObject()) {
                failed = true;
                failedError("Invalid override found (not a json object) at {}", context.value().getPath());
                continue;
            }
            JsonObject override = datum.getAsJsonObject();
            if (!override.has("model")) {
                failed = true;
                failedError("No model in override {} at {}", override.toString(), context.value().getPath());
                continue;
            }
            if (!override.get("model").isJsonPrimitive() || !override.get("model").getAsJsonPrimitive().isString()) {
                failed = true;
                failedError("Model in override {} is wrong type (not primitive) at {}", override.toString(), context.value().getPath());
                continue;
            }
            String model = override.getAsJsonPrimitive("model").getAsString();

            if (!FileUtils.modelExists(context.namespace(), model, job.assetDictionary())) {
                failed = true;
                failedError("Model in override {} does not exist at {}", override, context.value().getPath());
            }
        }
        if (failed)
            return failedError();
        return success();
    }
}
