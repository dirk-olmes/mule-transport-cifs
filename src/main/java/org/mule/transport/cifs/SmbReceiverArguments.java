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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmbReceiverArguments
{
    protected final Log logger = LogFactory.getLog(getClass());

    protected SmbConnector connector;
    protected Map<?, ?> endpointProperties;
    protected List<Object> arguments;

    public SmbReceiverArguments(SmbConnector smbConnector, Map<?, ?> properties)
    {
        super();
        connector = smbConnector;
        endpointProperties = properties;
        arguments = new ArrayList<Object>();
    }

    public  Object[] asArray()
    {
        addPollingFrequency();
        addMoveToDirectory();
        addMoveToPattern();
        addFileAge();

        return arguments.toArray();
    }

    private void addPollingFrequency()
    {
        long polling = connector.getPollingFrequency();

        if (endpointProperties != null)
        {
            String tempPolling = (String) endpointProperties.get(SmbConnector.PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null)
            {
                polling = Long.parseLong(tempPolling);
            }
        }

        if (polling <= 0)
        {
            polling = SmbConnector.DEFAULT_POLLING_FREQUENCY;
        }

        logger.debug("set polling frequency to " + polling);
        arguments.add(Long.valueOf(polling));
    }

    private void addMoveToDirectory()
    {
        String moveToDir = connector.getMoveToDirectory();

        if (endpointProperties != null)
        {
            String tempMoveToDir = (String) endpointProperties.get(SmbConnector.PROPERTY_MOVE_TO_DIRECTORY);
            if (tempMoveToDir != null)
            {
                moveToDir = tempMoveToDir;
            }
        }

        arguments.add(moveToDir != null ? moveToDir : "");
    }

    private void addMoveToPattern()
    {
        String moveToPattern = connector.getMoveToPattern();

        if (endpointProperties != null)
        {
            String tempMoveToPattern = (String) endpointProperties.get(SmbConnector.PROPERTY_MOVE_TO_PATTERN);
            if (tempMoveToPattern != null)
            {
                moveToPattern = tempMoveToPattern;
            }
        }

        logger.debug("set moveTo Pattern to: " + moveToPattern);
        arguments.add(moveToPattern != null ? moveToPattern : "");
    }

    private void addFileAge()
    {
        long fileAge = connector.getFileAge();

        if (endpointProperties != null)
        {
            String tempFileAge = (String) endpointProperties.get(SmbConnector.PROPERTY_FILE_AGE);
            if (tempFileAge != null)
            {
                try
                {
                    fileAge = Long.parseLong(tempFileAge);
                }
                catch (Exception ex)
                {
                    logger.error("Failed to set fileAge", ex);
                }
            }
        }

        logger.debug("set fileAge to: " + fileAge + "milliseconds");
        arguments.add(Long.valueOf(fileAge));
    }
}
