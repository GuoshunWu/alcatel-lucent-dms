package com.alcatel_lucent.dms.service;

import static com.alcatel_lucent.dms.util.Util.generateSpace;
import static org.apache.commons.lang.StringUtils.join;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

@Service("dictionaryService")
@Scope("singleton")
@SuppressWarnings("unchecked")
public class DictionaryServiceImpl extends BaseServiceImpl implements
        DictionaryService {

    private static Logger log = Logger.getLogger(DictionaryServiceImpl.class);

    public static Logger logDictDeliverSuccess = Logger
            .getLogger("DictDeliverSuccess");
    public static Logger logDictDeliverFail = Logger
            .getLogger("DictDeliverFail");

    public static Logger logDictDeliverWarning = Logger
            .getLogger("DictDeliverWaning");

    @Autowired
    private TextService textService;

    @Autowired
    private LanguageService langService;

    @Autowired
    private com.alcatel_lucent.dms.service.parser.DictionaryParser[] parsers;
    

    public DictionaryServiceImpl() {
        super();
    }

    public Collection<Dictionary> previewDictionaries(String rootDir, File file, Collection<BusinessWarning> warnings) throws BusinessException {
    	Collection<Dictionary> result = new ArrayList<Dictionary>();
    	rootDir = rootDir.replace("\\", "/");
        long before = System.currentTimeMillis();
    	for (com.alcatel_lucent.dms.service.parser.DictionaryParser parser : parsers) {
    		result.addAll(parser.parse(rootDir, file, warnings));
    	}
        long after = System.currentTimeMillis();
        log.info("**************preview directory '" + file.getAbsolutePath() + "' take " + (after - before)
                + " milliseconds of time.************");
    	return result;
    }
    
    /**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir   root directory save dct files
     * @param dtIds the collection of the id for dictionary to be generated.
     */
    public void generateDCTFiles(String dir, Collection<Long> dtIds, String[] langCodes) {
        if (dtIds.isEmpty()) return;
        String idList = dtIds.toString().replace("[", "(").replace("]", ")");
        String hsql = "from Dictionary where id in " + idList;
        Collection<Dictionary> dicts = (Collection<Dictionary>) getDao().retrieve(hsql);

        for (Dictionary dict : dicts) {
            log.info("**************************generate dictionary: " + dict.getName() + "***************************");
            if (dict.getFormat().equals(Constants.DICT_FORMAT_MDC)) {
                generateMDC(new File(dir, dict.getName()), dict.getId(), null);
            } else if (dict.getFormat().equals(Constants.DICT_FORMAT_DCT)){
                generateDCT(new File(dir, dict.getName()), dict, dict.getEncoding(), null);
            } else if (dict.getFormat().equals(Constants.DICT_FORMAT_XML_LABEL)) {
            	generateLabelXML(dir, dict.getId(), null);
            } else if (dict.getFormat().equals(Constants.DICT_FORMAT_XML_PROP)) {
            	generatePropXML(dir, dict.getId(), null);
            } else {
            	log.error("Unsupported dict format: " + dict.getFormat());
            }
        }
    }

    /**
     * Generate a dct file for a specific dictionary.
     *
     * @param filename  the file name for dct file
     * @param dctId     dictionary id for the dictionary to be generated.
     * @param encoding  the encoding for the file to write.
     * @param langCodes the language codes for which languages to be generated.
     */

    public void generateDCT(String filename, Long dctId, String encoding,
                            String[] langCodes) {
        generateDCT(new File(filename), dctId, encoding, langCodes);
    }

    public void generateDCT(File file, Long dctId, String encoding,
                            String[] langCodes) {
        Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
                dctId);
        if (null == dict) {
            log.warn("ID for " + dctId
                    + " Dictionary is not found in database.");
            throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND,
                    dctId);
        }
        generateDCT(file, dict, encoding, langCodes);
    }

    private void generateDCT(File file, Dictionary dict, String encoding,
                             String[] langCodes)
            throws BusinessException {
        // all the language code in dictionary
        ArrayList<String> dictLangCodes = dict.getAllLanguageCodesOrdered();

        if (langCodes != null) {
            List<String> listLangCodes = new ArrayList<String>(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            if (!listLangCodes.isEmpty()) {
                throw new BusinessException(
                        BusinessException.UNKNOWN_LANG_CODE,
                        listLangCodes.get(0));
            }
            // used for iteration.
            dictLangCodes.removeAll(Arrays.asList(langCodes));
        }

        if (null == encoding) {
            encoding = dict.getEncoding();
        }

        PrintStream out = null;
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(file)), true, encoding);
            // output support languages
            if (encoding.equals("UTF-16LE")) {
                out.write(new byte[]{(byte) 0xff, (byte) 0xfe});
            }
            out.println("-- Generated by DMS\n");
            if (dict.getAnnotation1() != null) {
            	out.print(dict.getAnnotation1());
            }
            boolean hasCHK = false;
            for (Label label : dict.getLabels()) {
            	if (label.getMaxLength() != null) {
            		hasCHK = true;
            		break;
            	}
            }
            if (hasCHK) {
            	dictLangCodes.add(0, "CHK");
            }
            out.println("LANGUAGES {" + join(dictLangCodes, ", ") + "}");
            out.println();
            dictLangCodes.remove("CHK");
            dictLangCodes.remove("GAE");

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
                if (label.getAnnotation1() != null) {
                	out.print(label.getAnnotation1());
                }
                out.println(label.getKey() + ":");
                if (label.getAnnotation2() != null) {
                	out.print(label.getAnnotation2());
                }
                String chk = generateCHK(label.getMaxLength(),
                        label.getReference());
                if (null != chk) {
                    out.print(checkFieldLangCodeString + convertContent(indentSize, chk, "\n", System.getProperty("line.separator")));
                    // output translation separator
                    out.println(",");
                }
                out.print(referenceFieldLangCodeString
                        + convertContent(indentSize, label.getReference(),
                        "\n", System.getProperty("line.separator")));
                for (String langCode : dictLangCodes) {
//                for (LabelTranslation trans :  label.getOrigTranslations()) {
                	LabelTranslation trans = label.getOrigTranslation(langCode);
                    // output translation separator
                    out.println(",");
                    
                    out.print("  " + langCode + " ");

                    // output dictLangCode translation
                    String translationString = label.getReference();
                    
                    // if need translation, get translation from context dictionary
                    // otherwise, get translation from original translation
                    DictionaryLanguage dl = dict.getDictLanguage(langCode);
                    if (trans != null && !trans.isNeedTranslation()) {
                    	translationString = trans.getOrigTranslation();
                    } else {
	                    for (Translation translation : label.getText()
	                            .getTranslations()) {
	                        if (translation.getLanguage().getId()
	                                .equals(dl.getLanguage().getId())) {
	                            translationString = translation.getTranslation();
	                            break;
	                        }
	                    }
                    }

                    String converedString = convertContent(indentSize,
                            translationString, "\n",
                            System.getProperty("line.separator"));

                    String charsetName = dl.getCharset().getName();
                    out.write(converedString.getBytes(charsetName));
                }
            }
            if (labels.length > 0) {
                out.println(";");
            }
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    @Override
    public void generateMDC(String file, Long dctId, String[] langCodes) throws BusinessException {
        generateMDC(new File(file), dctId, langCodes);
    }


    public void generateMDC(File file, Long dctId, String[] langCodes) throws BusinessException {

        Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
                dctId);
        if (null == dict) {
            log.warn("ID for " + dctId
                    + " Dictionary is not found in database.");
            throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND,
                    dctId);
        }

        // all the language code in dictionary
        HashSet<String> dictLangCodes = dict.getAllLanguageCodes();

        if (langCodes != null) {
            List<String> listLangCodes = new ArrayList<String>(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            if (!listLangCodes.isEmpty()) {
                throw new BusinessException(
                        BusinessException.UNKNOWN_LANG_CODE,
                        listLangCodes.get(0));
            }
            // used for iteration.
            dictLangCodes = new HashSet<String>(Arrays.asList(langCodes));
        }
        log.info("**************************generate dictionary: " + file.getPath() + "***************************");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        Collection<Label> labels = dict.getLabels();
        Collection<Translation> translations = null;

        //generate xml
        Document document = DocumentHelper.createDocument();
        document.addComment("Created by DMS");

        Element dictionaryElement = document.addElement("dictionary");
        Element messageStringElement = dictionaryElement.addElement("messageString");

        for (Label label : labels) {
            Element labelElement = messageStringElement.addElement(label.getKey());
            translations = label.getText().getTranslations();

            for (String dictLangCode : dictLangCodes) {
                Element langElement = labelElement.addElement("lang");
                String translatedString = label.getReference();

                DictionaryLanguage dl = dict.getDictLanguage(dictLangCode);

                Language dictLangCodeLanguage = dl.getLanguage();

                for (Translation translation : translations) {
                    if (translation.getLanguage().getId().equals(dictLangCodeLanguage.getId())) {
                        translatedString = translation.getTranslation();
                        break;
                    }
                }

                langElement.addAttribute("id", dictLangCode);
                langElement.setText(translatedString);
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
//      OutputFormat format = OutputFormat.createCompactFormat();

        format.setEncoding("UTF-8");

        try {
            XMLWriter output = new XMLWriter(new FileWriter(file), format);
            output.write(document);
            output.close();
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }

    }

    private String generateCHK(String maxLength, String reference) {
        if (null == maxLength) return null;
        log.debug("maxLength=" + maxLength + ", reference=" + reference);
        StringBuilder sb = new StringBuilder();
        //Trailing empty strings should not be discarded
        String[] sLineLens = maxLength.split(",", -1);
        String[] refers = reference.split("\n", -1);
        int maxLen = -1;

        int min = Math.min(sLineLens.length, refers.length);
        for (int i = 0; i < min; ++i) {
            maxLen = Integer.parseInt(sLineLens[i].trim());
            sb.append(refers[i].trim());
            int fill = maxLen - refers[i].length();
            int base = 0;
            while (fill-- > 0) {
                sb.append((char) (base++ % 10 + '0'));
            }
            sb.append("\n");
        }

        for (int i = min; i < sLineLens.length; ++i) {
            maxLen = Integer.parseInt(sLineLens[i].trim());
            int fill = maxLen;
            int base = 0;
            while (fill-- > 0) {
                sb.append((char) (base++ % 10 + '0'));
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
    public Dictionary importDictionary(Long appId, Dictionary dict, String version, int mode, String[] langCodes,
                                Map<String, String> langCharset,
                                Collection<BusinessWarning> warnings) {
        log.info("Start importing dictionary in " + (mode == Constants.DELIVERY_MODE ? "DELIVERY" : "TRANSLATION") + " mode");
        if (null == dict)
            return null;

        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dict.getName());

        // check langCodes parameter
        if (langCodes != null) {
            Collection<String> dictLangCodes = dict.getAllLanguageCodes();
            List<String> listLangCodes = new ArrayList<String>(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            // TODO: restore here after parse 1 work done.
            // if (!listLangCodes.isEmpty()) {
            //
            // throw new BusinessException(
            // BusinessException.UNKNOWN_LANG_CODE,
            // listLangCodes.get(0));
            // }
        }

        // unified langCodeList
        Collection<String> langCodeList = null;
        if (langCodes != null) {
            langCodeList = new ArrayList<String>();
            for (String langCode : langCodes) {
                langCodeList.add(getUnifiedLangCode(langCode));
            }
        }

        // unified langCharset
        Map<String, String> uniLangCharset = new HashMap<String, String>();
        for (String langCode : langCharset.keySet()) {
            uniLangCharset.put(getUnifiedLangCode(langCode), langCharset.get(langCode));
        }
        langCharset = uniLangCharset;
        Dictionary lastDict = null;



        // Associate base Dictionary
        Application app = (Application) getDao().retrieve(Application.class, appId);
        Map param = new HashMap();
        param.put("name", dict.getName());
        param.put("appBaseId", app.getBase().getId());
        DictionaryBase baseDBDict = (DictionaryBase) getDao().retrieveOne(
        		"from DictionaryBase where name=:name and applicationBase.id=:appBaseId", param);
        if(null==baseDBDict){
        	if (mode == Constants.TRANSLATION_MODE) {
        		throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dict.getName());
        	}
            baseDBDict = new DictionaryBase();
            baseDBDict.setName(dict.getName());
            baseDBDict.setFormat(dict.getFormat());
            baseDBDict.setEncoding(dict.getEncoding());
            baseDBDict.setPath(dict.getPath());
            baseDBDict.setApplicationBase(app.getBase());
            baseDBDict=(DictionaryBase)getDao().create(baseDBDict);
        } else{
            if (mode == Constants.DELIVERY_MODE) {
                baseDBDict.setName(dict.getName());
                baseDBDict.setFormat(dict.getFormat());
                baseDBDict.setEncoding(dict.getEncoding());
                baseDBDict.setPath(dict.getPath());
            }
        }


        param = new HashMap();
        param.put("version", version);
        param.put("baseId", baseDBDict.getId());
        Dictionary dbDict = (Dictionary) getDao().retrieveOne(
                "from Dictionary where version=:version and base.id=:baseId", param);
        // create dictionary if not exists
        if (null == dbDict) {
        	if (mode == Constants.TRANSLATION_MODE) {
        		throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dict.getName() + " " + version);
        	}
            // create dictionary
            log.info("Dictionary " + dict.getName()
                    + " not exist in database, create new one in database...");
            dbDict = new Dictionary();
            dbDict.setBase(baseDBDict);
            dbDict.setVersion(version);
            dbDict.setAnnotation1(dict.getAnnotation1());
            dbDict.setAnnotation2(dict.getAnnotation2());
            dbDict.setAnnotation3(dict.getAnnotation3());
            dbDict.setAnnotation4(dict.getAnnotation4());


            Collection<Dictionary> dictionaries= app.getDictionaries();
            if(null==dictionaries){
                dictionaries=new HashSet<Dictionary>();
                app.setDictionaries(dictionaries);
            }
            Iterator<Dictionary> iter = dictionaries.iterator();
            // remove previous dictionary version in the app
            while (iter.hasNext()) {
            	Dictionary appDict = iter.next();
            	if (appDict.getBase().getId().equals(baseDBDict.getId())) {
            		iter.remove();
            	}
            }
            dictionaries.add(dbDict);

            dbDict.setLocked(false);
            dbDict = (Dictionary) getDao().create(dbDict);
            if (mode == Constants.DELIVERY_MODE) {	// in case new dictionary version, compare with latest version
            	lastDict = getLatestDictionary(baseDBDict.getId(), dbDict.getId());
            }
        } else {
            if (mode == Constants.DELIVERY_MODE) {	// in case existing dictionary version, compare with the same version
            	lastDict = dbDict;
            	dbDict.setAnnotation1(dict.getAnnotation1());
            	dbDict.setAnnotation2(dict.getAnnotation2());
            	dbDict.setAnnotation3(dict.getAnnotation3());
            	dbDict.setAnnotation4(dict.getAnnotation4());
            }
        }
        

        // update dictionary languages in delivery mode
        if (mode == Constants.DELIVERY_MODE) {
	        for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
	            String uniLangCode = getUnifiedLangCode(dictLanguage.getLanguageCode());
	            if (langCodeList != null && !langCodeList.contains(uniLangCode)) {
	                continue;
	            }
	            String charsetName = langCharset.get(uniLangCode);
	            if (charsetName == null) {
	            	charsetName = langCharset.get("DEFAULT");
	            }
	            if (null == charsetName) {
	                nonBreakExceptions.addNestedException(new BusinessException(
	                        BusinessException.CHARSET_NOT_DEFINED, dictLanguage.getLanguageCode()));
	            } else {
	                mergeDictLanguage(dbDict, dictLanguage.getLanguage().getId(), 
	                		dictLanguage.getLanguageCode(), charsetName, dictLanguage.getSortNo(), 
	                		dictLanguage.getAnnotation1(), dictLanguage.getAnnotation2(), 
	                		dictLanguage.getAnnotation3(), dictLanguage.getAnnotation4());
	            }
	        }
        }

        // prepare data for creation: textMap, labelMap indexed by context
        log.info("Prepare data to import");
        Map<String, Collection<Text>> textMap = new HashMap<String, Collection<Text>>();
        Map<String, Collection<Label>> labelMap = new HashMap<String, Collection<Label>>();
        Map<Long, String> langCodeMap = dict.getLangCodeMap();
//        Map<Long, String> langCodeMap = new HashMap<Long, String>();
//        for (DictionaryLanguage dl : dict.getDictLanguages()) {
//            langCodeMap.put(dl.getLanguage().getId(), dl.getLanguageCode()) ;
//        }
        for (Label label : dict.getLabels()) {
            String contextName = label.getContext().getName();
            Text text = new Text();
            text.setReference(label.getReference());

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
                for (Iterator<LabelTranslation> iterator = label.getOrigTranslations()
                        .iterator(); iterator.hasNext(); ) {
                    LabelTranslation trans = iterator.next();
                    String langCode = langCodeMap.get(trans.getLanguage()
                            .getId());
                    String uniLangCode = getUnifiedLangCode(langCode);
                    if (!langCodeList.contains(uniLangCode)) {
                        iterator.remove();
                    }
                }
            }
            
            Label lastLabel = null;
            if (lastDict != null) {
            	lastLabel = lastDict.getLabel(label.getKey());
            }
            String labelWarnings = null;
            // for each translation
            // convert charset of translation strings
            // determine translation behaviors
            if (label.getOrigTranslations() != null) {
	            for (LabelTranslation trans : label.getOrigTranslations()) {
	                String langCode = langCodeMap.get(trans.getLanguage().getId());
	                String charsetName = langCharset.get(getUnifiedLangCode(langCode));
	                if (charsetName == null) {
	                	charsetName = langCharset.get("DEFAULT");
	                }
	                if (null == charsetName) {
	                    nonBreakExceptions.addNestedException(new BusinessException(
	                            BusinessException.CHARSET_NOT_DEFINED, langCode));
	                    continue;
	                }
	                try {
	                    boolean invalidText = false;
	                    if (!dict.getEncoding().equals(charsetName)) {
	                        byte[] source = trans.getOrigTranslation().getBytes(dict.getEncoding());
	                        String encodedTranslation = new String(source, charsetName);
	                        byte[] target = encodedTranslation.getBytes(charsetName);
	                        if (!Arrays.equals(source, target)) {
	                            invalidText = true;
	                            trans.setOrigTranslation(text.getReference());
	                            log.warn("Invalid encoding at label " + label.getKey() + " of dict " + dict.getPath());
	                        } else {
	                            trans.setOrigTranslation(encodedTranslation);
	                        }
	                    }
	
	                    labelWarnings = "";
	                    // check charset
	                    if (invalidText || !trans.isValidText()) {
	                        warnings.add(new BusinessWarning(
	                                BusinessWarning.INVALID_TEXT,
	                                trans.getOrigTranslation(), charsetName, langCode,
	                                label.getKey()));
	                        labelWarnings += BusinessWarning.INVALID_TEXT;
	                    }
	
	                    // check length
	                    if (!label.checkLength(trans.getOrigTranslation())) {
	                        warnings.add(new BusinessWarning(
	                                BusinessWarning.EXCEED_MAX_LENGTH, langCode,
	                                label.getKey()));
	                        if (!labelWarnings.isEmpty()) {
	                        	labelWarnings += ";";
	                        }
	                        labelWarnings += BusinessWarning.EXCEED_MAX_LENGTH;
	                    }
	                    trans.setWarnings(labelWarnings);
	                } catch (UnsupportedEncodingException e) {
	                    nonBreakExceptions.addNestedException(new BusinessException(
	                            BusinessException.CHARSET_NOT_FOUND, charsetName));
	                }
	                
	                // determine if the translation should take value from context dictionary
	                trans.setNeedTranslation(true);
	                if (lastLabel != null) {
	                	// get the original translation in latest version
	                	LabelTranslation lastTranslation = lastLabel.getOrigTranslation(trans.getLanguageCode());
	                	if (lastTranslation != null && 
	                			!lastTranslation.getOrigTranslation().equals(trans.getOrigTranslation()) &&
	                			!trans.getOrigTranslation().equals(label.getReference())) {
	                		// translation changed means the label was translated on developer side
	                		trans.setNeedTranslation(false);
	                	}
	                }
	                
	                Translation t = new Translation();
	                t.setText(text);
	                t.setTranslation(trans.getOrigTranslation());
	                t.setLanguage(trans.getLanguage());
	                
	                // determine translation status
	                if (trans.getRequestTranslation() != null) {
	                	t.setStatus(trans.getRequestTranslation().booleanValue() ? 
	                			Translation.STATUS_UNTRANSLATED : Translation.STATUS_TRANSLATED);
	                } else if (!trans.isNeedTranslation()) {
	                	t.setStatus(Translation.STATUS_TRANSLATED);
	                } else if (label.getReference().equals(trans.getOrigTranslation())) {
	                	t.setStatus(Translation.STATUS_UNTRANSLATED);
	                } else {
	                	t.setStatus(Translation.STATUS_TRANSLATED);
	                }
	                text.addTranslation(t);
	            } //for
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
            Collection<Label> labels = labelMap.get(contextName);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    context.getId(), texts, mode);
            
            // in TRANSLATION_MODE, no change to label
            if (mode == Constants.TRANSLATION_MODE) {
            	continue;
            }
            
            // NOTE: following code is only executed in DELIVERY_MODE
            int sortNo = 1;
            for (Label label : labels) {
                // create or update label
                Label dbLabel = dbDict.getLabel(label.getKey());
                boolean newLabel = true;
                if (dbLabel == null) {
                    label.setDictionary(dbDict);
                    label.setContext(context);
                    label.setText(dbTextMap.get(label.getReference()));
                    label.setSortNo(sortNo++);
                    dbLabel = (Label) dao.create(label, false);
                } else {
                    newLabel = false;
                    dbLabel.setContext(context);
                    dbLabel.setText(dbTextMap.get(label.getReference()));
                    dbLabel.setKey(label.getKey());
                    dbLabel.setDescription(label.getDescription());
                    dbLabel.setMaxLength(label.getMaxLength());
                    dbLabel.setReference(label.getReference());
                    dbLabel.setAnnotation1(label.getAnnotation1());
                    dbLabel.setAnnotation2(label.getAnnotation2());
                    dbLabel.setSortNo(sortNo++);
                }
                
                // create or update LabelTranslation
                if (label.getOrigTranslations() != null) {
	                for (LabelTranslation trans : label.getOrigTranslations()) {
	                	LabelTranslation dbLabelTrans = null;
	                	if (!newLabel) {
	                		dbLabelTrans = dbLabel.getOrigTranslation(trans.getLanguageCode());
	                	}
	                	if (dbLabelTrans == null) {
	                		trans.setLabel(dbLabel);
	                		trans.setLanguage((Language) dao.retrieve(Language.class, trans.getLanguage().getId()));
	                		dbLabelTrans = (LabelTranslation) dao.create(trans, false);
	                	} else {
	                		dbLabelTrans.setOrigTranslation(trans.getOrigTranslation());
	                		dbLabelTrans.setAnnotation1(trans.getAnnotation1());
	                		dbLabelTrans.setAnnotation2(trans.getAnnotation2());
	                		dbLabelTrans.setWarnings(trans.getWarnings());
	                		dbLabelTrans.setNeedTranslation(trans.isNeedTranslation());
	                		dbLabelTrans.setLanguageCode(trans.getLanguageCode());
	                		dbLabelTrans.setSortNo(trans.getSortNo());
	                	}
	                }
                }
            }
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }
        log.info("Import dictionary finish");
        return dbDict;
    }

    private String getUnifiedLangCode(String langCode) {
        return langCode.toUpperCase().replace("_", "-");
    }

    private DictionaryLanguage mergeDictLanguage(Dictionary dbDict,
                                                 Long languageId, String languageCode, String charsetName, 
                                                 int sortNo, String annotation1, String annotation2, 
                                                 String annotation3, String annotation4) {
        DictionaryLanguage dbDictLang = dbDict.getDictLanguage(languageCode);
        if (dbDictLang == null) {
            dbDictLang = new DictionaryLanguage();
            dbDictLang.setDictionary(dbDict);
            dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
                    languageId));
            dbDictLang.setCharset(langService.getCharset(charsetName));
            dbDictLang.setLanguageCode(languageCode);
            dbDictLang.setSortNo(sortNo);
            dbDictLang.setAnnotation1(annotation1);
            dbDictLang.setAnnotation2(annotation2);
            dbDictLang.setAnnotation3(annotation3);
            dbDictLang.setAnnotation4(annotation4);
            dbDictLang = (DictionaryLanguage) dao.create(dbDictLang);
        } else {
            dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
                    languageId));
            dbDictLang.setCharset(langService.getCharset(charsetName));
            dbDictLang.setLanguageCode(languageCode);
            dbDictLang.setSortNo(sortNo);
            dbDictLang.setAnnotation1(annotation1);
            dbDictLang.setAnnotation2(annotation2);
            dbDictLang.setAnnotation3(annotation3);
            dbDictLang.setAnnotation4(annotation4);
        }
        return dbDictLang;
    }

    public Dictionary getLatestDictionary(Long dictionaryBaseId, Long beforeDictionaryId) {
    	String hql = "from Dictionary where base.id=:baseId";
    	Map param = new HashMap();
    	param.put("baseId", dictionaryBaseId);
    	if (beforeDictionaryId != null) {
    		hql += " and id<:beforeId";
    		param.put("beforeId", beforeDictionaryId);
    	}
    	hql += " order by id desc";
    	return (Dictionary) dao.retrieveOne(hql, param);
    }
    
    public void removeDictionaryFromApplication(Long appId, Long dictId) {
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	app.removeDictionary(dictId);
    }
    
    public void removeDictionaryFromApplication(Long appId, Collection<Long> idList) {
    	for (Long id : idList) {
    		removeDictionaryFromApplication(appId, id);
    	}
    }
    
    public Long deleteDictionary(Long id) {
    	String hql = "select distinct a from Application a join a.dictionaries as d where d.id=:id";
    	Map param = new HashMap();
    	param.put("id", id);
    	Collection<Application> apps = dao.retrieve(hql, param);
    	for (Application app : apps) {
    		app.removeDictionary(id);
    	}
    	Dictionary dictionary = (Dictionary) dao.retrieve(Dictionary.class, id);
    	DictionaryBase dictBase = dictionary.getBase();
    	dao.delete(Dictionary.class, id);
    	
    	// delete dictBase if it doesn't contain other dictionary
    	if (dictBase.getDictionaries() == null || dictBase.getDictionaries().size() == 0 ||
    			dictBase.getDictionaries().size() == 1 && dictBase.getDictionaries().iterator().next().getId().equals(id)) {
    		dao.delete(dictBase);
    		return dictBase.getId();
    	}
    	return null;
    }
    
    public void deleteDictionary(Collection<Long> idList) {
    	for (Long id : idList) {
    		deleteDictionary(id);
    	}
    }
    
