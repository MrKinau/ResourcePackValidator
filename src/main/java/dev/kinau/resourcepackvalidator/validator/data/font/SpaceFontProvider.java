package dev.kinau.resourcepackvalidator.validator.data.font;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
@Getter
@ToString
public class SpaceFontProvider extends FontProvider {

    private final Map<String, Float> advances;

    public SpaceFontProvider(Map<String, Float> advances) {
        super("space");
        this.advances = advances;
    }

    @Override
    public List<Integer> getUsedCharacters() {
        if (advances == null) return Collections.emptyList();
        return advances.keySet().stream().flatMapToInt(String::codePoints).boxed().toList();
    }
}
