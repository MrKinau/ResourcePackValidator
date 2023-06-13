package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;

import java.util.Map;

public abstract class FileContextValidator<Input, Output> extends Validator<Input, FileContext, Output> {

    public FileContextValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected boolean shouldSkip(FileContext context) {
        return super.shouldSkip(context) || ignoreList().stream().anyMatch(pathMatcher -> pathMatcher.matches(context.value().toPath()));
    }
}
