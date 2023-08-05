package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestCase;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class MappingValidator<Input, Context extends ValidationContext<?>, Output> extends Validator<Input, Context, Collection<FileContextWithData<Output>>> {

    protected final List<Validator<Output, FileContext, ?>> chainedValidators = new ArrayList<>();

    public MappingValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    public <V> Validator<Input, Context, Collection<FileContextWithData<Output>>> thenForEachElement(Validator<Output, FileContext, V> next) {
        this.chainedValidators.add(next);
        return this;
    }

    @Override
    public ValidationResult.Status validate(ValidationJob job, Context context, Input data) {
        if (shouldSkip(context))
            return ValidationResult.Status.SKIPPED;
        TestCase testCase = null;
        boolean skipTestCase = skipTestCase(context);
        if (!skipTestCase)
            testCase = testSuite.getCase(getClass()).start();
        ValidationResult<Collection<FileContextWithData<Output>>> result = isValid(job, context, data);
        if (!skipTestCase)
            testCase.stop();

        boolean anyChainedValidatorFailed = false;
        for (Validator<Output, FileContext, ?> chainedValidator : chainedValidators) {
            for (FileContextWithData<Output> contextAndData : result.result()) {
                ValidationResult.Status status = chainedValidator.validate(job, contextAndData, contextAndData.data());
                if (status == ValidationResult.Status.FAILED)
                    anyChainedValidatorFailed = true;
            }
        }
        return anyChainedValidatorFailed ? ValidationResult.Status.FAILED : ValidationResult.Status.SUCCESS;
    }
}
