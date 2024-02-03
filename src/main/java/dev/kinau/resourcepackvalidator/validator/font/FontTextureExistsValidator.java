package dev.kinau.resourcepackvalidator.validator.font;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.data.font.BitmapFontProvider;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.Map;

public class FontTextureExistsValidator extends FileContextValidator<BitmapFontProvider, Void> {

    public FontTextureExistsValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Void> isValid(ValidationJob job, FileContext context, BitmapFontProvider fontProvider) {
        if (fontProvider.file() == null)
            return failedError("Font has invalid texture registered (file value is not string) for provider {} at {}", fontProvider, context.value().getPath());

        if (!FileUtils.textureExists(context.namespace(), fontProvider.file(), job.assetDictionary()))
            return failedError("Font has linked texture that is not present ({}) at {}", fontProvider + " » " + fontProvider.file(), context.value().getPath());

        return success();
    }


}
