package dev.kinau.resourcepackvalidator.validator.data.font;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Accessors(fluent = true)
@Getter
@ToString
public class BitmapFontProvider extends FontProvider {

    private final String file;
    private final Integer height;
    private final Integer ascent;
    private final List<String> chars;

    public BitmapFontProvider(String file, Integer height, Integer ascent, List<String> chars) {
        super("bitmap");
        this.file = file;
        this.height = height;
        this.ascent = ascent;
        this.chars = chars;
    }

    @Override
    public List<Integer> getUsedCharacters() {
        if (chars == null) return Collections.emptyList();
        return chars.stream().flatMapToInt(String::codePoints).boxed().toList();
    }
}
