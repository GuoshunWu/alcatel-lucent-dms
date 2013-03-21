package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.*;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.generator.*;
import com.alcatel_lucent.dms.service.generator.xmldict.XMLDictGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

@Service("dictionaryService")
@Scope("singleton")
@SuppressWarnings("unchecked")
public class DictionaryServiceImpl extends BaseServiceImpl implements
        DictionaryService {

    private static Logger log = LoggerFactory.getLogger(DictionaryServiceImpl.class);
    @Autowired
    private TextService textService;
    @Autowired
    private LanguageService langService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private com.alcatel_lucent.dms.service.parser.DictionaryParser[] parsers;
    private Map<Constants.DictionaryFormat, DictionaryGenerator> generators = new HashMap<Constants.DictionaryFormat, DictionaryGenerator>();

    public DictionaryServiceImpl() {
        super();
    }

    @Autowired
    public DictionaryServiceImpl(XMLDictGenerator xmlDictGenerator,
                                 DCTGenerator dctGenerator,
                                 MDCGenerator mdcGenerator,
                                 LabelXMLGenerator labelXMLGenerator,
                                 PropXMLGenerator propXMLGenerator,
                                 PropGenerator propGenerator,
                                 StandardExcelGenerator stdExcelGenerator,
                                 ICEJavaAlarmGenerator iceJavaAlarmGenerator) {
        generators.put(Constants.DictionaryFormat.XDCT, xmlDictGenerator);
        generators.put(Constants.DictionaryFormat.DCT, dctGenerator);
        generators.put(Constants.DictionaryFormat.MDC, mdcGenerator);
        generators.put(Constants.DictionaryFormat.XML_LABEL, labelXMLGenerator);
        generators.put(Constants.DictionaryFormat.XML_PROP, propXMLGenerator);
        generators.put(Constants.DictionaryFormat.TEXT_PROP, propGenerator);
        generators.put(Constants.DictionaryFormat.STD_EXCEL, stdExcelGenerator);
        generators.put(Constants.DictionaryFormat.ICE_JAVA_ALARM, iceJavaAlarmGenerator);
    }

    public Collection<Dictionary> previewDictionaries(String rootDir, File file, Long appId) throws BusinessException {
        Collection<Dictionary> result = new ArrayList<Dictionary>();
        BusinessException exceptions = new BusinessException(BusinessException.PREVIEW_DICT_ERRORS);
//        rootDir = rootDir.replace("\\", "/");
        rootDir = normalize(rootDir, true);
        long before = System.currentTimeMillis();
        HashSet<String> allAcceptedFiles = new HashSet<String>();
        for (com.alcatel_lucent.dms.service.parser.DictionaryParser parser : parsers) {
            Collection<File> acceptedFiles = new ArrayList<File>();
            try {
                result.addAll(parser.parse(rootDir, file, acceptedFiles));
            } catch (BusinessException e) {
                if (e.getErrorCode() == BusinessException.NESTED_ERROR) {
                    for (BusinessException subE : e.getNested()) {
                        exceptions.addNestedException(subE);
                    }
                } else {
                    exceptions.addNestedException(e);
                }
            }
            for (File acceptedFile : acceptedFiles) {
//                String filename = acceptedFile.getAbsolutePath().replace("\\", "/");
                String filename = normalize(acceptedFile.getAbsolutePath(), true);
                log.info("Accepted file: " + filename);
                allAcceptedFiles.add(filename);
            }
        }
        long after = System.currentTimeMillis();
        log.info("**************preview directory '" + file.getAbsolutePath() + "' take " + (after - before)
                + " milliseconds of time.************");
        // check files not accepted
        Collection<String> notAcceptedFiles = getNotAcceptedFiles(file, allAcceptedFiles);
        if (!notAcceptedFiles.isEmpty()) {
            for (String notAcceptedFile : notAcceptedFiles) {
                if (notAcceptedFile.startsWith(rootDir)) {
                    notAcceptedFile = notAcceptedFile.substring(rootDir.length());
                }
                exceptions.addNestedException(new BusinessException(BusinessException.UNRECOGNIZED_DICT_FILE, notAcceptedFile));
            }
        }
        if (exceptions.hasNestedException()) {
            log.error(exceptions.toString());
            throw exceptions;
        }

        // set id, version for preview process
        // if dictionary already exists, get language/charset/label information from existing one
        Application app = (Application) dao.retrieve(Application.class, appId);
        long dictFid = 1;
        for (Dictionary dict : result) {
            Dictionary dbDict = findLatestDictionaryInApp(appId, dict.getName());
            dict.setId(dictFid++);
            dict.setVersion(app.getVersion());
            if (dict.getDictLanguages() != null) {
                long dlFid = 1;
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    dl.setId(dlFid++);
                    if (dbDict != null) {
                        DictionaryLanguage dbDl = dbDict.getDictLanguage(dl.getLanguageCode());
                        if (dbDl != null) {
                            dl.setLanguage(dbDl.getLanguage());
                            dl.setCharset(dbDl.getCharset());
                        }
                    }
                }
            }
            if (dict.getLabels() != null) {
                long labelFid = 1;
                for (Label label : dict.getLabels()) {
                    label.setId(labelFid++);
                    if (dbDict != null) {
                        Label dbLabel = dbDict.getLabel(label.getKey());
                        if (dbLabel != null) {
                            label.setContext(dbLabel.getContext());
                            if (label.getMaxLength() == null) {
                                label.setMaxLength(dbLabel.getMaxLength());
                            }
                            if (label.getDescription() == null) {
                                label.setDescription(dbLabel.getDescription());
                            }
                        }
                    }
                }
            }
        }

        populateDefaultContext(result);

        for (Dictionary dict : result) {
            // populate additional errors and warnings
            dict.validate();
        }

        return result;
    }

    /**
     * Determine default context of labels which have no context info.
     * If the label was already assigned to a context, keep it;
     * If any translation or its status of the label is different than default context, take dictionary context;
     * Otherwise, take default context.
     *
     * @param dictList
     */
    private void populateDefaultContext(Collection<Dictionary> dictList) {
        Context defaultCtx = new Context(Context.DEFAULT);
        Context exclusionCtx = new Context(Context.EXCLUSION);
        Context dbDefaultCtx = textService.getContextByExpression(Context.DEFAULT, null);
        Context dbExclusionCtx = textService.getContextByExpression(Context.EXCLUSION, null);
        Map<String, Text> textMap = dbDefaultCtx == null ? new HashMap<String, Text>() :
                textService.getTextsAsMap(dbDefaultCtx.getId());
        Map<String, Text> exclusionMap = textService.getTextsAsMap(dbExclusionCtx.getId());
        for (Dictionary dict : dictList) {
            Context dictCtx = new Context(Context.DICT);
            if (dict.getLabels() == null) continue;
            for (Label label : dict.getLabels()) {
                if (label.getContext() != null)
                    continue;
                if (exclusionMap.containsKey(label.getReference())) {
                    label.setContext(exclusionCtx);
                    continue;
                }

                // check for each language, if translation in any language is conflict (either translation or status)
                // with Default context, set the label to dictionary context
                if (label.getOrigTranslations() != null) {
                    for (LabelTranslation lt : label.getOrigTranslations()) {
//                        if (lt == null || lt.getOrigTranslation() == null) {
//                            System.out.println("label.key= " + label.getKey());
//                            System.out.println("label translation = " + lt);
//                            System.out.println("label translation original= " + lt.getOrigTranslation());
//                        }
                        if (lt.getLanguage() != null && !lt.getOrigTranslation().equals(label.getReference())) {
                            Text text = textMap.get(label.getReference());
//                        	Text text = textService.getText(dbDefaultCtx.getId(), label.getReference());
                            if (text != null) {
                                Translation trans = text.getTranslation(lt.getLanguage().getId());
                                if (trans != null) {    // compare converted label translation with context translation
                                    String translation = lt.getOrigTranslation();
                                    DictionaryLanguage dl = dict.getDictLanguage(lt.getLanguageCode());
                                    if (dl != null && dl.getCharset() != null && dict.getEncoding() != null) {
                                        try {
                                            translation = new String(translation.getBytes(dict.getEncoding()), dl.getCharset().getName());
                                        } catch (UnsupportedEncodingException e) {
                                            log.error(e.toString());
                                        }
                                    }
                                    if (!trans.getTranslation().equals(translation) ||
                                            lt.getStatus() != null && lt.getStatus() != trans.getStatus()) {
                                        log.debug("Context conflict - Reference:" + label.getReference() + ", Translation:" + lt.getOrigTranslation() + ", ContextTranslation:" + trans.getTranslation());
                                        label.setContext(dictCtx);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (label.getContext() == null) {
                    label.setContext(defaultCtx);
                }

                // temporarily add in-memory LabelTranslations to Default context text map
                // to ensure no conflict translation in scope of current delivery are put into DEFAULT context
//                if (label.getContext().getName().equals(Context.DEFAULT)) {
//                    Text text = textMap.get(label.getReference());
//                    if (text == null) {
//                        text = new Text();
//                        text.setReference(label.getReference());
//                        textMap.put(label.getReference(), text);
//                    }
//                    if (label.getOrigTranslations() != null) {
//                        for (LabelTranslation lt : label.getOrigTranslations()) {
//                            if (lt.getLanguage() != null && !lt.getOrigTranslation().equals(label.getReference())) {
//                                Translation trans = text.getTranslation(lt.getLanguage().getId());
//                                if (trans == null) {
//                                    trans = new Translation();
//                                    String translation = lt.getOrigTranslation();
//                                    DictionaryLanguage dl = dict.getDictLanguage(lt.getLanguageCode());
//                                    if (dl != null && dl.getCharset() != null && dict.getEncoding() != null) {
//                                        try {
//                                            translation = new String(translation.getBytes(dict.getEncoding()), dl.getCharset().getName());
//                                        } catch (UnsupportedEncodingException e) {
//                                            log.error(e.toString());
//                                        }
//                                    }
//                                    trans.setLanguage(lt.getLanguage());
//                                    trans.setTranslation(translation);
//                                    text.addTranslation(trans);
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }

    }

    private Collection<String> getNotAcceptedFiles(File file,
                                                   HashSet<String> allAcceptedFiles) {
        Collection<String> result = new ArrayList<String>();
        if (!file.exists()) return result;
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (File subFile : subFiles) {
                result.addAll(getNotAcceptedFiles(subFile, allAcceptedFiles));
            }
        } else {
            String filename = file.getAbsolutePath().replace("\\", "/");
            if (!allAcceptedFiles.contains(filename)) {
                result.add(filename);
            }
        }
        return result;
    }

    /**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir   root directory save dct files
     * @param dtIds the collection of the id for dictionary to be generated.
     */
    public void generateDictFiles(String dir, Collection<Long> dtIds) {
        ProgressQueue.setProgress("Preparing data...", -1);
        if (dtIds.isEmpty()) return;
        File target = new File(dir);
        if (target.exists()) {
            if (target.isFile()) {
                throw new BusinessException(BusinessException.TARGET_IS_NOT_DIRECTORY, dir);
            }
        } else {
            if (!target.mkdirs()) {
                throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, dir);
            }
        }
        String idList = dtIds.toString().replace("[", "(").replace("]", ")");
        String hsql = "from Dictionary where id in " + idList;
        Collection<Dictionary> allDicts = (Collection<Dictionary>) getDao().retrieve(hsql);
        // group dictionaries by format
        Map<String, Collection<Dictionary>> formatGroup = new HashMap<String, Collection<Dictionary>>();
        for (Dictionary dict : allDicts) {
            Collection<Dictionary> dicts = formatGroup.get(dict.getFormat());
            if (dicts == null) {
                dicts = new ArrayList<Dictionary>();
                formatGroup.put(dict.getFormat(), dicts);
            }
            dicts.add(dict);
        }
        for (String format : formatGroup.keySet()) {
            DictionaryGenerator generator = getGenerator(format);
            generator.generateDict(target, formatGroup.get(format));
        }
    }

    private DictionaryGenerator getGenerator(String format) {
        DictionaryGenerator generator = generators.get(Constants.DictionaryFormat.getEnum(format));
        if (null == generator) throw new SystemError("Unsupported dict format: " + format);
        return generator;
    }

    @Override
    public Dictionary importDictionary(Long appId, Dictionary dict, String version, Constants.ImportingMode mode, String[] langCodes,
                                       Map<String, String> langCharset,
                                       Collection<BusinessWarning> warnings, DeliveryReport report) {
        log.info("Start importing dictionary in " + mode + " mode");
        if (null == dict)
            return null;

        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dict.getName());
        User user = UserContext.getInstance().getUser();
        // check langCodes parameter
        if (langCodes != null) {
            Collection<String> dictLangCodes = dict.getAllLanguageCodes();
            List<String> listLangCodes = new ArrayList<String>(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
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
        if (null == baseDBDict) {
            if (mode == Constants.ImportingMode.TRANSLATION) {
                throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dict.getName());
            }
            baseDBDict = new DictionaryBase();
            baseDBDict.setName(dict.getName());
            baseDBDict.setFormat(dict.getFormat());
            baseDBDict.setEncoding(dict.getEncoding());
            baseDBDict.setPath(dict.getPath());
            baseDBDict.setApplicationBase(app.getBase());
            baseDBDict.setOwner(user);
            baseDBDict = (DictionaryBase) getDao().create(baseDBDict);
        } else {
            if (mode == Constants.ImportingMode.DELIVERY) {
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
            if (mode == Constants.ImportingMode.TRANSLATION) {
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
            dbDict.setLocked(false);
            dbDict = (Dictionary) getDao().create(dbDict);
            if (mode == Constants.ImportingMode.DELIVERY) {    // in case new dictionary version, compare with latest version
                lastDict = getLatestDictionary(baseDBDict.getId(), dbDict.getId());
            }
        } else {
            if (mode == Constants.ImportingMode.DELIVERY) {    // in case existing dictionary version, compare with the same version
                lastDict = dbDict;
                dbDict.setAnnotation1(dict.getAnnotation1());
                dbDict.setAnnotation2(dict.getAnnotation2());
                dbDict.setAnnotation3(dict.getAnnotation3());
                dbDict.setAnnotation4(dict.getAnnotation4());
            }
        }

        // make sure the dictionary is added to the application
        Collection<Dictionary> dictionaries = app.getDictionaries();
        if (null == dictionaries) {
            dictionaries = new HashSet<Dictionary>();
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

        // update dictionary languages in delivery mode
        if (mode == Constants.ImportingMode.DELIVERY) {
            if (dbDict.getDictLanguages() != null) {    //remove all existing dict languages
                for (DictionaryLanguage dl : dbDict.getDictLanguages()) {
                    dao.delete(dl);
                }
            }
            for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
                String uniLangCode = getUnifiedLangCode(dictLanguage.getLanguageCode());
                if (langCodeList != null && !langCodeList.contains(uniLangCode)) {
                    continue;
                }
                String charsetName = null;
                if (dictLanguage.getCharset() != null) {
                    charsetName = dictLanguage.getCharset().getName();
                }
                if (charsetName == null) {
                    charsetName = langCharset.get(uniLangCode);
                    if (charsetName == null) {
                        charsetName = langCharset.get("DEFAULT");
                    }
                }
                if (null == charsetName) {
                    nonBreakExceptions.addNestedException(new BusinessException(
                            BusinessException.CHARSET_NOT_DEFINED, dictLanguage.getLanguageCode()));
                } else {
                    dictLanguage.setCharset(langService.getCharset(charsetName));
                }

                dictLanguage.setDictionary(dbDict);
                dictLanguage.setLanguage((Language) dao.retrieve(Language.class,
                        dictLanguage.getLanguage().getId()));
                dao.create(dictLanguage);
            }
        }

        // prepare data for creation: textMap, labelMap indexed by context
        log.info("Prepare data to import");
        ProgressQueue.setProgress(20);
        Map<String, Collection<Text>> textMap = new HashMap<String, Collection<Text>>();
        Map<String, Collection<Label>> labelMap = new HashMap<String, Collection<Label>>();
        Map<Long, String> langCodeMap = dict.getLangCodeMap();
//        Map<Long, String> langCodeMap = new HashMap<Long, String>();
//        for (DictionaryLanguage dl : dict.getDictLanguages()) {
//            langCodeMap.put(dl.getLanguage().getId(), dl.getLanguageCode()) ;
//        }
        int sortNo = 1;
        for (Label label : dict.getLabels()) {
            label.setSortNo(sortNo++);
            label.setRemoved(false);
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
                    // determine charset, first take value from DictionaryLanguage
                    // if not specified in DictionaryLanguage, then take value from langCharset parameter
                    trans.setLabel(label);
                    String langCode = langCodeMap.get(trans.getLanguage().getId());
                    DictionaryLanguage dl = dict.getDictLanguage(langCode);
                    String charsetName = dl.getCharset().getName();
                    boolean invalidText = false;
                    try {
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
//                            invalidText = true;
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
                    if (invalidText) {
                        t.setStatus(Translation.STATUS_UNTRANSLATED);
                    } else {
                        t.setStatus(populateTranslationStatus(trans));
                    }
                    text.addTranslation(t);
                } //for
            }
        }

        ProgressQueue.setProgress(50);
        // for each context, insert or update label/text/translation data
        for (String contextName : textMap.keySet()) {
            log.info("Importing data into context " + contextName);
            Context context = textService.getContextByExpression(contextName, dbDict);
            Collection<Text> texts = textMap.get(contextName);
            Collection<Label> labels = labelMap.get(contextName);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    context.getId(), texts, mode);

            // in TRANSLATION_MODE, no change to label
            if (mode == Constants.ImportingMode.TRANSLATION) {
                continue;
            }

            // NOTE: following code is only executed in DELIVERY_MODE
            for (Label label : labels) {
                // create or update label
                Label dbLabel = dbDict.getLabel(label.getKey());
                boolean newLabel = true;
                if (dbLabel == null) {
                    label.setDictionary(dbDict);
                    label.setContext(context);
                    label.setText(dbTextMap.get(label.getReference()));
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
                    dbLabel.setSortNo(label.getSortNo());
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
                            dbLabelTrans.setRequestTranslation(trans.getRequestTranslation());
                            dbLabelTrans.setLanguageCode(trans.getLanguageCode());
                            dbLabelTrans.setSortNo(trans.getSortNo());
                        }
                    }
                }
            }
            report.addData(context, dict, labels, dbTextMap);
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }
        historyService.logDelivery(dbDict, dbDict.getPath());
        log.info("Import dictionary finish");
        ProgressQueue.setProgress(80);
        return dbDict;
    }

    private int populateTranslationStatus(LabelTranslation trans) {
        Label label = trans.getLabel();
        if (trans.getStatus() != null) {    // status information is specified in dictionary
            return trans.getStatus();
        } else if (label.getReference().trim().isEmpty()) {
            return Translation.STATUS_TRANSLATED;
//        } else if (trans.getRequestTranslation() != null) {
//        	t.setStatus(trans.getRequestTranslation() ? Translation.STATUS_UNTRANSLATED : Translation.STATUS_TRANSLATED);
        } else if (!trans.isNeedTranslation()) {
            return Translation.STATUS_TRANSLATED;
        } else if (label.getReference().equals(trans.getOrigTranslation())) {
            return Translation.STATUS_UNTRANSLATED;
        } else {
            return Translation.STATUS_TRANSLATED;
        }
    }

    public DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, Constants.ImportingMode mode) throws BusinessException {
        DeliveryReport report = new DeliveryReport();
        Map<String, Collection<BusinessWarning>> warningMap = new TreeMap<String, Collection<BusinessWarning>>();
        for (Dictionary dict : dictList) {
            Map<String, String> langCharset = new HashMap<String, String>();
            if (dict.getDictLanguages() != null) {
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
                }
            }
            Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
            importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, warnings, report);
            warningMap.put(dict.getName(), warnings);
        }
        report.setWarningMap(warningMap);
        log.info(report.toString());
        return report;
    }

    private String getUnifiedLangCode(String langCode) {
        return langCode.toUpperCase().replace("_", "-");
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

    public Dictionary findLatestDictionaryInApp(Long appId, String dictionaryName) {
        String hql = "select obj from Application app join app.base.dictionaryBases db join db.dictionaries obj" +
                " where app.id=:appId and db.name=:name order by obj.id desc";
        Map param = new HashMap();
        param.put("appId", appId);
        param.put("name", dictionaryName);
        Dictionary dict = (Dictionary) dao.retrieveOne(hql, param);
        if (dict != null && dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                Hibernate.initialize(dl);
            }
        }
        return dict;
    }

    public void removeDictionaryFromApplication(Long appId, Long dictId) {
        Application app = (Application) dao.retrieve(Application.class, appId);
        app.removeDictionary(dictId);

        String hql = "select distinct a from Application a join a.dictionaries as d where d.id=:id";
        Map param = new HashMap();
        param.put("id", dictId);
        Collection<Application> apps = dao.retrieve(hql, param);
        if (apps.isEmpty()) {
            Dictionary dictionary = (Dictionary) dao.retrieve(Dictionary.class, dictId);
            if (dictionary.getHistories() != null) {
                for (DictionaryHistory his : dictionary.getHistories()) {
                    dao.delete(his);
                }
            }
            DictionaryBase dictBase = dictionary.getBase();
            dao.delete(Dictionary.class, dictId);

            // delete dictBase if it doesn't contain other dictionary
            if (dictBase.getDictionaries() == null || dictBase.getDictionaries().size() == 0 ||
                    dictBase.getDictionaries().size() == 1 && dictBase.getDictionaries().iterator().next().getId().equals(dictId)) {
                dao.delete(dictBase);
            }
        }
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
        if (dictionary.getHistories() != null) {
            for (DictionaryHistory his : dictionary.getHistories()) {
                dao.delete(his);
            }
        }
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

    public int getLabelNumByApp(Long appId) {
        String hql = "select count(l) from Application app join app.dictionaries d join d.labels l where app.id=:appId and l.removed=false";
        Map param = new HashMap();
        param.put("appId", appId);
        Number count = (Number) dao.retrieveOne(hql, param);
        return count == null ? 0 : count.intValue();
    }

    public void updateDictionaryFormat(Long id, String format) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
        if (!Constants.DictionaryFormat.isValid(format)) {
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

    public void changeDictionaryInApp(Long appId, Long newDictId) throws BusinessException {
        Application app = (Application) dao.retrieve(Application.class, appId);
        Dictionary newDict = (Dictionary) dao.retrieve(Dictionary.class, newDictId);
        Long baseDictId = newDict.getBase().getId();
        Collection<Dictionary> dictionaries = app.getDictionaries();
        boolean found = false;
        if (dictionaries != null) {
            Iterator<Dictionary> iterator = dictionaries.iterator();
            while (iterator.hasNext()) {
                Dictionary dict = iterator.next();
                if (dict.getBase().getId().equals(baseDictId)) {
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

    public void updateLabelKey(Long labelId, String key) throws BusinessException {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        if (label.getKey().equals(key)) return;
        Label existing = label.getDictionary().getLabel(key);
        if (existing != null) {
            throw new BusinessException(BusinessException.DUPLICATE_LABEL_KEY);
        }
        label.setKey(key);
    }

    public void updateLabelReference(Long labelId, String reference) {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        if (label.getReference().equals(reference)) return;
        label.setReference(reference);
        // re-associate text unless EXCLUSION context
        if (!label.getContext().getName().equals(Context.EXCLUSION)) {
            Text text = textService.getText(label.getContext().getId(), reference);
            if (text == null) {
                text = textService.addText(label.getContext().getId(), reference);
            }
            label.setText(text);
        }
        // reset LabelTranslation objects
        if (label.getOrigTranslations() != null) {
            for (LabelTranslation lt : label.getOrigTranslations()) {
                lt.setOrigTranslation(reference);
                lt.setNeedTranslation(true);
                lt.setRequestTranslation(null);
            }
        }
    }

    public void updateLabels(Collection<Long> idList, String maxLength,
                             String description, String contextExp) {
        Context ctx = null;
        Collection<Label> labelsToChangeContext = new ArrayList<Label>();
        for (Long id : idList) {
            Label label = (Label) dao.retrieve(Label.class, id);
            if (maxLength != null) {
                label.setMaxLength(maxLength);
            }
            if (description != null) {
                label.setDescription(description);
            }
            if (contextExp != null) {
                if (ctx == null) {
                    ctx = textService.getContextByExpression(contextExp, label.getDictionary());
                }
                if (!label.getContext().getId().equals(ctx.getId())) {
                    // context changed
                    labelsToChangeContext.add(label);
                }
            }
        }
        if (!labelsToChangeContext.isEmpty()) {
            updateLabelContext(ctx, labelsToChangeContext);
        }
    }

    private void updateLabelContext(Context context, Collection<Label> labels) {
        Collection<Text> texts = new ArrayList<Text>();
        for (Label label : labels) {
            label.setContext(context);
            Text text = new Text();
            text.setReference(label.getReference());
            if (label.getOrigTranslations() != null) {
                for (LabelTranslation lt : label.getOrigTranslations()) {
                    Translation trans = new Translation();
                    trans.setTranslation(lt.getOrigTranslation());
                    trans.setLanguage(lt.getLanguage());
                    trans.setStatus(populateTranslationStatus(lt));
                    text.addTranslation(trans);
                }
            }
            texts.add(text);
        }
        Map<String, Text> textMap = textService.updateTranslations(context.getId(), texts, Constants.ImportingMode.DELIVERY);
        for (Label label : labels) {
            label.setText(textMap.get(label.getReference()));
        }
    }

    public void deleteLabels(Collection<Long> labelIds) {
        for (Long id : labelIds) {
            Label label = (Label) dao.retrieve(Label.class, id);
            label.setRemoved(true);
        }
    }

    public Label addLabel(Long dictId, String key, String reference, String maxLength, String contextExp, String description) {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        if (dict.getLabel(key) != null) {
            throw new BusinessException(BusinessException.DUPLICATE_LABEL_KEY);
        }
        Context context = textService.getContextByExpression(contextExp, dict);
        Text text = textService.getText(context.getId(), reference);
        if (text == null) {
            text = textService.addText(context.getId(), reference);
        }
        Label label = new Label();
        label.setDictionary(dict);
        label.setKey(key);
        label.setReference(reference);
        label.setMaxLength(maxLength);
        label.setContext(context);
        label.setDescription(description);
        label.setText(text);
        label.setSortNo(dict.getMaxSortNo() + 1);
        label.setRemoved(false);
        label = (Label) dao.create(label);
        return label;
    }
}
