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

import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbUtil
{
    public static boolean environmentIsSet()
    {
        if (StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword())
            && StringUtils.isNotBlank(getHost()))
        {
            return true;
        }
        return false;
    }

    public static String getUsername()
    {
        return System.getProperty("smb.user");
    }

    public static String getPassword()
    {
        return System.getProperty("smb.password");
    }

    public static String getHost()
    {
        return System.getProperty("smb.host");
    }

    public void createFile(String filename, String content) throws IOException
    {
        SmbFile file = createSmbFile(filename);

        OutputStream outputStream = null;
        try
        {
            outputStream = file.getOutputStream();
            IOUtils.write(content, outputStream);
        }
        finally
        {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public void deleteFile(String filename) throws IOException
    {
        SmbFile file = createSmbFile(filename);
        if (file.exists())
        {
            file.delete();
        }
    }

    public boolean fileExists(String filename) throws MalformedURLException, SmbException
    {
        SmbFile file = createSmbFile(filename);
        return file.exists();
    }

    public byte[] getContentsOfFile(String filename) throws IOException
    {
        SmbFile file = createSmbFile(filename);

        InputStream in = null;
        try
        {
            in = file.getInputStream();
            return IOUtils.toByteArray(in);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    private SmbFile createSmbFile(String filename) throws MalformedURLException
    {
        String url = String.format("smb://%s:%s@%s/mule-share/%s", getUsername(), getPassword(),
            getHost(), filename);
        return new SmbFile(url);
    }
}
