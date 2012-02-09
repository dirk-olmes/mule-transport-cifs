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

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmbReceiverArgumentsTestCase
{
    @Test
    public void pollingFrequencyShouldBeDefaultFromConnectorIfEnpointPropertiesAreNull()
    {
        long pollingFrequency = 42;
        SmbConnector connector = createConnectorWithPollingFrequency(pollingFrequency);

        Object[] arguments = new SmbReceiverArguments(connector, null).asArray();
        assertEquals(pollingFrequency, arguments[0]);
    }

    @Test
    public void pollingFrequencyFromEndpointPropertiesShouldOverrideConnectorValue()
    {
        SmbConnector connector = createConnectorWithPollingFrequency(1000);
        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_POLLING_FREQUENCY, "42");

        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(42l, arguments[0]);
    }

    @Test
    public void pollingFrequencyShouldFallBackToDefaultOnInvalidValue()
    {
        long pollingFrequency = 42;

        SmbConnector connector = createConnectorWithPollingFrequency(pollingFrequency);
        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_POLLING_FREQUENCY, "-1");

        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(SmbConnector.DEFAULT_POLLING_FREQUENCY, arguments[0]);
    }

    @Test
    public void moveToDirectoryShouldBeDefaultFromConnectorIfEnpointPropertiesAreNull()
    {
        String moveToDir = "/tmp/foo";
        SmbConnector connector = createConnectorWithMoveToDirectory(moveToDir);

        Object[] arguments = new SmbReceiverArguments(connector, null).asArray();
        assertEquals(moveToDir, arguments[1]);

    }

    @Test
    public void moveToDirectoryFromEndpointPropertiesShouldOverrideConnectorValue()
    {
        SmbConnector connector = createConnectorWithMoveToDirectory("/some/other/dir");

        String moveToDir = "/tmp/foo";
        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_MOVE_TO_DIRECTORY, moveToDir);


        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(moveToDir, arguments[1]);
    }

    @Test
    public void moveToPatternShouldBeDefaultFromConnectorIfEnpointPropertiesAreNull()
    {
        String moveToPattern = "#[function:uuid]";
        SmbConnector connector = createConnectorWithMoveToPattern(moveToPattern);

        Object[] arguments = new SmbReceiverArguments(connector, null).asArray();
        assertEquals(moveToPattern, arguments[2]);
    }

    @Test
    public void moveToPatternFromEndpointPropertiesShouldOverrideConnectorValue()
    {
        SmbConnector connector = createConnectorWithMoveToPattern("#[function:foobar]");

        String moveToPattern = "#[function:uuid]";
        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_MOVE_TO_PATTERN, moveToPattern);


        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(moveToPattern, arguments[2]);
    }

    @Test
    public void fileAgeShouldBeDefaultFromConnectorIfEnpointPropertiesAreNull()
    {
        long fileAge = 42;
        SmbConnector connector = createConnectorWithFileAge(fileAge);

        Object[] arguments = new SmbReceiverArguments(connector, null).asArray();
        assertEquals(fileAge, arguments[3]);
    }

    @Test
    public void fileAgeFromEndpointPropertiesShouldOverrideConnectorValue()
    {
        SmbConnector connector = createConnectorWithFileAge(10000);

        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_FILE_AGE, "42");


        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(42l, arguments[3]);
    }

    @Test
    public void illlegalFileAgeFromEndpointPropertiesShouldBeOverriddenByTheConnectorDefault()
    {
        long fileAge = 42;
        SmbConnector connector = createConnectorWithFileAge(fileAge);

        Map<String, String> endpointProperties =
            Collections.singletonMap(SmbConnector.PROPERTY_FILE_AGE, "invalid");

        Object[] arguments = new SmbReceiverArguments(connector, endpointProperties).asArray();
        assertEquals(42l, arguments[3]);

    }

    private SmbConnector createConnectorWithPollingFrequency(long pollingFrequency)
    {
        SmbConnector connector = new SmbConnector(null);
        connector.setPollingFrequency(pollingFrequency);
        return connector;
    }

    private SmbConnector createConnectorWithMoveToDirectory(String dir)
    {
        SmbConnector connector = new SmbConnector(null);
        connector.setMoveToDirectory(dir);
        return connector;
    }

    private SmbConnector createConnectorWithMoveToPattern(String pattern)
    {
        SmbConnector connector = new SmbConnector(null);
        connector.setMoveToPattern(pattern);
        return connector;
    }

    private SmbConnector createConnectorWithFileAge(long age)
    {
        SmbConnector connector = new SmbConnector(null);
        connector.setFileAge(age);
        return connector;
    }
}
