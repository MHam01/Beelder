package com.beelder.processor.classbuilder;

import com.beelder.processor.classbuilder.entities.Clazz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Singleton class to store all current {@link Clazz} instance, used for later building.
 */
public final class ClazzBuilder {
    private static final ClazzBuilder instance = new ClazzBuilder();

    private ClazzBuilder() {
        // Singleton
    }

    /**
     * Maps class names to {@link Clazz} objects.
     */
    private final Map<String, Clazz> cache = new HashMap<>();

    /**
     * Returns a stored class for the given name, creates it if
     * not existing.
     *
     * @param name The classes name
     * @return The class object
     */
    public static Clazz getRootForName(final String name) {
        return instance.cache.computeIfAbsent(name, Clazz::new);
    }

    /**
     * @return Unmodifiable set containing all stored classes
     */
    public static Set<Clazz> fetchAllClazzes() {
        return instance.cache.values().stream().collect(Collectors.toUnmodifiableSet());
    }
}