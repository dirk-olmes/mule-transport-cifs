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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.file.ExpressionFilenameParser;

import org.junit.Test;

public class SmbNamespaceHandlerTestCase extends FunctionalTestCase
{
    public SmbNamespaceHandlerTestCase()
    {
        super();
        setStartContext(false);
    }

    @Override
    protected String getConfigResources()
    {
        return "smb-namespace-config.xml";
    }

    @Test
    public void smbConnectorAttributes() throws Exception
    {
        SmbConnector connector = (SmbConnector) muleContext.getRegistry().lookupConnector("smbConnector");
        assertNotNull(connector);

        assertEquals(42, connector.getFileAge());

        assertTrue(connector.getFilenameParser() instanceof ExpressionFilenameParser);
        assertEquals("#[function:uuid]", connector.getMoveToPattern());
    }

    @Test
    public void fileAgeFromEndpointShouldOverrideConnectorConfiguration() throws Exception
    {
        ImmutableEndpoint endpoint = lookupEndpoint("epWithFileAge");
        assertEquals("smb://localhost/foo", endpoint.getAddress());
        assertEquals("99", endpoint.getProperty("fileAge"));
    }

    private ImmutableEndpoint lookupEndpoint(String endpointName) throws Exception
    {
        EndpointBuilder builder = muleContext.getRegistry().lookupEndpointBuilder(endpointName);
        return builder.buildInboundEndpoint();
    }
}
