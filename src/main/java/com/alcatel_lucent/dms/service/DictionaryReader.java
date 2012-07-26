/**
 *
 */
package com.alcatel_lucent.dms.service;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.ISOLanguageCode;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

/**
 * @author Guoshun.Wu
 */
public class DictionaryReader extends LineNumberReader {

	private Dictionary dictionary;
	private Context context;

	private Collection<BusinessWarning> warnings;
	private LanguageService languageService;

	// Language pattern in dct file
	private static final Pattern patternLanguage = Pattern
			.compile("^LANGUAGES\\s*\\{((?:[\\w-]{2,5}\\s*,?\\s*)+)\\}$");
	private static final Pattern patternLabelLanguageCode = Pattern
			.compile("^([\\w-]{2,5})\\s*[\\w\\W]*$");

	private Logger log = Logger.getLogger(DictionaryReader.class);
	private String lastLine = ";";
	private String currentLine;

	protected DictionaryReader(Reader in, Dictionary dictionary) {
		super(in);
		this.dictionary = dictionary;
		this.context = new Context();
		context.setName(dictionary.getName());
		this.warnings = new ArrayList<BusinessWarning>();
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public Collection<BusinessWarning> getWarnnings() {
		return warnings;
	}

	public Collection<DictionaryLanguage> readLanguages() throws IOException {

		String line = readLine();
		Matcher m = patternLanguage.matcher(line);
		if (!m.matches()) {
			log.error("Parser was broken on line: " + line);
			throw new BusinessException(BusinessException.INVALID_DCT_FILE,
					getLineNumber(), dictionary.getName());
		}
		String[] languageCodes = m.group(1).split("\\s*,\\s*");

		Collection<DictionaryLanguage> dictLangs = new ArrayList<DictionaryLanguage>();
		DictionaryLanguage dl = null;

		for (String languageCode : languageCodes) {
			if ("CHK".equals(languageCode))
				continue;

			dl = new DictionaryLanguage();

			dl.setDictionary(dictionary);
			dl.setLanguageCode(languageCode);
			dl.setLanguage(getLanguage(languageCode));

			Charset charset = null;
			// DictionaryLanguage CharSet is the dictionary encoding
			charset = languageService.getCharsets().get(
					dictionary.getEncoding());
			if (null == charset) {
				throw new BusinessException(
						BusinessException.CHARSET_NOT_FOUND,
						dictionary.getEncoding());
			}
			dl.setCharset(charset);

			dictLangs.add(dl);
		}

		return dictLangs;

	}

	private Language getLanguage(String languageCode) {
		// language
		// query alcatelLanguageCode table to find the related Language
		ISOLanguageCode isoCode = null;
		AlcatelLanguageCode alCode = languageService
				.getAlcatelLanguageCode(languageCode);
		if (null == alCode) {
			isoCode = languageService.getISOLanguageCode(languageCode.replace(
					'_', '-'));
			if (null == isoCode) {
				throw new BusinessException(
						BusinessException.UNKNOWN_LANG_CODE, languageCode);
			}
			return isoCode.getLanguage();
		}
		return alCode.getLanguage();
	}

	public Dictionary readDictionary() throws IOException {

		Collection<DictionaryLanguage> dictLanguages = null;

		BusinessException nonBreakExceptions = new BusinessException(
				BusinessException.NESTED_DCT_PARSE_ERROR, dictionary.getName());

		// first read Languages
		try {
			dictLanguages = readLanguages();
		} catch (BusinessException e) {
			nonBreakExceptions.addNestedException(e);
		}

		dictionary.setDictLanguages(dictLanguages);

		// readLabels
		Label label = null;
		Collection<Label> labels = new HashSet<Label>();
		String labelLine = readLine();
		if (null == labelLine) {
			dictionary.setLabels(labels);
			dictionary.setDictLanguages(new HashSet<DictionaryLanguage>());
			return dictionary;
		}
		StringBuilder labKeyLine = new StringBuilder(labelLine);
		HashSet<String> labelKeys = new HashSet<String>();

		while (!labKeyLine.toString().equals("null")) {
			try {
				label = readLabel(labKeyLine);
			} catch (BusinessException e) {
				nonBreakExceptions.addNestedException(e);
			}
			if (null == label)
				break;
			if (labelKeys.contains(label.getKey())) {
				warnings.add(new BusinessWarning(
						BusinessWarning.DUPLICATE_LABEL_KEY, getLineNumber(),
						label.getKey()));
			} else {
				labelKeys.add(label.getKey());
				labels.add(label);
			}
		}

		if (nonBreakExceptions.hasNestedException()) {
			throw nonBreakExceptions;
		}
		dictionary.setLabels(labels);
		return dictionary;
	}

	public Label readLabel(StringBuilder labelKey) throws IOException,
			BusinessException {
		if (!lastLine.endsWith(";") && !lastLine.endsWith("}")) {
			warnings.add(new BusinessWarning(BusinessWarning.UNCLOSED_LABEL,
					getLineNumber(), labelKey.toString()));
		}
		if (!labelKey.toString().endsWith(":")) {
			throw new BusinessException(BusinessException.INVALID_DCT_FILE,
					getLineNumber(), dictionary.getPath());
		}

		/*
		 * End sign is next Label begin, which is the line end with colon
		 */
		String key = labelKey.substring(0, labelKey.length() - 1);

		Label label = new Label();
		label.setKey(key);
		label.setDictionary(dictionary);
		label.setContext(context);
		label.setDescription(null);

		Map<String, String> entriesInLabel = new HashMap<String, String>();

		BusinessException exceptions = new BusinessException(
				BusinessException.NESTED_LABEL_ERROR, key);

		Set<String> dictLangCodes = dictionary.getAllLanguageCodes();
		dictLangCodes.add("CHK");

		String line = null;
		String langCode = null;
		// read until file end or next label start
		while ((null != line || null != (line = readLine()))
				&& !isLabelKeyLine(line)) {
			// get an entry, current line should be an entry start

			langCode = isLabelEntryStart(line);

			if (!lastLine.endsWith(",") && !lastLine.endsWith(":")) {
				warnings.add(new BusinessWarning(
						BusinessWarning.UNCLOSED_LABEL_ENTRY, getLineNumber(),
						langCode, key));
			}

			if (null == langCode) {
				exceptions.addNestedException(new BusinessException(
						BusinessException.NESTED_LABEL_ERROR, getLineNumber(),
						key));
			}

			if (!dictLangCodes.contains(langCode)) {
				exceptions.addNestedException(new BusinessException(
						BusinessException.UNDEFINED_LANG_CODE, getLineNumber(),
						langCode, key));
			}

			if (!isValidLangCode(langCode)) {
				exceptions.addNestedException(new BusinessException(
						BusinessException.LANGUAGE_NOT_FOUND, getLineNumber(),
						langCode));
			}

			if (entriesInLabel.containsKey(langCode)) {
				warnings.add(new BusinessWarning(
						BusinessWarning.DUPLICATE_LANG_CODE, getLineNumber(),
						langCode));
			}

			if (isUnclosedQuota(line)) {
				warnings.add(new BusinessWarning(
						BusinessWarning.UNCLOSED_QUOTA, getLineNumber(),
						langCode, key));
			}

			// read entry content
			StringBuilder buffer = new StringBuilder();

			// remove the langCode, blank characters and quotation marks
			line = line.replaceFirst(langCode, "").trim().replace("\"", "");
			if (line.endsWith(",") || line.endsWith(";")) {
				line = line.substring(0, line.length() - 1);
			}
			buffer.append(line);

			// read until file end or next label start or next entry start
			while (null != (line = readLine())
					&& (null == isLabelEntryStart(line))
					&& !isLabelKeyLine(line)) {

				if (isUnclosedQuota(line)) {
					warnings.add(new BusinessWarning(
							BusinessWarning.UNCLOSED_QUOTA, getLineNumber(),
							langCode, key));
				}

				if (line.endsWith(",") || line.endsWith(";")) {
					line = line.replace("\"", "");
					buffer.append("\n");
					buffer.append(line.substring(0, line.length() - 1));
				} else {
					line = line.replace("\"", "");
					buffer.append("\n");
					buffer.append(line);
				}
			}
			entriesInLabel.put(langCode, buffer.toString());
		}

		// analysis entries for reference, maxLength, text
		String gae = entriesInLabel.get("GAE");
		if (null == gae) {
			exceptions.addNestedException(new BusinessException(
					BusinessException.NO_REFERENCE_TEXT, getLineNumber(), label
							.getKey()));
		}
		label.setReference(gae);
		Text text = new Text();
		text.setContext(context);
		text.setReference(gae);
		text.setStatus(0);

		Collection<Translation> translations = new HashSet<Translation>();
		Translation trans = null;

		for (Map.Entry<String, String> entry : entriesInLabel.entrySet()) {

			if (entry.getKey().equals("CHK") || entry.getKey().equals("GAE")) {
				continue;
			}

			trans = new Translation();
			trans.setText(text);

			Language language = this.getLanguage(entry.getKey());
			trans.setLanguage(language);
			trans.setTranslation(entry.getValue());

			translations.add(trans);
		}
		text.setTranslations(translations);

		label.setText(text);

		String maxLenStr = entriesInLabel.get("CHK");
		if (null != maxLenStr) {
			String[] maxLenArray = maxLenStr.split("\n");

			String maxLength = "" + maxLenArray[0].length();
			for (int i = 1; i < maxLenArray.length; ++i) {
				maxLength += "," + maxLenArray[i].length();
			}
			label.setMaxLength(maxLength);
		}

		labelKey.delete(0, labelKey.length());
		labelKey.append(line);

		if (exceptions.hasNestedException()) {
			throw exceptions;
		}

		return label;
	}

	private boolean isLabelKeyLine(String line) {
		return line.endsWith(":");
	}

	@Override
	public String readLine() throws IOException {
		String line = null;
		// skip comment and blank line
		while (null != (line = super.readLine())) {
			line = line.trim();
			if (isCommentOrBlankLine(line)) {
				log.debug(String.format(
						"[line: %d]In file %s is comment or blank line, skip.",
						getLineNumber(), dictionary.getPath()));
				continue;
			}
			break;
		}
		if (null == line)
			return null;
		line = removeComments(line);
		lastLine = currentLine;
		currentLine = line;
		return line;
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
		StringBuilder sb = new StringBuilder();
		StringReader sr = new StringReader(line);
		int ch = -1;
		int quotNum = 0;
		while (-1 != (ch = sr.read())) {
			if (ch == '-') {
				int nextch = (char) sr.read();
				if (('-' == nextch && 0 == quotNum % 2) || -1 == nextch) {
					break;
				}
				if ('"' == nextch) {
					quotNum++;
				}
				sb.append((char) ch);
				sb.append((char) nextch);
			} else {
				if (ch == '"') {
					quotNum++;
				}
				sb.append((char) ch);
			}
		}
		sr.close();
		return sb.toString().trim();
	}

	private boolean isCommentOrBlankLine(String line) {
		return line.startsWith("--") || line.isEmpty()
				|| line.charAt(0) == '\uFEFF';
	}

	/**
	 * Test if a langCode is valid(exist in database)
	 * 
	 * @param langCode
	 * @author Guoshun.Wu
	 */
	private boolean isValidLangCode(String langCode) {
		Set<String> allLangCodes = new HashSet<String>(languageService
				.getAlcatelLanguageCodes().keySet());
		allLangCodes.addAll(languageService.getISOLanguageCodes().keySet());
		allLangCodes.add("CHK");
		langCode = langCode.replace('_', '-');
		return allLangCodes.contains(langCode);
	}

	/**
	 * Test if a line is a entry start line in an label the language code will
	 * be returned or null if this is is not an entry start line
	 * 
	 * @param line
	 */
	private String isLabelEntryStart(String line) {
		Matcher m = patternLabelLanguageCode.matcher(line);
		if (!m.matches())
			return null;
		return m.group(1);
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
}
