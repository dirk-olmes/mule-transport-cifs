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
import org.mule.transport.AbstractMuleMessageFactory;

public class SmbMuleMessageFactory extends AbstractMuleMessageFactory
{
    public SmbMuleMessageFactory(MuleContext context)
    {
        super(context);
    }

    @Override
    protected Class<?>[] getSupportedTransportMessageTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object extractPayload(Object transportMessage, String encoding) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
}
