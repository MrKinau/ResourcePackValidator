package dev.kinau.resourcepackvalidator.validator.context;

import com.google.gson.JsonElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
public class JsonElementWithFile {

    private final File file;
    private final JsonElement element;

    @Override
    public String toString() {
        return file.getPath();
    }
}
