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

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.transport.AbstractMessageDispatcher;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class SmbMessageDispatcher extends AbstractMessageDispatcher
{
    protected SmbConnector smbConnector;

    public SmbMessageDispatcher(OutboundEndpoint endpoint)
    {
        super(endpoint);
        smbConnector = (SmbConnector) endpoint.getConnector();
    }

    @Override
    public void doDispatch(MuleEvent event) throws Exception
    {
        Object data = event.getMessage().getPayload();
        OutputStream out = smbConnector.getOutputStream(getEndpoint(), event);

        try
        {
            if (data instanceof InputStream)
            {
                InputStream is = ((InputStream)data);
                IOUtils.copy(is, out);
                is.close();
            }
            else
            {
                byte[] dataBytes;
                if (data instanceof byte[])
                {
                    dataBytes = (byte[])data;
                }
                else
                {
                    dataBytes = data.toString().getBytes(event.getEncoding());
                }
                IOUtils.write(dataBytes, out);
            }
        }
        finally
        {
            out.flush();
            out.close();
        }
    }

    @Override
    public MuleMessage doSend(MuleEvent event) throws Exception
    {
        doDispatch(event);
        return null;
    }
}
