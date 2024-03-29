package dev.kinau.resourcepackvalidator.validator.context;

import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode
@Accessors(fluent = true)
@Getter
public class FileContext implements ValidationContext<File> {

    private final OverlayNamespace namespace;
    private final File value;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + namespace.getName() + "@" + value.getPath() + ")";
    }
}
