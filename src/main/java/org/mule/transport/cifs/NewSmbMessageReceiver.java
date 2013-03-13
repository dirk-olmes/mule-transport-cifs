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
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.transport.ConnectException;

import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * <code>SmbMessageReceiver</code> TODO document
 */
public class NewSmbMessageReceiver extends AbstractPollingMessageReceiver
{
    protected final SmbConnector smbConnector;
    protected final FilenameFilter filenameFilter;
    protected final String smbPath;
    protected final Set<String> scheduledFiles = Collections.synchronizedSet(new HashSet<String>());
    protected final Set<String> currentFiles = Collections.synchronizedSet(new HashSet<String>());

    public NewSmbMessageReceiver(Connector connector, Service service, InboundEndpoint endpoint)
        throws CreateException
    {
        super(connector, service, endpoint);
        logger.warn("callig SmbMessageReceiver!");

        // this.setFrequency(frequency);

        this.smbConnector = (SmbConnector)connector;

        if (endpoint.getFilter() instanceof FilenameFilter)
        {
            this.filenameFilter = (FilenameFilter)endpoint.getFilter();
        }
        else
        {
            this.filenameFilter = null;
        }

        EndpointURI uri = endpoint.getEndpointURI();

        if (SmbConnector.checkNullOrBlank(uri.getUser()) || SmbConnector.checkNullOrBlank(uri.getPassword()))
        {
            logger.warn("No user or password supplied. Attempting to connect with just smb://<host>/<path>");
            logger.info("smb://" + uri.getHost() + uri.getPath());
            smbPath = "smb://" + uri.getHost() + uri.getPath();
        }
        else
        {
            smbPath = "smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                      + uri.getPath();
        }
        logger.warn(smbPath);
    }

    @Override
    public void doConnect() throws ConnectException
    {
        /*
         * IMPLEMENTATION NOTE: This method should make a connection to the
         * underlying transport i.e. connect to a socket or register a soap service.
         * When there is no connection to be made this method should be used to check
         * that resources are available. For example the FileMessageReceiver checks
         * that the directories it will be using are available and readable. The
         * MessageReceiver should remain in a 'stopped' state even after the
         * doConnect() method is called. This means that a connection has been made
         * but no events will be received until the start() method is called. Calling
         * start() on the MessageReceiver will call doConnect() if the receiver
         * hasn't connected already.
         */

        /*
         * IMPLEMENTATION NOTE: If you need to spawn any threads such as worker
         * threads for this receiver you can schedule a worker thread with the work
         * manager i.e. getWorkManager().scheduleWork(worker, WorkManager.INDEFINITE,
         * null, null); Where 'worker' implemments javax.resource.spi.work.Work
         */

        /*
         * IMPLEMENTATION NOTE: When throwing an exception from this method you need
         * to throw an ConnectException that accepts a Message, a cause exception and
         * a reference to this MessageReceiver i.e. throw new ConnectException(new
         * Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
         */

        // TODO the code necessay to Connect to the underlying resource
    }

    @Override
    public void doDisconnect() throws ConnectException
    {
        /*
         * IMPLEMENTATION NOTE: Disconnects and tidies up any rources allocted using
         * the doConnect() method. This method should return the MessageReceiver into
         * a disconnected state so that it can be connected again using the
         * doConnect() method.
         */

        // TODO release any resources here
    }

    @Override
    public void doStart()
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should perform any actions necessary to enable the
         * reciever to start reciving events. This is different to the doConnect()
         * method which actually makes a connection to the transport, but leaves the
         * MessageReceiver in a stopped state. For polling-based MessageReceivers the
         * start() method simply starts the polling thread, for the Axis Message
         * receiver the start method on the SOAPService is called. What action is
         * performed here depends on the transport being used. Most of the time a
         * custom provider doesn't need to override this method.
         */
    }

    @Override
    public void doStop()
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should perform any actions necessary to stop the
         * reciever from receiving events.
         */
    }

    @Override
    public void doDispose()
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Is called when the Conector is being dispoed and
         * should clean up any resources. The doStop() and doDisconnect() methods
         * will be called implicitly when this method is called.
         */
    }

    @Override
    public void poll() throws Exception
    {
        /*
         * IMPLEMENTATION NOTE: Once you have read the object it can be passed into
         * Mule by first wrapping the object with the Message adapter for this
         * transport and calling routeMessage i.e. MessageAdapter adapter =
         * connector.getMessageAdapter(object); routeMessage(new
         * MuleMessage(adapter), endpoint.isSynchronous());
         */

        // TODO request a message from the underlying technology e.g. Read a
        // file
        byte[] data = null;
        SmbFile smbFile;
        SmbFileInputStream sfis = null;
        EndpointURI uri = endpoint.getEndpointURI();

        try
        {
            if (SmbConnector.checkNullOrBlank(uri.getUser())
                || SmbConnector.checkNullOrBlank(uri.getPassword()))
            {
                logger.warn("No user or password supplied. Attempting to connect with just smb://<host>/<path>");
                logger.warn("smb://" + uri.getHost() + uri.getPath());
                smbFile = new SmbFile("smb://" + uri.getHost() + uri.getPath());
            }
            else
            {
                smbFile = new SmbFile("smb://" + uri.getUser() + ":" + uri.getPassword() + "@"
                                      + uri.getHost() + uri.getPath());
            }
            sfis = new SmbFileInputStream(smbFile);
            data = new byte[(int)smbFile.length()];
            sfis.read(data);
            logger.warn(new String(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // TODO is this class even used?
//        MessageAdapter adapter = connector.getMessageAdapter(new String(data));
//        routeMessage(new DefaultMuleMessage(adapter), endpoint.isSynchronous());
//        sfis.close();
    }

}
