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

import org.mule.api.MuleMessage;
import org.mule.api.transport.MuleMessageFactory;
import org.mule.transport.AbstractMuleMessageFactoryTestCase;
import org.mule.transport.file.FileConnector;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import jcifs.smb.SmbFile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SmbMuleMessageFactoryTestCase extends AbstractMuleMessageFactoryTestCase
{
    private static byte[] MOCK_CONTENT = "MOCK_CONTENT".getBytes();
    private static String MOCK_FILENAME = "sample.txt";
    private static final long MOCK_LENGTH = 42;

    @Override
    protected MuleMessageFactory doCreateMuleMessageFactory()
    {
        return new SmbMuleMessageFactory(muleContext);
    }

    @Override
    protected Object getValidTransportMessage() throws Exception
    {
        SmbFile file = mock(SmbFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(MOCK_CONTENT));
        when(file.getName()).thenReturn(MOCK_FILENAME);
        when(file.length()).thenReturn(MOCK_LENGTH);
        return file;
    }

    @Override
    protected Object getUnsupportedTransportMessage()
    {
        return "this is not a valid transport message for the cifs transport";
    }

    @Override
    public void testValidPayload() throws Exception
    {
        MuleMessage message = createMuleMessageFromValidTransportMessage();
        assertNotNull(message);

        Object payload = message.getPayload();
        assertTrue(payload instanceof byte[]);
        assertTrue(Arrays.equals(MOCK_CONTENT, (byte[]) payload));
    }

    @Test
    public void messageShouldHaveTransportSpecificMessageProperties() throws Exception
    {
        MuleMessage message = createMuleMessageFromValidTransportMessage();
        assertEquals(MOCK_FILENAME, message.getOutboundProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME));
        assertEquals(MOCK_LENGTH, message.getOutboundProperty(FileConnector.PROPERTY_FILE_SIZE));
    }

    private MuleMessage createMuleMessageFromValidTransportMessage() throws Exception
    {
        MuleMessageFactory factory = createMuleMessageFactory();

        Object payload = getValidTransportMessage();
        return factory.create(payload, encoding);
    }
}

