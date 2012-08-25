/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cifs;

import org.mule.api.endpoint.InboundEndpoint;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmbConnectorFactoryTestCase extends AbstractMuleContextTestCase
{
    public void testCreateFromFactory() throws Exception
    {
        InboundEndpoint endpoint = muleContext.getEndpointFactory().getInboundEndpoint(getEndpointURI());
        assertNotNull(endpoint);
        assertNotNull(endpoint.getConnector());
        assertTrue(endpoint.getConnector() instanceof SmbConnector);
        assertEquals("smb://host/path", endpoint.getEndpointURI().getAddress());
    }

    public String getEndpointURI()
    {
        return "smb://user:password@host/path";
    }
}
