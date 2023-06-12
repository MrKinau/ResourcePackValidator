package dev.kinau.resourcepackvalidator.validator;

import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Validator<Input, Context extends ValidationContext<?>, Output> {

    protected final List<Validator<Output, Context, ?>> chainedValidators = new ArrayList<>();

    public <V> Validator<Input, Context, Output> then(Validator<Output, Context, V> next) {
        this.chainedValidators.add(next);
        return this;
    }

    public boolean validate(ValidationJob job, Context context, Input data) {
        ValidationResult<Output> result = isValid(job, context, data);
        if (result.status() == ValidationResult.Status.FAILED)
            return false;

        for (Validator<Output, Context, ?> chainedValidator : chainedValidators) {
            chainedValidator.validate(job, context, result.result());
        }
        return true;
    }

    protected abstract ValidationResult<Output> isValid(ValidationJob job, Context context, Input data);

    final protected ValidationResult<Output> failedError(String error, Object... args) {
        log.error(error, args);
        return new ValidationResult<>(null, ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> failedWarning(String error, Object... args) {
        log.warn(error, args);
        return new ValidationResult<>(null, ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> failedSilent(String error, Object... args) {
        log.debug(error, args);
        return new ValidationResult<>(null, ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> failedSilent() {
        return new ValidationResult<>(null, ValidationResult.Status.FAILED);
    }

    final protected ValidationResult<Output> success(Output result) {
        return new ValidationResult<>(result, ValidationResult.Status.SUCCESS);
    }

    final protected ValidationResult<Output> success() {
        return new ValidationResult<>(null, ValidationResult.Status.SUCCESS);
    }
}
