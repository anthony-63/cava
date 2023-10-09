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
package jdk.jfr.internal.dcmd;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jdk.jfr.internal.OldObjectSample;
import jdk.jfr.internal.Utils;
import jdk.jfr.internal.query.Configuration;
import jdk.jfr.internal.query.QueryPrinter;
import jdk.jfr.internal.query.ViewPrinter;
import jdk.jfr.internal.util.Columnizer;
import jdk.jfr.internal.util.UserDataException;
import jdk.jfr.internal.util.UserSyntaxException;
/**
 * JFR.view
 * <p>
 * The implementation is also used by DCmdQuery since there
 * is little difference between JFR.query and JFR.view.
 */
// Instantiated by native
public class DCmdView extends AbstractDCmd {

    protected void execute(ArgumentParser parser) throws DCmdException {
        parser.checkUnknownArguments();
        if (!parser.checkMandatory()) {
            println("The argument 'view' is mandatory");
            println();
            printHelpText();
            return;
        }
        Configuration configuration = new Configuration();
        configuration.output = getOutput();
        configuration.endTime = Instant.now().minusSeconds(1);
        String view = parser.getOption("view");
        if (view.startsWith("memory-leaks")) {
            // Make sure old object sample event is part of data.
            OldObjectSample.emit(0);
            Utils.waitFlush(10_000);
            configuration.endTime = Instant.now();
        }
        try (QueryRecording recording = new QueryRecording(configuration, parser)) {
            ViewPrinter printer = new ViewPrinter(configuration, recording.getStream());
            printer.execute(view);
        } catch (UserDataException e) {
            throw new DCmdException(e.getMessage());
        } catch (UserSyntaxException e) {
            throw new DCmdException(e.getMessage());
        } catch (IOException e) {
            throw new DCmdException("Could not open repository. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new DCmdException(e.getMessage() + ". See help JFR.view");
        }
    }

    @Override
    public String[] printHelp() {
        List<String> lines = new ArrayList<>();
        lines.addAll(getOptions().lines().toList());
        lines.add("");
        lines.addAll(ViewPrinter.getAvailableViews());
        lines.add("");
        lines.add(" The <view> parameter can be an event type name. Use the 'JFR.view types'");
        lines.add(" to see a list. To display all views, use 'JFR.view all-views'. To display");
        lines.add(" all events, use 'JFR.view all-events'.");
        lines.add("");
        lines.addAll(getExamples().lines().toList());
        return lines.toArray(String[]::new);
    }

    public String getOptions() {
        // 0123456789001234567890012345678900123456789001234567890012345678900123456789001234567890
        return """
                Options:

                 cell-height   (Optional) Maximum number of rows in a table cell. (INTEGER, no default value)

                 maxage        (Optional) Length of time for the view to span. (INTEGER followed by
                               's' for seconds 'm' for minutes or 'h' for hours, default value is 10m)

                 maxsize       (Optional) Maximum size for the view to span in bytes if one of
                               the following suffixes is not used: 'm' or 'M' for megabytes OR
                               'g' or 'G' for gigabytes. (STRING, default value is 32MB)

                 truncate      (Optional) How to truncate content that exceeds space in a table cell.
                               Mode can be 'beginning' or 'end'. (STRING, default value 'end')

                 verbose       (Optional) Displays the query that makes up the view.
                               (BOOLEAN, default value false)

                 <view>        (Mandatory) Name of the view or event type to display.
                               See list below for available views. (STRING, no default value)

                 width         (Optional) The width of the view in characters
                               (INTEGER, no default value)""";
    }

    public String getExamples() {
        return """
                Example usage:

                 $ jcmd <pid> JFR.view gc

                 $ jcmd <pid< JFR.view width=160 hot-methods

                 $ jcmd <pid> JFR.view verbose=true allocation-by-class

                 $ jcmd <pid> JFR.view contention-by-site

                 $ jcmd <pid> JFR.view jdk.GarbageCollection

                 $ jcmd <pid> JFR.view cell-height=5 ThreadStart

                 $ jcmd <pid> JFR.view truncate=beginning SystemProcess""";
    }

    @Override
    public Argument[] getArgumentInfos() {
        return new Argument[] {
            new Argument("cell-height",
                "Maximum heigth of a table cell",
                "JULONG", false, true, "1", false),
            new Argument("maxage",
                "Maximum duration of data to view, in (s)econds, (m)inutes, (h)ours, or (d)ays, e.g. 60m, or 0 for no limit",
                "NANOTIME", false, true, "10m", false),
            new Argument("maxsize",
                "Maximum amount of bytes to view, in (M)B or (G)B, e.g. 500M, or 0 for no limit",
                "MEMORY SIZE", false, true, "100M", false),
            new Argument("truncate",
                "Truncation mode if value doesn't fit in a table cell, valid values are 'beginning' and 'end'",
                "STRING", false, true, "end", false),
            new Argument("verbose",
                "Display additional information about the view, such as the underlying query",
                "BOOLEAN", false, true, "false", false),
            new Argument("view",
                "Name of the view, for example hot-methods",
                "STRING", true, false, null, false),
            new Argument("width",
                "Maximum number of horizontal characters",
                "JULONG", false, true, "100", false)
        };
   }
}
