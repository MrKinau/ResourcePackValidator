package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestCase;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonElementMapper extends Validator<ValidationJob, EmptyValidationContext, Collection<FileContextWithData<JsonElement>>> {

    protected final List<Validator<JsonElement, FileContext, ?>> chainedValidators = new ArrayList<>();

    public JsonElementMapper(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    public <V> Validator<ValidationJob, EmptyValidationContext, Collection<FileContextWithData<JsonElement>>> thenForEachElement(Validator<JsonElement, FileContext, V> next) {
        this.chainedValidators.add(next);
        return this;
    }

    @Override
    public ValidationResult.Status validate(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        if (shouldSkip(context))
            return ValidationResult.Status.SKIPPED;
        TestCase testCase = null;
        boolean skipTestCase = skipTestCase(context);
        if (!skipTestCase)
            testCase = testSuite.getCase(getClass()).start();
        ValidationResult<Collection<FileContextWithData<JsonElement>>> result = isValid(job, context, data);
        if (!skipTestCase)
            testCase.stop();

        boolean anyChainedValidatorFailed = false;
        for (Validator<JsonElement, FileContext, ?> chainedValidator : chainedValidators) {
            for (FileContextWithData<JsonElement> contextAndData : result.result()) {
                ValidationResult.Status status = chainedValidator.validate(job, contextAndData, contextAndData.data());
                if (status == ValidationResult.Status.FAILED)
                    anyChainedValidatorFailed = true;
            }
        }
        return anyChainedValidatorFailed ? ValidationResult.Status.FAILED : ValidationResult.Status.SUCCESS;
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
