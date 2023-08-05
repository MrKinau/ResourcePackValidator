package dev.kinau.resourcepackvalidator.cache;

import com.google.gson.JsonElement;

import java.io.File;

public class JsonElementWithFile extends DataWithFile<JsonElement> {

    public JsonElementWithFile(File file, JsonElement element) {
        super(file, element);
    }
}