/*    
    public Map<Long, int[]> getDictTranslationSummary(Long dictId) {
    	Map<Long, int[]> result = new HashMap<Long, int[]>();
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	String hql = "select ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Dictionary d join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where d.id=:dictId and ot.language=t.language" +
    			" group by ot.language.id";
    	Map param = new HashMap();
    	param.put("dictId", dictId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		result.put((Long) row[0], new int[] {((Number)row[1]).intValue(), ((Number)row[2]).intValue(), ((Number)row[3]).intValue()});
    	}
    	return result;
    }
*/
    
    public Map<Long, Map<Long, int[]>> getDictTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select d.id,ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by d.id,ot.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }

    public Map<Long, Map<Long, int[]>> getAppTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select a.id,ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by a.id,ot.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }
    
    public int getLabelNumByApp(Long appId) {
    	String hql = "select count(l) from Application app join app.dictionaries d join d.labels l where app.id=:appId";
    	Map param = new HashMap();
    	param.put("appId", appId);
    	Number count = (Number) dao.retrieveOne(hql, param);
    	return count == null ? 0 : count.intValue();
    }

    public void updateDictionaryFormat(Long id, String format) throws BusinessException {
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
    	// TODO: update valid format list
    	String[] validFormats = {"DCT", "Dictionary conf", "XML labels", "XML properties", "Text properties"};
    	if (!Arrays.asList(validFormats).contains(format)) {
    		throw new BusinessException(BusinessException.INVALID_DICT_FORMAT, format);
    	}
    	dict.setFormat(format);
    }
    
    public void updateDictionaryEncoding(Long id, String encoding) throws BusinessException {
    	encoding = encoding.trim();
    	if (!encoding.equals("ISO-8859-1") && !encoding.equals("UTF-8") && !encoding.equals("UTF-16LE")) {
    		throw new BusinessException(BusinessException.INVALID_DICT_ENCODING, encoding);
    	}
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
    	dict.setEncoding(encoding);
    }
    
    public void changeDictionaryInApp(Long appId, Long oldDictId, Long newDictId) throws BusinessException {
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	Dictionary oldDict = (Dictionary) dao.retrieve(Dictionary.class, oldDictId);
    	Dictionary newDict = (Dictionary) dao.retrieve(Dictionary.class, newDictId);
    	if (!oldDict.getBase().getId().equals(newDict.getBase().getId())) {
    		throw new BusinessException(BusinessException.DICTIONARIES_NOT_SAME_BASE);
    	}
    	Collection<Dictionary> dictionaries = app.getDictionaries();
    	boolean found = false;
    	if (dictionaries != null) {
    		Iterator<Dictionary> iterator = dictionaries.iterator();
    		while (iterator.hasNext()) {
    			Dictionary dict = iterator.next();
    			if (dict.getId().equals(oldDictId)) {
    				iterator.remove();
    				found = true;
    				break;
    			}
    		}
    	}
    	if (found) {
    		dictionaries.add(newDict);
    	} else {
    		throw new BusinessException(BusinessException.DICTIONARY_NOT_IN_APP);
    	}
    }
    
    public DictionaryLanguage addLanguage(Long dictId, String code, Long languageId, Long charsetId) throws BusinessException {
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	if (dict.getDictLanguage(code) != null) {
    		throw new BusinessException(BusinessException.DUPLICATE_LANG_CODE, dict.getName(), code);
    	}
    	DictionaryLanguage dl = new DictionaryLanguage();
    	dl.setDictionary(dict);
    	dl.setLanguageCode(code);
    	dl.setLanguage((Language) dao.retrieve(Language.class, languageId));
    	dl.setCharset((Charset) dao.retrieve(Charset.class, charsetId));
    	dl.setSortNo(dict.getMaxSortNo());
    	return (DictionaryLanguage) dao.create(dl);
    }
    
    public DictionaryLanguage updateDictionaryLanguage(Long id, String code, Long languageId, Long charsetId) {
    	DictionaryLanguage dl = (DictionaryLanguage) dao.retrieve(DictionaryLanguage.class, id);
    	if (code != null) {
    		dl.setLanguageCode(code);
    	}
    	if (languageId != null) {
        	dl.setLanguage((Language) dao.retrieve(Language.class, languageId));
    	}
    	if (charsetId != null) {
        	dl.setCharset((Charset) dao.retrieve(Charset.class, charsetId));
    	}
    	return dl;
    }
    
    public void removeDictionaryLanguage(Collection<Long> ids) {
    	for (Long id : ids) {
        	DictionaryLanguage dl = (DictionaryLanguage) dao.retrieve(DictionaryLanguage.class, id);
        	dao.delete(dl);
    	}
    }
    
    public void updateLabels(Collection<Long> idList, String maxLength,
			String description, String context) {
    	Map<String, Context> newContextMap = new HashMap<String, Context>();
    	for (Long id : idList) {
    		Label label = (Label) dao.retrieve(Label.class, id);
    		if (maxLength != null) {
    			label.setMaxLength(maxLength);
    		}
    		if (description != null) {
    			label.setDescription(description);
    		}
    		if (context != null) {
    			Context ctx = newContextMap.get(context);
    			if (ctx == null) {
	    			ctx = textService.getContextByName(context);
	    			if (ctx == null) {
	    				ctx = new Context();
	    				ctx.getName();
	    				ctx = (Context) dao.create(ctx);
	    				newContextMap.put(context, ctx);
	    			}
    			}
    			label.setContext(ctx);
    		}
    	}
    }
    
    public void generateLabelXML(String targetDir, Long dictId, String[] langCodes) throws BusinessException {
    	File target = new File(targetDir);
    	if (target.exists()) {
    		if (target.isFile()) {
    			throw new BusinessException(BusinessException.TARGET_IS_NOT_DIRECTORY, targetDir);
    		}
    	} else {
    		if (!target.mkdirs()) {
    			throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, targetDir);
    		}
    	}
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	
    	// create reference language file
    	generateLabelXML(targetDir, dict, null);
    	
    	// generate for each language
    	HashSet<String> langCodeSet = null;
    	if (langCodes != null) {
    		langCodeSet = new HashSet<String>();
    		langCodeSet.addAll(Arrays.asList(langCodes));
    	}
    	if (dict.getDictLanguages() != null) {
    		for (DictionaryLanguage dl : dict.getDictLanguages()) {
    			String langCode = dl.getLanguageCode();
    			if (langCodeSet != null && !langCodeSet.contains(langCode)) {
    				continue;
    			}
    			generateLabelXML(targetDir, dict, dl);
    		}
    	}
    }

	private void generateLabelXML(String targetDir, Dictionary dict, DictionaryLanguage dl) {
    	Document doc = DocumentHelper.createDocument();
    	doc.addComment("\n# Generated by DMS using language " + (dl == null ? "en" : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
    	String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
    	String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
    	String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
    	String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());
    	if (dictComments != null) {
    		String[] comments = dictComments.split("\n");
			for (String comment : comments) {
				comment = comment.replace("\\n", "\n");
				comment = comment.replace("\\\\", "\\");
				doc.addComment(comment);
			}
    	}
    	Element eleLabels = doc.addElement("LABELS");
    	if (dictNamespaces != null) {
    		String[] nsList = dictNamespaces.split("\n");
    		for (String ns : nsList) {
    			String[] keyValue = ns.split("=", 2);
    			if (keyValue.length == 2) {
    				eleLabels.addNamespace(keyValue[0], keyValue[1]);
    			}
    		}
    	}
    	if (dictAttributes != null) {
    		String[] attributes = dictAttributes.split("\n");
			for (String entry : attributes) {
				String[] keyValue = entry.split("=", 2);
				if (keyValue.length == 2) {
					eleLabels.addAttribute(keyValue[0], keyValue[1]);
				}
			}
    	}
    	if (dict.getLabels() != null) {
	    	for (Label label : dict.getLabels()) {
	    		String text = label.getReference();
	    		String annotation1 = label.getAnnotation1();	// attributes
	    		String annotation2 = label.getAnnotation2();	// leading comments
	    		if (dl != null) {
	    			LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
	    			if (lt != null) {
	    				annotation1 = lt.getAnnotation1();
	    				annotation2 = lt.getAnnotation2();
	    			}
	    			if (lt != null && !lt.isNeedTranslation()) {	// no translation needed
	    				text = lt.getOrigTranslation();
	    			} else {	// get translation from context dictionary
	    				Translation trans = label.getText().getTranslation(dl.getLanguage().getId());
	    				text = (trans == null ? label.getReference() : trans.getTranslation());
	    			}
	    		}
	    		// add leading comments
	    		if (annotation2 != null) {
	    			String[] comments = annotation2.split("\n");
	    			for (String comment : comments) {
	    				eleLabels.addComment(comment);
	    			}
	    		}
	    		// create label
	    		Element eleLabel = eleLabels.addElement("LABEL");
	    		eleLabel.addAttribute("label_id", label.getKey());
	    		if (annotation1 != null) {
	    			String[] attributes = annotation1.split("\n");
	    			for (String entry : attributes) {
	    				String[] keyValue = entry.split("=", 2);
	    				if (keyValue.length == 2) {
	    					eleLabel.addAttribute(keyValue[0], keyValue[1]);
	    				}
	    			}
	    		}
	    		eleLabel.addText(text);
	    		if (text.indexOf('\n') != -1) { // preserve line breaks among the text
		    		eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
	    		}
	    	}
    	}
    	
    	// output
    	String filename = dict.getName();
    	if (dl != null) {
    		int pos = filename.lastIndexOf("en.xml");
    		if (pos != -1) {
    			filename = filename.substring(0, pos) + dl.getLanguageCode() + ".xml";
    		} else {
    			filename += dl.getLanguageCode() + ".xml";
    		}
    	}
    	OutputFormat format = OutputFormat.createPrettyPrint();
    	XMLWriter output = null;
    	try {
    		File targetFile = new File(targetDir, filename);
    		if (!targetFile.getParentFile().exists()) {
    			targetFile.getParentFile().mkdirs();
    		}
    		output = new XMLWriter(new FileWriter(targetFile), format);
    		if (processingInstructions != null) {
    			String[] piList = processingInstructions.split("\n");
    			for (String pi : piList) {
    				String[] keyValue = pi.split("=", 2);
    				if (keyValue.length == 2) {
    					output.processingInstruction(keyValue[0], keyValue[1]);
    				}
    			}
    		}
    		output.write(doc);
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new SystemError(e);
    	} finally {
    		if (output != null) try {output.close();} catch (Exception e) {}
    	}
	}
	
    public void generatePropXML(String targetDir, Long dictId, String[] langCodes) throws BusinessException {
    	File target = new File(targetDir);
    	if (target.exists()) {
    		if (target.isFile()) {
    			throw new BusinessException(BusinessException.TARGET_IS_NOT_DIRECTORY, targetDir);
    		}
    	} else {
    		if (!target.mkdirs()) {
    			throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, targetDir);
    		}
    	}
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	
    	// create reference language file
    	generatePropXML(targetDir, dict, null);
    	
    	// generate for each language
    	HashSet<String> langCodeSet = null;
    	if (langCodes != null) {
    		langCodeSet = new HashSet<String>();
    		langCodeSet.addAll(Arrays.asList(langCodes));
    	}
    	if (dict.getDictLanguages() != null) {
    		for (DictionaryLanguage dl : dict.getDictLanguages()) {
    			String langCode = dl.getLanguageCode();
    			if (langCodeSet != null && !langCodeSet.contains(langCode)) {
    				continue;
    			}
    			generatePropXML(targetDir, dict, dl);
    		}
    	}
    }

    private void generatePropXML(String targetDir, Dictionary dict, DictionaryLanguage dl) {
    	Document doc = DocumentHelper.createDocument();
    	doc.addComment("\n# Generated by DMS using language " + (dl == null ? "en" : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
    	String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
    	String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
    	String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
    	String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());
    	if (dictComments != null) {
    		String[] comments = dictComments.split("\n");
			for (String comment : comments) {
				comment = comment.replace("\\n", "\n");
				comment = comment.replace("\\\\", "\\");
				doc.addComment(comment);
			}
    	}
    	Element eleLabels = doc.addElement("properties");
    	if (dictNamespaces != null) {
    		String[] nsList = dictNamespaces.split("\n");
    		for (String ns : nsList) {
    			String[] keyValue = ns.split("=", 2);
    			if (keyValue.length == 2) {
    				eleLabels.addNamespace(keyValue[0], keyValue[1]);
    			}
    		}
    	}
    	if (dictAttributes != null) {
    		String[] attributes = dictAttributes.split("\n");
			for (String entry : attributes) {
				String[] keyValue = entry.split("=", 2);
				if (keyValue.length == 2) {
					eleLabels.addAttribute(keyValue[0], keyValue[1]);
				}
			}
    	}
    	if (dict.getLabels() != null) {
	    	for (Label label : dict.getLabels()) {
	    		String text = label.getReference();
	    		String annotation1 = label.getAnnotation1();	// attributes
	    		String annotation2 = label.getAnnotation2();	// leading comments
	    		if (dl != null) {
	    			LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
	    			if (lt != null) {
	    				annotation1 = lt.getAnnotation1();
	    				annotation2 = lt.getAnnotation2();
	    			}
	    			if (lt != null && !lt.isNeedTranslation()) {	// no translation needed
	    				text = lt.getOrigTranslation();
	    			} else {	// get translation from context dictionary
	    				Translation trans = label.getText().getTranslation(dl.getLanguage().getId());
	    				text = (trans == null ? label.getReference() : trans.getTranslation());
	    			}
	    		}
	    		// add leading comments
	    		if (annotation2 != null) {
	    			String[] comments = annotation2.split("\n");
	    			for (String comment : comments) {
	    				eleLabels.addComment(comment);
	    			}
	    		}
	    		// create label
	    		Element eleLabel = eleLabels.addElement("entry");
	    		eleLabel.addAttribute("key", label.getKey());
	    		if (annotation1 != null) {
	    			String[] attributes = annotation1.split("\n");
	    			for (String entry : attributes) {
	    				String[] keyValue = entry.split("=", 2);
	    				if (keyValue.length == 2) {
	    					eleLabel.addAttribute(keyValue[0], keyValue[1]);
	    				}
	    			}
	    		}
	    		eleLabel.addText(text);
	    		if (text.indexOf('\n') != -1) {	// preserve line breaks among the text
		    		eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
	    		}
	    	}
    	}
    	
    	// output
    	String filename = dict.getName();
    	if (dl != null) {
    		int pos = filename.lastIndexOf("en.xml");
    		if (pos != -1) {
    			filename = filename.substring(0, pos) + dl.getLanguageCode() + ".xml";
    		} else {
    			filename += dl.getLanguageCode() + ".xml";
    		}
    	}
    	OutputFormat format = OutputFormat.createPrettyPrint();
    	XMLWriter output = null;
    	try {
    		File targetFile = new File(targetDir, filename);
    		if (!targetFile.getParentFile().exists()) {
    			targetFile.getParentFile().mkdirs();
    		}
    		output = new XMLWriter(new FileWriter(targetFile), format);
    		if (processingInstructions != null) {
    			String[] piList = processingInstructions.split("\n");
    			for (String pi : piList) {
    				String[] keyValue = pi.split("=", 2);
    				if (keyValue.length == 2) {
    					output.processingInstruction(keyValue[0], keyValue[1]);
    				}
    			}
    		}
    		output.write(doc);
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new SystemError(e);
    	} finally {
    		if (output != null) try {output.close();} catch (Exception e) {}
    	}
	}
	
}
