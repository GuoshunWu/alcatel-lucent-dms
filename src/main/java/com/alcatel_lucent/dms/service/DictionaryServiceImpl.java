package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.*;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.action.app.CapitalizeAction;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.service.generator.GeneratorSettings;
import com.alcatel_lucent.dms.service.parser.DictionaryParser;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import views.JQGrid;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;

import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.CollectionUtils.subtract;
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
    private DaoService dao;

    @Autowired
    private GlossaryService glossaryService;

    @Autowired
    private List<DictionaryParser> parsers;
    private Map<String, DictionaryGenerator> generators;

    private Transformer file2NormalizedNameTransformer = new Transformer() {
        @Override
        public Object transform(Object input) {
            File f = (File) input;
            return normalize(f.getAbsolutePath());
        }
    };

    private Transformer name2FileTransformer = new Transformer() {
        @Override
        public Object transform(Object input) {
            return new File((String) input);
        }
    };


    public DictionaryServiceImpl() {
    }

    @Autowired
    public void setGenerators(Map<String, DictionaryGenerator> generators) {
        this.generators = generators;

        Map<String, DictionaryGenerator> tmpGeneratorMap = new HashMap<String, DictionaryGenerator>();

        Collection<DictionaryGenerator> tmpGenerators = generators.values();
        for (DictionaryGenerator generator : tmpGenerators) {
            tmpGeneratorMap.put(generator.getFormat().toString(), generator);
        }
        this.generators = tmpGeneratorMap;
    }

    public Collection<Dictionary> previewDictionaries(String rootDir, File file, Long appId) throws BusinessException {
        Collection<Dictionary> result = new ArrayList<Dictionary>();
        BusinessException exceptions = new BusinessException(BusinessException.PREVIEW_DICT_ERRORS);
        rootDir = normalize(rootDir, true);
        long before = System.currentTimeMillis();
        HashSet<File> allAcceptedFiles = new HashSet<File>();
        for (DictionaryParser parser : parsers) {
            ProgressQueue.setProgress("" + allAcceptedFiles.size() + " file(s) were accepted. Scanning for " + parser.getFormat().toString() + "...", -1);
            Collection<File> acceptedFiles = new ArrayList<File>();
            try {
                result.addAll(parser.parse(rootDir, file, acceptedFiles));
                // remove accepted file to avoid repeatedly parse
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
                String filename = normalize(acceptedFile.getAbsolutePath(), true);
                log.info("Accepted file: " + filename);
                allAcceptedFiles.add(acceptedFile);
            }
        }
        long after = System.currentTimeMillis();
        log.info("**************preview directory '" + file.getAbsolutePath() + "' take " + (after - before)
                + " milliseconds of time.************");
        // check files not accepted
//        Collection<File> notAcceptedFiles = FileUtils.listFiles(file, null, true);
        Collection<File> files = FileUtils.listFiles(file, null, true);
        Collection<String> strNotAcceptedFiles = subtract(collect(files, file2NormalizedNameTransformer), collect(allAcceptedFiles, file2NormalizedNameTransformer));
        Collection<File> notAcceptedFiles = collect(strNotAcceptedFiles, name2FileTransformer);

//      getNotAcceptedFiles(file, allAcceptedFiles);

        if (!notAcceptedFiles.isEmpty()) {
            String normalRoot = FilenameUtils.normalize(rootDir, true);
            for (File notAcceptedFile : notAcceptedFiles) {
                String fName = FilenameUtils.normalize(notAcceptedFile.getAbsolutePath(), true);
                if (fName.startsWith(normalRoot)) {
                    fName = fName.substring(normalRoot.length() + 1);
                }
                exceptions.addNestedException(new BusinessException(BusinessException.UNRECOGNIZED_DICT_FILE, fName));
            }
        }
        if (exceptions.hasNestedException()) {
            log.error(exceptions.toString());
            throw exceptions;
        }

        ProgressQueue.setProgress("Initializing...", -1);
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
                            if (dl.getCharset() == null) {
                                dl.setCharset(dbDl.getCharset());
                            }
                            if (dl.getLanguage() == null) {
                                dl.setLanguage(dbDl.getLanguage());
                                dict.updateLanguage(dl.getLanguageCode(), dbDl.getLanguage());
                            }
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
                            // copy context value from existing label unless:
                            // * reference text was changed
                            // * the existing context value is [DEFAULT] or [DICT] or [LABEL]
                            if (label.getContext() == null && label.getReference().equals(dbLabel.getReference())
                                    && !dbLabel.getContext().getName().equals(Context.DEFAULT)
                                    && !dbLabel.getContext().getName().equals(Context.DICT)
                                    && !dbLabel.getContext().getName().equals(Context.LABEL)) {
                                label.setContext(dbLabel.getContext());
                            }
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

        // replace glossary before populating context
        for (Dictionary dict : result) {
            glossaryService.consistentGlossariesInDict(dict);
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
    public void populateDefaultContext(Collection<Dictionary> dictList) {
        Context defaultCtx = new Context(Context.DEFAULT);
        Context exclusionCtx = new Context(Context.EXCLUSION);
        Context dbDefaultCtx = textService.getContextByExpression(Context.DEFAULT, (Dictionary) null);
//        Context dbExclusionCtx = textService.getContextByExpression(Context.EXCLUSION, null);
        Collection<String> references = new ArrayList<String>();
        for (Dictionary dict : dictList) {
            if (dict.getLabels() == null) continue;
            for (Label label : dict.getLabels()) {
                references.add(label.getReference());
            }
        }
        MultiKeyMap translationMap = dbDefaultCtx == null ? new MultiKeyMap() :
                textService.getTranslationsAsMap(dbDefaultCtx.getId(), references);
        references = null;    // release memory
//        Map<String, Text> exclusionMap = textService.getTextsAsMap(dbExclusionCtx.getId());
        Collection<Glossary> glossaryObjects = glossaryService.getNotDirtyGlossaries();
        HashSet<String> glossaries = new HashSet<String>();
        for (Glossary glossary : glossaryObjects) {
            if (!glossary.getTranslate()) {
                glossaries.add(glossary.getText());
            }
        }
        MultiKeyMap unsavedTranslationMap = new MultiKeyMap();
        // save dict context text map
        MultiKeyMap dictTranslationMap = new MultiKeyMap();

        for (Dictionary dict : dictList) {
            boolean langError = false;
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (dl.getLanguage() == null || dl.getCharset() == null) {
                    langError = true;
                    break;
                }
            }
            if (langError) continue; // don't populate context if language information is not complete
            Context dictCtx = new Context(Context.DICT);

            if (dict.getLabels() == null) continue;
            for (Label label : dict.getLabels()) {
                if (label.getContext() != null) {
                    continue;
                }
                if (glossaries.contains(label.getReference())) {
                    label.setContext(exclusionCtx);
                    continue;
                }
                if (StringUtils.isBlank(label.getReference())) {
                    label.setContext(exclusionCtx);
                    continue;
                }

                // if contained label translation is empty and status is T, force context to be LABEL
                Context labelCtx = new Context(textService.populateContextKey(Context.LABEL, label), Context.LABEL);
                if (label.getOrigTranslations() != null) {
                    for (LabelTranslation lt : label.getOrigTranslations()) {
                        if (lt.getStatus() != null && lt.getStatus() == Translation.STATUS_TRANSLATED
                                && StringUtils.isBlank(lt.getOrigTranslation())) {
                            label.setContext(labelCtx);
                            break;
                        }
                    }
                }
                if (label.getContext() != null) continue;

                // check for each language, if translation in any language is conflict (either translation or status)
                // with Default context, set the label to dictionary context
                if (isConflict(dict, label, translationMap) || isConflict(dict, label, unsavedTranslationMap)) {
                    if (isConflict(dict, label, dictTranslationMap)) {
                        label.setContext(labelCtx);
                    } else {
                        label.setContext(dictCtx);
                        updateTranslationMap(dict, label, dictTranslationMap);
                    }
                    continue;
                }
                if (label.getContext() == null) {
                    label.setContext(defaultCtx);

                    // temporarily add in-memory LabelTranslations to unsaved Default context text map
                    // to ensure no conflict translation in scope of current delivery are put into DEFAULT context
                    updateTranslationMap(dict, label, unsavedTranslationMap);
                }
            }
        }
    }

    /**
     * Populate text for label
     *
     * @param label Label
     */
    private void updateTranslationMap(Dictionary dict, Label label, MultiKeyMap translationMap) {

        if (CollectionUtils.isEmpty(label.getOrigTranslations())) {
            return;
        }

        // add Translations for text
        for (LabelTranslation lt : label.getOrigTranslations()) {
            if (lt.getLabel() == null) lt.setLabel(label);
            //skip if original translation equals reference
            if (null == lt.getLanguage() || lt.getOrigTranslation().equals(label.getReference())) continue;

            Translation trans = (Translation) translationMap.get(label.getReference(), lt.getLanguage().getId());
            String translation = lt.getOrigTranslation();
            DictionaryLanguage dl = dict.getDictLanguage(lt.getLanguageCode());
            if (dl != null && dl.getCharset() != null && dict.getEncoding() != null) {
                try {
                    translation = new String(translation.getBytes(dict.getEncoding()), dl.getCharset().getName());
                } catch (UnsupportedEncodingException e) {
                    log.error(e.toString());
                }
            }
            if (trans == null) {
                trans = new Translation();
                trans.setLanguage(lt.getLanguage());
                trans.setTranslation(translation);
                trans.setStatus(populateTranslationStatus(lt));
                translationMap.put(label.getReference(), lt.getLanguage().getId(), trans);
            } else if (trans.getTranslation().equals(label.getReference()) && !translation.equals(label.getReference())) { //?
                trans.setTranslation(translation);
            }
        }
    }

    /**
     * check for each language, if translation in any language is conflict (either translation or status)
     * with Default context, set the label to dictionary context
     *
     * @param dict
     * @param label
     * @param translationMap all the texts in DB with reference as key, text as value.
     * @return
     */
    private boolean isConflict(Dictionary dict, Label label, MultiKeyMap translationMap) {
        if (null == label.getOrigTranslations()) return false;

        for (LabelTranslation lt : label.getOrigTranslations()) {
            if (lt.getLanguage() == null ||
                    lt.getOrigTranslation().equals(label.getReference())
                            && (lt.getStatus() == null || lt.getStatus() != Translation.STATUS_TRANSLATED)) continue;
            Translation trans = (Translation) translationMap.get(label.getReference(), lt.getLanguage().getId());
            if (null == trans) continue;
            // compare converted label translation with context translation
            String translation = lt.getOrigTranslation();
            DictionaryLanguage dl = dict.getDictLanguage(lt.getLanguageCode());

            if (dl != null && dl.getCharset() != null && dict.getEncoding() != null) {
                try {
                    translation = new String(translation.getBytes(dict.getEncoding()), dl.getCharset().getName());
                } catch (UnsupportedEncodingException e) {
                    log.error(e.toString());
                }
            }
            if ((!trans.getTranslation().equals(translation) ||
                    lt.getStatus() != null && lt.getStatus() != trans.getStatus())
                    && trans.getStatus() == Translation.STATUS_TRANSLATED) {
                log.info("Context conflict - Reference:" + label.getReference() + ", Translation:" + lt.getOrigTranslation() + ", ContextTranslation:" + trans.getTranslation());
                return true;
            }
        }
        return false;
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
        generateDictFiles(dir, dtIds, new GeneratorSettings());
    }

    public void generateDictFiles(String dir, Collection<Long> dtIds, GeneratorSettings settings) {
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
        MultiValueMap<String, Dictionary> formatGroup = new LinkedMultiValueMap<String, Dictionary>();
        for (Dictionary dict : allDicts) {
            formatGroup.add(dict.getFormat(), dict);
        }
        for (String format : formatGroup.keySet()) {
            DictionaryGenerator generator = getGenerator(format);
            generator.generateDict(target, formatGroup.get(format), settings);
        }
    }

    private DictionaryGenerator getGenerator(String format) {
        DictionaryGenerator generator = generators.get(format);
        if (null == generator) throw new SystemError("Unsupported dict format: " + format);
        return generator;
    }

    @Override
    public Dictionary importDictionary(Long appId, Dictionary dict, String version, Constants.ImportingMode mode, String[] langCodes,
                                       Map<String, String> langCharset, ImportSettings settings,
                                       Collection<BusinessWarning> warnings, DeliveryReport report) {
        log.info("Start importing dictionary in " + mode + " mode");
        if (null == dict) return null;

        if (mode == Constants.ImportingMode.TEST) {
            dao.setRollbackOnly();
            mode = Constants.ImportingMode.DELIVERY;
        }
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
            dbDict.setReferenceLanguage(dict.getReferenceLanguage());
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
                dbDict.setReferenceLanguage(dict.getReferenceLanguage());
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

        for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {    // make sure dl.dictionary is not null
            if (dictLanguage.getDictionary() == null) dictLanguage.setDictionary(dict);
        }
        // update dictionary languages in delivery mode
        boolean noLanguage = dict.hasNoLanguage();    // whether the imported dictionary contains no language other than ref language
        boolean needAppendLanguage = settings.isAutoCreateLang() && noLanguage;
        if (mode == Constants.ImportingMode.DELIVERY) {
            if (needAppendLanguage) {    // GAE-only dictionary and need auto creating languages
                // if the dictionary is new, copy languages from any other existing dictionary in the application
                if (dbDict.hasNoLanguage()) {
                    Dictionary anotherDict = findDictionaryForAppendingLanguage(appId);
                    if (anotherDict != null) {
                        for (DictionaryLanguage dictLanguage : anotherDict.getDictLanguages()) {
                            if (dbDict.getDictLanguage(dictLanguage.getLanguageCode()) != null) continue;
                            DictionaryLanguage dl = new DictionaryLanguage();
                            dl.setLanguageCode(dictLanguage.getLanguageCode());
                            dl.setLanguage(dictLanguage.getLanguage());
                            dl.setCharset(dictLanguage.getCharset());
                            dl.setSortNo(dictLanguage.getSortNo());
                            dl.setDictionary(dbDict);
                            dl = (DictionaryLanguage) dao.create(dl);
                            dict.addDictLanguage(dl);
                        }
                    }
                } else {    // if the dictionary already exists, do nothing change on the languages
                    for (DictionaryLanguage dl : dbDict.getDictLanguages()) {
                        if (!dl.isReference()) {
                            dict.addDictLanguage(dl);
                        }
                    }
                }
            } else {    // normal case
                // remove all existing dict languages
                if (dbDict.getDictLanguages() != null) {
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
        }

        // prepare data for creation: textMap, labelMap indexed by context
        log.info("Prepare data to import");
        ProgressQueue.setProgress(10);
        // context key
        MultiValueMap<String, Text> textMap = new LinkedMultiValueMap<String, Text>();
        MultiValueMap<String, Label> labelMap = new LinkedMultiValueMap<String, Label>();

        Map<Long, String> langCodeMap = dict.getLangCodeMap();
//        Map<Long, String> langCodeMap = new HashMap<Long, String>();
//        for (DictionaryLanguage dl : dict.getDictLanguages()) {
//            langCodeMap.put(dl.getLanguage().getId(), dl.getLanguageCode()) ;
//        }
        Context defaultCtx = textService.getContextByExpression(Context.DEFAULT, (Dictionary) null);
        int sortNo = 1;
        int diffLabel = 0;            // number of new or changed label
        int diffTranslation = 0;    // number of different translations in dictionary
        int diffTranslated = 0;        // number of newly translated strings in dictionary
        Collection<Label> labelsToCreate = new ArrayList<Label>();
        Collection<LabelTranslation> labelTranslationsToCreate = new ArrayList<LabelTranslation>();

        // populate context
        for (Label label : dict.getLabels()) {
            Context ctx = label.getContext();
            String contextName = label.getContext().getName();
            if (Context.LABEL.equals(contextName)) {// distinct label context
                String actualContextName = textService.populateContextKey(Context.LABEL, label);
                // create context first, and then texts in LABEL context will be processed separately
                String contextKey = textService.getContextKeyByExpressionForLabel(actualContextName, dbDict.getId());
                ctx = textService.getContextByKey(contextKey);
                if (ctx == null) {
                    ctx = textService.createContext(Context.LABEL, contextKey);
                }
            } else {
                ctx = textService.getContextByExpression(contextName, dbDict);
            }
            label.setContext(ctx);
        }

        HashSet<String> labelKeys = new HashSet<String>();
        for (Label label : dict.getLabels()) {
            Context ctx = label.getContext();
            label.setSortNo(sortNo++);
            label.setRemoved(false);
            labelKeys.add(label.getKey());
            Label lastLabel = null;
            if (lastDict != null) {
                lastLabel = lastDict.getLabel(label.getKey());
            }

            Label dbLabel = dbDict.getLabel(label.getKey());
            if (dbLabel == null) {    // ready for creation
                label.setId(null);
                label.setDictionary(dbDict);
                labelsToCreate.add(label);
                dbLabel = label;
                dbDict.addLabel(dbLabel);
            	diffLabel++;
                // make sure all LabelTranslation objects under the new label are marked as NEW
                if (label.getOrigTranslations() != null) {
                    for (LabelTranslation trans : label.getOrigTranslations()) {
                        trans.setId(null);
                    }
                }
            } else if (lastLabel != null && !lastLabel.getReference().equals(label.getReference())) {
            	diffLabel++;
            }

            Text text = new Text();
            text.setContext(label.getContext());
            text.setRefLabel(dbLabel);
            text.setReference(label.getReference());
            textMap.add(ctx.getName(), text);
            labelMap.add(ctx.getName(), label);

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

            String labelWarnings = null;
            // for each translation
            // convert charset of translation strings
            // determine translation behaviors
            if (label.getOrigTranslations() != null) {
                for (LabelTranslation trans : label.getOrigTranslations()) {
                    // determine charset, first take value from DictionaryLanguage
                    // if not specified in DictionaryLanguage, then take value from langCharset parameter
                    trans.setLabel(dbLabel);
                    String langCode = trans.getLanguageCode();
                    DictionaryLanguage dl = dict.getDictLanguage(langCode);
                    trans.setLanguage(dl.getLanguage());    // update language in LabelTranslation by dl
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

                    // count diff translation
                    String currentTranslation = lastLabel == null ? label.getReference() : lastLabel.getTranslation(langCode);
                    boolean diff = false;
                    if (!currentTranslation.equals(trans.getOrigTranslation())) {
                        diffTranslation++;
                        diff = true;
                    }

                    // read needTranslation flag from parser
                    // trans.setNeedTranslation(true);

                    if (lastLabel != null && ctx.getKey().equals(lastLabel.getContext().getKey())) {
                        // get the original translation in latest version
                        LabelTranslation lastTranslation = lastLabel.getOrigTranslation(trans.getLanguageCode());
                        if (label.getReference().equals(lastLabel.getReference())) {    // reference is not changed
/*
                            if (lastTranslation != null &&
                                    !lastTranslation.getOrigTranslation().equals(trans.getOrigTranslation()) &&
                                    !trans.getOrigTranslation().equals(label.getReference())) {
                                // translation changed means the label was translated on developer side
                                trans.setNeedTranslation(false);
                            }
*/
                            // don't update context translation if both translation and translation is not changed in the delivered dictionary
                            // unless status is specified in dict because we don't know if status is changed in dict
                            if (lastTranslation != null
                                    && lastTranslation.getOrigTranslation().equals(trans.getOrigTranslation())
                                    && trans.getStatus() == null) {
                                continue;    // don't add this labelTranslation into text.translations
                            }
                        } else {    // reference is changed
                            // don't update context translation if translation is not changed in the delivered dictionary
                            if (lastTranslation != null
                                    && lastTranslation.getOrigTranslation().equals(trans.getOrigTranslation())) {
                                continue;    // don't add this labelTranslation into text.translations
                            }
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

                    // count diff translated
                    if (diff && t.getStatus() == Translation.STATUS_TRANSLATED) {
                        diffTranslated++;
                    }

                } //for each labelTranslation
            }

            // align translations with dictLanguages in purpose of getting translation suggestion
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (!dl.isReference() && label.getOrigTranslation(dl.getLanguageCode()) == null) {
                    Translation t = new Translation();
                    t.setText(text);
                    t.setTranslation(label.getReference());
                    t.setLanguage(dl.getLanguage());
                    t.setStatus(Translation.STATUS_UNTRANSLATED);
                    text.addTranslation(t);
                }
            }
        }    // for each label

        // merge duplicate texts
        mergeDuplicateTexts(textMap);

        ProgressQueue.setProgress(20);

        // persistent Label objects before updateTranslation, it's needed by translation history
        dao.createArray(labelsToCreate.toArray(new Label[0]));

        ProgressQueue.setProgress(30);

        // process LABEL contexts
        int ctxStep = 0;
        if (textMap.containsKey(Context.LABEL)) {
            Collection<Label> labels = labelMap.get(Context.LABEL);
            Collection<Text> texts = textMap.get(Context.LABEL);
            log.info("Importing data into context " + Context.LABEL);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    null, texts, mode, TranslationHistory.TRANS_OPER_DELIVER);
            // update DEFAULT context from each LABEL context, so the DEFAULT context would be a union of all translations
            textService.updateTranslations(defaultCtx.getId(), texts, Constants.ImportingMode.SUPPLEMENT, TranslationHistory.TRANS_OPER_SUGGEST);
            if (mode == Constants.ImportingMode.DELIVERY) {
                for (Label label : labels) {
                    // update label
                    Label dbLabel = dbDict.getLabel(label.getKey());
                    Text dbText = dbTextMap.get(label.getContext().getKey());    // key of dbTextMap is context key in this case
                    dbLabel.setContext(label.getContext());
                    dbLabel.setText(dbText);
                    dbLabel.setKey(label.getKey());
                    dbLabel.setDescription(label.getDescription());
                    dbLabel.setMaxLength(label.getMaxLength());
                    dbLabel.setReference(label.getReference());
                    dbLabel.setAnnotation1(label.getAnnotation1());
                    dbLabel.setAnnotation2(label.getAnnotation2());
                    dbLabel.setAnnotation3(label.getAnnotation3());
                    dbLabel.setAnnotation4(label.getAnnotation4());
                    dbLabel.setFontName(label.getFontName());
                    dbLabel.setFontSize(label.getFontSize());
                    dbLabel.setSortNo(label.getSortNo());

                    Collection<Label> oneLabel = new ArrayList<Label>();
                    oneLabel.add(label);
                    Map<String, Text> oneTextMap = new HashMap<String, Text>();
                    oneTextMap.put(label.getReference(), dbText);
                    report.addData(label.getContext(), dict, oneLabel, oneTextMap);
                }
            }
            ProgressQueue.setProgress(30 + (int) Math.round(++ctxStep * 50.0 / textMap.size()));
        }

        // process non-LABEL contexts
        for (String contextName : textMap.keySet()) {
            Collection<Text> texts = textMap.get(contextName);
            Collection<Label> labels = labelMap.get(contextName);
            Context context = null;
            if (contextName.equals(Context.LABEL)) {
                continue;
            } else {
                context = texts.iterator().next().getContext();
            }

            log.info("Importing data into context " + contextName);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    context.getId(), texts, mode, TranslationHistory.TRANS_OPER_DELIVER);
            // update DEFAULT context from each DICT context or LABEL context, so the DEFAULT context would be a union of all translations
            if (context.isNameIn(Context.DICT, Context.LABEL)) {
                textService.updateTranslations(defaultCtx.getId(), texts, Constants.ImportingMode.SUPPLEMENT, TranslationHistory.TRANS_OPER_SUGGEST);
            }

            // in TRANSLATION_MODE, no change to label
            if (mode == Constants.ImportingMode.TRANSLATION) {
                continue;
            }

            // NOTE: following code is only executed in DELIVERY_MODE
            for (Label label : labels) {
                // update label
                Label dbLabel = dbDict.getLabel(label.getKey());
                dbLabel.setContext(context);
                dbLabel.setText(dbTextMap.get(label.getReference()));
                dbLabel.setKey(label.getKey());
                dbLabel.setDescription(label.getDescription());
                dbLabel.setMaxLength(label.getMaxLength());
                dbLabel.setReference(label.getReference());
                dbLabel.setAnnotation1(label.getAnnotation1());
                dbLabel.setAnnotation2(label.getAnnotation2());
                dbLabel.setAnnotation3(label.getAnnotation3());
                dbLabel.setAnnotation4(label.getAnnotation4());
                dbLabel.setFontName(label.getFontName());
                dbLabel.setFontSize(label.getFontSize());
                dbLabel.setSortNo(label.getSortNo());
            }
            report.addData(context, dict, labels, dbTextMap);
            ProgressQueue.setProgress(30 + (int) Math.round(++ctxStep * 50.0 / textMap.size()));
        }
        
        // process old labels which is removed in the dictionary file being imported
        for (Label label : dbDict.getAvailableLabels()) {
        	if (!labelKeys.contains(label.getKey())) {
        		if (settings.isRemoveOldLabels()) {	// remove the label
        			label.setRemoved(true);
        		} else {	// put the label to the end of dictionary
        			label.setSortNo(sortNo++);
        		}
        	}
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }

        report.addDiffLabelNum(diffLabel);
        report.addDiffTranslationNum(diffTranslation);
        report.addDiffTranslatedNum(diffTranslated);

        historyService.logDelivery(dbDict, dbDict.getPath());

        // insert or update LabelTranslation objects
        log.info("Updating LabelTranslation objects...");
        for (Label label : dict.getLabels()) {
            // create or update LabelTranslation
            Label dbLabel = dbDict.getLabel(label.getKey());
            if (label.getOrigTranslations() != null) {
                for (LabelTranslation trans : label.getOrigTranslations()) {
                    LabelTranslation dbLabelTrans = dbLabel.getOrigTranslation(trans.getLanguageCode());
                    if (dbLabelTrans == null || dbLabelTrans.getId() == null) {
                        trans.setLabel(dbLabel);
                        trans.setLanguage((Language) dao.retrieve(Language.class, trans.getLanguage().getId()));
                        labelTranslationsToCreate.add(trans);
                    } else {
                        dbLabelTrans.setOrigTranslation(trans.getOrigTranslation());
                        dbLabelTrans.setAnnotation1(trans.getAnnotation1());
                        dbLabelTrans.setAnnotation2(trans.getAnnotation2());
                        dbLabelTrans.setComment(trans.getComment());
                        dbLabelTrans.setWarnings(trans.getWarnings());
                        dbLabelTrans.setNeedTranslation(trans.isNeedTranslation());
                        dbLabelTrans.setRequestTranslation(trans.getRequestTranslation());
                        dbLabelTrans.setLanguageCode(trans.getLanguageCode());
                        dbLabelTrans.setSortNo(trans.getSortNo());
                    }
                }    // for each labelTranslation
            }
        }
        dao.createArray(labelTranslationsToCreate.toArray(new LabelTranslation[0]));
        log.info("Import dictionary finish");
        ProgressQueue.setProgress(90);
        return dbDict;
    }

    /**
     * Merge duplicate texts in textMap.
     * Translated string will be chosen prior
     *
     * @param textMap
     */
    private void mergeDuplicateTexts(MultiValueMap<String, Text> textMap) {
        for (String ctx : textMap.keySet()) {
            if (ctx.equals(Context.LABEL)) continue;    // allow same reference for LABEL context
            Collection<Text> texts = textMap.get(ctx);
            Map<String, Text> refMap = new HashMap<String, Text>();

            for (Iterator<Text> iter = texts.iterator(); iter.hasNext(); ) {
                Text text = iter.next();
                Text existText = refMap.get(text.getReference());
                if (existText != null) {    // do merge and remove current text
                    log.info("Merge duplicate text: " + text.getReference());
                    if (null != text.getTranslations()) {
                        for (Translation trans : text.getTranslations()) {
                            Translation existTrans = existText.getTranslation(trans.getLanguage().getId());
                            if (existTrans == null) {
                                trans.setText(existText);
                                existText.addTranslation(trans);
                            } else if (existTrans.getStatus() == Translation.STATUS_UNTRANSLATED && trans.getStatus() == Translation.STATUS_TRANSLATED) {
                                existTrans.setTranslation(trans.getTranslation());
                                existTrans.setStatus(trans.getStatus());
                            }
                        }
                    }
                    iter.remove();
                } else {
                    refMap.put(text.getReference(), text);
                }
            }
        }
    }

    /**
     * Find out a dictionary in the application which contains languages
     *
     * @param appId
     * @return
     */
    private Dictionary findDictionaryForAppendingLanguage(Long appId) {
        Application app = (Application) dao.retrieve(Application.class, appId);
        if (app.getDictionaries() != null) {
            for (Dictionary dict : app.getDictionaries()) {
                if (!dict.hasNoLanguage()) return dict;
            }
        }
        return null;
    }

    private int populateTranslationStatus(LabelTranslation trans) {
        Label label = trans.getLabel();
        if (trans.getStatus() != null) {    // status information is specified in dictionary
            return trans.getStatus();
        } else if (label.getReference().trim().isEmpty()) {
            return Translation.STATUS_TRANSLATED;
//        } else if (trans.getRequestTranslation() != null && trans.getRequestTranslation()) {
//        	return Translation.STATUS_UNTRANSLATED;
        } else if (!trans.isNeedTranslation()) {
            return Translation.STATUS_TRANSLATED;
        } else if (label.getReference().equals(trans.getOrigTranslation()) || trans.getOrigTranslation().trim().isEmpty()) {
            return Translation.STATUS_UNTRANSLATED;
        } else {
            return Translation.STATUS_TRANSLATED;
        }
    }

    @Deprecated
    public DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, Constants.ImportingMode mode) throws BusinessException {
        DeliveryReport report = new DeliveryReport();
        for (Dictionary dict : dictList) {
            Map<String, String> langCharset = new HashMap<String, String>();
            if (dict.getDictLanguages() != null) {
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
                }
            }
            Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
            try {
                importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, null, warnings, report);
            } catch (UnexpectedRollbackException e) {
                if (mode == Constants.ImportingMode.TEST) {
                    log.info("Rolled back all changes of importing because of TEST mode");
                } else {
                    throw e;
                }
            }
        }
