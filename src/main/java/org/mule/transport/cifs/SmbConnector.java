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

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.config.ConfigurationException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.service.Service;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.MessageReceiver;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractConnector;
import org.mule.transport.file.ExpressionFilenameParser;
import org.mule.transport.file.FilenameParser;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbFile;

public class SmbConnector extends AbstractConnector
{
    /* This constant defines the main transport protocol identifier */
    public static final String SMB = "smb";
    // only
    public static final int DEFAULT_POLLING_FREQUENCY = 1000;
    public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern"; // outbound
    // only
    public static final String PROPERTY_FILENAME = "filename";
    private long pollingFrequency;
    private String outputPattern;

    private FilenameParser filenameParser = new ExpressionFilenameParser();

    public static final String PROPERTY_FILE_AGE = "fileAge"; // inbound only
    public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern"; // inbound
    // only
    public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory"; // inbound
    // only

    private String moveToPattern = "";
    private String moveToDirectory = "";
    private boolean checkFileAge = false;
    private long fileAge = 0;

    public SmbConnector(MuleContext context)
    {
        super(context);
    }

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    /*
     * IMPLEMENTATION NOTE: All configuaration for the transport should be set on the
     * Connector object, this is the object that gets configured in MuleXml
     */

    @Override
    public void doInitialise() throws InitialisationException
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Is called once all bean properties have been set on
         * the connector and can be used to validate and initialise the connectors
         * state.
         */
    }

    @Override
    public void doConnect() throws Exception
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Makes a connection to the underlying resource. When
         * connections are managed at the receiver/dispatcher level, this method may
         * do nothing
         */
    }

    @Override
    public void doDisconnect() throws Exception
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Disconnects any connections made in the connect
         * method If the connect method did not do anything then this method
         * shouldn't do anything either.
         */
    }

    @Override
    public void doStart() throws MuleException
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: If there is a single server instance or connection
         * associated with the connector i.e. AxisServer or a Jms Connection or Jdbc
         * Connection, this method should put the resource in a started state here.
         */
    }

    @Override
    public void doStop() throws MuleException
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should put any associated resources into a stopped
         * state. Mule will automatically call the stop() method.
         */
    }

    @Override
    public void doDispose()
    {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should clean up any open resources associated with
         * the connector.
         */
    }

    @Override
    public OutputStream getOutputStream(OutboundEndpoint endpoint, MuleEvent event) throws MuleException
    {
        SmbFile smbFile;
        OutputStream os = null;
        String filename = getFilename(endpoint, event.getMessage());
        EndpointURI uri = endpoint.getEndpointURI();

        try
        {
            if (checkNullOrBlank(uri.getUser()) || checkNullOrBlank(uri.getPassword()))
            {
                logger.warn("No user or password supplied. Attempting to connect with just smb://<host>/<path>");
                logger.info("smb://" + uri.getHost() + uri.getPath() + "/" + filename);
                smbFile = new SmbFile("smb://" + uri.getHost() + uri.getPath() + filename);
            }
            else
            {
                logger.info("smb://" + uri.getUser() + ":" + uri.getPassword() + "@" + uri.getHost()
                            + uri.getPath() + filename);
                smbFile = new SmbFile("smb://" + uri.getUser() + ":" + uri.getPassword() + "@"
                                      + uri.getHost() + uri.getPath() + filename);
            }
            if (!smbFile.exists())
            {
                smbFile.createNewFile();
            }
            os = smbFile.getOutputStream();
        }
        catch (Exception e)
        {
            throw new DispatchException(CoreMessages.streamingFailedNoStream(), event, endpoint, e);
        }

        return os;
    }

    private String getFilename(ImmutableEndpoint endpoint, MuleMessage message) throws MuleException
    {
        String filename = (String)message.getProperty(SmbConnector.PROPERTY_FILENAME);
        String outPattern = (String)endpoint.getProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN);
        if (outPattern == null)
        {
            outPattern = message.getStringProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN, getOutputPattern());
        }
        if (outPattern != null || filename == null)
        {
            filename = generateFilename(message, outPattern);
        }
        if (filename == null)
        {
            throw new ConfigurationException(CoreMessages.objectIsNull("filename"));
        }
        return filename;
    }

    private String generateFilename(MuleMessage message, String pattern)
    {
        if (pattern == null)
        {
            pattern = getOutputPattern();
        }
        return getFilenameParser().getFilename(message, pattern);
    }

    public void setOutputPattern(String outputPattern)
    {
        this.outputPattern = outputPattern;
    }

    public String getOutputPattern()
    {
        return outputPattern;
    }

    public static boolean checkNullOrBlank(String validate)
    {
        return (validate == null || validate.equals(""));
    }

    public String getProtocol()
    {
        return SMB;
    }

    public MessageReceiver createReceiver(Service service, InboundEndpoint endpoint) throws Exception
    {
        List args = getReceiverArguments(endpoint.getProperties());
        return serviceDescriptor.createMessageReceiver(this, service, endpoint, args.toArray());
    }

    protected List getReceiverArguments(Map endpointProperties)
    {
        List args = new ArrayList();

        long polling = getPollingFrequency();
        String moveToDir = getMoveToDirectory();
        String moveToPattern = getMoveToPattern();

        if (endpointProperties != null)
        {
            // Override properties on the endpoint for the specific endpoint
            String tempPolling = (String)endpointProperties.get(PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null)
            {
                polling = Long.parseLong(tempPolling);
            }

            String move = (String)endpointProperties.get(PROPERTY_MOVE_TO_DIRECTORY);
            if (move != null)
            {
                moveToDir = move;
            }

            String tempMoveToPattern = (String)endpointProperties.get(PROPERTY_MOVE_TO_PATTERN);
            if (tempMoveToPattern != null)
            {
                logger.debug("set moveTo Pattern to: " + tempMoveToPattern);
                moveToPattern = tempMoveToPattern;
            }

            String tempFileAge = (String)endpointProperties.get(PROPERTY_FILE_AGE);
            if (tempFileAge != null)
            {
                try
                {
                    logger.debug("set fileAge to: " + tempFileAge + "millisec");
                    setFileAge(Long.parseLong(tempFileAge));
                }
                catch (Exception ex1)
                {
                    logger.error("Failed to set fileAge", ex1);
                }
            }

        }

        if (polling <= 0)
        {
            polling = DEFAULT_POLLING_FREQUENCY;
        }

        logger.debug("set polling frequency to " + polling);
        args.add(polling);
        args.add(moveToDir != null ? moveToDir : "");
        args.add(moveToPattern != null ? moveToPattern : "");
        args.add(new Long(fileAge));

        return args;
    }

    /**
     * Override this method to do extra checking on the file.
     */
    protected boolean validateFile(SmbFile file)
    {
        if (getCheckFileAge())
        {
            long fileAge = getFileAge();
            long lastMod = file.getLastModified();
            long now = System.currentTimeMillis();
            long thisFileAge = now - lastMod;

            logger.debug("fileAge = " + thisFileAge + ", expected = " + fileAge + ", now = " + now
                         + ", lastMod = " + lastMod);
            if (thisFileAge < fileAge)
            {
                logger.debug("The file has not aged enough yet, will return nothing for: " + file.getName());
                return false;
            }
        }
        return true;
    }

    public boolean isStreaming()
    {
        return false;
    }

    /**
     * @return Returns the filenameParser.
     */
    public FilenameParser getFilenameParser()
    {
        return filenameParser;
    }

    /**
     * @param filenameParser The filenameParser to set.
     */
    public void setFilenameParser(FilenameParser filenameParser)
    {
        this.filenameParser = filenameParser;
    }

    /**
     * @return Returns the pollingFrequency.
     */
    public long getPollingFrequency()
    {
        return pollingFrequency;
    }

    /**
     * @param pollingFrequency The pollingFrequency to set.
     */
    public void setPollingFrequency(long pollingFrequency)
    {
        this.pollingFrequency = pollingFrequency;
    }

    /**
     * Getter for property 'moveToDirectory'.
     *
     * @return Returns the moveToDirectory.
     */
    public String getMoveToDirectory()
    {
        return moveToDirectory;
    }

    /**
     * Setter for property 'moveToDirectory'.
     *
     * @param dir The moveToDirectory to set.
     */
    public void setMoveToDirectory(String dir)
    {
        this.moveToDirectory = dir;
    }

    /**
     * Getter for property 'moveToPattern'.
     *
     * @return Returns the moveToPattern.
     */
    public String getMoveToPattern()
    {
        return moveToPattern;
    }

    /**
     * Setter for property 'moveToPattern '.
     *
     * @param moveToPattern The moveToPattern to set.
     */
    public void setMoveToPattern(String moveToPattern)
    {
        this.moveToPattern = moveToPattern;
    }

    /**
     * Getter for property 'fileAge'.
     *
     * @return Returns the fileAge.
     */
    public long getFileAge()
    {
        return fileAge;
    }

    public boolean getCheckFileAge()
    {
        return checkFileAge;
    }

    /**
     * Setter for property 'fileAge'.
     *
     * @param fileAge The fileAge in milliseconds to set.
     */
    public void setFileAge(long fileAge)
    {
        this.fileAge = fileAge;
        this.checkFileAge = true;
    }
}
