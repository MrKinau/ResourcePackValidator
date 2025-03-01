package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import org.slf4j.event.Level;

import java.util.Map;

public class ModelHasParticleTextureValidator extends FileContextValidator<Map<String, String>, Void> {

    public ModelHasParticleTextureValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Void> isValid(ValidationJob job, FileContext context, Map<String, String> data) {
        if (!FileUtils.isArmorModel(context.value(), context.namespace())) {
            if (!data.containsKey("particle") && !data.containsKey("layer0")) {
                failedError("Model has not particle texture referenced at {}", context.value().getPath());
            }
        }
        return success();
    }

    @Override
    protected Level defaultFailedLogLevel() {
        return Level.WARN;
    }
}