//        report.setWarningMap(warningMap);
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

        // simulate a delivery activity in order to benefit from auto-match of other contexts
        // construct texts parameter
        Map<Long, Collection<Text>> ctxMap = new HashMap<Long, Collection<Text>>();
        if (dict.getLabels() != null) {
            for (Label label : dict.getLabels()) {
                Collection<Text> texts = ctxMap.get(label.getContext().getId());
                if (texts == null) {
                    texts = new ArrayList<Text>();
                    ctxMap.put(label.getContext().getId(), texts);
                }
                Text text = new Text();
                text.setRefLabel(label);
                text.setReference(label.getReference());
                Translation trans = new Translation();
                trans.setTranslation(label.getReference());
                trans.setLanguage((Language) dao.retrieve(Language.class, languageId));
                trans.setStatus(Translation.STATUS_UNTRANSLATED);
                text.addTranslation(trans);
                texts.add(text);
            }
        }
        for (Long ctxId : ctxMap.keySet()) {
            Collection<Text> texts = ctxMap.get(ctxId);
            textService.updateTranslations(ctxId, texts, Constants.ImportingMode.DELIVERY, TranslationHistory.TRANS_OPER_NEW);
        }
        // add dictLanguage
        DictionaryLanguage dl = new DictionaryLanguage();
        dl.setDictionary(dict);
        dl.setLanguageCode(code);
        dl.setLanguage((Language) dao.retrieve(Language.class, languageId));
        dl.setCharset((Charset) dao.retrieve(Charset.class, charsetId));
        dl.setSortNo(dict.getMaxDictLanguageSortNo());
        dl = (DictionaryLanguage) dao.create(dl);

        return dl;
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
            if (!dl.isReference()) {
                dao.delete(dl);
            }
        }
    }

    public int removeDictionaryLanguageInBatch(Collection<Long> dictIds, String languageCode) {
        int count = 0;
        for (Long dictId : dictIds) {
            Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
            if (dict.getDictLanguages() != null) {
                Iterator<DictionaryLanguage> iter = dict.getDictLanguages().iterator();
                while (iter.hasNext()) {
                    DictionaryLanguage dl = iter.next();
                    if (dl.getLanguageCode().equals(languageCode) && !dl.isReference()) {
                        iter.remove();
                        dao.delete(dl);
                        count++;
                    }
                }
            }

        }
        return count;
    }

    public void updateLabelKey(Long labelId, String key) throws BusinessException {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        if (label.getKey().equals(key)) return;
        Label existing = label.getDictionary().getLabel(key);
        if (existing != null) {
            throw new BusinessException(BusinessException.DUPLICATE_LABEL_KEY, key);
        }
        label.setKey(key);
    }

    public Label updateLabelReference(Long labelId, String reference) {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        String oldReference = label.getReference();
        label.setReference(reference);
        Collection<GlossaryMatchObject> GlossaryMatchObjects = glossaryService.consistentGlossariesInLabelRef(label);

        // re-associate text unless EXCLUSION context
        if (!label.getContext().getName().equals(Context.EXCLUSION)) {
            Text text = updateLabelTranslations(label);
            label.setText(text);
        }
        // reset LabelTranslation objects
        if (!reference.equals(oldReference) && label.getOrigTranslations() != null) {
            for (LabelTranslation lt : label.getOrigTranslations()) {
                lt.setOrigTranslation(reference);
                lt.setNeedTranslation(true);
                lt.setRequestTranslation(null);
            }
        }
        return label;
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
                    if (contextExp.equals(Context.LABEL)) {
                        String expression = textService.populateContextKey(contextExp, label);
                        ctx = textService.getContextByExpressionForLabel(expression, label.getDictionary().getId());
                    } else {
                        ctx = textService.getContextByExpression(contextExp, label.getDictionary());
                    }
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

    public void updateLabelContext(Context context, Collection<Label> labels) {
        Collection<Text> texts = new ArrayList<Text>();
        for (Label label : labels) {
            label.setContext(context);
            Text text = new Text();
            text.setRefLabel(label);
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
        Map<String, Text> textMap = textService.updateTranslations(context.getId(), texts, Constants.ImportingMode.SUPPLEMENT, TranslationHistory.TRANS_OPER_NEW);
        for (Label label : labels) {
            label.setText(textMap.get(label.getReference()));
        }
    }

    public void updateLabelContextWithTranslations(Context context, Label label) {
        label.setContext(context);
        Collection<Text> texts = new ArrayList<Text>();
        Text text = new Text();
        text.setRefLabel(label);
        text.setReference(label.getReference());
        for (DictionaryLanguage dl : label.getDictionary().getDictLanguages()) {
            if (dl.isReference()) continue;
            Translation translation = label.getTranslationObject(dl);
            Translation trans = new Translation();
            trans.setTranslation(translation.getTranslation());
            trans.setLanguage(dl.getLanguage());
            trans.setStatus(translation.getStatus());
            trans.setTranslationType(translation.getTranslationType());
            text.addTranslation(trans);
        }
        texts.add(text);
        Map<String, Text> textMap = textService.updateTranslations(context.getId(), texts, Constants.ImportingMode.TRANSLATION, TranslationHistory.TRANS_OPER_NEW);
        label.setText(textMap.get(label.getReference()));
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
            throw new BusinessException(BusinessException.DUPLICATE_LABEL_KEY, key);
        }
        if (maxLength != null && maxLength.trim().isEmpty()) {
            maxLength = null;
        }
        Label label = new Label();
        label.setDictionary(dict);
        label.setKey(key);
        label.setReference(reference);
        label.setMaxLength(maxLength);
        label.setDescription(description);


//        label.setText(text);
        label.setSortNo(dict.getMaxLabelSortNo() + 1);
        label.setRemoved(false);
        Context context = textService.getContextByExpression(contextExp, dict);
        label.setContext(context);

        glossaryService.consistentGlossariesInLabelRef(label);
        label = (Label) dao.create(label);

        Text text = updateLabelTranslations(label);
        label.setText(text);
        return label;
    }

    /**
     * Update translation for a new label.
     * The method is invoked when a new label was added or reference text of a label is changed.
     * An auto-match of translation action will be performed.
     *
     * @param label new label or the label whose reference text was changed
     * @return text object linked to the label
     */
    private Text updateLabelTranslations(Label label) {
        Collection<Text> texts = new ArrayList<Text>();
        Text text = new Text();
        text.setRefLabel(label);
        text.setReference(label.getReference());
        texts.add(text);
        if (label.getDictionary().getDictLanguages() != null) {
            for (DictionaryLanguage dl : label.getDictionary().getDictLanguages()) {
                if (label.getDictionary().getReferenceLanguage().equals(dl.getLanguageCode()))
                    continue;
                Translation trans = new Translation();
                trans.setTranslation(label.getReference());
                trans.setLanguage(dl.getLanguage());
                trans.setStatus(Translation.STATUS_UNTRANSLATED);
                text.addTranslation(trans);
            }
        }
        Map<String, Text> textMap = textService.updateTranslations(label.getContext().getId(), texts, Constants.ImportingMode.DELIVERY, TranslationHistory.TRANS_OPER_NEW);
        return textMap.get(label.getReference());
    }


    @Override
    public void changeDictCapitalization(Collection<Long> dictIds, Collection<Long> langIds, int style) {
        HashSet<Long> langSet = new HashSet<Long>();
        if (langIds != null) {
            langSet.addAll(langIds);
        }
        int curDict = 1;
        int dictTotal = dictIds.size();

        for (Long id : dictIds) {
            Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
            String dictName = dict.getName();
            Collection<Label> availableLabels = dict.getAvailableLabels();
            String msg = String.format("[%d/%d] Processing dictionary: %s", (int) curDict, dictTotal, dictName);
            ProgressQueue.getInstance().setProgress(msg, (int) Math.round(curDict * 100.0 / dictTotal));
            changeLabelCapitalization(dict, availableLabels, langSet, style);
            curDict++;
        }
        ProgressQueue.getInstance().setProgress("Completing...", -1);
    }

    @Override
    public void changeLabelCapitalization(Collection<Long> labelIds,
                                          Collection<Long> langIds, int style) {
        Dictionary dict = null;
        HashSet<Long> langSet = new HashSet<Long>();
        if (langIds != null) {
            langSet.addAll(langIds);
        }

        Collection<Label> labels = new ArrayList<Label>();
        for (Long id : labelIds) {
            Label label = (Label) dao.retrieve(Label.class, id);
            if (dict == null) {
                dict = label.getDictionary();
            }
            labels.add(label);
        }
        changeLabelCapitalization(dict, labels, langSet, style);
        ProgressQueue.getInstance().setProgress("Completing...", -1);
    }

    private void changeLabelCapitalization(Dictionary dict, Collection<Label> labels, HashSet<Long> langSet, int style) {
        Map<Long, Collection<Text>> contextMap = new HashMap<Long, Collection<Text>>();
        Map<Long, Collection<Label>> labelMap = new HashMap<Long, Collection<Label>>();
        for (Label label : labels) {
            label.setCapitalization(style);
            String oldReference = label.getReference();
            label.setReference(capitalizeText(oldReference, style, Locale.ENGLISH));
            Collection<GlossaryMatchObject> GlossaryMatchObjects = glossaryService.consistentGlossariesInLabelRef(label);
            String newReference = label.getReference();
            Text text = label.getText();
            Text newText = new Text();
            newText.setRefLabel(label);
            newText.setReference(newReference);
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                Translation translation = text.getTranslation(dl.getLanguage().getId());
                if (translation != null) {
                    String oldTranslation = translation.getTranslation();
                    String newTranslation = oldTranslation;
                    if (langSet.contains(dl.getLanguage().getId())) {    // capitalization required
                        newTranslation = capitalizeText(oldTranslation, style, langService.getLocale(dl.getLanguage()));
                        // glossary matching
                        for (GlossaryMatchObject gmo : GlossaryMatchObjects) {
                            newTranslation = gmo.getProcessedString(newTranslation);
                        }
                    } else if (oldTranslation.equals(oldReference)) {    // update translation if it's same with old reference
                        newTranslation = newReference;
                    }
                    if (newReference.equals(oldReference)) {    // if reference is not changed, update translations directly
                        if (!newTranslation.equals(oldTranslation)) {
                            translation.setTranslation(newTranslation);
                            translation.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                            historyService.addTranslationHistory(translation, label, TranslationHistory.TRANS_OPER_CAPITALIZE, null);
                        }
                    } else {    // if reference is changed, prepare data for calling textService.updateTranslations() in batch
                        // duplicate existing translations and capitalize them if required
                        Translation newTrans = new Translation();
                        newTrans.setLanguage(translation.getLanguage());
                        newTrans.setTranslation(newTranslation);
                        newTrans.setTranslationType(translation.getTranslationType());
                        newTrans.setStatus(translation.getStatus());
                        newText.addTranslation(newTrans);
                    }
                }
            }
            if (!newReference.equals(oldReference)) { // if reference is changed, prepare data for calling textService.updateTranslations() in batch
                Long ctxId = text.getContext().getId();
                Collection<Label> ctxLabels = labelMap.get(ctxId);
                Collection<Text> texts = contextMap.get(ctxId);
                if (texts == null) {
                    texts = new ArrayList<Text>();
                    ctxLabels = new ArrayList<Label>();
                    contextMap.put(ctxId, texts);
                    labelMap.put(ctxId, ctxLabels);
                }
                texts.add(newText);
                ctxLabels.add(label);
            }
        }

        // update text and translations for each context
        for (Long ctxId : contextMap.keySet()) {
            Map<String, Text> textMap = textService.updateTranslations(ctxId, contextMap.get(ctxId), Constants.ImportingMode.TRANSLATION, TranslationHistory.TRANS_OPER_CAPITALIZE);
            // re-associate text with labels
            for (Label label : labelMap.get(ctxId)) {
                label.setText(textMap.get(label.getReference()));
            }
        }

        historyService.flushHistoryQueue();
    }

    private String capitalizeText(String text, int style, Locale locale) {
        if (StringUtils.isBlank(text)) return text;

        if (style == Label.CAPITALIZATION_ALL_UPPER_CASE) return text.toUpperCase(locale);
        if (style == Label.CAPITALIZATION_ALL_LOWER_CASE) return text.toLowerCase(locale);

        if (style == Label.CAPITALIZATION_FIRST_CAPITALIZED_ONLY) {

            return String.valueOf(text.charAt(0)).toUpperCase(locale) + text.substring(1);
        }
        if (style == Label.CAPITALIZATION_FIRST_CAPITALIZED) {
            text = text.toLowerCase(locale);
            for (int i = 0; i < text.length(); i++) {
                if (!isWordBoundary(text.charAt(i))) {
                    text = text.substring(0, i) + text.substring(i, i + 1).toUpperCase(locale) + text.substring(i + 1);
                    break;
                }
            }
        }
        if (style == Label.CAPITALIZATION_ALL_CAPITALIZED) {
            text = text.toLowerCase(locale);
            boolean inWord = false;
            for (int i = 0; i < text.length(); i++) {
                if (isWordBoundary(text.charAt(i))) {
                    inWord = false;
                } else {
                    if (!inWord) {
                        text = text.substring(0, i) + text.substring(i, i + 1).toUpperCase(locale) + text.substring(i + 1);
                        inWord = true;
                    }
                }
            }
        }
        return text;
    }

    private boolean isWordBoundary(char ch) {
        return Character.isWhitespace(ch) || ",./<>?;':[]{}()+=`~!@#$%^&*|\\\"".indexOf(ch) != -1;
    }

    @Override
    public Collection<Dictionary> findDictionaries(String prod, String app, String ver) {
        String hql = "select distinct obj.dictionaries from Application obj" +
                " where obj.base.productBase.name=:prod" +
                " and obj.base.name=:app" +
                " and obj.version=:ver";
        Map param = new HashMap();
        param.put("prod", prod);
        param.put("app", app);
        param.put("ver", ver);
        return dao.retrieve(hql, param);
    }

    @Override
    public Application findApplication(String prod, String app, String ver) {
        String hql = "from Application obj" +
                " where obj.base.productBase.name=:prod" +
                " and obj.base.name=:app" +
                " and obj.version=:ver";
        Map param = new HashMap();
        param.put("prod", prod);
        param.put("app", app);
        param.put("ver", ver);
        return (Application) dao.retrieveOne(hql, param);
    }

    @Override
    public Collection<ValidationInfo> findDictionaryValidations(Long dictId, String type) {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        if (null == dict) return Arrays.asList();
        dict.validate();
        Collection validations = type.equals("errors") ? dict.getDictErrors() : dict.getDictWarnings();
        return validations;
    }
}
