package com.alcatel_lucent.dms.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

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
		// TODO Auto-generated method stub
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
							&& isLanguageInLangCodes(trans.getLanguage(),
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
							&& isLanguageInLangCodes(trans.getLanguage(),
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
				for (Translation trans : dbLabel.getText().getTranslations()) {
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
					if (null != dbTrans) {
						dbTrans.setTranslation(trans.getTranslation());
					} else {
						getDao().create(trans);
					}
				}
			}

		}
		return dbDict;
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
		List<String> langCodeList = new ArrayList<String>();
		for (AlcatelLanguageCode alCode : getLanguageAlcatelLanguageCode(language)) {
			langCodeList.add(alCode.getCode());
		}
		return langCodeList.removeAll(Arrays.asList(langCodes));
	}
}
