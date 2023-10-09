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

package sun.awt.screencast;

import sun.awt.UNIXToolkit;
import sun.security.action.GetPropertyAction;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.security.AccessController;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Helper class for grabbing pixels from the screen using the
 * <a href="https://flatpak.github.io/xdg-desktop-portal/#gdbus-org.freedesktop.portal.ScreenCast">
 * org.freedesktop.portal.ScreenCast API</a>
 */

@SuppressWarnings("removal")
public class ScreencastHelper {

    static final boolean SCREENCAST_DEBUG;
    private static final boolean IS_NATIVE_LOADED;


    private static final int ERROR = -1;
    private static final int DENIED = -11;
    private static final int OUT_OF_BOUNDS = -12;

    private ScreencastHelper() {
    }

    static {
        SCREENCAST_DEBUG = Boolean.parseBoolean(
                               AccessController.doPrivileged(
                                       new GetPropertyAction(
                                               "awt.robot.screenshotDebug",
                                               "false"
                                       )
                               ));

        boolean loadFailed = false;

        if (!(Toolkit.getDefaultToolkit() instanceof UNIXToolkit tk
              && tk.loadGTK())
              || !loadPipewire(SCREENCAST_DEBUG)) {

            System.err.println(
                    "Could not load native libraries for ScreencastHelper"
            );

            loadFailed = true;
        }

        IS_NATIVE_LOADED = !loadFailed;
    }

    public static boolean isAvailable() {
        return IS_NATIVE_LOADED;
    }

    private static native boolean loadPipewire(boolean screencastDebug);

    private static native int getRGBPixelsImpl(
            int x, int y, int width, int height,
            int[] pixelArray,
            int[] affectedScreensBoundsArray,
            String token
    );

    private static List<Rectangle> getSystemScreensBounds() {
        return Arrays
                .stream(GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getScreenDevices())
                .map(graphicsDevice ->
                        graphicsDevice.getDefaultConfiguration().getBounds()
                ).toList();
    }

    public static synchronized void getRGBPixels(
            int x, int y, int width, int height, int[] pixelArray
    ) {
        if (!IS_NATIVE_LOADED) return;

        Rectangle captureArea = new Rectangle(x, y, width, height);

        List<Rectangle> affectedScreenBounds = getSystemScreensBounds()
                .stream()
                .filter(captureArea::intersects)
                .toList();

        if (SCREENCAST_DEBUG) {
            System.out.printf("// getRGBPixels in %s, affectedScreenBounds %s\n",
                    captureArea, affectedScreenBounds);
        }

        if (affectedScreenBounds.isEmpty()) {
            if (SCREENCAST_DEBUG) {
                System.out.println("// getRGBPixels - requested area "
                        + "outside of any screen");
            }
            return;
        }

        int retVal;
        Set<TokenItem> tokensForRectangle =
                TokenStorage.getTokens(affectedScreenBounds);

        int[] affectedScreenBoundsArray = affectedScreenBounds
                .stream()
                .filter(captureArea::intersects)
                .flatMapToInt(bounds -> IntStream.of(
                        bounds.x, bounds.y,
                        bounds.width, bounds.height
                ))
                .toArray();

        for (TokenItem tokenItem : tokensForRectangle) {
            retVal = getRGBPixelsImpl(
                    x, y, width, height,
                    pixelArray,
                    affectedScreenBoundsArray,
                    tokenItem.token
            );

            if (retVal >= 0) { // we have received a screen data
                return;
            } else if (!checkReturnValue(retVal)) {
                return;
            } // else, try other tokens
        }

        // we do not have a saved token or it did not work,
        // try without the token to show the system's permission request window
        retVal = getRGBPixelsImpl(
                x, y, width, height,
                pixelArray,
                affectedScreenBoundsArray,
                null
        );

        checkReturnValue(retVal);
    }

    private static boolean checkReturnValue(int retVal) {
        if (retVal == DENIED) {
            // user explicitly denied the capture, no more tries.
            throw new SecurityException(
                    "Screen Capture in the selected area was not allowed"
            );
        } else if (retVal == ERROR) {
            if (SCREENCAST_DEBUG) {
                System.err.println("Screen capture failed.");
            }
        } else if (retVal == OUT_OF_BOUNDS) {
            if (SCREENCAST_DEBUG) {
                System.err.println(
                        "Token does not provide access to requested area.");
            }
        }
        return retVal != ERROR;
    }
}
