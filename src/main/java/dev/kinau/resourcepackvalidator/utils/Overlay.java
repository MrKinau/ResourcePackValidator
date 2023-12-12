package dev.kinau.resourcepackvalidator.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Overlay {
    private final File file;

    public String getName() {
        return file.getName();
    }
}
