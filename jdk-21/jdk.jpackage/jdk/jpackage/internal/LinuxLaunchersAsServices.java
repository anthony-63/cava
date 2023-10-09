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
import java.util.List;
import java.util.Map;
import static jdk.jpackage.internal.OverridableResource.createResource;

/**
 * Helper to install launchers as services using "systemd".
 */
public final class LinuxLaunchersAsServices extends UnixLaunchersAsServices {

    private LinuxLaunchersAsServices(PlatformPackage thePackage,
            Map<String, Object> params) throws IOException {
        super(thePackage, REQUIRED_PACKAGES, params, li -> {
            return new Launcher(thePackage, li.getName(), params);
        });
    }

    static ShellCustomAction create(PlatformPackage thePackage,
            Map<String, Object> params) throws IOException {
        if (StandardBundlerParam.isRuntimeInstaller(params)) {
            return ShellCustomAction.nop(REPLACEMENT_STRING_IDS);
        }
        return new LinuxLaunchersAsServices(thePackage, params);
    }

    public static Path getServiceUnitFileName(String packageName,
            String launcherName) {
        String baseName = launcherName.replaceAll("[\\s]", "_");
        return Path.of(packageName + "-" + baseName + ".service");
    }

    private static class Launcher extends UnixLauncherAsService {

        Launcher(PlatformPackage thePackage, String name,
                Map<String, Object> mainParams) {
            super(name, mainParams, createResource("unit-template.service",
                    mainParams).setCategory(I18N.getString(
                            "resource.systemd-unit-file")));

            unitFilename = getServiceUnitFileName(thePackage.name(), getName());

            getResource()
                    .setPublicName(unitFilename)
                    .addSubstitutionDataEntry("APPLICATION_LAUNCHER",
                            Enquoter.forPropertyValues().applyTo(
                                    thePackage.installedApplicationLayout().launchersDirectory().resolve(
                                            getName()).toString()));
        }

        @Override
        Path descriptorFilePath(Path root) {
            return root.resolve("lib/systemd/system").resolve(unitFilename);
        }

        private final Path unitFilename;
    }

    private final static List<String> REQUIRED_PACKAGES = List.of("systemd",
            "coreutils" /* /usr/bin/wc */, "grep");
}
