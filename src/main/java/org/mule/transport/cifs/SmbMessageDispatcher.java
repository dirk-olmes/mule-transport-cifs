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
        /*
         * IMPLEMENTATION NOTE: This is invoked when the endpoint is asynchronous. It
         * should invoke the transport but not return any result. If a result is
         * returned it should be ignorred, but if the underlying transport does have
         * a notion of asynchronous processing, that should be invoked. This method
         * is executed in a different thread to the request thread.
         */

        // TODO Write the client code here to dispatch the event over this
        // transport
        // throw new UnsupportedOperationException("doDispatch");
        Object data = event.transformMessage();
        OutputStream out = smbConnector.getOutputStream((OutboundEndpoint) event.getEndpoint(), event);

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
        /*
         * IMPLEMENTATION NOTE: Should send the event payload over the transport. If
         * there is a response from the transport it shuold be returned from this
         * method. The sendEvent method is called when the endpoint is running
         * synchronously and any response returned will ultimately be passed back to
         * the callee. This method is executed in the same thread as the request
         * thread.
         */

        // TODO Write the client code here to send the event over this
        // transport (or to dispatch the event to a store or repository)
        // TODO Once the event has been sent, return the result (if any)
        // wrapped in a MuleMessage object
        // throw new UnsupportedOperationException("doSend");
        doDispatch(event);
        return null;
    }
}
