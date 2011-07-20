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


public class SmbOutboundTestCase extends AbstractSmbTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "smb-outbound-config.xml";
    }

    public void testFileWasSentToSmbServer() throws Exception
    {
        muleContext.getClient().dispatch("vm://data", TEST_MESSAGE, null);
        new PollingProber(10000, 1000).check(new FileExistsOnSmbServer("out.txt"));

        byte[] contents = new SmbUtil().getContentsOfFile("out.txt");
        assertEquals(TEST_MESSAGE.getBytes(), contents);
    }
}
