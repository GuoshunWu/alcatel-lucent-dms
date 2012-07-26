package com.alcatel_lucent.dms.service;

import static com.alcatel_lucent.dms.util.Util.generateSpace;
import static com.alcatel_lucent.dms.util.Util.getObjectProperiesList;
import static org.apache.commons.lang.StringUtils.join;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.Util;

@Service("dictionaryService")
@Scope("singleton")
public class DictionaryServiceImpl extends BaseServiceImpl implements
		DictionaryService {

	private static Logger log = Logger.getLogger(DictionaryServiceImpl.class);

	public static Logger logDictDeliverSuccess = Logger
			.getLogger("DictDeliverSuccess");
	public static Logger logDictDeliverFail = Logger
			.getLogger("DictDeliverFail");

	public static Logger DictDeliverWarning = Logger
			.getLogger("DictDeliverWaning");

	@Autowired
	private TextService textService;

	@Autowired
	private DictionaryParser dictionaryParser;

	@Autowired
	private LanguageService langService;

	@Autowired
	private DictionaryProp dictProp;

	public DictionaryServiceImpl() {
		super();
	}

	public int deleteDCT(String dctName) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", dctName);

		List<Dictionary> dicts = dao.retrieve(
				"from Dictionary where name=:name", params);
		int result = 0;
		for (Dictionary dict : dicts) {
			dao.delete(dict);
			result++;
		}
		return result;
	}

	public Dictionary deliverDCT(String dictionaryName, String path,
			InputStream dctInputStream, Long appId, String encoding,
			String[] langCodes, Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException,
			IOException {

		long before = System.currentTimeMillis();
		Dictionary dict = previewDCT(dictionaryName, path, dctInputStream,
				appId, encoding, warnings);
		long after = System.currentTimeMillis();
		log.info("**************previewDCT take " + (after - before)
				+ " milliseconds of time.************");

		log.info("Dictionary " + dict.getName()
				+ " is about to import to database");

		before = System.currentTimeMillis();
		dict = importDCT(dict, langCodes, langCharset, warnings);
		after = System.currentTimeMillis();
		log.info("************importDCT take " + (after - before)
				+ " milliseconds of time.**************");

		return dict;
	}

	public Dictionary deliverDCT(String dictionaryName, String filename,
			Long appId, String encoding, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException {
		InputStream is;
		try {
			is = new FileInputStream(filename);
			if (null == encoding) {
				byte[] bom = new byte[Util.UTF8_BOM_LENGTH];
				is.read(bom);
				encoding = Util.detectEncoding(bom);
				is.close();
				is = new FileInputStream(filename);
			}
			return deliverDCT(dictionaryName, filename, is, appId, encoding,
					langCodes, langCharset, warnings);
		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		}

	}

	/**
	 * Deliver dct files in a directory After using dictionary properties, now
	 * encoding and langCharset parameter are useless.
	 * 
	 * @param rootDir
	 * @param file
	 * 
	 * */
	public Collection<Dictionary> deliverDCTFiles(String rootDir, File file,
			Long appId, String encoding, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException {

		if (!file.exists())
			return null;

		Collection<Dictionary> deliveredDicts = new ArrayList<Dictionary>();

		if (file.isDirectory()) {
			File[] dctFileOrDirs = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory() || Util.isDCTFile(pathname)
							|| Util.isZipFile(pathname);
				}
			});
			for (File dctFile : dctFileOrDirs) {
				Collection<Dictionary> subDeliveredDicts = deliverDCTFiles(
						rootDir, dctFile, appId, encoding, langCodes,
						langCharset, warnings);
				deliveredDicts.addAll(subDeliveredDicts);
			}
			return deliveredDicts;
		}

		if (Util.isZipFile(file)) {
			try {
				Collection<Dictionary> zipDeliveredDicts = deliverZipDCTFile(
						rootDir, new ZipFile(file), appId, encoding, langCodes,
						langCharset, warnings);
				deliveredDicts.addAll(zipDeliveredDicts);
			} catch (IOException e) {
				throw new SystemError(e.getMessage());
			}
			return deliveredDicts;
		}

		// normal dct file
		Dictionary dict = null;
		try {
			rootDir = rootDir.replace("\\", "/");
			String dictPath = file.getAbsolutePath().replace("\\", "/");
			String dictName = dictPath.replace(rootDir, "");

			encoding = dictProp.getDictionaryEncoding(dictName);
			langCharset = dictProp.getDictionaryCharsets(dictName);

			warnings = new ArrayList<BusinessWarning>();

			dict = deliverDCT(dictName, dictPath, appId, encoding, langCodes,
					langCharset, warnings);
			if (!warnings.isEmpty()) {
				join(warnings,'\n').replace("\"", "\"\"");
				String forCSV = warnings.toString().replace("\"", "\"\"");
				forCSV=join(warnings,'\n').replace("\"", "\"\"");
				DictDeliverWarning.warn(String.format("%s,%s,%s,\"%s\"",
						file.getName(), encoding, file.getAbsolutePath(),
						forCSV));
			}
		} catch (BusinessException e) {
			String forCSV = e.toString().replace("\"", "\"\"");
			logDictDeliverFail.error(String.format("%s,%s,%s,\"%s\"",
					file.getName(), encoding, file.getAbsolutePath(), forCSV));
			log.error(e);
		}
		if (null != dict) {
			dict.setDictLanguages(null);
			dict.setLabels(null);
			deliveredDicts.add(dict);
		}
		return deliveredDicts;
	}

	/**
	 * Deliver a Zip file into database.
	 * */
	private Collection<Dictionary> deliverZipDCTFile(String rootDir,
			ZipFile file, Long appId, String encoding, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException {

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

				if (null == encoding) {
					byte[] bom = new byte[Util.UTF8_BOM_LENGTH];
					is.read(bom);
					encoding = Util.detectEncoding(bom);
				}
				String dictionaryName = entry.getName();
				String path = file.getName() + dictionaryName;
				Dictionary dict = null;
				try {
					dict = deliverDCT(dictionaryName, path, is, appId,
							encoding, langCodes, langCharset, warnings);
				} catch (BusinessException e) {
					log.error(e);
				}
				if (null != dict) {
					deliveredDicts.add(dict);
				}
			} catch (IOException e) {
				throw new SystemError(e.getMessage());
			}
		}

		return deliveredDicts;
	}

	public void generateDCT(String filename, Long dctId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {

		Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
				dctId);
		if (null == dict) {
			log.warn("ID for " + dctId
					+ " Dictionary is not found in database.");
			throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND,
					dctId);
		}

		// all the language code in dictionary
		Collection dictLangCodes = getObjectProperiesList(
				dict.getDictLanguages(), "languageCode");

		if (langCodes != null) {
			List<String> listLangCodes = new ArrayList(Arrays.asList(langCodes));
			listLangCodes.removeAll(dictLangCodes);
			if (!listLangCodes.isEmpty()) {
				throw new BusinessException(
						BusinessException.UNKNOWN_LANG_CODE,
						listLangCodes.get(0));
			}
			// used for iteration.
			dictLangCodes = Arrays.asList(langCodes);
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
			String checkFieldLangCodeString = String.format("  %s ",
					Label.CHECK_FIELD_NAME);
			String referenceFieldLangCodeString = String.format("  %s ",
					Label.REFERENCE_FIELD_NAME);
			int indentSize = checkFieldLangCodeString.length();

			Label[] labels = dict.getLabels().toArray(new Label[0]);
			for (int i = 0; i < labels.length; ++i) {
				label = labels[i];

				if (i > 0) {
					// output label separator
					out.println(";");
					out.println();
				}
				out.println(label.getKey() + ":");

				out.print(checkFieldLangCodeString
						+ convertContent(
								indentSize,
								generateCHK(label.getMaxLength(),
										label.getReference()), "\n",
								System.getProperty("line.separator")));
				// output translation separator
				out.println(",");

				out.print(referenceFieldLangCodeString
						+ convertContent(indentSize, label.getReference(),
								"\n", System.getProperty("line.separator")));

				String dictLang = null;
				for (Object objDictLang : dictLangCodes) {
					dictLang = (String) objDictLang;
					// output translation separator
					out.println(",");

					out.print("  " + dictLang + " ");

					// output langCode translation
					String translationString = label.getReference();

					Language dictLangCodeLanguage = langService
							.getAlcatelLanguageCodes().get(dictLang)
							.getLanguage();
					for (Translation translation : label.getText()
							.getTranslations()) {
						if (translation.getLanguage().getId()
								.equals(dictLangCodeLanguage.getId())) {
							translationString = translation.getTranslation();
							break;
						}
					}

					String converedString = convertContent(indentSize,
							translationString, "\n",
							System.getProperty("line.separator"));

					String charsetName = langCharset.get(dictLang);
					if (null == charsetName) {
						throw new BusinessException(
								BusinessException.CHARSET_NOT_FOUND,
								charsetName);
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

	public Dictionary previewDCT(String dictionaryName, String filename,
			Long appId, String encoding, Collection<BusinessWarning> warnings)
			throws BusinessException {
		File file = new File(filename);
		if (!file.exists()) {
			throw new BusinessException(BusinessException.DCT_FILE_NOT_FOUND,
					file.getName());
		}
		InputStream is;
		try {
			is = new FileInputStream(file);
			if (!file.exists()) {
				throw new BusinessException(
						BusinessException.DCT_FILE_NOT_FOUND, file.getName());
			}
			return previewDCT(dictionaryName, filename, is, appId, encoding,
					warnings);
		} catch (IOException e) {
			throw new SystemError(e.getMessage());
		}

	}

	public Dictionary previewDCT(String dictionaryName, String path,
			InputStream dctInputStream, Long appId, String encoding,
			Collection<BusinessWarning> warnings) throws BusinessException,
			IOException {
		Application app = (Application) getDao().retrieve(Application.class,
				appId);

		if (null == app) {
			throw new BusinessException(
					BusinessException.APPLICATION_NOT_FOUND, appId);
		}
		
		//TODO: temp test reader, 
//		Dictionary dict = dictionaryParser.parse(app, dictionaryName, path,
//				dctInputStream, encoding, warnings,null);

		Dictionary dict = dictionaryParser.parse(app, dictionaryName, path,
				dctInputStream, encoding, warnings,null);

		return dict;
	}

	public Dictionary importDCT(Dictionary dict, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) {
		log.info("Start importing DCT");
		if (null == dict)
			return null;

		BusinessException nonBreakExceptions = new BusinessException(
				BusinessException.NESTED_DCT_PARSE_ERROR, dict.getName());
		
		// check langCodes parameter
		Collection<String> langCodeList = null;
		if (langCodes != null) {
			Collection dictLangCodes = getObjectProperiesList(
					dict.getDictLanguages(), "languageCode");
			List<String> listLangCodes = new ArrayList(Arrays.asList(langCodes));
			listLangCodes.removeAll(dictLangCodes);
			// TODO: restore here after parse 1 work done.
			// if (!listLangCodes.isEmpty()) {
			//
			// throw new BusinessException(
			// BusinessException.UNKNOWN_LANG_CODE,
			// listLangCodes.get(0));
			// }
			langCodeList = Arrays.asList(langCodes);
		}

		Dictionary dbDict = (Dictionary) getDao().retrieveOne(
				"from Dictionary where name=:name",
				JSONObject.fromObject(String.format("{'name':'%s'}",
						dict.getName())));
		// create dictionary if not exists
		if (null == dbDict) {
			// create dictionary
			log.info("Dictionary " + dict.getName()
					+ " not exist in database, create new one in database...");
			dbDict = new Dictionary();
			dbDict.setApplication((Application) dao.retrieve(Application.class,
					dict.getApplication().getId()));
			dbDict.setEncoding(dict.getEncoding());
			dbDict.setFormat(dict.getFormat());
			dbDict.setName(dict.getName());
			dbDict.setPath(dict.getPath());
			dbDict.setLocked(false);
			dbDict = (Dictionary) getDao().create(dbDict);
		}

		// update dictionary languages
		for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
			if (langCodeList != null && !langCodeList.contains(dictLanguage.getLanguageCode())) {
				continue;
			}
			String charsetName = langCharset.get(dictLanguage.getLanguageCode());
			if (null == charsetName) {
				nonBreakExceptions.addNestedException(new BusinessException(
						BusinessException.CHARSET_NOT_DEFINED, dictLanguage.getLanguageCode()));
			}			
			mergeDictLanguage(dbDict, dictLanguage.getLanguage().getId(),
					dictLanguage.getLanguageCode(), charsetName);
		}

		// prepare textMap, labelMap by context
		log.info("Prepare data to import");
		Map<String, Collection<Text>> textMap = new HashMap<String, Collection<Text>>();
		Map<String, Collection<Label>> labelMap = new HashMap<String, Collection<Label>>();
		Map<Long, String> langCodeMap = dict.getLangCodeMap();
		for (Label label : dict.getLabels()) {
			String contextName = label.getContext().getName();
			Text text = label.getText();

			Collection<Text> texts = textMap.get(contextName);
			if (texts == null) {
				texts = new ArrayList<Text>();
				textMap.put(contextName, texts);
			}
			texts.add(text);

			Collection<Label> labels = labelMap.get(contextName);
			if (labels == null) {
				labels = new ArrayList<Label>();
				labelMap.put(contextName, labels);
			}
			labels.add(label);

			// filter by langCodes parameter
			if (langCodeList != null) {
				for (Iterator<Translation> iterator = text.getTranslations()
						.iterator(); iterator.hasNext();) {
					Translation trans = iterator.next();
					String langCode = langCodeMap.get(trans.getLanguage()
							.getId());
					if (!langCodeList.contains(langCode)) {
						iterator.remove();
					}
				}
			}

			// convert charset of translation strings
			for (Translation trans : text.getTranslations()) {
				String langCode = langCodeMap.get(trans.getLanguage().getId());
				String charsetName = langCharset.get(langCode);
				if (null == charsetName) {
					nonBreakExceptions.addNestedException(new BusinessException(
							BusinessException.CHARSET_NOT_DEFINED, langCode));
					continue;
				}
				try {
					String encodedTranslation = new String(trans
							.getTranslation().getBytes(dict.getEncoding()),
							charsetName);
					trans.setTranslation(encodedTranslation);

					// check charset
					if (!trans.isValidText()) {
						warnings.add(new BusinessWarning(
								BusinessWarning.INVALID_TEXT,
								encodedTranslation, charsetName, langCode,
								label.getKey()));
					}

					// check length
					if (!label.checkLength(encodedTranslation)) {
						warnings.add(new BusinessWarning(
								BusinessWarning.EXCEED_MAX_LENGTH, langCode,
								label.getKey()));
					}
				} catch (UnsupportedEncodingException e) {
					nonBreakExceptions.addNestedException(new BusinessException(
							BusinessException.CHARSET_NOT_FOUND, charsetName));
				}
			}
		}

		// for each context, insert or update label/text/translation data
		for (String contextName : textMap.keySet()) {
			log.info("Importing data into context " + contextName);
			Context context = textService.getContextByName(contextName);
			if (context == null) {
				context = new Context();
				context.setName(contextName);
				context = (Context) dao.create(context);
			}
			Collection<Text> texts = textMap.get(contextName);
			Map<String, Text> dbTextMap = textService.updateTranslations(
					context.getId(), texts);
			Collection<Label> labels = labelMap.get(contextName);
			for (Label label : labels) {
				// create or update label
				Label dbLabel = dbDict.getLabel(label.getKey());
				if (dbLabel == null) {
					label.setDictionary(dbDict);
					label.setContext(context);
					label.setText(dbTextMap.get(label.getReference()));
					dbLabel = (Label) dao.create(label, false);
				} else {
					dbLabel.setContext(context);
					dbLabel.setText(dbTextMap.get(label.getReference()));
					dbLabel.setKey(label.getKey());
					dbLabel.setDescription(label.getDescription());
					dbLabel.setMaxLength(label.getMaxLength());
					dbLabel.setReference(label.getReference());
				}
			}
		}
		
		if (nonBreakExceptions.hasNestedException()) {
			throw nonBreakExceptions;
		}
		log.info("Import DCT finish");
		return dbDict;
	}

	private DictionaryLanguage mergeDictLanguage(Dictionary dbDict,
			Long languageId, String languageCode, String charsetName) {
		DictionaryLanguage dbDictLang = dbDict.getDictLanguage(languageId);
		if (dbDictLang == null) {
			dbDictLang = new DictionaryLanguage();
			dbDictLang.setDictionary(dbDict);
			dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
					languageId));
			dbDictLang.setCharset(langService.getCharset(charsetName));
			dbDictLang.setLanguageCode(languageCode);
			dbDictLang = (DictionaryLanguage) dao.create(dbDictLang);
		} else {
			dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
					languageId));
			dbDictLang.setCharset(langService.getCharset(charsetName));
			dbDictLang.setLanguageCode(languageCode);
		}
		return dbDictLang;
	}

}
