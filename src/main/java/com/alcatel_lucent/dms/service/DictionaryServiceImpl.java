package com.alcatel_lucent.dms.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

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
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

public class DictionaryServiceImpl extends BaseServiceImpl implements
		DictionaryService {

	private static Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	public static final int UTF8_BOM_LENGTH = 3;
	public static final int UTF16_BOM_LENGTH = 2;

	public static final String lineSeparator = System
			.getProperty("line.separator");

	// current line in file
	private String currentLine = null;
	// current file reader
	private BufferedReader br = null;

	// langCharset mapping of language code and its source charset name
	private Map<String, String> langCharset = null;

	private List<String> allAlcatelLangCodes = null;

	// current dictionary
	private Dictionary dict = null;

	// langCodes Alcatel code of languages to generate, null if all languages
	// should be exported
	
	private String[] langCodes;

	// current file encoding
	private String encoding;

	public DictionaryServiceImpl() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Dictionary deliverDCT(String filename, Long appId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {
		this.langCharset = langCharset;
		this.langCodes = langCodes;
		this.encoding = encoding;
		
		
		File dctFile = new File(filename);
		if (!dctFile.exists()) {
			throw BusinessException.DCT_FILE_NOT_FOUND;
		}

		
		log.warn("\n######################begin deliver: "+filename+"##########################\n");
		
		// get all alcatel language codes

		this.allAlcatelLangCodes = getDao().retrieve(
				"select code from AlcatelLanguageCode");
		// an special check code
		allAlcatelLangCodes.add("CHK");

		dict = new Dictionary();
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
			if (null == this.encoding) {
				this.encoding = detectEncoding(dctFile);
			}
			dict.setEncoding(this.encoding);

			FileInputStream fis = new FileInputStream(dctFile);
			// Creates an BufferedReader that uses the encoding charset.
			br = new BufferedReader(new InputStreamReader(fis, this.encoding));

			// languageCodes in current dictionary file
			String[] languageCodes = null;
			// Language pattern in dct file
			Pattern patternLanguage = Pattern
					.compile("^LANGUAGES\\s*\\{((?:\\w{3},?\\s*)+)\\}$");

			Collection<Label> labels = new HashSet<Label>();

			while (null != (currentLine = br.readLine())) {
				currentLine = currentLine.trim();
				// ignore the comment line and blank line
				if (isCommentOrBlankLine(currentLine)) {
					continue;
				}
				currentLine = removeComments(currentLine);

				Matcher m = patternLanguage.matcher(currentLine);
				// is LANGUAGES
				if (m.matches()) {
					languageCodes = m.group(1).split(",");
					dict.setDictLanguages(getDictLanguages(languageCodes));
				} else if (currentLine.endsWith(":")) {// a label start
					labels.add(readLabel(ctx));
				} else {
					throw BusinessException.INVALID_DCT_FILE;
				}

			}

			dict.setLabels(labels);
			br.close();
		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		}

		// TODO update or insert dictionary object to DB
		getDao().create(dict);
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
	 * Read a Label from current dct file
	 * 
	 * @author Guoshun.Wu
	 * 
	 * @return Label
	 * @throws BusinessException
	 * @throws IOException
	 * */
	private Label readLabel(Context ctx) throws BusinessException, IOException {
		String key = currentLine.replace(":", "");
		Label label = new Label();
		label.setDictionary(dict);
		label.setContext(ctx);
		label.setKey(key);

		Map<String, String> entriesInLable = new HashMap<String, String>();

		// read the entries one by one
		while (null != (currentLine = br.readLine())) {
			currentLine = currentLine.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(currentLine)) {
				continue;
			}
			currentLine = removeComments(currentLine);

			String langCode = null;
			String content = null;
			// an entry start, end with ","
			if (null != (langCode = isLineStartLangCode(allAlcatelLangCodes))) {
				// remove the langCode, blank characters and quotation marks
				currentLine = currentLine.replaceFirst(langCode, "").trim()
						.replace("\"", "");
				content = readContent();

				String charSetName = langCharset.get(langCode);
				if (null == charSetName) {
					throw BusinessException.CHARSET_NOT_FOUND;
				}
				entriesInLable
						.put(langCode,
								new String(content.getBytes(this.encoding),
										charSetName));
			}

			if (null == currentLine || currentLine.endsWith(";")) {// Label end
				break;
			}
		}

		// analysis entries for reference, maxLength, text
		String gae = entriesInLable.get("GAE");
		if (null == gae) {
			throw BusinessException.INVALID_DCT_FILE;
		}
		label.setReference(gae);
		Text text = new Text();
		text.setContext(ctx);
		text.setReference(gae);
		text.setStatus(0);

		Collection<Translation> translations = new HashSet<Translation>();
		Translation trans = null;

		for (Map.Entry<String, String> entry : entriesInLable.entrySet()) {

			if (entry.getKey().equals("CHK")) {
				continue;
			}

			trans = new Translation();
			trans.setText(text);
			AlcatelLanguageCode alCode = (AlcatelLanguageCode) getDao()
					.retrieve(AlcatelLanguageCode.class, entry.getKey());
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
	 * Read entry content from current dictionary file
	 * 
	 * @author Guoshun.Wu
	 * @throws IOException
	 * 
	 * */
	private String readContent() throws IOException {

		// only one line
		if (currentLine.endsWith(",") || currentLine.endsWith(";")) {
			return currentLine.substring(0, currentLine.length() - 1);
		}

		// multiple line
		StringBuilder buffer = new StringBuilder(currentLine);

		while (null != (currentLine = br.readLine())) {
			currentLine = currentLine.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(currentLine)) {
				continue;
			}
			currentLine = removeComments(currentLine);

			currentLine = currentLine.replace("\"", "");
			if (currentLine.endsWith(",") || currentLine.endsWith(";")) {

				buffer.append(lineSeparator);
				buffer.append(currentLine.substring(0, currentLine.length() - 1));
				return buffer.toString();
			}

			buffer.append(lineSeparator);
			buffer.append(currentLine);
		}
		return buffer.toString();
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
	private String isLineStartLangCode(List<String> langCodes) {
		for (Object langCode : langCodes) {
			if (currentLine.startsWith((String) langCode)) {
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
	 * @param langCharset
	 *            mapping of language code and its source charset name
	 * @return DictionaryLanguage collection
	 * */
	private Collection<DictionaryLanguage> getDictLanguages(
			String[] languageCodes) {
		Collection<DictionaryLanguage> dictLanguages = new HashSet<DictionaryLanguage>();

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
