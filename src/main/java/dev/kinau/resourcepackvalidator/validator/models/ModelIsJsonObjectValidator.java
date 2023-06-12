package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

public class ModelIsJsonObjectValidator extends FileContextValidator<JsonElement, JsonObject> {

    @Override
    protected ValidationResult<JsonObject> isValid(ValidationJob job, FileContext context, JsonElement data) {
        if (data.isJsonObject())
            return success(data.getAsJsonObject());
        return failedError("Found invalid model (not Json Object) at {}", context.value().getPath());
    }
}
