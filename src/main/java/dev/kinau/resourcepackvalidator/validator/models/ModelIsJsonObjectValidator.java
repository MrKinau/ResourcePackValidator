package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.Map;

public class ModelIsJsonObjectValidator extends FileContextValidator<JsonElement, JsonObject> {

    public ModelIsJsonObjectValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonElement data) {
        if (data.isJsonObject())
            return success(data.getAsJsonObject());
        return failedError("Found invalid model (not Json Object) at {}", context.value().getPath());
    }
}
