package dev.kinau.resourcepackvalidator.validator.texture;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.awt.image.BufferedImage;
import java.util.Map;

public class TextureIsNotCorruptedValidator extends FileContextValidator<BufferedImage, BufferedImage> {

    public TextureIsNotCorruptedValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<BufferedImage> isValid(ValidationJob job, FileContext context, BufferedImage data) {
        if (data == null)
            return failedError("Texture is corrupted, which is located at {}", context.value().getPath());
        return success(data);
    }
}
