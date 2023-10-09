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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shell script resource.
 */
final class ShellScriptResource {

    ShellScriptResource(String publicFileName) {
        this.publicFileName = Path.of(publicFileName);
    }

    void saveInFolder(Path folder) throws IOException {
        Path dstFile = folder.resolve(publicFileName);
        resource.saveToFile(dstFile);

        Files.setPosixFilePermissions(dstFile, Stream.of(execPerms, Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.OTHERS_READ
        )).flatMap(x -> x.stream()).collect(Collectors.toSet()));
    }

    ShellScriptResource setResource(OverridableResource v) {
        resource = v;
        return this;
    }

    ShellScriptResource onlyOwnerCanExecute(boolean v) {
        execPerms = v ? OWNER_CAN_EXECUTE : ALL_CAN_EXECUTE;
        return this;
    }

    OverridableResource getResource() {
        return resource;
    }

    final Path publicFileName;
    private Set<PosixFilePermission> execPerms = ALL_CAN_EXECUTE;
    private OverridableResource resource;

    private final static Set<PosixFilePermission> ALL_CAN_EXECUTE = Set.of(
            PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_EXECUTE);
    private final static Set<PosixFilePermission> OWNER_CAN_EXECUTE = Set.of(
            PosixFilePermission.OWNER_EXECUTE);
}
