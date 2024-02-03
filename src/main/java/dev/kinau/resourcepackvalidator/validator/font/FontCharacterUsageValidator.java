package dev.kinau.resourcepackvalidator.validator.font;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Chars;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.data.font.FontProvider;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class FontCharacterUsageValidator extends FileContextValidator<FontProvider, Void> {

    private final Multimap<File, Integer> usedCharactersMap = HashMultimap.create();

    public FontCharacterUsageValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Void> isValid(ValidationJob job, FileContext context, FontProvider fontProvider) {
        boolean failed = false;
        for (Integer usedCharacter : fontProvider.getUsedCharacters()) {
            if (usedCharacter == 0) continue; // Exception for null byte. because it's used at end of Mojangs characters
            if (usedCharacter == ' ') continue; // Exception for space, because it's registered as space provider and character
            if (usedCharactersMap.containsEntry(context.value(), usedCharacter)) {
                failed = true;
                failedError("Font provider uses the same character multiple times (file: {}, character:|{}|,|{}|)", context.value().getPath(), String.copyValueOf(Character.toChars(usedCharacter)), Chars.asList(Character.toChars(usedCharacter)).stream().map(Integer::toHexString).map(s -> "\\u" + s).collect(Collectors.joining()));
            }
            usedCharactersMap.put(context.value(), usedCharacter);
        }

        if (failed)
            return failedError();
        return success();
    }
}
