package com.alcatel_lucent.dms.service;

import static org.apache.commons.lang.StringUtils.join;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.DictionaryParser;

public class DictionaryServiceImpl extends BaseServiceImpl implements
		DictionaryService {

	private static Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	public DictionaryServiceImpl() {
		super();
	}

	public Dictionary deliverDCT(String filename, Long appId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {

		Dictionary dict = previewDCT(filename, appId, encoding);
		dict = importDCT(dict, langCodes, langCharset);
		return dict;
	}

	public void generateDCT(String filename, Long dctId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {

		Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
				dctId);
		if (null == dict) {
			log.warn("ID for " + dctId
					+ " Dictionary is not found in database.");
			throw BusinessException.DICTIONARY_NOT_FOUND;
		}

		// all the language code in dictionary
		Collection dictLangCodes = getObjectProperiesList(
				dict.getDictLanguages(), "languageCode");

		if (langCodes != null) {
			List<String> listLangCodes = Arrays.asList(langCodes);
			listLangCodes.removeAll(dictLangCodes);
			if (!listLangCodes.isEmpty()) {
				throw BusinessException.LANGUAGE_NOT_FOUND;
			}
		}

		if (null == encoding) {
			encoding = dict.getEncoding();
		}

		PrintStream out = null;
		try {
			out = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(filename)), true, encoding);
			// output support languages

			out.println("LANGUAGES {" + join(dictLangCodes, ", ") + "}");
			out.println();

			// output labels

			Label label = null;
			int indentSize = "  CHK ".length();

			Label[] labels = dict.getLabels().toArray(new Label[0]);
			for (int i = 0; i < labels.length; ++i) {
				label = labels[i];

				if (i > 0) {
					// output label separator
					out.println(";");
					out.println();
				}
				out.println(label.getKey() + ":");

				out.print("  CHK "
						+ convertContent(
								indentSize,
								generateCHK(label.getMaxLength(),
										label.getReference()), "\n",
								System.getProperty("line.separator")));
				// output translation separator
				out.println(",");

				out.print("  GAE "
						+ convertContent(indentSize, label.getReference(),
								"\n", System.getProperty("line.separator")));
				//

				for (DictionaryLanguage dictLang : dict.getDictLanguages()) {
					if (langCodes != null
							&& Arrays.asList(langCodes).contains(
									dictLang.getLanguageCode())) {
						continue;
					}
					// output translation separator
					out.println(",");

					out.print("  " + dictLang.getLanguageCode() + " ");

					// output langCode translation
					String translationString = label.getReference();

					Language dictLangCodeLanguage = getAlcatelLanguageCodes()
							.get(dictLang.getLanguageCode()).getLanguage();
					for (Translation translation : label.getText()
							.getTranslations()) {
						if (translation.getLanguage().getId() == dictLangCodeLanguage
								.getId()) {
							translationString = translation.getTranslation();
							break;
						}
					}

					String converedString = convertContent(indentSize,
							translationString, "\n",
							System.getProperty("line.separator"));

					String charsetName = langCharset.get(dictLang
							.getLanguageCode());
					if (null == charsetName) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					out.write(converedString.getBytes(charsetName));
				}

			}

		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		} finally {
			if (null != out) {
				out.close();
			}
		}
	}

	private String generateCHK(String maxLength, String reference) {

		StringBuilder sb = new StringBuilder();
		String[] sLineLens = maxLength.split(",");
		String[] refers = reference.split("\n");
		int maxLen = -1;
		for (int i = 0; i < sLineLens.length; ++i) {
			maxLen = Integer.parseInt(sLineLens[i].trim());
			sb.append(refers[i].trim());
			int fill = maxLen - refers[i].length();
			char baseChar = '0';
			while (fill-- > 0) {
				sb.append(baseChar++);
				if (baseChar > '9')
					baseChar = '0';
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private String convertContent(int indentSize, String content,
			String contentLineSeparator, String joinedStringLineSeparator) {
		String[] contents = content.split(contentLineSeparator);
		for (int i = 0; i < contents.length; ++i) {
			contents[i] = "\"" + contents[i] + "\"";
		}
		return join(contents, joinedStringLineSeparator
				+ generateSpace(indentSize));
	}

	public Dictionary previewDCT(String filename, Long appId, String encoding)
			throws BusinessException {
		Application app = (Application) getDao().retrieve(Application.class,
				appId);
		Dictionary dict = null;
		try {
			DictionaryParser dictParser = DictionaryParser
					.getDictionaryParser(this);
			dict = dictParser.parse(app, filename, encoding);
		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		}
		return dict;
	}

	public Dictionary importDCT(Dictionary dict, String[] langCodes,
			Map<String, String> langCharset) {
		if (null == dict)
			return null;
		TextService textService = (TextService) SpringContext.getService(TextService.class);
		Dictionary dbDict = (Dictionary) getDao().retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject(String.format("{'name':'%s'}",
						dict.getName())));
		// first time import
		if (null == dbDict) {
			// create dictionary
			log.info("Dictionary " + dict.getName()
					+ " not exist in database, create new one in database...");
			dbDict = new Dictionary();
			dbDict.setApplication((Application) dao.retrieve(Application.class, dict.getApplication().getId()));
			dbDict.setEncoding(dict.getEncoding());
			dbDict.setFormat(dict.getFormat());
			dbDict.setName(dict.getName());
			dbDict.setPath(dict.getPath());
			dbDict.setLocked(false);
			dbDict = (Dictionary) getDao().create(dbDict);
		}
		
		// update dictionary languages
		for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
			mergeDictLanguage(dbDict, dictLanguage.getLanguage().getId(), dictLanguage.getLanguageCode(), dictLanguage.getCharset().getName());
		}

		for (Label label : dict.getLabels()) {
			// create context if necessary
			Context context = label.getContext();
			Context dbContext = (Context) getDao().retrieveOne(
					"from Context where name=:name",
					JSONObject.fromObject(String.format("{'name':'%s'}",
							context.getName())));
			if (dbContext == null) {
				dbContext = (Context) getDao().create(context);
			}
			
			// create or update text and translations
			Text text = label.getText();
			HashSet<String> langCodeSet = null;
			if (langCodes != null) {
				langCodeSet = new HashSet<String>(Arrays.asList(langCodes));
			}
			Map<Long, String> translationMap = new HashMap<Long, String>();
			for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
				if (langCodeSet != null && !langCodeSet.contains(dictLanguage.getLanguageCode())) {
					continue;
				}
				String charsetName = langCharset.get(dictLanguage.getLanguageCode());
				if (null == charsetName) {
					throw BusinessException.CHARSET_NOT_DEFINED.param(dictLanguage.getLanguageCode());
				}
				Translation trans = text.getTranslation(dictLanguage.getLanguage().getId());
				if (null == trans) {
					continue;
				}
				try {
					String encodedTranslation = new String(trans.getTranslation()
							.getBytes(dict.getEncoding()), charsetName);
					translationMap.put(dictLanguage.getLanguage().getId(), encodedTranslation);
				} catch (UnsupportedEncodingException e) {
					throw BusinessException.CHARSET_NOT_FOUND.param(charsetName);
				}
			}
			Text dbText = textService.addTranslations(dbContext.getId(), label.getReference(), translationMap);

			// create or update label
			Label dbLabel = dbDict.getLabel(label.getKey());
			if (dbLabel == null) {
				label.setDictionary(dbDict);
				label.setContext(dbContext);
				label.setText(dbText);
				dbLabel = (Label) dao.create(label);
			} else {
				dbLabel.setContext(dbContext);
				dbLabel.setText(dbText);
				dbLabel.setKey(label.getKey());
				dbLabel.setDescription(label.getDescription());
				dbLabel.setMaxLength(label.getMaxLength());
				dbLabel.setReference(label.getReference());
			}
		}
		return dbDict;
	}
	public static String generateSpace(int count) {
		if (count < 0) {
			throw new IllegalArgumentException(
					"count must be greater than or equal 0.");
		}
		char[] chs = new char[count];
		for (int i = 0; i < count; i++) {
			chs[i] = ' ';
		}
		return new String(chs);
	}

	private List<String> getLangCodes(
			List<AlcatelLanguageCode> alcatelLanguageCodes) {
		List<String> langCodes = new ArrayList<String>();
		for (AlcatelLanguageCode alCode : alcatelLanguageCodes) {
			langCodes.add(alCode.getCode());
		}
		return langCodes;
	}

	private DictionaryLanguage mergeDictLanguage(Dictionary dbDict, Long languageId, String languageCode, String charsetName) {
		DictionaryLanguage dbDictLang = dbDict.getDictLanguage(languageId);
		if (dbDictLang == null) {
			dbDictLang = new DictionaryLanguage();
			dbDictLang.setDictionary(dbDict);
			dbDictLang.setLanguage((Language) dao.retrieve(Language.class, languageId));
			dbDictLang.setCharset(getCharset(charsetName));
			dbDictLang.setLanguageCode(languageCode);
			dbDictLang = (DictionaryLanguage) dao.create(dbDictLang);
		} else {
			dbDictLang.setLanguage((Language) dao.retrieve(Language.class, languageId));
			dbDictLang.setCharset(getCharset(charsetName));
			dbDictLang.setLanguageCode(languageCode);
		}
		return dbDictLang;
	}
	

	public List getObjectProperiesList(Collection collection,
			String propertyName) {
		List propertiesList = new ArrayList<Object>();
		for (Object obj : collection) {
			Object value = null;
			try {
				value = PropertyUtils.getProperty(obj, propertyName);
			} catch (IllegalAccessException e) {
				throw new SystemError(e.getMessage());
			} catch (InvocationTargetException e) {
				throw new SystemError(e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new SystemError(e.getMessage());
			}
			propertiesList.add(value);
		}
		return propertiesList;
	}
}
