package dev.kinau.resourcepackvalidator.validator.data.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public abstract class FontProvider {
    private final String type;

    public abstract List<Integer> getUsedCharacters();
}
