/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cifs.util;

import org.mule.tck.probe.Probe;
import org.mule.transport.cifs.functional.SmbOutboundTestCase;

public class FileExistsOnSmbServer implements Probe
{
    private String filename;

    public FileExistsOnSmbServer(String filename)
    {
        super();
        this.filename = filename;
    }

    public boolean isSatisfied()
    {
        try
        {
            return new SmbUtil().fileExists(filename);
        }
        catch (Exception ex)
        {
            SmbOutboundTestCase.fail(ex.getMessage());

            // this is not strictly necessary but the compiler insists on it
            return false;
        }
    }

    public String describeFailure()
    {
        return "file did not arrive on the SMB server";
    }
}

