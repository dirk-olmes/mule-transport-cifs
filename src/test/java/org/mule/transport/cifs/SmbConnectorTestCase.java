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

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;

import jcifs.smb.SmbFile;

import static org.mockito.Mockito.mock;

public class SmbConnectorTestCase extends AbstractConnectorTestCase
{
    @Override
    public Connector createConnector() throws Exception
    {
        SmbConnector connector = new SmbConnector(muleContext);
        connector.setName("Test");
        return connector;
    }

    @Override
    public String getTestEndpointURI()
    {
        return "smb://user:password@host/path";
    }

    @Override
    public Object getValidMessage() throws Exception
    {
        return mock(SmbFile.class);
    }
}
