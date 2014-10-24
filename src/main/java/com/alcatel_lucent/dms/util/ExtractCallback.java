package com.alcatel_lucent.dms.util;

import com.alcatel_lucent.dms.BusinessException;
import com.google.common.io.Files;
import net.sf.sevenzipjbinding.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.alcatel_lucent.dms.BusinessException.DECOMPRESS_ARCHIVE_ERROR;

/**
 * Created by Administrator on 2014/6/8 0008.
 */


public class ExtractCallback implements IArchiveExtractCallback {
    private int index;

    private ISevenZipInArchive inArchive;
    private File archiveFile;
    private File destinationDir;

    private BufferedOutputStream bos;

    public ExtractCallback(ISevenZipInArchive inArchive, File destinationDir, File archiveFile) {
        this.inArchive = inArchive;
        this.destinationDir = destinationDir;
        this.archiveFile = archiveFile;
    }

    static Logger log = LoggerFactory.getLogger(ExtractCallback.class);

    @Override
    public ISequentialOutStream getStream(final int index, ExtractAskMode extractAskMode) throws SevenZipException {
        this.index = index;
        if (extractAskMode != ExtractAskMode.EXTRACT) {
            return null;
        }
        String archivePath = (String) inArchive.getProperty(index, PropID.PATH);

        if ((Boolean) inArchive.getProperty(index, PropID.IS_FOLDER)) {
            try {
                FileUtils.forceMkdir(destinationDir);
            } catch (IOException e) {
                e.printStackTrace();
                throw new BusinessException(DECOMPRESS_ARCHIVE_ERROR, archivePath, archiveFile.getAbsolutePath());
            }
            return null;
        }

        File destinationFile = new File(destinationDir, archivePath);
        try {
            Files.createParentDirs(destinationFile);
            bos = new BufferedOutputStream(new FileOutputStream(destinationFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(null == bos){
            log.error("Create file {} error", destinationFile);
            return null;
        }
        final BufferedOutputStream finalBos = bos;
        ISequentialOutStream outStream = new ISequentialOutStream() {
            @Override
            public int write(byte[] data) throws SevenZipException {
                try {
                    finalBos.write(data);
                } catch (Exception e) {
                    throw new SevenZipException("Error writing output file", e);
                }
                return data.length;
            }
        };

        return outStream;
    }


    @Override
    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
        if (extractOperationResult != ExtractOperationResult.OK) {
            log.error("Extraction {} error in archive {}.", inArchive.getProperty(index, PropID.PATH), archiveFile.getAbsolutePath());
            throw new BusinessException(DECOMPRESS_ARCHIVE_ERROR, inArchive.getProperty(index, PropID.NAME),
                    archiveFile.getAbsolutePath());
        } else {
            log.info("Extraction {}...", inArchive.getProperty(index, PropID.PATH));
        }
        IOUtils.closeQuietly(bos);
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

