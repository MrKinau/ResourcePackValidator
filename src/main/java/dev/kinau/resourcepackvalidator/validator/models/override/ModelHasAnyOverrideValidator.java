package dev.kinau.resourcepackvalidator.validator.models.override;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

public class ModelHasAnyOverrideValidator extends FileContextValidator<JsonObject, JsonArray> {

    @Override
    protected ValidationResult<JsonArray> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        if (!modelObj.has("overrides"))
            return failedSilent();
        if (!modelObj.get("overrides").isJsonArray())
            return failedSilent();
        return success(modelObj.getAsJsonArray("overrides"));
    }
}
