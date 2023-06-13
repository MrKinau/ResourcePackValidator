package dev.kinau.resourcepackvalidator.report;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Accessors(fluent = true)
@Getter
public class TestSuite {

    private final List<TestCase> testCases = new ArrayList<>();

    public TestCase getCase(Class<?> clazz) {
        return getCase(clazz.getSimpleName());
    }

    public TestCase getCase(String caseName) {
        Optional<TestCase> optTestCase = testCases.stream().filter(testCase -> testCase.name().equals(caseName))
                .findAny();
        if (optTestCase.isPresent())
            return optTestCase.get();
        TestCase testCase = new TestCase(caseName);
        testCases.add(testCase);
        return testCase;
    }

    public boolean hasNoFailure() {
        return testCases.stream().noneMatch(testCase -> testCase.failure() != null && testCase.failure().error());
    }
}
