package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonElementMapper extends MappingValidator<ValidationJob, EmptyValidationContext, JsonElement> {

    public JsonElementMapper(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Collection<FileContextWithData<JsonElement>>> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        List<FileContextWithData<JsonElement>> filesWithData = new ArrayList<>();
        job.jsonCache().values().forEach(namespaceJsonCache -> {
            if (!namespaceJsonCache.cache().containsKey(FileUtils.Directory.MODELS)) return;
            namespaceJsonCache.cache().get(FileUtils.Directory.MODELS).forEach(element -> {
                filesWithData.add(new FileContextWithData<>(namespaceJsonCache.namespace(), element.file(), element.element()));
            });
        });
        return success(filesWithData);
    }
}
