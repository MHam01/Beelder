package com.beelder.processor.classbuilder.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.beelder.processor.utils.StringBuilderUtils.indent;

public final class StatementBuilder {
    private StatementBuilder() {
        // Util class for creating statements, mostly used in Methods
    }


    /**
     * Creates a new assignment of the form "source.assign = to;".
     *
     * @return Assignment as string
     */
    public static String createAssignment(final String source, final String assign, final String to) {
        return String.format("%s.%s = %s;", source, assign, to);
    }

    /**
     * Creates a new method call of the form "source.method(param0, param1, ...);".
     *
     * @return Call as string
     */
    public static String createMethodCall(final String source, final String method, final String... params) {
        return String.format("%s.%s(%s);", source, method, String.join(", ", params));
    }

    /**
     * Creates a new try block from the given lines.
     *
     * @param lines Initial lines
     * @return The try block
     */
    public static TryBlock createTryBlock(final String... lines) {
        return new TryBlock(lines);
    }

    public static class TryBlock extends Type {
        private final List<String> lines = new ArrayList<>();
        private final Map<String, List<String>> catchClauses = new HashMap<>();

        private TryBlock(String... lines) {
            super("TRY");

            Collections.addAll(this.lines, lines);
        }

        /**
         * Adds a new line to this try block, ";" is possible needed.
         *
         * @param line The line to add
         */
        public void addLine(final String line) {
            this.lines.add(line);
        }

        /**
         * Adds a new line to this trys catch block for the given name, ";" is possible needed.
         *
         * @param line The line to add
         * @param catches The catch block to add to
         */
        public void addLineToCatchClause(final String line, final String catches) {
            this.catchClauses.computeIfAbsent(catches, k -> new ArrayList<>()).add(line);
        }

        /**
         * Adds a new line to this trys catch block for the given names joined with " | ",
         * ";" is possible needed.
         *
         * @param line The line to add
         * @param catches The catch block to add to
         */
        public void addLineToCatchClause(final String line, final String... catches) {
            addLineToCatchClause(line, String.join(" | ", catches));
        }

        @Override
        public String build(int depth) {
            final StringBuilder tryString = new StringBuilder("try {\n");
            this.lines.forEach(l -> indent(tryString, depth + 1).append(l).append('\n'));
            indent(tryString, depth).append("}");

            if(!this.catchClauses.isEmpty()) {
                for(final String catches:catchClauses.keySet()) {
                    tryString.append(" catch (").append(catches).append(" exc) {\n");
                    this.catchClauses.get(catches).forEach(l -> indent(tryString, depth + 1).append(l).append('\n'));
                    indent(tryString, depth).append("}");
                }
            }

            return tryString.toString();
        }
    }
}
