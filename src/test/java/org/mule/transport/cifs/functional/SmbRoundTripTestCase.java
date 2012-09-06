/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cifs.functional;

import org.mule.tck.probe.PollingProber;
import org.mule.transport.cifs.util.FileExistsOnSmbServer;
import org.mule.transport.cifs.util.SmbUtil;

import org.junit.Test;

public class SmbRoundTripTestCase extends AbstractSmbTestCase
{
    private static final String FILENAME = "input.txt";

    @Override
    protected String getConfigResources()
    {
        return "smb-round-trip-config.xml";
    }

    @Test
    public void messageShouldBeReceivedFromSmbServer() throws Exception
    {
        putFile();

        String processedFile = "processed/" + FILENAME;
        new PollingProber(10000, 1000).check(new FileExistsOnSmbServer(processedFile));
    }

    private void putFile() throws Exception
    {
        new SmbUtil().createFile(FILENAME, TEST_MESSAGE);
    }
}
