package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IOSParser extends DictionaryParser {
    public static final String STRING_FILE_NAME = "Localizable.strings";
    private static final NameFileFilter iosResFilter = new NameFileFilter(STRING_FILE_NAME, IOCase.INSENSITIVE);
    public static final String PARENT_DIR_EXTENSION = ".lproj";

    private static final Pattern labelPattern = Pattern.compile("\"(.*)\"\\s*=\\s*\"(.*)\";");

    public static final String DEFAULT_DICT_NAME = "DefaultDictionary";

    public static final String REFERENCE_LANG_CODE = "GAE";
    public static final String REFERENCE_LANG_DIR = "Base";


    @Autowired
    private LanguageService languageService;

    /**
     * Parse dictionaries into object.
     *
     * @param rootDir       part of path to be trimmed in dictionary name
     * @param file          directory or file to be parsed, if file is a dictionary, files under the dictionary will be parsed recursively.
     * @param acceptedFiles output parameter, holder of accepted files list
     * @return list of Dictionary objects
     * @throws com.alcatel_lucent.dms.BusinessException fatal errors, multiple exceptions can be put into a NESTED_ERROR exception.
     *                                                  It's suggested to go through all files before throw a fatal error, so that user can get as more as possible
     *                                                  information about errors.
     */
    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        if (exceptions.hasNestedException()) throw exceptions;
        return result;
    }

    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.IOS_RESOURCE;
    }

    /**
     * Get file parent base name and language code
     *
     * @param file file
     * @return a pair which left element is parent base name and right is language code, if no language code, the
     * right element would be empty string.
     */
    private Pair<String, String> getFileLangCodeAndBaseName(String rootDir, File file) {
        File parentFile = file.getParentFile();
        String langCodeName = FilenameUtils.getBaseName(parentFile.getName());
        //get parent's parent path
        String path = "/";
        if (null != parentFile.getParentFile()) {
            path = FilenameUtils.normalize(parentFile.getParent(), true) + path;
        }
        if (path.startsWith(rootDir)) path = path.substring(rootDir.length() + 1);
        if (path.isEmpty()) path = DEFAULT_DICT_NAME;
        return Pair.of(StringUtils.removeEnd(path, "/"), langCodeName);
    }


    /**
     * Retrieves  and removes the reference file in the collection.
     *
     * @param files dictionary file collection
     * @return reference file or null if not found
     */
    private File takeReferenceFileFromCollection(String rootDir, Collection<File> files) {
        Iterator<File> fileIterator = files.iterator();
        File file = null;
        while (fileIterator.hasNext()) {
            file = fileIterator.next();
            Pair<String, String> pBaseNameAndLangCode = getFileLangCodeAndBaseName(rootDir, file);
            if (pBaseNameAndLangCode.getRight().equalsIgnoreCase(REFERENCE_LANG_DIR)) {
                files.remove(file);
                return file;
            }
        }
        return null;
    }


    /**
     * Grouping the files in sub directories recursively by dictionary, the files with same parent base name and file name
     * are considered as one dictionary.
     *
     * @param rootFile must be a directory
     * @return A multi value map which parent base name, file separator and file name as key and files in dictionary as value
     */
    private MultiValueMap<String, File> groupingFiles(File rootFile) {
        Collection<File> files = FileUtils.listFiles(rootFile, iosResFilter, DirectoryFileFilter.DIRECTORY);
        MultiValueMap<String, File> dictionariesGroups = new LinkedMultiValueMap<String, File>();
        for (File subFile : files) {
            Pair<String, String> pBaseNameAndLangCode = getFileLangCodeAndBaseName(FilenameUtils.normalize(rootFile.getAbsolutePath(), true), subFile);
            String dictName = pBaseNameAndLangCode.getLeft();
            dictionariesGroups.add(dictName, subFile);
        }
        return dictionariesGroups;
    }

    private Dictionary parseDictionary(String rootDir, String dictName, Collection<File> filesInDict, Collection<File> acceptedFiles) throws BusinessException {
        File refFile = takeReferenceFileFromCollection(rootDir, filesInDict);
        if (null == refFile) {
            throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE);
        }
        Pair<String, String> nameLangCodePair = getFileLangCodeAndBaseName(rootDir, refFile);
        if (null == dictName) {
            dictName = nameLangCodePair.getKey();
        }

        // accept other files in ios dictionary directory structure
        Collection<File> allFiles = FileUtils.listFiles(refFile.getParentFile().getParentFile(), TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        allFiles.remove(refFile);
        allFiles.removeAll(filesInDict);
        acceptedFiles.addAll(allFiles);

        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        Dictionary dict = readReferenceFile(dictName, refFile, null, warnings);
        acceptedFiles.add(refFile);
        // read other language translations

        int sortNo = 1;

        BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_IOS_RESOURCE_ERROR, dict.getName());

        for (File file : filesInDict) {
            nameLangCodePair = getFileLangCodeAndBaseName(rootDir, file);
            // add DictionaryLanguage for dict
            String referenceCode = nameLangCodePair.getValue();
            if (referenceCode.equals(REFERENCE_LANG_DIR)) referenceCode = REFERENCE_LANG_CODE;
            DictionaryLanguage dictionaryLanguage = getDictionaryLanguage(referenceCode, sortNo);
            dictionaryLanguage.setDictionary(dict);
            dict.addDictLanguage(dictionaryLanguage);

            readLabels(file, dict, dictionaryLanguage, warnings);
            acceptedFiles.add(file);
        }

        if (dictExceptions.hasNestedException()) {
            throw dictExceptions;
        }
        dict.setParseWarnings(warnings);
        return dict;
    }

    private Dictionary readReferenceFile(String dictName, File refFile, String refLangCode, Collection<BusinessWarning> warnings) throws BusinessException {
        if (StringUtils.isEmpty(refLangCode)) refLangCode = REFERENCE_LANG_CODE;
        // create dictionary.
        log.info("Parsing reference iso string file '" + refFile.getAbsolutePath() + "'");
        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(refFile.getAbsolutePath());
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(getFormat().toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);
        dictionary.setLabels(new ArrayList<Label>());

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setReferenceLanguage(refLangCode);
        dictionary.setReferenceLanguage(REFERENCE_LANG_CODE);

        // add reference dictLanguage object
        DictionaryLanguage refDictLanguage = getDictionaryLanguage(refLangCode, 0);
        refDictLanguage.setDictionary(dictionary);
        dictLanguages.add(refDictLanguage);

        readLabels(refFile, dictionary, refDictLanguage, warnings);
        return dictionary;

    }

    private DictionaryLanguage getDictionaryLanguage(String langCode, int sortNo) {
        DictionaryLanguage dictLanguage = new DictionaryLanguage();
        dictLanguage.setLanguageCode(langCode);
        dictLanguage.setLanguage(languageService.getLanguage(langCode));
        dictLanguage.setCharset(languageService.getCharset("UTF-8"));
        dictLanguage.setSortNo(sortNo);
        return dictLanguage;
    }


    private ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        rootDir = FilenameUtils.normalize(rootDir, true);
        ArrayList<Dictionary> deliveredDictionaries = new ArrayList<Dictionary>();
        // single file import is disallowed for ios resource format
        if (!file.exists() || file.isFile()) {
            return deliveredDictionaries;
        }

        // file is directory
        MultiValueMap<String, File> dictionariesGroups = groupingFiles(file);
        Set<Map.Entry<String, List<File>>> entries = dictionariesGroups.entrySet();
        for (Map.Entry<String, List<File>> entry : entries) {
            try {
                String dictName = entry.getKey();
                List<File> dictFiles = entry.getValue();
                deliveredDictionaries.add(parseDictionary(rootDir, dictName, dictFiles, acceptedFiles));
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
        }
        return deliveredDictionaries;
    }


    @SuppressWarnings("unchecked")
    private void readLabels(File file, Dictionary dict, DictionaryLanguage dl, Collection<BusinessWarning> warnings) {
        List<String> lines = null;
        try {
            lines = FileUtils.readLines(file, dict.getEncoding());
        } catch (IOException e) {
            throw new BusinessException(BusinessException.INVALID_DICT_FORMAT, this.getFormat());
        }
        // other language level saved in each label translation annotation
        int sortNo = 1;
        int rowNumber = 1;

        List<String> comment = new ArrayList<String>();
        for (String line : lines) {
            Matcher matcher = labelPattern.matcher(line.trim());
            if (matcher.matches()) { // got a label
                sortNo += readLabel(dict, matcher.group(1), matcher.group(2), comment, rowNumber++, sortNo, dl, warnings);
                comment.clear();
            } else {
                comment.add(line);
            }
        }
    }


    private int readLabel(Dictionary dict, String key, String value, List<String> comment, int rowNumber, int sortNo, DictionaryLanguage dl, Collection<BusinessWarning> warnings) {
        boolean isReference = dl.getLanguageCode().equals(REFERENCE_LANG_CODE);
        String strComment = comment.isEmpty() ? null : StringUtils.join(comment, "\n");

        if (isReference) {
            Label label = new Label();
            label.setKey(key);
            label.setReference(value);
            label.setAnnotation1(strComment);
            label.setSortNo(sortNo);
            label.setOrigTranslations(new ArrayList<LabelTranslation>());

            dict.addLabel(label);
            label.setDictionary(dict);
            return 1;
        }

        // add a new label translation
        Label label = dict.getLabel(key);
        if (null == label) {
            warnings.add(new BusinessWarning(BusinessWarning.LABEL_KEY_BLANK, dl.getLanguageCode(), rowNumber, 1));
            return 0;
        }

        LabelTranslation lt = new LabelTranslation();
        lt.setAnnotation1(strComment);
        lt.setSortNo(sortNo);


        lt.setLanguage(dl.getLanguage());
        lt.setLanguageCode(dl.getLanguageCode());

        lt.setStatus(Translation.STATUS_TRANSLATED);
        lt.setOrigTranslation(value);

        lt.setLabel(label);
        label.addLabelTranslation(lt);
        return 1;
    }
}
