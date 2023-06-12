package dev.kinau.resourcepackvalidator.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class Tuple<A, B> {
    private final A a;
    private final B b;
}
