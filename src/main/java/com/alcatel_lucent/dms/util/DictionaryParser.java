/**
 * 
 */
package com.alcatel_lucent.dms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.BaseServiceImpl;
import com.alcatel_lucent.dms.service.DictionaryServiceImpl;

/**
 * @author guoshunw
 * 
 */
public class DictionaryParser {

	public static final String lineSeparator = "\n";
	// System.getProperty("line.separator");

	// Language pattern in dct file
	private static final Pattern patternLanguage = Pattern
			.compile("^LANGUAGES\\s*\\{((?:\\w{3},?\\s*)+)\\}$");

	public static final int UTF8_BOM_LENGTH = 3;
	public static final int UTF16_BOM_LENGTH = 2;

	private BaseServiceImpl baseService;
	private static DictionaryParser dictionaryParser;

	private Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	public static DictionaryParser getDictionaryParser(
			BaseServiceImpl baseService) {
		if (null == dictionaryParser) {
			dictionaryParser = new DictionaryParser(baseService);
		}
		return dictionaryParser;
	}

	public DictionaryParser(BaseServiceImpl baseService) {
		this.baseService = baseService;

	}

	/**
	 * Parse a given dct file and generate a Dictionary Object
	 * 
	 * */

	public Dictionary parse(Application app, String filename, String encoding)
			throws IOException {
		File dctFile = new File(filename);
		if (!dctFile.exists()) {
			throw BusinessException.DCT_FILE_NOT_FOUND;
		}

		log.warn("\n######################begin deliver: " + dctFile.getName()
				+ "##########################\n");

		Dictionary dict = new Dictionary();
		dict.setName(dctFile.getName());
		dict.setFormat("dct");
		dict.setPath(dctFile.getPath());

		if (null == app) {
			throw BusinessException.APPLICATION_NOT_FOUND;
		}
		dict.setApplication(app);

		// create or related context
		Context context = new Context();
		context.setName(dict.getName());
		if (null == encoding) {

			encoding = detectEncoding(dctFile);
		}
		dict.setEncoding(encoding);

		Set<Label> labels = new HashSet<Label>();

		FileInputStream fis = new FileInputStream(dctFile);
		// Creates an BufferedReader that uses the encoding charset.
		BufferedReader dctReader = new BufferedReader(new InputStreamReader(
				fis, encoding));
		String line = null;
		while (null != (line = dctReader.readLine())) {
			line = line.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(line)) {
				continue;
			}
			line = removeComments(line);

			Matcher m = patternLanguage.matcher(line);
			// is LANGUAGES
			if (m.matches()) {
				// languageCodes in current dictionary file
				String[] languageCodes = m.group(1).split(",");
				Collection<DictionaryLanguage> dictLanguages = generateDictLanguages(
						languageCodes, dict, encoding);
				dict.setDictLanguages(dictLanguages);
			} else if (line.endsWith(":")) {// a label start
				Label newLabel=readLabel(dctReader, line, dict, context);
				labels.add(newLabel);
			} else {
				throw BusinessException.INVALID_DCT_FILE;
			}
		}
		dict.setLabels(labels);
		
