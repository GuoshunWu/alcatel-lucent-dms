/**
 * 
 */
package com.alcatel_lucent.dms.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.ISOLanguageCode;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LanguageCode;
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
			.compile("^LANGUAGES\\s*\\{((?:[\\w-]{2,5},?\\s*)+)\\}$");

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

	public Dictionary parse(Application app, String dictionaryName,
			String path, InputStream dctInputStream, String encoding,
			Collection<BusinessWarning> warnings) throws IOException {

		Dictionary dict = new Dictionary();
		dict.setName(dictionaryName);
		dict.setFormat("dct");
		dict.setPath(path);

		dict.setApplication(app);
		dict.setEncoding(encoding);

		Set<Label> labels = new HashSet<Label>();

		// create or related context
		Context context = new Context();
		context.setName(dict.getName());

		BusinessException nonBreakExceptions = new BusinessException(
				BusinessException.NESTED_DCT_PARSE_ERROR, dict.getName());

		try {
			// Creates an BufferedReader that uses the encoding charset.
			if (null == encoding) {
				throw new NullPointerException("Encoding is null.");
			}
			if (dctInputStream instanceof FileInputStream) {
				FileInputStream fdis = (FileInputStream) dctInputStream;
				FileChannel channel = fdis.getChannel();
				long fileSize = channel.size();
				MappedByteBuffer mbf = channel.map(
						FileChannel.MapMode.READ_ONLY, 0, fileSize);
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
			BufferedReader dctReader = new BufferedReader(
					new InputStreamReader(dctInputStream, encoding));

			String line = null;
			HashSet<String> declaredLangCodes = new HashSet<String>();
			HashSet<String> labelKeys = new HashSet<String>();
			log.debug("Processing DCT file " + dictionaryName);

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
					for (int i = 0; i < languageCodes.length; i++) {
						declaredLangCodes.add(languageCodes[i].trim());
					}
					try {
						Collection<DictionaryLanguage> dictLanguages = generateDictLanguages(
								languageCodes, dict, encoding);
						dict.setDictLanguages(dictLanguages);
					} catch (BusinessException e) {
						nonBreakExceptions.addNestedException(e);
					}
				} else if (line.endsWith(":")) {// a label start
					log.debug("Processing label " + line);
					try {
						Label newLabel = readLabel(dctReader, line, dict,
								context, declaredLangCodes, warnings);
						if (labelKeys.contains(newLabel.getKey())) {
							nonBreakExceptions
									.addNestedException(new BusinessException(
											BusinessException.DUPLICATE_LABEL_KEY,
											newLabel.getKey()));
						} else {
							labelKeys.add(newLabel.getKey());
							labels.add(newLabel);
						}
					} catch (BusinessException e) {
						nonBreakExceptions.addNestedException(e);
					}
				} else {
					log.error("Parser was broken on line: " + line);
					throw new BusinessException(
							BusinessException.INVALID_DCT_FILE, dict.getName());
				}
			}
			dict.setLabels(labels);

			if (nonBreakExceptions.hasNestedException()) {
				throw nonBreakExceptions;
			}
		} finally {
			if (dctInputStream != null)
				dctInputStream.close();
		}

		return dict;
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
			Dictionary dict, Context context, HashSet<String> languageCodes,
			Collection<BusinessWarning> warnings) throws BusinessException,
			IOException {
		String key = line.replace(":", "");
		Label label = new Label();
		label.setDictionary(dict);
		label.setContext(context);
		label.setKey(key);

		BusinessException exceptions = new BusinessException(
				BusinessException.NESTED_LABEL_ERROR, key);

		Map<String, String> entriesInLable = new HashMap<String, String>();

		boolean isLabelEnds = false;
		// read the entries one by one
		while (!isLabelEnds && null != (line = dctReader.readLine())) {
			line = line.trim();
			// ignore the comment line and blank line
			if (isCommentOrBlankLine(line)) {
				continue;
			}
			line = removeComments(line);

			String langCode = null;

			StringBuilder buffer = new StringBuilder();
			// an entry start, end with ","
			if (null != (langCode = isLineStartWithLangCode(line))) {
				if (!languageCodes.contains(langCode)) {
					exceptions.addNestedException(new BusinessException(
							BusinessException.UNDEFINED_LANG_CODE, langCode));
				}
				if (entriesInLable.containsKey(langCode)) {
					exceptions.addNestedException(new BusinessException(
							BusinessException.DUPLICATE_LANG_CODE, langCode));
				}
				isLabelEnds = false;

				if (isUnclosedQuota(line)) {
					warnings.add(new BusinessWarning(
							BusinessWarning.UNCLOSED_QUOTA, langCode, key));
				}
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

						if (isUnclosedQuota(line)) {
							warnings.add(new BusinessWarning(
									BusinessWarning.UNCLOSED_QUOTA, langCode,
									key));
						}
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
			exceptions.addNestedException(new BusinessException(
					BusinessException.NO_REFERENCE_TEXT, label.getKey()));
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

			LanguageCode lCode = languageCodeCheck(entry.getKey());
			trans.setLanguage(lCode.getLanguage());
			trans.setTranslation(entry.getValue());

			translations.add(trans);
		}
		text.setTranslations(translations);

		label.setText(text);

		String maxLenStr = entriesInLable.get("CHK");
		if (null != maxLenStr) {
			String[] maxLenArray = maxLenStr.split(lineSeparator);

			String maxLength = "" + maxLenArray[0].length();
			for (int i = 1; i < maxLenArray.length; ++i) {
				maxLength += "," + maxLenArray[i].length();
			}
			label.setMaxLength(maxLength);
		}

		if (exceptions.hasNestedException()) {
			throw exceptions;
		}

		return label;
	}

	/**
	 * Check if text has ending quota
	 * 
	 * @param line
	 * @return
	 */
	private boolean isUnclosedQuota(String line) {
		if (line.endsWith(",") || line.endsWith(";")) {
			line = line.substring(0, line.length() - 1).trim();
		}
		return !line.endsWith("\"");
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
	private String isLineStartWithLangCode(String line) {
		Set<String> allLangCodes = new HashSet<String>(baseService
				.getAlcatelLanguageCodes().keySet());
		allLangCodes.addAll(baseService.getISOLanguageCodes().keySet());
		allLangCodes.add("CHK");

		for (String langCode : allLangCodes) {
			if (line.startsWith(langCode + " ")) {
				return langCode;
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
				throw new BusinessException(
						BusinessException.CHARSET_NOT_FOUND, encoding);
			}
			dictLanguage.setLanguage(languageCodeCheck(languageCode)
					.getLanguage());

			dictLanguage.setCharset(charset);
			dictLanguages.add(dictLanguage);
		}
		return dictLanguages;
	}

	private LanguageCode languageCodeCheck(String languageCode) {
		// language
		// query alcatelLanguageCode table to find the related Language
		ISOLanguageCode isoCode = null;
		AlcatelLanguageCode alCode = baseService
				.getAlcatelLanguageCode(languageCode);
		if (null == alCode) {
			isoCode = baseService.getISOLanguageCode(languageCode.replace('_',
					'-'));
			if (null == isoCode) {
				throw new BusinessException(
						BusinessException.UNKNOWN_LANG_CODE, languageCode);
			}
			return isoCode;
		}
		return alCode;
	}

	/**
	 * Remove the trailing comments on line
	 * 
	 * @author Guoshun.Wu Date: 2012-07-04
	 * @return processed line
	 * @throws IOException
	 * */
	private String removeComments(String line) throws IOException {
		line.trim();
		int quotIndex = line.lastIndexOf("\"");
		if (-1 == quotIndex) {
			quotIndex = 0;
		}
		int commentIndex = line.indexOf("--", quotIndex);
		if (-1 != commentIndex) {
			line = line.substring(0, commentIndex);
		}
		return line.trim();
	}

	private boolean isCommentOrBlankLine(String line) {
		return line.startsWith("--") || line.isEmpty();
	}

}
