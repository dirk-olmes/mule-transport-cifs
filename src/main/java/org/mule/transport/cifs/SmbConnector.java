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
import org.mule.api.config.ConfigurationException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.MessageReceiver;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractConnector;
import org.mule.transport.file.ExpressionFilenameParser;
import org.mule.transport.file.FilenameParser;

import java.io.OutputStream;
import java.util.Map;

import jcifs.smb.SmbFile;

public class SmbConnector extends AbstractConnector
{
    /* This constant defines the main transport protocol identifier */
    public static final String SMB = "smb";
    // only
    public static final long DEFAULT_POLLING_FREQUENCY = 1000;
    public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern"; // outbound
    // only
    public static final String PROPERTY_FILENAME = "filename";
    private long pollingFrequency;
    private String outputPattern;

    private FilenameParser filenameParser;

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

        filenameParser = new ExpressionFilenameParser();
        filenameParser.setMuleContext(context);
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
        OutputStream stream = null;

        SmbFile smbFile;
        try
        {
            String filename = getFilename(endpoint, event);
            EndpointURI uri = endpoint.getEndpointURI();

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

            stream = smbFile.getOutputStream();
        }
        catch (Exception e)
        {
            throw new DispatchException(CoreMessages.streamingFailedNoStream(), event, endpoint, e);
        }

        return stream;
    }

    private String getFilename(OutboundEndpoint endpoint, MuleEvent event) throws MuleException
    {
        String filename = new FilenameBuilder(this, endpoint).getFilename(event);

        if (filename == null)
        {
            throw new ConfigurationException(CoreMessages.objectIsNull("filename"));
        }

        return filename;
    }

    public void setOutputPattern(String outputPattern)
    {
        this.outputPattern = outputPattern;
    }

    public String getOutputPattern()
    {
        return outputPattern;
    }

    // TODO replace with StringUtils
    public static boolean checkNullOrBlank(String validate)
    {
        return (validate == null || validate.equals(""));
    }

    public String getProtocol()
    {
        return SMB;
    }

    @Override
    public MessageReceiver createReceiver(FlowConstruct flowConstruct, InboundEndpoint endpoint) throws Exception
    {
        Map<?, ?> endpointProperties = endpoint.getProperties();
        Object[] additionalArguments = new SmbReceiverArguments(this, endpointProperties).asArray();
        return serviceDescriptor.createMessageReceiver(this, flowConstruct, endpoint, additionalArguments);
    }

    /**
     * Override this method to do extra checking on the file.
     */
    protected boolean validateFile(SmbFile file)
    {
        if (checkFileAge)
        {
            long lastMod = file.getLastModified();
            long now = System.currentTimeMillis();
            long currentAge = now - lastMod;

            logger.debug("fileAge = " + currentAge + ", expected = " + fileAge + ", now = " + now
                         + ", lastMod = " + lastMod);
            if (currentAge < fileAge)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("The file has not aged enough yet, will return nothing for: " + file.getName());
                }
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
