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
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.transport.file.FileConnector;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilenameBuilderTestCase extends AbstractMuleContextTestCase
{
    private static final String PATTERN = "hello-#[message:payload]";

    private SmbConnector connector;
    private OutboundEndpoint endpoint;
    private MuleMessage message;

    public FilenameBuilderTestCase()
    {
        super();
        setDisposeContextPerClass(true);
    }

    @Override
    protected void doSetUp() throws Exception
    {
        connector = new SmbConnector(muleContext);
        connector.setOutputPattern(PATTERN);

        endpoint = mock(OutboundEndpoint.class);

        message = mock(MuleMessage.class);
        when(message.getPayload()).thenReturn("world");
    }

    @Test
    public void filenameOnMuleMessageShouldOverrideAllOtherConfiguration()
    {
        String filenameOnMessage = "foo.txt";

        when(message.getOutboundProperty(SmbConnector.PROPERTY_FILENAME)).thenReturn(filenameOnMessage);
        when(message.getOutboundProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN)).thenReturn("other-filename");

        FilenameBuilder builder = new FilenameBuilder(null, null);
        String filename = builder.getFilename(message);
        assertEquals(filenameOnMessage, filename);
    }

    @Test
    public void outputPatternOnMessageShouldOverrideAllOtherConfiguration()
    {
        when(message.getOutboundProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN)).thenReturn(PATTERN);

        FilenameBuilder builder = new FilenameBuilder(connector, null);
        String filename = builder.getFilename(message);
        assertEquals("hello-world", filename);
    }

    @Test
    public void outputPatternOnEndpointShouldOverrideConnectorConfig()
    {
        when(endpoint.getProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN)).thenReturn(PATTERN);

        FilenameBuilder builder = new FilenameBuilder(connector, endpoint);
        String filename = builder.getFilename(message);
        assertEquals("hello-world", filename);
    }

    @Test
    public void outputPatternOnConnectorShouldProduceFilename()
    {
        FilenameBuilder builder = new FilenameBuilder(connector, endpoint);
        String filename = builder.getFilename(message);
        assertEquals("hello-world", filename);
    }

    @Test
    public void originalFilenameShouldBeUsedIfNoOtherConfigIsPresent()
    {
        connector.setOutputPattern(null);

        String originalFilename = "orig.txt";
        when(message.getOutboundProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME)).thenReturn(originalFilename);

        FilenameBuilder builder = new FilenameBuilder(connector, endpoint);
        String filename = builder.getFilename(message);
        assertEquals(originalFilename, filename);
    }

    @Test
    public void noConfigurationAndNoMatchingMessagePropertyShouldUseExpressionParserFromConnector()
    {
        connector.setOutputPattern(null);

        FilenameBuilder builder = new FilenameBuilder(connector, endpoint);
        String filename = builder.getFilename(message);
        assertTrue(filename.endsWith(".dat"));
    }
}
