package dev.kinau.resourcepackvalidator.validator.context;

import dev.kinau.resourcepackvalidator.utils.Namespace;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

@Accessors(fluent = true)
@Getter
public class FileContextWithData<T> extends FileContext {

    private final T data;

    public FileContextWithData(Namespace namespace, File value, T data) {
        super(namespace, value);
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString() + " + " + getClass().getSimpleName() + "(" + Optional.ofNullable(data).map(Objects::toString).orElse("null") + ")";
    }
}
