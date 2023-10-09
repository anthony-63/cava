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

package sun.net.httpserver.simpleserver;

/**
 * A class that represents a URI path segment.
 */
final class URIPathSegment {

    private URIPathSegment() { throw new AssertionError(); }

    /**
     * Checks if the segment of a URI path is supported.
     *
     * @param segment the segment string
     * @return true
     */
    static boolean isSupported(String segment) {
        return true;
    }
}
