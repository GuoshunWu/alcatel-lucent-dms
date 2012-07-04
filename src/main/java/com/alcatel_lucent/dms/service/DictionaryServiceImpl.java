package com.alcatel_lucent.dms.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;

public class DictionaryServiceImpl extends BaseServiceImpl implements
		DictionaryService {
	public static final int UTF8_BOM_LENGTH = 3;
	public static final int UTF16_BOM_LENGTH = 2;

	public Dictionary deliverDCT(String filename, Long appId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {
		File dctFile = new File(filename);
		if (!dctFile.exists()) {
			throw BusinessException.DCT_FILE_NOT_FOUND;
		}
		Dictionary dict = new Dictionary();
		dict.setName(dctFile.getName());
		dict.setFormat("dct");
		dict.setPath(dctFile.getPath());
		// retrieve dict related Application
		Application app = (Application) getDao().retrieve(Application.class,
				appId);

		if (null == app) {
			throw BusinessException.APPLICATION_NOT_FOUND;
		}
		dict.setApplication(app);

		// create or retrieve dict related context
		Context ctx = (Context) getDao().retrieveOne(
				"from Context where name = :name",
				JSONObject.fromObject("{'name':'" + dict.getName() + "'}"));
		if (null == ctx) {
			ctx = new Context();
			ctx.setName(dict.getName());
			getDao().create(ctx);
		}

		try {
			if (null == encoding) {
				encoding = detectEncoding(dctFile);
			}
			dict.setEncoding(encoding);

			FileInputStream fis = new FileInputStream(dctFile);
			// Creates an BufferedReader that uses the encoding charset.
			BufferedReader br = new BufferedReader(new InputStreamReader(fis,
					encoding));
			String line = null;
			// languageCodes in current dictionary file
			String[] languageCodes = null;
			// Language pattern in dct file
			Pattern patternLanguage = Pattern
					.compile("^LANGUAGES\\s*\\{((?:\\w{3},?\\s*)+)\\}$");

			// get all alcatel language codes
			Collection<Label> labels = new ArrayList<Label>();
			List<?> allAlcatelLangCodes = getDao().retrieve(
					"select code from AlcatelLanguageCode");

			while (null != (line = br.readLine())) {
				line = line.trim();
				// ignore the comment line and blank line
				if (isCommentOrBlankLine(line)) {
					continue;
				}
				line = removeComments(line);

				Matcher m = patternLanguage.matcher(line);
				// is LANGUAGES
				if (m.matches()) {
					languageCodes = m.group(1).split(",");
					dict.setDictLanguages(getDictLanguages(languageCodes, dict,
							langCharset));
				} else if (line.endsWith(":")) {// a label start
					labels.add(getLabel(line, br, dict, ctx,
							allAlcatelLangCodes, langCodes, langCharset));
				} else {
					throw BusinessException.INVALID_DCT_FILE;
				}

			}

			dict.setLabels(labels);
			br.close();
		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		}

		// update dictionary object to DB
		
		return dict;
	}

	/**
	 * Remove the trailing comments on line
	 * 
	 * @author Guoshun.Wu Date: 2012-07-04
	 * 
	 * @param line
	 *            to be processed line
	 * @return processed line
	 * */
	private String removeComments(String line) {
		String nLine = line.trim();
		// remove trailing comments
		Matcher m_line = Pattern.compile("(.*?[^\"\'])--.*").matcher(nLine);
		if (!m_line.matches()) {
			return nLine;
		}
		return m_line.group(1).trim();
	}

	private boolean isCommentOrBlankLine(String line) {
		return line.startsWith("--") || line.isEmpty();
	}

	/**
	 * Get a Label from given BufferedReader
	 * 
	 * @author Guoshun.Wu
	 * 
	 * @return Label
	 * @throws BusinessException
	 * @throws IOException
	 * */
	private Label getLabel(String key, BufferedReader br, Dictionary dict,
			Context ctx, List<?> allLangCodes, String[] langCodesToImport,
			Map<String, String> langCharset) throws BusinessException,
			IOException {
		key = key.replace(":", "");
		Label label = new Label();
		label.setDictionary(dict);
		label.setContext(ctx);
		label.setKey(key);
		String line = null;

		while (null != (line = br.readLine())) {
			line = line.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(line)) {
				continue;
			}
			line = removeComments(line);

			if (isLineStartWithListElements(line, allLangCodes)) {// a Text
																	// start,
																	// end with
																	// ","

			}

			// maxLength
			// text
			// reference

			if (line.endsWith(";")) {// Label end
				break;
			}
		}

		return label;
	}

	/**
	 * Return true if line start with any element in List
	 * 
	 * @author Guoshun.Wu
	 * @param line
	 * @param langCodes
	 *            alcatel-lucent language codes list
	 * 
	 * */
	private boolean isLineStartWithListElements(String line, List<?> langCodes) {
		for (Object langCode : langCodes) {
			if (line.startsWith((String) langCode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Populate the DictionaryLanguage collection according to the languageCodes
	 * extracted from dct file
	 * 
	 * @author Guoshun.Wu Date: 2012-07-03
	 * @param languageCodes
	 *            language codes extracted from dct file
	 * @param dict
	 *            the dictionary object which represent the dct file
	 * @param langCharset
	 *            mapping of language code and its source charset name
	 * @return DictionaryLanguage collection
	 * */
	public Collection<DictionaryLanguage> getDictLanguages(
			String[] languageCodes, Dictionary dict,
			Map<String, String> langCharset) {
		List<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();

		for (String languageCode : languageCodes) {
			languageCode = languageCode.trim();
			if ("CHK".equals(languageCode)) {// length code
				continue;
			}

			DictionaryLanguage dictLanguage = new DictionaryLanguage();
			dictLanguage.setLanguageCode(languageCode);
			dictLanguage.setDictionary(dict);

			String charsetName = langCharset.get(languageCode);
			if (null == charsetName) {
				// TODO Do a more detailed error handling
				throw BusinessException.CHARSET_NOT_FOUND;
			}
			// query database by charsetName to find find the specific Charset
			// Object
			Charset charset = (Charset) getDao().retrieveOne(
					"from Charset where name = :name",
					JSONObject.fromObject("{'name':'" + charsetName + "'}"));
			if (null == charset) {
				// TODO Do a more detailed error handling
				throw BusinessException.CHARSET_NOT_FOUND;
			}
			dictLanguage.setCharset(charset);

			// language
			// query alcatelLanguageCode table to find the related Language
			AlcatelLanguageCode alCode = (AlcatelLanguageCode) getDao()
					.retrieve(AlcatelLanguageCode.class, languageCode);
			if (null == alCode) {
				// TODO Do a more detailed error handling
				throw BusinessException.LANGUAGE_NOT_FOUND;
			}
			Language language = alCode.getLanguage();
			if (null == language) {
				// TODO Do a more detailed error handling
				throw BusinessException.LANGUAGE_NOT_FOUND;
			}
			dictLanguage.setLanguage(language);
			dictLanguages.add(dictLanguage);
		}
		return dictLanguages;
	}

	/**
	 * Detect the encoding of a File by BOM(byte order mark).
	 * 
	 * @author Guoshun.Wu Date: 2012-07-01
	 * 
	 * @param file
	 *            given File
	 * @return file encoding
	 * */
	public String detectEncoding(File file) throws IOException {
		byte[] utf8BOM = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf, };
		byte[] utf16LEBOM = new byte[] { (byte) 0xff, (byte) 0xfe };
		byte[] utf16BEBOM = new byte[] { (byte) 0xfe, (byte) 0xff };

		byte[] buf = new byte[UTF8_BOM_LENGTH];
		FileInputStream fis = new FileInputStream(file);
		fis.read(buf);
		fis.close();
		if (Arrays.equals(utf8BOM, buf)) {
			return "UTF-8";
		}
		if (Arrays.equals(utf16LEBOM, Arrays.copyOf(buf, UTF16_BOM_LENGTH))
				|| Arrays.equals(utf16BEBOM,
						Arrays.copyOf(buf, UTF16_BOM_LENGTH))) {
			return "UTF-16";
		}
		return "ISO-8859-1";
	}

	public void generateDCT(String filename, Long dctId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {
		// TODO Auto-generated method stub

	}

}
