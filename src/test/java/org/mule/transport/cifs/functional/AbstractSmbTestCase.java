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

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.cifs.util.SmbUtil;

import java.util.Arrays;

public abstract class AbstractSmbTestCase extends FunctionalTestCase
{
    @Override
    protected boolean isDisabledInThisEnvironment()
    {
        boolean environmentIsSet = SmbUtil.environmentIsSet();
        if (environmentIsSet == false)
        {
            logger.warn("Test ist disabled because the required system properties were not set");
            return true;
        }
        return false;
    }

    public void assertEquals(byte[] bytes1, byte[] bytes2)
    {
        assertTrue(Arrays.equals(bytes1, bytes2));
    }
}


