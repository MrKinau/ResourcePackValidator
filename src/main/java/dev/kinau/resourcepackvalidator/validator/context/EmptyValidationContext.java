package dev.kinau.resourcepackvalidator.validator.context;

public class EmptyValidationContext implements ValidationContext<Object> {
    @Override
    public Object value() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
