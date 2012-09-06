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
import org.mule.transport.file.FileConnector;
import org.mule.transport.file.FilenameParser;

public class FilenameBuilder
{
    private final OutboundEndpoint endpoint;
    private final SmbConnector connector;

    public FilenameBuilder(SmbConnector connector, OutboundEndpoint endpoint)
    {
        super();
        this.connector = connector;
        this.endpoint = endpoint;
    }

    public String getFilename(MuleEvent event)
    {
        return getFilename(event.getMessage());
    }

    public String getFilename(MuleMessage message)
    {
        String filename = message.getOutboundProperty(SmbConnector.PROPERTY_FILENAME);
        if (filename == null)
        {
            filename = filenameFromOutputPatternOnMessage(message);
        }

        if (filename == null)
        {
            filename = filenameFromOutputPatternOnEndpoint(message);
        }

        if (filename == null)
        {
            filename = filenameFromOutputPatternOnConnector(message);
        }

        if (filename == null)
        {
            filename = message.getOutboundProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME);
        }

        if (filename == null)
        {
            filename = filenameFromFilenameParserOnConnector(message);
        }

        return filename;
    }

    protected String filenameFromOutputPatternOnMessage(MuleMessage message)
    {
        String pattern = message.getOutboundProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN);
        return parse(message, pattern);
    }

    protected String filenameFromOutputPatternOnEndpoint(MuleMessage message)
    {
        String pattern = (String)endpoint.getProperty(SmbConnector.PROPERTY_OUTPUT_PATTERN);
        return parse(message, pattern);
    }

    protected String filenameFromOutputPatternOnConnector(MuleMessage message)
    {
        String pattern = connector.getOutputPattern();
        return parse(message, pattern);
    }

    private String filenameFromFilenameParserOnConnector(MuleMessage message)
    {
        return evaluateFilenameParser(message, null);
    }

    protected String parse(MuleMessage message, String pattern)
    {
        if (pattern == null)
        {
            return null;
        }

        return evaluateFilenameParser(message, pattern);
    }

    protected String evaluateFilenameParser(MuleMessage message, String pattern)
    {
        FilenameParser parser = connector.getFilenameParser();
        return parser.getFilename(message, pattern);
    }
}
