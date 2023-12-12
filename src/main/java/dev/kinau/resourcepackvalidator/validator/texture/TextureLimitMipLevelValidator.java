package dev.kinau.resourcepackvalidator.validator.texture;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;

public class TextureLimitMipLevelValidator extends FileContextValidator<BufferedImage, Void> {

    public TextureLimitMipLevelValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Void> isValid(ValidationJob job, FileContext context, BufferedImage data) {
        int width = data.getWidth();
        int height = data.getHeight();
        int minPowerOfTwo = getMinimumPowerOfTwo(width, height);
        if (minPowerOfTwo < MipLevel.FOUR.powerOfTwo()) {
            MipLevel mipLevel = Arrays.stream(MipLevel.values()).filter(mipLevels -> minPowerOfTwo == mipLevels.powerOfTwo()).findAny().orElse(MipLevel.ZERO);
            return failedError("Texture {} limits mip level from 4 to {}, because of minimum power of two: {} size {}x{}", context.value().getPath(), mipLevel.displayNumber(), minPowerOfTwo, width, height);
        }
        return success();
    }

    private int getMinimumPowerOfTwo(int width, int height) {
        return Math.min(getMinimumPowerOfTwo(width), getMinimumPowerOfTwo(height));
    }

    private int getMinimumPowerOfTwo(int n) {
        int power = 1;
        while (n >= 2 && n % 2 == 0) {
            power *= 2;
            n /= 2;
        }
        return power;
    }

    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = true)
    enum MipLevel {
        ZERO(0, 1),
        ONE(1, 2),
        TWO(2, 4),
        THREE(3, 8),
        FOUR(4, 16);

        private final int displayNumber;
        private final int powerOfTwo;
    }
}
