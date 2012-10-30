package com.alcatel_lucent.dms.service.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.service.DictionaryProp;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

@Component("DCTParser")
public class DCTParser extends DictionaryParser {

	public static final String lineSeparator = "\n";
	// System.getProperty("line.separator");

	@Autowired
	private LanguageService languageService;
	
    @Autowired
    private DictionaryProp dictProp;
    
    private BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
    private boolean isFirstLevel = true;
    
	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
		boolean entry = isFirstLevel; 
		isFirstLevel = false;
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
                deliveredDicts.addAll(parse(rootDir, dctFile, acceptedFiles));
            }
            if (entry && exceptions.hasNestedException()) {
            	throw exceptions;
            }
            return deliveredDicts;
        } else if (!Util.isDCTFile(file)) {
        	return deliveredDicts;
        }

        if (Util.isZipFile(file)) {
            try {
                deliveredDicts.addAll(parseZip(new ZipFile(file)));
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
            if (entry && exceptions.hasNestedException()) {
            	throw exceptions;
            }
            return deliveredDicts;
        }
        
        String dictPath = file.getAbsolutePath().replace("\\", "/");
		String dictName = dictPath;
		if (rootDir != null && dictName.startsWith(rootDir)) {
			dictName = dictName.substring(rootDir.length());
		}
        FileInputStream in = null;
        try {
        	in = new FileInputStream(file);
        	try {
        		Dictionary dict = parseDCT(dictName, dictPath, in);
        		deliveredDicts.add(dict);
        	} catch (BusinessException e) {
        		exceptions.addNestedException(e);
        	}
    		acceptedFiles.add(file);
            if (entry && exceptions.hasNestedException()) {
            	throw exceptions;
            }
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

	@SuppressWarnings("unchecked")
	private Collection<Dictionary> parseZip(ZipFile file) throws BusinessException {
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
                Dictionary dict = parseDCT(dictionaryName, path, is);
                if (null != dict) {
                    deliveredDicts.add(dict);
                }
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        return deliveredDicts;
	}
	
	private Dictionary parseDCT(String dictName, String path, InputStream in) throws BusinessException {
        log.info("Parsing DCT file '" + dictName + "'");
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        String encoding = null;
        try {
        	encoding = dictProp.getDictionaryEncoding(dictName);
        } catch (Exception e) {
        	log.warn("Encoding is not specified for '" + dictName + "', using ISO8859-1.");
        	encoding = "ISO-8859-1";
        }
        
        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(path);
        dictBase.setEncoding(encoding);
        dictBase.setFormat(Constants.DICT_FORMAT_DCT);
        
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
	 		dict.setParseWarnings(warnings);
			dr.close();
			return dict;
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e.toString());
		}
	}

}
