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

public final class CLibraryImpl implements CLibrary {

    static {
        System.loadLibrary("le");
        initIDs();
    }

    private static native void initIDs();

    @Override
    public native void tcgetattr(int fd, termios termios) throws LastErrorException;

    @Override
    public native void tcsetattr(int fd, int cmd, termios termios) throws LastErrorException;

    @Override
    public void ioctl(int fd, int cmd, winsize data) throws LastErrorException {
        if (cmd == CLibrary.TIOCGWINSZ || cmd == CLibrary.TIOCSWINSZ) {
            ioctl0(fd, cmd, data);
        } else {
            throw new UnsupportedOperationException("Command: " + cmd + ", not supported.");
        }
    }

    private native void ioctl0(int fd, int cmd, winsize data) throws LastErrorException;

    @Override
    public native int isatty(int fd);

    @Override
    public native void ttyname_r(int fd, byte[] buf, int len) throws LastErrorException;

}
