/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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

/**
 * An implementation of Association
 */
public class AssociationImpl extends Association {
    public AssociationImpl(int associationID,
                           int maxInStreams,
                           int maxOutStreams) {
        super(associationID, maxInStreams, maxOutStreams);
    }

    @Override
    public String toString() {
        return super.toString() + "[associationID:" +
                associationID() +
                ", maxIn:" +
                maxInboundStreams() +
                ", maxOut:" +
                maxOutboundStreams() +
                "]";
    }
}

