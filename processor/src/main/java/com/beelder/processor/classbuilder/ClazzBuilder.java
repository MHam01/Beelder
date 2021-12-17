package com.beelder.processor.classbuilder;

import com.beelder.processor.classbuilder.entities.Clazz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClazzBuilder {
    private static ClazzBuilder instance = new ClazzBuilder();

    private ClazzBuilder() {
        // Singleton
    }

    private Map<String, Clazz> cache = new HashMap<>();

    public static Clazz getRootForName(final String name) {
        return instance.cache.computeIfAbsent(name, Clazz::new);
    }

    public static Set<Clazz> fetchAllClazzes() {
        return instance.cache.values().stream().collect(Collectors.toUnmodifiableSet());
    }
}