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

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Shell scripts of a package.
 */
final class PackageScripts<T extends Enum<T> & Supplier<OverridableResource>> {

    static <T extends Enum<T> & Supplier<OverridableResource>> PackageScripts<T> create(
            Class<T> scriptIdsType) {
        return new PackageScripts<>(scriptIdsType);
    }

    PackageScripts(Class<T> scriptIdsType) {
        scripts = EnumSet.allOf(scriptIdsType).stream().collect(
                Collectors.toMap(UnaryOperator.identity(), scriptId -> {
                    return new ShellScriptResource(scriptId.name()).setResource(
                            scriptId.get());
                }));
    }

    PackageScripts<T> setSubstitutionData(T id, Map<String, String> data) {
        scripts.get(id).getResource().setSubstitutionData(data);
        return this;
    }

    PackageScripts<T> setSubstitutionData(Map<String, String> data) {
        scripts.values().forEach(
                script -> script.getResource().setSubstitutionData(data));
        return this;
    }

    PackageScripts<T> setResourceDir(Path v) throws IOException {
        for (var script : scripts.values()) {
            script.getResource().setResourceDir(v);
        }
        return this;
    }

    void saveInFolder(Path folder) throws IOException {
        for (var script : scripts.values()) {
            script.saveInFolder(folder);
        }
    }

    static class ResourceConfig {

        ResourceConfig(String defaultName, String categoryId) {
            this.defaultName = defaultName;
            this.category = I18N.getString(categoryId);
        }

        OverridableResource createResource() {
            var resource = new OverridableResource(defaultName).setCategory(category);
            return getDefaultPublicName().map(resource::setPublicName).orElse(
                    resource);
        }

        private Optional<String> getDefaultPublicName() {
            final String wellKnownSuffix = ".template";
            if (defaultName.endsWith(wellKnownSuffix)) {
                return Optional.of(defaultName.substring(0, defaultName.length()
                        - wellKnownSuffix.length()));
            }
            return Optional.ofNullable(null);
        }

        private final String defaultName;
        private final String category;
    }

    private final Map<T, ShellScriptResource> scripts;
}