		fis.close();
		return dict;
	}

	/**
	 * Read a Label from current dct file
	 * 
	 * @author Guoshun.Wu
	 * 
	 * @return Label
	 * @throws BusinessException
	 * @throws IOException
	 * */
	private Label readLabel(BufferedReader dctReader, String line,
			Dictionary dict, Context context) throws BusinessException,
			IOException {

		String key = line.replace(":", "");
		Label label = new Label();
		label.setDictionary(dict);
		label.setContext(context);
		label.setKey(key);

		Map<String, String> entriesInLable = new HashMap<String, String>();

		boolean isLabelEnds = false;
		// read the entries one by one
		while (null != (line = dctReader.readLine()) && !isLabelEnds) {
			line = line.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(line)) {
				continue;
			}
			line = removeComments(line);

			String langCode = null;

			StringBuilder buffer = new StringBuilder();
			// an entry start, end with ","
			if (null != (langCode = isLineStartLangCode(line))) {
				isLabelEnds = false;
				// remove the langCode, blank characters and quotation marks
				line = line.replaceFirst(langCode, "").trim().replace("\"", "");

				// read content
				// only one line
				if (line.endsWith(",") || line.endsWith(";")) {
					buffer.append(line.substring(0, line.length() - 1));
					if (line.endsWith(";")) {
						isLabelEnds = true;
					}
				} else {
					// multiple line
					buffer = new StringBuilder(line);
					while (null != (line = dctReader.readLine())) {
						line = line.trim();
						// ignore the comment line and blank line
						if (isCommentOrBlankLine(line)) {
							continue;
						}
						line = removeComments(line);

						line = line.replace("\"", "");
						if (line.endsWith(",") || line.endsWith(";")) {
							buffer.append(lineSeparator);
							buffer.append(line.substring(0, line.length() - 1));

							if (line.endsWith(";")) {
								isLabelEnds = true;
							}
							break;
						}
						buffer.append(lineSeparator);
						buffer.append(line);
					}

				}
				entriesInLable.put(langCode, buffer.toString());
			}

		}

		// analysis entries for reference, maxLength, text
		String gae = entriesInLable.get("GAE");
		if (null == gae) {
			throw BusinessException.INVALID_DCT_FILE;
		}
		label.setReference(gae);
		Text text = new Text();
		text.setContext(context);
		text.setReference(gae);
		text.setStatus(0);

		Collection<Translation> translations = new HashSet<Translation>();
		Translation trans = null;

		for (Map.Entry<String, String> entry : entriesInLable.entrySet()) {

			if (entry.getKey().equals("CHK") || entry.getKey().equals("GAE")) {
				continue;
			}

			trans = new Translation();
			trans.setText(text);
			AlcatelLanguageCode alCode = (AlcatelLanguageCode) baseService
					.getAlcatelLanguageCodes().get(entry.getKey());
			if (null == alCode) {
				throw BusinessException.LANGUAGE_NOT_FOUND;
			}

			trans.setLanguage(alCode.getLanguage());
			trans.setTranslation(entry.getValue());

			translations.add(trans);
		}
		text.setTranslations(translations);

		label.setText(text);

		String maxLenStr = entriesInLable.get("CHK");
		if (null == maxLenStr) {
			throw BusinessException.INVALID_DCT_FILE;
		}
		String[] maxLenArray = maxLenStr.split(lineSeparator);

		String maxLength = "" + maxLenArray[0].length();
		if (maxLenArray.length > 1) {
			for (int i = 1; i < maxLenArray.length; ++i) {
				maxLength += ", " + maxLenArray[i].length();
			}
		}
		label.setMaxLength(maxLength);

		return label;
	}

	/**
	 * Return langCode if currentLine start with specific LangCode in List, or
	 * return null
	 * 
	 * @author Guoshun.Wu
	 * @param line
	 * @param langCodes
	 *            alcatel-lucent language codes list
	 * 
	 * */
	private String isLineStartLangCode(String line) {
		Set<String> allAlLangCodes = new HashSet(baseService
				.getAlcatelLanguageCodes().keySet());
		allAlLangCodes.add("CHK");

		for (Object langCode : allAlLangCodes) {
			if (line.startsWith((String) langCode)) {
				return (String) langCode;
			}
		}
		return null;
	}

	/**
	 * Populate the DictionaryLanguage collection according to the languageCodes
	 * extracted from dct file
	 * 
	 * @author Guoshun.Wu Date: 2012-07-03
	 * @param languageCodes
	 *            language codes extracted from dct file
	 * @return DictionaryLanguage collection
	 * */
	private Collection<DictionaryLanguage> generateDictLanguages(
			String[] languageCodes, Dictionary dict, String encoding) {
		Set<DictionaryLanguage> dictLanguages = new HashSet<DictionaryLanguage>();

		for (String languageCode : languageCodes) {
			languageCode = languageCode.trim();
			if ("CHK".equals(languageCode)) {// length code
				continue;
			}

			DictionaryLanguage dictLanguage = new DictionaryLanguage();
			dictLanguage.setLanguageCode(languageCode);
			dictLanguage.setDictionary(dict);

			Charset charset = null;
			charset = baseService.getCharsets().get(encoding);
			if (null == charset) {
				// TODO Do a more detailed error handling
				throw BusinessException.CHARSET_NOT_FOUND;
			}
			dictLanguage.setCharset(charset);

			// language
			// query alcatelLanguageCode table to find the related Language
			AlcatelLanguageCode alCode = baseService.getAlcatelLanguageCodes()
					.get(languageCode);

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
	 * Remove the trailing comments on line
	 * 
	 * @author Guoshun.Wu Date: 2012-07-04
	 * @return processed line
	 * */
	private String removeComments(String line) {
		line.trim();
		// remove trailing comments
		Matcher m_line = Pattern.compile("(.*?[^\"\'])--.*").matcher(line);
		if (m_line.matches()) {
			line = m_line.group(1).trim();
		}
		return line;
	}

	private boolean isCommentOrBlankLine(String line) {
		return line.startsWith("--") || line.isEmpty();
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

}
