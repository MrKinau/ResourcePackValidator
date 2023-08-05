package dev.kinau.resourcepackvalidator.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
public abstract class DataWithFile<Data> {

    private final File file;
    private final Data element;

    @Override
    public String toString() {
        return file.getPath();
    }
}
