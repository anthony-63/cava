/*
 * Copyright (c) 2016, 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.jfr.internal.management;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.security.AccessControlContext;

import jdk.jfr.Configuration;
import jdk.jfr.EventSettings;
import jdk.jfr.EventType;
import jdk.jfr.Recording;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.internal.JVMSupport;
import jdk.jfr.internal.LogLevel;
import jdk.jfr.internal.LogTag;
import jdk.jfr.internal.Logger;
import jdk.jfr.internal.MetadataRepository;
import jdk.jfr.internal.PlatformRecording;
import jdk.jfr.internal.PrivateAccess;
import jdk.jfr.internal.SecuritySupport.SafePath;
import jdk.jfr.internal.Utils;
import jdk.jfr.internal.WriteableUserPath;
import jdk.jfr.internal.consumer.AbstractEventStream;
import jdk.jfr.internal.consumer.EventDirectoryStream;
import jdk.jfr.internal.consumer.FileAccess;
import jdk.jfr.internal.instrument.JDKEvents;

/**
 * The management API in module jdk.management.jfr should be built on top of the
 * public API in jdk.jfr. Before putting more functionality here, consider if it
 * should not be part of the public API, and if not, please provide motivation
 *
 */
public final class ManagementSupport {

    // Purpose of this method is to expose the event types to the
    // FlightRecorderMXBean without instantiating Flight Recorder.
    //
    // This allows:
    //
    // 1) discoverability, so event settings can be exposed without the need to
    // create a new Recording in FlightRecorderMXBean.
    //
    // 2) a graphical JMX client to list all attributes to the user, without
    // loading JFR memory buffers. This is especially important when there is
    // no intent to use Flight Recorder.
    //
    // An alternative design would be to make FlightRecorder#getEventTypes
    // static, but it would the make the API look strange
    //
    public static List<EventType> getEventTypes() {
        // would normally be checked when a Flight Recorder instance is created
        Utils.checkAccessFlightRecorder();
        if (JVMSupport.isNotAvailable()) {
            return List.of();
        }
        JDKEvents.initialize(); // make sure JDK events are available
        return Collections.unmodifiableList(MetadataRepository.getInstance().getRegisteredEventTypes());
    }

    // Reuse internal code for parsing a timespan
    public static long parseTimespan(String s) {
        return Utils.parseTimespan(s);
    }

    // Reuse internal code for converting nanoseconds since epoch to Instant
    public static Instant epochNanosToInstant(long epochNanos) {
      return Utils.epochNanosToInstant(epochNanos);
    }

    // Reuse internal code for formatting settings
    public static final String formatTimespan(Duration dValue, String separation) {
        return Utils.formatTimespan(dValue, separation);
    }

    // Reuse internal logging mechanism
    public static void logError(String message) {
        Logger.log(LogTag.JFR, LogLevel.ERROR, message);
    }

    // Reuse internal logging mechanism
    public static void logDebug(String message) {
        Logger.log(LogTag.JFR, LogLevel.DEBUG, message);
    }

    // Get the textual representation when the destination was set, which
    // requires access to jdk.jfr.internal.PlatformRecording
    public static String getDestinationOriginalText(Recording recording) {
        PlatformRecording pr = PrivateAccess.getInstance().getPlatformRecording(recording);
        WriteableUserPath wup = pr.getDestination();
        return wup == null ? null : wup.getOriginalText();
    }

    // Needed to check if destination can be set, so FlightRecorderMXBean::setRecordingOption
    // can abort if not all data is valid
    public static void checkSetDestination(Recording recording, String destination) throws IOException{
        PlatformRecording pr = PrivateAccess.getInstance().getPlatformRecording(recording);
        if(destination != null){
            WriteableUserPath wup = new WriteableUserPath(Paths.get(destination));
            pr.checkSetDestination(wup);
        }
    }

    // Needed to modify setting using fluent API.
    public static EventSettings newEventSettings(EventSettingsModifier esm) {
        return PrivateAccess.getInstance().newEventSettings(esm);
    }

    // Needed callback to detect when a chunk has been parsed.
    public static void removePath(Recording recording, Path path) {
        PlatformRecording pr = PrivateAccess.getInstance().getPlatformRecording(recording);
        pr.removePath(new SafePath(path));
    }

    // Needed callback to detect when a chunk has been parsed.
    public static void setOnChunkCompleteHandler(EventStream stream, Consumer<Long> consumer) {
        EventDirectoryStream eds = (EventDirectoryStream) stream;
        eds.setChunkCompleteHandler(consumer);
    }

    // Needed to start an ongoing stream at the right chunk, which
    // can be identified by the start time with nanosecond precision.
    public static long getStartTimeNanos(Recording recording) {
        PlatformRecording pr = PrivateAccess.getInstance().getPlatformRecording(recording);
        return pr.getStartNanos();
    }

    // Needed to produce Configuration objects for MetadataEvent
    public static Configuration newConfiguration(String name, String label, String description, String provider,
          Map<String, String> settings, String contents) {
        return PrivateAccess.getInstance().newConfiguration(name, label, description, provider, settings, contents);
    }

    // Can't use EventStream.openRepository(...) because
    // EventStream::onMetadataData need to supply MetadataEvent
    // with configuration objects
    public static EventStream newEventDirectoryStream(
            @SuppressWarnings("removal")
            AccessControlContext acc,
            Path directory,
            List<Configuration> confs) throws IOException {
        return new EventDirectoryStream(
            acc,
            directory,
            FileAccess.UNPRIVILEGED,
            null,
            confs,
            false
        );
    }

    // An EventStream is passive, so a stop() method doesn't fit well in the API.
    // RemoteRecordingStream::stop() implementation need to prevent stream
    // from being closed, so this method is needed
    public static void setCloseOnComplete(EventStream stream, boolean closeOnComplete) {
        AbstractEventStream aes = (AbstractEventStream) stream;
        aes.setCloseOnComplete(closeOnComplete);
    }

    // Internal method needed to block parser
    public static StreamBarrier activateStreamBarrier(EventStream stream) {
        EventDirectoryStream aes = (EventDirectoryStream) stream;
        return aes.activateStreamBarrier();
    }
}