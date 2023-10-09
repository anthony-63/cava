/*
 * Copyright (c) 2005, 2023, Oracle and/or its affiliates. All rights reserved.
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

package sun.net.httpserver;

import java.util.*;

class ContextList {

    private final LinkedList<HttpContextImpl> list = new LinkedList<>();

    public synchronized void add (HttpContextImpl ctx) {
        assert ctx.getPath() != null;
        if (contains(ctx)) {
            throw new IllegalArgumentException ("cannot add context to list");
        }
        list.add (ctx);
    }

    boolean contains(HttpContextImpl ctx) {
        return findContext(ctx.getProtocol(), ctx.getPath(), true) != null;
    }

    public synchronized int size () {
        return list.size();
    }

   /* initially contexts are located only by protocol:path.
    * Context with longest prefix matches (currently case-sensitive)
    */
    synchronized HttpContextImpl findContext (String protocol, String path) {
        return findContext (protocol, path, false);
    }

    synchronized HttpContextImpl findContext (String protocol, String path, boolean exact) {
        protocol = protocol.toLowerCase(Locale.ROOT);
        String longest = "";
        HttpContextImpl lc = null;
        for (HttpContextImpl ctx: list) {
            if (!ctx.getProtocol().equals(protocol)) {
                continue;
            }
            String cpath = ctx.getPath();
            if (exact && !cpath.equals (path)) {
                continue;
            } else if (!exact && !path.startsWith(cpath)) {
                continue;
            }
            if (cpath.length() > longest.length()) {
                longest = cpath;
                lc = ctx;
            }
        }
        return lc;
    }

    public synchronized void remove (String protocol, String path)
        throws IllegalArgumentException
    {
        HttpContextImpl ctx = findContext (protocol, path, true);
        if (ctx == null) {
            throw new IllegalArgumentException ("cannot remove element from list");
        }
        list.remove (ctx);
    }

    public synchronized void remove (HttpContextImpl context)
        throws IllegalArgumentException
    {
        for (HttpContextImpl ctx: list) {
            if (ctx.equals (context)) {
                list.remove (ctx);
                return;
            }
        }
        throw new IllegalArgumentException ("no such context in list");
    }
}
