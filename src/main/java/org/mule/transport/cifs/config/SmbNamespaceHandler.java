/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cifs.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.cifs.SmbConnector;
import org.mule.transport.file.ExpressionFilenameParser;
import org.mule.transport.file.FilenameParser;

/**
 * Registers a Bean Definition Parser for handling <code><smb:connector></code> elements
 * and supporting endpoint elements.
 */
public class SmbNamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
        registerConnectorDefinitionParser(SmbConnector.class);
        registerStandardTransportEndpoints(SmbConnector.SMB, URIBuilder.HOST_ATTRIBUTES);

        registerBeanDefinitionParser("custom-filename-parser", new ChildDefinitionParser("filenameParser", null, FilenameParser.class));
        registerBeanDefinitionParser("expression-filename-parser", new ChildDefinitionParser("filenameParser", ExpressionFilenameParser.class));
    }
}
