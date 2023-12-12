package dev.kinau.resourcepackvalidator.cache;

import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.OverlayNamespace;

import javax.imageio.ImageIO;
import java.io.File;

public class NamespaceTextureCache extends NamespacedCache<TextureWithFile> {

    public NamespaceTextureCache(OverlayNamespace namespace) {
        super(namespace, ".png");
    }

    @Override
    protected boolean isValid(FileUtils.Directory directory) {
        return true;
    }

    @Override
    protected TextureWithFile loadFile(File file) throws Exception {
        return new TextureWithFile(file, ImageIO.read(file));
    }
}
