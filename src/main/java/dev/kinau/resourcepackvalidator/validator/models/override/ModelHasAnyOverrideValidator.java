package dev.kinau.resourcepackvalidator.validator.models.override;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.Map;

public class ModelHasAnyOverrideValidator extends FileContextValidator<JsonObject, JsonArray> {

    public ModelHasAnyOverrideValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<JsonArray> isValid(ValidationJob job, FileContext context, JsonObject modelObj) {
        if (!modelObj.has("overrides"))
            return skip();
        if (!modelObj.get("overrides").isJsonArray())
            return skip();
        return success(modelObj.getAsJsonArray("overrides"));
    }
}
