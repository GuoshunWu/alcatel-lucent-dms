package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.TransformedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * OTC web dictionary parser
 * Created by Guoshun Wu on 13-12-16.
 */

@Component
public class OTCWebParser extends DictionaryParser {

    private static final String PARENT_BASE_NAME = "en-us";
    public static final String REFERENCE_LANG_CODE = PARENT_BASE_NAME;
    private static final String START_FUNCTION_NAME = "define";
    private static final String[] extensions = new String[]{"js"};
    private static final String LANG_CODE_SEPARATOR = "-";

    public static final String LANG_PATTERN = "^[a-zA-Z]{2}(?:\\" + LANG_CODE_SEPARATOR + "[a-zA-Z]{2})?$";
    public static final String DEFAULT_FILE_ENCODING = "UTF-8";


    private final SuffixFileFilter jsFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    @Autowired
    private LanguageService languageService;

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {

        ArrayList<Dictionary> deliveredDictionaries = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDictionaries;

        if (file.isFile()) { // single file import, only reference file is permitted.
            return deliveredDictionaries;
        }

        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);

        // file is directory
        MultiValueMap<String, DictEntry> dictionariesGroups = groupingFiles(file);
        Collection<List<DictEntry>> dictionaries = dictionariesGroups.values();

        for (List<DictEntry> dictionaryEntries : dictionaries) {
            try {
                deliveredDictionaries.add(parseDictionary(dictionaryEntries, acceptedFiles));
            } catch (BusinessException e) {
                if (e.getErrorCode() != BusinessException.INVALID_XML_FILE) {
                    exceptions.addNestedException(e);
                }
            }
        }
        if (exceptions.hasNestedException()) {
            throw exceptions;
        }
        return deliveredDictionaries;
    }

    private Dictionary parseDictionary(List<DictEntry> dictionaryEntries, Collection<File> acceptedFiles) {
        Predicate isRefPredicate = PredicateUtils.invokerPredicate("isReferenceFile");
        DictEntry refEntry = (DictEntry) CollectionUtils.find(dictionaryEntries, isRefPredicate);
        if (null == refEntry) {
            throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE);
        }
        //
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();

        Dictionary dict = readReferenceFile(refEntry, warnings);
        acceptedFiles.add(refEntry.getFile());
        int dictLangSortNo = 1;
        for (DictEntry dictEntry : dictionaryEntries) {
            if (dictEntry.isReferenceFile()) continue;
            readLabels(dictEntry, dict, dictLangSortNo++, warnings);
            acceptedFiles.add(dictEntry.getFile());
        }
        return dict;
    }

    private Dictionary readReferenceFile(DictEntry refEntry, Collection<BusinessWarning> warnings) {
        log.info("Parsing reference js file '" + refEntry.getFileName() + "'");
        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(refEntry.getFileName());
        // Ignore path for the dictionary
        dictBase.setPath("");
        dictBase.setEncoding(DEFAULT_FILE_ENCODING);
        dictBase.setFormat(getFormat().toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setReferenceLanguage(REFERENCE_LANG_CODE);

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setReferenceLanguage(refEntry.getLangCode());

        readLabels(refEntry, dictionary, 0, warnings);
        return dictionary;
    }

    @SuppressWarnings("unchecked")
    private void readLabels(DictEntry dictEntry, Dictionary dict, int dictLangSortNo, Collection<BusinessWarning> warnings) {
        String fileContent;
        try {
            fileContent = FileUtils.readFileToString(dictEntry.getFile(), DEFAULT_FILE_ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }

        // add reference dictLanguage object
        DictionaryLanguage dl = getDictionaryLanguage(dictEntry.getLangCode(), dictLangSortNo);
        dl.setDictionary(dict);
        dict.addDictLanguage(dl);

        String jsonString = getJSONContent(fileContent);
        if (jsonString.isEmpty()) {
            warnings.add(new BusinessWarning(BusinessWarning.OTC_WEB_DEFINE_NOT_FOUND, dictEntry.getFileName()));
            return;
        }
        JSONObject jsonObject = JSONObject.fromObject(jsonString);

        Set<Map.Entry<String, String>> strLabels = toFlatMap(jsonObject).entrySet();

        boolean isRef = dictEntry.isReferenceFile();
        HashSet<String> keys = new HashSet<String>();

        int sortNo = 0;
        for (Map.Entry<String, String> strLabel : strLabels) {
            String key = strLabel.getKey();
            String text = strLabel.getValue();
            if (isRef) {
                if (keys.contains(key)) {
                    // add this type of warning only for label
                    warnings.add(new BusinessWarning(BusinessWarning.DUPLICATE_LABEL_KEY, -1, key));
                    continue;
                }
                Label label = new Label();
                label.setKey(key);
                label.setReference(text);
                label.setSortNo(sortNo++);
                label.setOrigTranslations(new ArrayList<LabelTranslation>());

                dict.addLabel(label);
                label.setDictionary(dict);
                keys.add(strLabel.getKey());
            } else {
                // add a new label translation
                Label label = dict.getLabel(key);
                if (null == label) {
                    warnings.add(new BusinessWarning(BusinessWarning.ANDROID_LABEL_KEY_BLANK, key, dl.getLanguageCode()));
                    continue;
                }

                LabelTranslation lt = new LabelTranslation();
                lt.setSortNo(sortNo++);

                lt.setLanguage(dl.getLanguage());
                lt.setLanguageCode(dl.getLanguageCode());
                lt.setOrigTranslation(text);

                lt.setLabel(label);
                label.addLabelTranslation(lt);

            }
        }
    }

    private static String getJSONContent(String contentString) {
        int index = contentString.indexOf(START_FUNCTION_NAME) + START_FUNCTION_NAME.length();
        int maxLen = contentString.length() - START_FUNCTION_NAME.length() - 1;
        StringBuilder sb = new StringBuilder();
        Character lastCh = '\uffff';
        Stack<Character> bracketStack = new Stack<Character>();

        do {
            Character ch = contentString.charAt(index++);
            if ('\\' != lastCh && ch == '(') bracketStack.push(ch);
            else if ('\\' != lastCh && ch == ')') bracketStack.pop();
            lastCh = ch;
            sb.append(ch);
        } while (!bracketStack.empty() || index > maxLen);

        return sb.substring(1, sb.length() - 1);
    }

    private DictionaryLanguage getDictionaryLanguage(String langCode, int sortNo) {
        DictionaryLanguage dictLanguage = new DictionaryLanguage();
        dictLanguage.setLanguageCode(langCode);
        dictLanguage.setLanguage(languageService.getLanguage(langCode));
        dictLanguage.setCharset(languageService.getCharset("UTF-8"));
        dictLanguage.setSortNo(sortNo);
        return dictLanguage;
    }


    /**
     * Grouping the files in sub directories recursively by dictionary, the files with same file name
     * are considered as one dictionary.
     *
     * @param rootFile must be a directory
     * @return A multi value map which file name as key and files in dictionary as value
     */
    private MultiValueMap<String, DictEntry> groupingFiles(File rootFile) {
        Collection<File> files = FileUtils.listFiles(rootFile, jsFilter, TrueFileFilter.INSTANCE);
        MultiValueMap<String, DictEntry> dictionariesGroups = new LinkedMultiValueMap<String, DictEntry>();
        for (File subFile : files) {
            DictEntry entry = new DictEntry(subFile);
            if (!entry.isOTEWebFile()) continue;
            dictionariesGroups.add(entry.getFileName(), entry);
        }
        return dictionariesGroups;
    }

    private static Map<String, String> toFlatMap(Map<String, Object> original) {
        return toFlatMap(original, null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> toFlatMap(Map<String, Object> original, String separator) {
        if (separator == null) separator = ".";
        Map<String, String> result = new LinkedHashMap<String, String>();
        Set<Map.Entry<String, Object>> entrySet = original.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            final String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Object[]) {
                //todo add translate
            } else if (value instanceof Map) {
                final String finalSeparator = separator;
                Map<String, String> subMap = TransformedMap.decorateTransform(toFlatMap((Map) value, separator), new Transformer() {
                    @Override
                    public Object transform(Object input) {
                        return key + finalSeparator + input;
                    }
                }, null);
                result.putAll(subMap);
            } else {
                result.put(entry.getKey(), value.toString());
            }
        }
        return result;
    }


    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_WEB;
    }
}
