package dev.kinau.resourcepackvalidator.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class ValidationResult<U> {

    private final U result;
    private final Status status;

    public enum Status {
        SUCCESS,
        FAILED,
        SKIPPED
    }
}
