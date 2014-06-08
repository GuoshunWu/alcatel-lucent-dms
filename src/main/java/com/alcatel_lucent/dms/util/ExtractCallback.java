package com.alcatel_lucent.dms.util;

import com.google.common.io.Files;
import net.sf.sevenzipjbinding.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2014/6/8 0008.
 */


public class ExtractCallback implements IArchiveExtractCallback {
    private int index;

    private ISevenZipInArchive inArchive;
    private String destDir;

    public ExtractCallback(ISevenZipInArchive inArchive, String destDir) {
        this.inArchive = inArchive;
        this.destDir = destDir;
    }

    static Logger log = LoggerFactory.getLogger(ExtractCallback.class);

    @Override
    public ISequentialOutStream getStream(final int index, ExtractAskMode extractAskMode) throws SevenZipException {
        this.index = index;
        if (extractAskMode != ExtractAskMode.EXTRACT) {
            return null;
        }
        final File destFile = new File(destDir, inArchive.getStringProperty(index, PropID.PATH));

        if ((Boolean) inArchive.getProperty(index, PropID.IS_FOLDER)) {
            try {
                FileUtils.forceMkdir(destFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        return new ISequentialOutStream() {
            public int write(byte[] data) throws SevenZipException {
                BufferedOutputStream bos = null;
                try {
                    Files.createParentDirs(destFile);
                    bos = new BufferedOutputStream(new FileOutputStream(destFile));
                    IOUtils.write(data, bos);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    IOUtils.closeQuietly(bos);
                }
                return data.length; // Return amount of proceed data
            }
        };
    }


    @Override
    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
        if (extractOperationResult != ExtractOperationResult.OK) {
            log.warn("Extraction {} error.", inArchive.getProperty(index, PropID.PATH));
        } else {
            log.info("Extraction {}...", inArchive.getProperty(index, PropID.PATH));
        }
    }

    @Override
    public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {

    }

    @Override
    public void setTotal(long total) throws SevenZipException {

    }

    @Override
    public void setCompleted(long completeValue) throws SevenZipException {

    }
}

