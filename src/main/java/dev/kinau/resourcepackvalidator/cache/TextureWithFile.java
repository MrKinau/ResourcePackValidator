package dev.kinau.resourcepackvalidator.cache;

import java.awt.image.BufferedImage;
import java.io.File;

public class TextureWithFile extends DataWithFile<BufferedImage> {

    public TextureWithFile(File file, BufferedImage element) {
        super(file, element);
    }
}
