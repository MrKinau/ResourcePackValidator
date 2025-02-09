package dev.kinau.resourcepackvalidator.validator.general;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.cache.JsonElementWithFile;
import dev.kinau.resourcepackvalidator.cache.NamespaceJsonCache;
import dev.kinau.resourcepackvalidator.cache.NamespaceTextureCache;
import dev.kinau.resourcepackvalidator.cache.TextureWithFile;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class InvalidPathValidator extends Validator<ValidationJob, EmptyValidationContext, Void> {

    public InvalidPathValidator(Map<String, JsonObject> config, TestSuite testSuite) {
        super(config, testSuite);
    }

    @Override
    protected ValidationResult<Void> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        boolean failed = false;
        Set<String> invalidNamespaces = new HashSet<>();

        for (NamespaceJsonCache namespaceJsonCache : job.jsonCache().values()) {
            String namespace = namespaceJsonCache.namespace().getNamespaceName();
            if (!isValidNamespace(namespace)) {
                failed = true;
                failedError("Invalid namespace name {} (allowed are [a-z0-9-_.])", namespace);
                invalidNamespaces.add(namespace);
            }
            for (Map.Entry<FileUtils.Directory, JsonElementWithFile> entry : namespaceJsonCache.cache().entries()) {
                JsonElementWithFile value = entry.getValue();
                String path = FileUtils.getRelPath(value.file(), entry.getKey(), namespace);
                if (!isValidPath(path)) {
                    failed = true;
                    failedError("Invalid path {}:{} (allowed are [a-z0-9-_./])", namespace, path);
                }
            }
        }

        for (NamespaceTextureCache namespaceTextureCache : job.textureCache().values()) {
            String namespace = namespaceTextureCache.namespace().getNamespaceName();
            if (!invalidNamespaces.contains(namespace) && !isValidNamespace(namespace)) {
                failed = true;
                failedError("Invalid namespace name {} (allowed are [a-z0-9-_.])", namespace);
            }
            for (Map.Entry<FileUtils.Directory, TextureWithFile> entry : namespaceTextureCache.cache().entries()) {
                TextureWithFile value = entry.getValue();
                String path = FileUtils.getRelPath(value.file(), entry.getKey(), namespace);
                if (!isValidPath(path)) {
                    failed = true;
                    failedError("Invalid path {}:{} (allowed are [a-z0-9-_./])", namespace, path);
                }
            }
        }

        return failed ? failedError() : success();
    }

    private boolean isValidString(String str, Collection<Character> extraChars) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!(c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.' || extraChars.contains(c))) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidNamespace(String namespace) {
        return isValidString(namespace, Collections.emptySet());
    }

    private boolean isValidPath(String path) {
        return isValidString(path, Set.of('/'));
    }
}
