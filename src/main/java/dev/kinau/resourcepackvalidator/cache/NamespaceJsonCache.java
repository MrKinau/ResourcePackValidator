package dev.kinau.resourcepackvalidator.cache;

import com.google.gson.JsonParser;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.Namespace;

import java.io.File;
import java.io.FileReader;

public class NamespaceJsonCache extends NamespacedCache<JsonElementWithFile> {

    public NamespaceJsonCache(Namespace namespace) {
        super(namespace, ".json");
    }

    @Override
    protected boolean isValid(FileUtils.Directory directory) {
        return directory.containsJson();
    }

    @Override
    protected JsonElementWithFile loadFile(File file) throws Exception {
        return new JsonElementWithFile(file, JsonParser.parseReader(new FileReader(file)));
    }
}
