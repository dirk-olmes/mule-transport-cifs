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

public class SmbCreateIntermediatePathTestCase extends AbstractSmbTestCase
{
	private static final String OUTPUT_FOLDER = "nonexistent-path/";
    private static final String OUTPUT_FILENAME = OUTPUT_FOLDER + "out.txt";

    @Override
    protected String getConfigResources()
    {
        return "smb-create-intermediate-path.xml";
    }

    @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();

        if (isDisabledInThisEnvironment() == false)
        {
            new SmbUtil().deleteFile(OUTPUT_FOLDER);
        }
    }

    @Test
    public void fileWasSentToSmbServer() throws Exception
    {
        muleContext.getClient().dispatch("vm://data", TEST_MESSAGE, null);
        new PollingProber(10000, 1000).check(new FileExistsOnSmbServer(OUTPUT_FILENAME));

        byte[] contents = new SmbUtil().getContentsOfFile(OUTPUT_FILENAME);
        assertEquals(TEST_MESSAGE.getBytes(), contents);
    }
}
