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
package jdk.internal.org.jline.terminal.impl.jna.linux;

import jdk.internal.org.jline.terminal.impl.jna.LastErrorException;
import jdk.internal.org.jline.terminal.impl.jna.linux.LinuxNativePty.UtilLibrary;

public final class UtilLibraryImpl implements UtilLibrary {

    @Override
    public void openpty(int[] master, int[] slave, byte[] name, CLibrary.termios t, CLibrary.winsize s) throws LastErrorException {
        throw new UnsupportedOperationException();
    }

}
