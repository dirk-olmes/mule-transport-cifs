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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.mule.api.MuleEventContext;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.transport.cifs.util.SmbUtil;
import org.mule.util.concurrent.Latch;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class SmbFileAgeTestCase extends AbstractSmbTestCase
{
    private static Log log = LogFactory.getLog(SmbFileAgeTestCase.class);

    private static final long CONFIGURED_FILE_AGE = 5000;
    private static final String FILENAME = "aging-file.txt";

    private Latch eventLatch = new Latch();
    private Exception exceptionInWriterThread = null;

    @Override
    protected String getConfigResources()
    {
        return "smb-file-age-config.xml";
    }

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        new SmbUtil().deleteFile(FILENAME);
    }

    @Test
    public void fileAgeShouldBeRespected() throws Exception
    {
        Callback callback = new Callback(eventLatch);

        FunctionalTestComponent ftc = (FunctionalTestComponent) getComponent("fileAgeFlow");
        ftc.setEventCallback(callback);

        forkWriterThread();
        long start = System.currentTimeMillis();

        assertTrue("file did not arrive in time", eventLatch.await(30, TimeUnit.SECONDS));
        assertNull(exceptionInWriterThread);

        long diff = callback.eventReceivedTime - start;
        assertTrue(diff >= CONFIGURED_FILE_AGE);
    }

    private void forkWriterThread()
    {
        Thread writerThread = new Thread(new SmbWriter(), "smbWriter");
        writerThread.start();
    }

    private class SmbWriter implements Runnable
    {
        private static final long SLEEP_TIME = 1000;

        public void run()
        {
            OutputStream out = null;
            try
            {
                out = new SmbUtil().openStream(FILENAME);
                long chunkCount = CONFIGURED_FILE_AGE / SLEEP_TIME;
                for (int i = 0; i < chunkCount; i++)
                {
                    Thread.sleep(1000);
                    out.write(TEST_MESSAGE.getBytes("UTF-8"));
                    log.debug("wrote message chunk");
                }
            }
            catch (Exception ex)
            {
                exceptionInWriterThread = ex;
            }
            finally
            {
                IOUtils.closeQuietly(out);
            }
        }
    }

    private static class Callback implements EventCallback
    {
        public long eventReceivedTime = 0;
        private Latch latch;

        public Callback(Latch latch)
        {
            super();
            this.latch = latch;
        }

        public void eventReceived(MuleEventContext context, Object component) throws Exception
        {
            eventReceivedTime = System.currentTimeMillis();
            latch.countDown();
        }
    }
}
