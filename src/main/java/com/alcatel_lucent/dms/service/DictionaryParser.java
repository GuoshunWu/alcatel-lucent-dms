/**
 * 
 */
package com.alcatel_lucent.dms.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.util.Util;

/**
 * @author guoshunw
 * 
 */

@Component("dictionaryParser")
public class DictionaryParser {

	public static final String lineSeparator = "\n";
	// System.getProperty("line.separator");

	private Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	@Autowired
	private LanguageService languageService;

	public DictionaryParser() {
	}

	public Dictionary parse(Dictionary dictionary, Reader dctReader,
			Collection<BusinessWarning> warnings) throws IOException,
			BusinessException {
		DictionaryReader dr = new DictionaryReader(dctReader, dictionary);

        dr.setLanguageService(this.languageService);
 		Dictionary dict = dr.readDictionary();
		warnings.addAll(dr.getWarnnings());
		dr.close();
		return dict;
	}

	public Dictionary parse(Application app, String dictionaryName,
			String path, InputStream dctInputStream, String encoding,
			Collection<BusinessWarning> warnings)
			throws IOException {

		// Creates an BufferedReader that uses the encoding charset.
		if (null == encoding) {
			throw new NullPointerException("Encoding is null.");
		}

        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictionaryName);
        dictBase.setPath(path);
        dictBase.setEncoding(encoding);
        dictBase.setApplicationBase(app.getBase());
        dictBase.setFormat("dct");
        
		Dictionary dictionary = new Dictionary();
		dictionary.setApplication(app);
		dictionary.setBase(dictBase);

		if (dctInputStream instanceof FileInputStream) {
			FileInputStream fdis = (FileInputStream) dctInputStream;
			FileChannel channel = fdis.getChannel();
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
				dctInputStream.close();

				dctInputStream = new ByteArrayInputStream(buf);
			}
		}
		BufferedReader dctReader = new BufferedReader(new InputStreamReader(
				dctInputStream, encoding));

		return parse(dictionary, dctReader, warnings);
	}

	/**
	 * Parse a given dct file and generate a Dictionary Object
	 * 
	 * */

	public Dictionary parse(Application app, String dictionaryName,
			String filename, String encoding,
			Collection<BusinessWarning> warnings) throws IOException {
		return parse(app, dictionaryName, new File(filename), encoding,
				warnings);
	}

	/**
	 * Parse a given dct file and generate a Dictionary Object
	 * 
	 * */

	public Dictionary parse(Application app, String dictionaryName, File file,
			String encoding, Collection<BusinessWarning> warnings)
			throws IOException {
		if (!file.exists()) {
			throw new BusinessException(BusinessException.DCT_FILE_NOT_FOUND,
					file.getName());
		}

		log.info("\n######################begin deliver: " + file.getName()
				+ "##########################\n");
		if (null == encoding) {
			encoding = Util.detectEncoding(file);
		}
		InputStream is = new FileInputStream(file);
		Dictionary dict = parse(app, dictionaryName, file.getPath(), is,
				encoding, warnings);
		is.close();
		return dict;
	}
}
