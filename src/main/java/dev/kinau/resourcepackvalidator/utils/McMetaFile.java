package dev.kinau.resourcepackvalidator.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class McMetaFile {
    private final File file;
    private final List<Overlay> overlays;
}
