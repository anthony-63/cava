/*
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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
package sun.awt.X11;

import java.awt.*;
import java.awt.peer.*;

import java.util.Vector;
import sun.util.logging.PlatformLogger;
import sun.awt.AWTAccessor;

public class XMenuPeer extends XMenuItemPeer implements MenuPeer {

    /************************************************
     *
     * Data members
     *
     ************************************************/
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XMenuPeer");

    /**
     * Window that correspond to this menu
     */
    XMenuWindow menuWindow;

    /************************************************
     *
     * Construction
     *
     ************************************************/
    XMenuPeer(Menu target) {
        super(target);
    }

    /**
     * This function is called when menu is bound
     * to its container window. Creates submenu window
     * that fills its items vector while construction
     */
    @Override
    void setContainer(XBaseMenuWindow container) {
        super.setContainer(container);
        menuWindow = new XMenuWindow(this);
    }


    /************************************************
     *
     * Implementation of interface methods
     *
     ************************************************/

    /*
     * From MenuComponentPeer
     */

    /**
     * Disposes menu window if needed
     */
    @Override
    public void dispose() {
        if (menuWindow != null) {
            menuWindow.dispose();
        }
        super.dispose();
    }

    /**
     * Resets text metrics for this item, for its menu window
     * and for all descendant menu windows
     */
    @Override
    public void setFont(Font font) {
        //TODO:We can decrease count of repaints here
        //and get rid of recursion
        resetTextMetrics();

        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.setItemsFont(font);
        }

        repaintIfShowing();
    }

    /*
     * From MenuPeer
     */
    @Override
    public void addItem(MenuItem item) {
        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.addItem(item);
        } else {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("Attempt to use XMenuWindowPeer without window");
            }
        }
    }

    @Override
    public void delItem(int index) {
        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.delItem(index);
        } else {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("Attempt to use XMenuWindowPeer without window");
            }
        }
    }

    /************************************************
     *
     * Access to target's fields
     *
     ************************************************/
    Vector<MenuItem> getTargetItems() {
        return AWTAccessor.getMenuAccessor().getItems((Menu)getTarget());
    }

    /************************************************
     *
     * Overridden behaviour
     *
     ************************************************/
    @Override
    boolean isSeparator() {
        return false;
    }

    //Fix for 6180416: Shortcut keys are displayed against Menus on XToolkit
    //Menu should always return null as shortcutText
    @Override
    String getShortcutText() {
        return null;
    }

    /************************************************
     *
     * Utility functions
     *
     ************************************************/

    /**
     * Returns menu window of this menu or null
     * it this menu has no container and so its
     * window can't be created.
     */
    XMenuWindow getMenuWindow() {
        return menuWindow;
    }

}
