/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cifs.functional;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.transport.cifs.util.SmbUtil;
import org.mule.transport.file.FileConnector;
import org.mule.util.concurrent.Latch;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmbInboundTestCase extends AbstractSmbTestCase
{
    private MuleMessage message;
    private Latch messageReceived = new Latch();

    @Override
    protected String getConfigResources()
    {
        return "smb-inbound-config.xml";
    }

    @Test
    public void messageShouldBeReceivedFromSmbServer() throws Exception
    {
        FunctionalTestComponent ftc = (FunctionalTestComponent) getComponent("smbInbound");
        ftc.setEventCallback(new InboundEventCallback());

        putFile();

        assertTrue(messageReceived.await(30, TimeUnit.SECONDS));
        assertNotNull(message);
        assertEquals(TEST_MESSAGE.getBytes(), message.getPayloadAsBytes());
        Assert.assertEquals("input.txt", message.getOutboundProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME));
    }

    private void putFile() throws Exception
    {
        new SmbUtil().createFile("input.txt", TEST_MESSAGE);
    }

    private class InboundEventCallback implements EventCallback
    {
        public void eventReceived(MuleEventContext context, Object component) throws Exception
        {
            message = context.getMessage();
            messageReceived.countDown();
        }
    }
}
