package dev.kinau.resourcepackvalidator.validator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestCase;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class Validator<Input, Context extends ValidationContext<?>, Output> {

    private final Map<String, JsonObject> config;
    protected final TestSuite testSuite;
    protected final List<Validator<Output, Context, ?>> chainedValidators = new ArrayList<>();

    public <V> Validator<Input, Context, Output> then(Validator<Output, Context, V> next) {
        this.chainedValidators.add(next);
        return this;
    }

    public ValidationResult.Status validate(ValidationJob job, Context context, Input data) {
        if (shouldSkip(context))
            return ValidationResult.Status.SKIPPED;
        TestCase testCase = null;
        boolean skipTestCase = skipTestCase(context);
        if (!skipTestCase)
            testCase = testSuite.getCase(getClass()).start();
        ValidationResult<Output> result = isValid(job, context, data);
        if (!skipTestCase)
            testCase.stop();
        if (result.status() != ValidationResult.Status.SUCCESS)
            return result.status();

        boolean anyChainedValidatorFailed = false;
        for (Validator<Output, Context, ?> chainedValidator : chainedValidators) {
            ValidationResult.Status status = chainedValidator.validate(job, context, result.result());
            if (status == ValidationResult.Status.FAILED)
                anyChainedValidatorFailed = true;
        }
        return anyChainedValidatorFailed ? ValidationResult.Status.FAILED : ValidationResult.Status.SUCCESS;
    }

    protected abstract ValidationResult<Output> isValid(ValidationJob job, Context context, Input data);

    protected Level defaultFailedLogLevel() {
        return Level.ERROR;
    }

    protected boolean shouldSkip(Context context) {
        return !configValue("enabled", new JsonPrimitive(true)).getAsBoolean();
    }

    protected boolean skipTestCase(Context context) {
        return false;
    }

    final protected JsonObject config() {
        return config.getOrDefault(getClass().getSimpleName(), new JsonObject());
    }

    final protected <T extends JsonElement> T configValue(String key, T defaultValue) {
        return (T) (Optional.ofNullable(config().get(key)).orElse(defaultValue));
    }

    final protected String logPrefix() {
        return String.format("[%-30s] ", getClass().getSimpleName());
    }

    final protected ValidationResult<Output> failedError(String error, Object... args) {
        Level logLevel = failedLogLevel();
        log.atLevel(logLevel).log(logPrefix() + error, args);
        testSuite.getCase(getClass()).addErrorNoMessage(logLevel, String.format(error.replace("{}", "%s"), args));
        return new ValidationResult<>(null, logLevel == Level.WARN ? ValidationResult.Status.SKIPPED : ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> failedError() {
        Level logLevel = failedLogLevel();
        return new ValidationResult<>(null, logLevel == Level.WARN ? ValidationResult.Status.SKIPPED : ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> skip(String error, Object... args) {
        log.debug(logPrefix() + error, args);
        return new ValidationResult<>(null, ValidationResult.Status.SKIPPED);
    }

    final protected ValidationResult<Output> skip() {
        return new ValidationResult<>(null, ValidationResult.Status.SKIPPED);
    }

    final protected ValidationResult<Output> success(Output result) {
        return new ValidationResult<>(result, ValidationResult.Status.SUCCESS);
    }

    final protected ValidationResult<Output> success() {
        return new ValidationResult<>(null, ValidationResult.Status.SUCCESS);
    }

    final protected Level failedLogLevel() {
        try {
            return Level.valueOf(configValue("logLevel", new JsonPrimitive(defaultFailedLogLevel().name())).getAsString());
        } catch (Throwable e) {
            log.warn("Invalid configured log level. Possible values are " + Arrays.stream(Level.values())
                    .map(Enum::name).collect(Collectors.joining(", ")), e);
        }
        return defaultFailedLogLevel();
    }

    final protected List<PathMatcher> ignoreList() {
        List<PathMatcher> ignoreList = new ArrayList<>();
        for (JsonElement element : configValue("ignore", new JsonArray())) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) continue;
            ignoreList.add(FileSystems.getDefault().getPathMatcher(element.getAsString()));
        }
        return ignoreList;
    }
}
