package dev.kinau.resourcepackvalidator.validator.context;

public interface ValidationContext<T> {

    EmptyValidationContext EMPTY = new EmptyValidationContext();

    T value();
}
