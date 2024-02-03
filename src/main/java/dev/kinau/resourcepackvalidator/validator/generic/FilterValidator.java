package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.ValidationContext;

import java.util.Map;
import java.util.function.BiFunction;

public class FilterValidator<Input, Context extends ValidationContext<?>, Output> extends Validator<Input, Context, Output> {

    private final BiFunction<ValidationJob, Input, Output> predicate;

    public FilterValidator(Map<String, JsonObject> config, TestSuite testSuite, BiFunction<ValidationJob, Input, Output> predicate) {
        super(config, testSuite);
        this.predicate = predicate;
    }

    @Override
    protected ValidationResult<Output> isValid(ValidationJob job, Context context, Input data) {
        Output out = predicate.apply(job, data);
        if (out == null)
            return skip();
        return success(out);    }
}
