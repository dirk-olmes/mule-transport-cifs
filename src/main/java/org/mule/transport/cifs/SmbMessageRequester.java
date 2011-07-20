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
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.transport.AbstractMessageRequester;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;

public class SmbMessageRequester extends AbstractMessageRequester
{
    protected final SmbConnector connector;

    public SmbMessageRequester(InboundEndpoint endpoint)
    {
        super(endpoint);
        this.connector = (SmbConnector)endpoint.getConnector();
    }

    /**
     * Make a specific request to the underlying transport
     *
     * @param timeout the maximum time the operation should block before returning.
     *            The call should return immediately if there is data available. If
     *            no data becomes available before the timeout elapses, null will be
     *            returned
     * @return the result of the request wrapped in a MuleMessage object. Null will
     *         be returned if no data was avaialable
     * @throws Exception if the call to the underlying protocal cuases an exception
     */
    @Override
    protected MuleMessage doRequest(long timeout) throws Exception
    {
        try
        {
            FilenameFilter filenameFilter = null;
            if (endpoint.getFilter() instanceof FilenameFilter)
            {
                filenameFilter = (FilenameFilter)endpoint.getFilter();
            }

            EndpointURI uri = endpoint.getEndpointURI();
            String smbPath = null;
            if (SmbConnector.checkNullOrBlank(uri.getUser())
                || SmbConnector.checkNullOrBlank(uri.getPassword()))
            {
                logger.warn("No user or password supplied. Attempting to connect with just smb://<host>/<path>");
                logger.info("smb://" + uri.getHost() + uri.getPath());
                smbPath = "smb://" + uri.getHost() + uri.getPath();
            }
            else
            {
                logger.info("smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                            + uri.getPath());
                smbPath = "smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                          + uri.getPath();
            }

            SmbFile[] files = new SmbFile(smbPath).listFiles();

            if (files == null || files.length == 0)
            {
                return null;
            }

            List fileList = new ArrayList();

            SmbFile file = null;
            for (int i = 0; i < files.length; i++)
            {
                file = files[i];
                if (file.isFile())
                {
                    if (filenameFilter == null || filenameFilter.accept(null, file.getName()))
                    {
                        if (connector.validateFile(file))
                        {
                            fileList.add(file);
                            // only read the first one
                            break;
                        }
                    }
                }
            }
            if (fileList.size() == 0)
            {
                return null;
            }

            return createMuleMessage(file);
        }
        finally
        {
            logger.debug("leaving doRequest()");
        }
    }
}
