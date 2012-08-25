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
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.util.StringUtils;

import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.spi.work.Work;

import jcifs.smb.SmbFile;

/**
 * <code>SmbMessageReceiver</code> TODO document
 */
public class SmbMessageReceiver extends AbstractPollingMessageReceiver
{
    private String moveToDir = "";
    private String moveToPattern = "";
    private long fileAge = 0;
    protected final SmbConnector connector;
    protected final FilenameFilter filenameFilter;
    protected final String smbPath;
    protected final Set<String> scheduledFiles = Collections.synchronizedSet(new HashSet<String>());
    protected final Set<String> currentFiles = Collections.synchronizedSet(new HashSet<String>());

    /**
     * This constructor is only used for instantiating the receiver from the connector test case
     */
//    @Deprecated
//    public SmbMessageReceiver(Connector connector, FlowConstruct flowConstruct, InboundEndpoint endpoint)
//        throws CreateException
//    {
//        this(connector, flowConstruct, endpoint, DEFAULT_POLL_FREQUENCY, null, null, 0);
//    }

    public SmbMessageReceiver(Connector connector, FlowConstruct flowConstruct, InboundEndpoint endpoint,
        long frequency, String moveToDir, String moveToPattern, long fileAge) throws CreateException
    {
        super(connector, flowConstruct, endpoint);

        this.setFrequency(frequency);
        this.moveToDir = moveToDir;
        this.moveToPattern = moveToPattern;
        this.fileAge = fileAge;

        this.connector = (SmbConnector)connector;

        if (endpoint.getFilter() instanceof FilenameFilter)
        {
            this.filenameFilter = (FilenameFilter)endpoint.getFilter();
        }
        else
        {
            this.filenameFilter = null;
        }

        EndpointURI uri = endpoint.getEndpointURI();

        if (StringUtils.isBlank(uri.getUser()) || StringUtils.isBlank(uri.getPassword()))
        {
            logger.warn("No user or password supplied. Attempting to connect with just smb://<host>/<path>");
            logger.warn("smb://" + uri.getHost() + uri.getPath());
            smbPath = "smb://" + uri.getHost() + uri.getPath();
        }
        else
        {
            smbPath = "smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                      + uri.getPath();
        }
    }

    @Override
    public void poll() throws Exception
    {
        SmbFile[] files = listFiles();
        if (logger.isDebugEnabled())
        {
            logger.debug("Poll encountered " + files.length + " new file(s)");
        }

        synchronized (scheduledFiles)
        {
            for (SmbFile file : files)
            {
                String fileName = file.getName();

                if (!scheduledFiles.contains(fileName) && !currentFiles.contains(fileName))
                {
                    scheduledFiles.add(fileName);
                    getWorkManager().scheduleWork(new SmbWork(fileName, file));
                }
            }
        }
    }

    protected SmbFile[] listFiles() throws Exception
    {
        try
        {
            SmbFile[] files = new SmbFile(smbPath).listFiles();

            if (files == null || files.length == 0)
            {
                return files;
            }

            List<SmbFile> v = new ArrayList<SmbFile>();

            for (SmbFile file : files)
            {
                if (file.isFile())
                {
                    if (filenameFilter == null || filenameFilter.accept(null, file.getName()))
                    {
                        v.add(file);
                    }
                }
            }

            return v.toArray(new SmbFile[v.size()]);
        }
        finally
        {
            // TODO
        }
    }

    protected void processFile(SmbFile file) throws Exception
    {
        try
        {
            if (!connector.validateFile(file))
            {
                return;
            }
            else
            {
                MuleMessage message = createMuleMessage(file);
                routeMessage(message);
                postProcess(file, message);
            }
        }
        catch (Exception e)
        {
            throw new IOException(MessageFormat.format(
                "Failed to processFile SmbFile {0}. Smb error: " + e.getMessage(), file.getName(), e));
        }
    }

    protected void postProcess(SmbFile file, MuleMessage message) throws Exception
    {
        if (!StringUtils.isEmpty(moveToDir))
        {
            String destinationFileName = file.getName();

            if (!StringUtils.isEmpty(moveToPattern))
            {
                destinationFileName = (connector).getFilenameParser().getFilename(message, moveToPattern);
            }

            SmbFile dest;
            EndpointURI uri = endpoint.getEndpointURI();

            if (SmbConnector.checkNullOrBlank(uri.getUser())
                || SmbConnector.checkNullOrBlank(uri.getPassword()))
            {
                dest = new SmbFile("smb://" + uri.getHost() + moveToDir + destinationFileName);
            }
            else
            {
                dest = new SmbFile("smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                                   + moveToDir + destinationFileName);
            }

            logger.debug("dest: " + dest);

            try
            {
                file.renameTo(dest);
            }
            catch (Exception e)
            {
                throw new IOException(MessageFormat.format(
                    "Failed to rename file " + file.getName() + " to " + dest.getName() + ". Smb error! "
                                    + e.getMessage(), new Object[]{ file.getName(),
                        moveToDir + destinationFileName, e }));
            }

            logger.debug("Renamed processed file " + file.getName() + " to " + moveToDir
                         + destinationFileName);
        }
        else
        {
            try
            {
                file.delete();
            }
            catch (Exception e)
            {
                throw new IOException(MessageFormat.format("Failed to delete file " + file.getName()
                                                           + ". Smb error: " + e.getMessage(),
                    file.getName(), null));
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted processed file " + file.getName());
            }
        }
    }

    @Override
    protected void doConnect() throws Exception
    {
        // no op
    }

    private final class SmbWork implements Work
    {
        private final String name;
        private final SmbFile file;

        private SmbWork(String name, SmbFile file)
        {
            this.name = name;
            this.file = file;
        }

        public void run()
        {
            try
            {
                currentFiles.add(name);
                processFile(file);
            }
            catch (Exception e)
            {
                connector.getMuleContext().getExceptionListener().handleException(e);
            }
            finally
            {
                currentFiles.remove(name);
                scheduledFiles.remove(name);
            }
        }

        public void release()
        {
            // no op
        }
    }

}
