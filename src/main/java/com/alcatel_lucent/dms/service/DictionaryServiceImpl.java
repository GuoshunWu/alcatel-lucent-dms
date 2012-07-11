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
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.alcatel_lucent.dms.BusinessException;
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
		if (null == encoding) {
			encoding = dict.getEncoding();
		}

		PrintStream out = null;
		try {
			out = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(filename)), true, encoding);
			// output support languages

			Collection dictLangCodes = getObjectProperiesList(
					dict.getDictLanguages(), "languageCode");
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
				
				Translation translation = null;
				Translation[] translations = label.getText().getTranslations()
						.toArray(new Translation[0]);

				for (int j = 0; j < translations.length; ++j) {
					translation = translations[j];
					if (langCodes != null
							&& !isLanguageInLangCodes(
									translation.getLanguage(), langCodes)) {
						continue;
					}
					// output translation separator
					out.println(",");

					List<String> langCodeList = getLangCodes(getLanguageAlcatelLanguageCode(translation
							.getLanguage()));
					String langCode = langCodeList.get(0);
					out.print("  " + langCode + " ");

					String charsetName = langCharset.get(langCode);
					if (null == charsetName) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					String converedString = convertContent(indentSize,
							translation.getTranslation(), "\n",
							System.getProperty("line.separator"));
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

	@Override
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

	@Override
	public Dictionary importDCT(Dictionary dict, String[] langCodes,
			Map<String, String> langCharset) {
		if (null == dict)
			return null;
		Dictionary dbDict = (Dictionary) getDao().retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject(String.format("{'name':'%s'}",
						dict.getName())));
		// first time import
		if (null == dbDict) {
			log.info("Dictionary " + dict.getName()
					+ " not exist in database, create new one in database...");
			dbDict = (Dictionary) getDao().create(dict);

			for (DictionaryLanguage dictLanguage : dbDict.getDictLanguages()) {
				getDao().create(dictLanguage);
			}

			for (Label label : dbDict.getLabels()) {
				Context context = label.getContext();
				Context dbContext = (Context) getDao().retrieveOne(
						"from Context where name=:name",
						JSONObject.fromObject(String.format("{'name':'%s'}",
								context.getName())));
				if (dbContext == null) {
					dbContext = (Context) getDao().create(context);
				}
				label.setContext(dbContext);

				Text text = label.getText();

				Map params = new HashMap();
				params.put("reference", text.getReference());
				params.put("contextid", dbContext.getId());

				Text dbText = (Text) getDao()
						.retrieveOne(
								"from Text where reference= :reference and context.id=:contextid",
								params);
				if (null == dbText) {
					dbText = (Text) getDao().create(text);
				}
				label.setText(dbText);
				getDao().create(label);

				for (Translation trans : text.getTranslations()) {
					if (langCodes != null
							&& !isLanguageInLangCodes(trans.getLanguage(),
									langCodes)) {
						continue;
					}

					List<AlcatelLanguageCode> alCodes = getLanguageAlcatelLanguageCode(trans
							.getLanguage());
					if (alCodes.isEmpty()) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					String encodedTranslation = null;

					String charsetName = null;
					for (AlcatelLanguageCode alCode : alCodes) {
						charsetName = langCharset.get(alCode.getCode());
						if (null != charsetName) {
							break;
						}
					}
					if (null == charsetName) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					try {
						encodedTranslation = new String(trans.getTranslation()
								.getBytes(dict.getEncoding()), charsetName);
					} catch (UnsupportedEncodingException e) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					trans.setTranslation(encodedTranslation);
					getDao().create(trans);
				}
			}
			return dbDict;
		}

		log.info("Dictionary " + dict.getName()
				+ " exist in database, merge it in database...");
		// merged memory dict to dbDict

		/*
		 * 具有相同字典，且具有相同languageCode可认为是同一DictionaryLanguage对象
		 * 如此需要重写DictionaryLanguage, Dictionary 等Entity的equals方法
		 */
		for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
			DictionaryLanguage dbDictLang = (DictionaryLanguage) getDao()
					.retrieveOne(
							"from DictionaryLanguage where dictionary.name=:dictName and languageCode=:languageCode",
							JSONObject.fromObject(String.format(
									"{'dictName':'%s','languageCode':'%s'}",
									dictLanguage.getDictionary().getName(),
									dictLanguage.getLanguageCode())));
			if (dbDictLang == null) {
				log.info("added " + dictLanguage.getLanguageCode()
						+ " to DictionaryLanguage");
				dictLanguage.setDictionary(dbDict);
				dictLanguage = (DictionaryLanguage) getDao().create(
						dictLanguage);
			}
		}

		/*
		 * 具有相同字典，相同key的Label可认为是同一Label对象
		 */
		// dict.getLabels().removeAll(dbDict.getLabels());
		Context dbContext = dbDict.getLabels().toArray(new Label[0])[0]
				.getContext();
		for (Label label : dict.getLabels()) {
			Label dbLabel = (Label) getDao()
					.retrieveOne(
							"from Label where dictionary.name=:dictName and key =:key",
							JSONObject.fromObject(String.format(
									"{'dictName':'%s','key':'%s'}", label
											.getDictionary().getName(), label
											.getKey())));
			if (null == dbLabel) {
				label.setContext(dbContext);
				Text text = label.getText();

				Map params = new HashMap();
				params.put("reference", text.getReference());
				params.put("contextid", dbContext.getId());

				Text dbText = (Text) getDao()
						.retrieveOne(
								"from Text where reference= :reference and context.id=:contextid",
								params);
				if (null == dbText) {
					dbText = (Text) getDao().create(text);
				}
				label.setText(dbText);
				getDao().create(label);

				for (Translation trans : text.getTranslations()) {
					if (langCodes != null
							&& !isLanguageInLangCodes(trans.getLanguage(),
									langCodes)) {
						continue;
					}

					List<AlcatelLanguageCode> alCodes = getLanguageAlcatelLanguageCode(trans
							.getLanguage());
					if (alCodes.isEmpty()) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					String encodedTranslation = null;

					String charsetName = null;
					for (AlcatelLanguageCode alCode : alCodes) {
						charsetName = langCharset.get(alCode.getCode());
						if (null != charsetName) {
							break;
						}
					}
					if (null == charsetName) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					try {
						encodedTranslation = new String(trans.getTranslation()
								.getBytes(dict.getEncoding()), charsetName);
					} catch (UnsupportedEncodingException e) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					trans.setTranslation(encodedTranslation);
					getDao().create(trans);
				}

			} else {
				/*
				 * 若Label在本Dictionary中存在，则只需要添加或者更新每个Translation 词条
				 */

				for (Translation trans : label.getText().getTranslations()) {
					if (langCodes != null
							&& !isLanguageInLangCodes(trans.getLanguage(),
									langCodes)) {
						continue;
					}
					/*
					 * 若此Translation词条在数据库本Label中存在则用内存对象更新数据库对象，否则 将内存对象插入到数据库
					 */

					Map<String, Object> params = new HashMap<String, Object>();
					params.put("langid", trans.getLanguage().getId());
					params.put("textref", trans.getText().getReference());

					Translation dbTrans = (Translation) getDao()
							.retrieveOne(
									"from Translation where language.id=:langid and text.reference=:textref",
									params);

					List<AlcatelLanguageCode> alCodes = getLanguageAlcatelLanguageCode(trans
							.getLanguage());
					if (alCodes.isEmpty()) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}
					String encodedTranslation = null;

					String charsetName = null;
					for (AlcatelLanguageCode alCode : alCodes) {
						charsetName = langCharset.get(alCode.getCode());
						if (null != charsetName) {
							break;
						}
					}
					if (null == charsetName) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}

					try {
						encodedTranslation = new String(trans.getTranslation()
								.getBytes(dict.getEncoding()), charsetName);
					} catch (UnsupportedEncodingException e) {
						throw BusinessException.CHARSET_NOT_FOUND;
					}

					if (null != dbTrans) {
						dbTrans.setTranslation(encodedTranslation);
					} else {
						trans.setText(dbLabel.getText());
						trans.setTranslation(encodedTranslation);
						getDao().create(trans);
					}
				}
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

	private List<AlcatelLanguageCode> getLanguageAlcatelLanguageCode(
			Language language) {
		List<AlcatelLanguageCode> alCodes = new ArrayList<AlcatelLanguageCode>();
		for (AlcatelLanguageCode alCode : this.getAlcatelLanguageCodes()
				.values()) {
			if (alCode.getLanguage().getId() == language.getId()) {
				alCodes.add(alCode);
			}
		}
		return alCodes;
	}

	/**
	 * 
	 * */
	private boolean isLanguageInLangCodes(Language language, String[] langCodes) {
		List<String> langCodeList = getLangCodes(getLanguageAlcatelLanguageCode(language));
		return langCodeList.removeAll(Arrays.asList(langCodes));
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
