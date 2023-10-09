/*
 * Copyright (c) 2009, 2021, Oracle and/or its affiliates. All rights reserved.
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
package sun.nio.ch.sctp;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import java.lang.annotation.Native;

/**
 * An implementation of AssociationChangeNotification
 */
public class AssociationChange extends AssociationChangeNotification
    implements SctpNotification
{
    /* static final ints so that they can be referenced from native */
    @Native private static final int SCTP_COMM_UP = 1;
    @Native private static final int SCTP_COMM_LOST = 2;
    @Native private static final int SCTP_RESTART = 3;
    @Native private static final int SCTP_SHUTDOWN = 4;
    @Native private static final int SCTP_CANT_START = 5;

    private Association association;

    /* assocId is used to look up the association before the notification is
     * returned to user code */
    private final int assocId;
    private final AssocChangeEvent event;
    private final int maxOutStreams;
    private final int maxInStreams;

    /* Invoked from native */
    private AssociationChange(int assocId,
                              int intEvent,
                              int maxOutStreams,
                              int maxInStreams) {
        this.event = switch (intEvent) {
            case SCTP_COMM_UP    -> AssocChangeEvent.COMM_UP;
            case SCTP_COMM_LOST  -> AssocChangeEvent.COMM_LOST;
            case SCTP_RESTART    -> AssocChangeEvent.RESTART;
            case SCTP_SHUTDOWN   -> AssocChangeEvent.SHUTDOWN;
            case SCTP_CANT_START -> AssocChangeEvent.CANT_START;
            default -> throw new AssertionError(
                    "Unknown Association Change Event type: " + intEvent);
        };

        this.assocId = assocId;
        this.maxOutStreams = maxOutStreams;
        this.maxInStreams = maxInStreams;
    }

    @Override
    public int assocId() {
        return assocId;
    }

    @Override
    public void setAssociation(Association association) {
        this.association = association;
    }

    @Override
    public Association association() {
        assert event == AssocChangeEvent.CANT_START ? true : association != null;
        return association;
    }

    @Override
    public AssocChangeEvent event() {
        return event;
    }

    int maxOutStreams() {
        return maxOutStreams;
    }

    int maxInStreams() {
        return maxInStreams;
    }

    @Override
    public String toString() {
        return super.toString() + " [" +
                "Association:" + association +
                ", Event: " + event + "]";
    }
}
