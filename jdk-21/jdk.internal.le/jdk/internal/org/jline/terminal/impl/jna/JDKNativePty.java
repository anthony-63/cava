/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.org.jline.terminal.impl.jna;

import java.io.IOException;
import jdk.internal.org.jline.terminal.Attributes;
import jdk.internal.org.jline.terminal.Size;
import jdk.internal.org.jline.terminal.impl.jna.linux.LinuxNativePty;
import jdk.internal.org.jline.terminal.spi.TerminalProvider;

class JDKNativePty {

    static JnaNativePty current(TerminalProvider.Stream console) throws IOException {
        return LinuxNativePty.current(console);
    }

    static JnaNativePty open(Attributes attr, Size size) throws IOException {
        return LinuxNativePty.open(attr, size);
    }

    static int isatty(int fd) {
        return LinuxNativePty.isatty(fd);
    }

    static String ttyname(int fd) {
        return LinuxNativePty.ttyname(fd);
    }

}
