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

import org.mule.api.endpoint.EndpointURI;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmbEndpointTestCase extends AbstractMuleContextTestCase
{
    @Test
	public void validEndpointURI() throws Exception
    {
    	String url = "smb://masonshoe;ajr:07Admar1177@172.16.2.23/C$/TEMP/hello.txt";
        EndpointURI uri = new MuleEndpointURI(url, muleContext);

        assertEquals("smb", uri.getScheme());
        assertEquals("masonshoe;ajr", uri.getUser());
        assertEquals("07Admar1177", uri.getPassword());
        assertEquals("172.16.2.23", uri.getHost());
        assertEquals(0, uri.getParams().size());
    }
}
