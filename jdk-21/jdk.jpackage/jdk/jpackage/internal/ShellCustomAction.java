/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package jdk.jpackage.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface to add custom actions composed of shell commands to installers.
 */
abstract class ShellCustomAction {

    List<String> requiredPackages() {
        return Collections.emptyList();
    }

    final Map<String, String> create() throws IOException {
        Map<String, String> result = new HashMap<>();
        replacementStringIds().forEach(key -> {
            result.put(key, "");
        });
        result.putAll(createImpl());
        return result;
    }

    protected List<String> replacementStringIds() {
        return Collections.emptyList();
    }

    protected abstract Map<String, String> createImpl() throws IOException;

    static ShellCustomAction nop(List<String> replacementStringIds) {
        return new ShellCustomAction() {
            @Override
            protected List<String> replacementStringIds() {
                return replacementStringIds;
            }

            @Override
            protected Map<String, String> createImpl() throws IOException {
                return Map.of();
            }
        };
    }

    protected static String stringifyShellCommands(String... commands) {
        return stringifyShellCommands(Arrays.asList(commands));
    }

    protected static String stringifyShellCommands(List<String> commands) {
        return String.join("\n", commands.stream().filter(
                s -> s != null && !s.isEmpty()).toList());
    }

    protected static String stringifyTextFile(String resourceName) throws IOException {
        try ( InputStream is = OverridableResource.readDefault(resourceName);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
