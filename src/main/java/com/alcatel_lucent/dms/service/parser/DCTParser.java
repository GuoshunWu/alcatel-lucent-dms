package com.alcatel_lucent.dms.service.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.service.DictionaryProp;
import com.alcatel_lucent.dms.service.DictionaryServiceImpl;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

@Component("DCTParser")
public class DCTParser extends DictionaryParser {

	public static final String lineSeparator = "\n";
	// System.getProperty("line.separator");

	private Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	@Autowired
	private LanguageService languageService;
	
    @Autowired
    private DictionaryProp dictProp;
    
	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<BusinessWarning> warnings) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;
        if (file.isDirectory()) {
            File[] dctFileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isDCTFile(pathname)
                            || Util.isZipFile(pathname);
                }
            });
            for (File dctFile : dctFileOrDirs) {
                deliveredDicts.addAll(parse(rootDir, dctFile, warnings));
            }
            return deliveredDicts;
        }

        if (Util.isZipFile(file)) {
            try {
                deliveredDicts.addAll(parseZip(new ZipFile(file), warnings));
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
            return deliveredDicts;
        }
        
        String dictPath = file.getAbsolutePath().replace("\\", "/");
        String dictName = rootDir == null ? dictPath : dictPath.replace(rootDir, "");
        FileInputStream in = null;
        try {
        	in = new FileInputStream(file);
            Dictionary dict = parseDCT(dictName, dictPath, in, warnings);
    		deliveredDicts.add(dict);
            return deliveredDicts;
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new BusinessException(e.toString());
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	private Collection<Dictionary> parseZip(ZipFile file, Collection<BusinessWarning> warnings) throws BusinessException {
        Collection<Dictionary> deliveredDicts = new ArrayList<Dictionary>();

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
        ZipEntry entry = null;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!Util.isDCTFile(entry.getName())) {
                continue;
            }
            try {
                InputStream is = file.getInputStream(entry);
                String dictionaryName = entry.getName();
                String path = file.getName() + dictionaryName;
                Dictionary dict = parseDCT(dictionaryName, path, is, warnings);
                if (null != dict) {
                    deliveredDicts.add(dict);
                }
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        return deliveredDicts;
	}
	
	private Dictionary parseDCT(String dictName, String path, InputStream in, Collection<BusinessWarning> warnings) throws BusinessException {
        String encoding = dictProp.getDictionaryEncoding(dictName);
        
        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(path);
        dictBase.setEncoding(encoding);
        dictBase.setFormat("dct");
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);

		try {
/*
			if (in instanceof FileInputStream) {
				FileChannel channel = ((FileInputStream) in).getChannel();
				long fileSize = channel.size();
				MappedByteBuffer mbf = channel.map(FileChannel.MapMode.READ_ONLY,
						0, fileSize);
				// file size less that 200 MB
				int MAX_FILE_SIZE = 1024 * 1024 * 200;
				byte[] buf = null;
				if (fileSize < MAX_FILE_SIZE) {
					buf = new byte[(int) fileSize];
					mbf.get(buf);
					channel.close();
					in = new ByteArrayInputStream(buf);
				} else {
					throw new BusinessException(BusinessException.FILE_TOO_LARGE, path);
				}
			}
*/			
			BufferedReader dctReader = new BufferedReader(new InputStreamReader(
					in, encoding));
			DCTReader dr = new DCTReader(dctReader, dictionary);
	        dr.setLanguageService(this.languageService);
	 		Dictionary dict = dr.readDictionary();
			warnings.addAll(dr.getWarnnings());
			dr.close();
			return dict;
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e.toString());
		}
	}

}
