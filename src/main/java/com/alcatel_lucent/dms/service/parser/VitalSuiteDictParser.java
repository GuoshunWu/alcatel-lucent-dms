package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.normalize;

@SuppressWarnings("unchecked")
@Component
public class VitalSuiteDictParser extends DictionaryParser {

    private static final Collection<String> REFERENCE_CODE = Arrays.asList("EN", "EN-EN");
    private FileFilter textFilter = new OrFileFilter(new SuffixFileFilter(Arrays.asList(".txt")), DirectoryFileFilter.INSTANCE);

    private static final Pattern linePattern = Pattern.compile("^([A-Za-z0-9-\\.]+)\\s*\\{\"(.*?)\"\\}$");
    private static final Pattern filePattern = Pattern.compile("^((?://.*\\s*)*\\s*[a-zA-Z]{2}(?:[-_][a-zA-Z]{2})?\\s*(?://.*\\s)*)\\s*\\{([\\s\\S]*)\\}\\s*(?://.*\\s*)*$");

    /**
     * The lines start with following keys should not be treated as normal label.
     */
    private static final Collection<String> specialTextKeys = Arrays.asList("Language-English", "Language-Display");

    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.VITAL_SUITE;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        return result;
    }


    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists() || file.isFile()) return deliveredDicts;
        rootDir = normalize(rootDir, true);


        File[] fileOrDirs = file.listFiles(textFilter);
        MultiValueMap<String, File> textFiles = new LinkedMultiValueMap<String, File>();
        Map<String, File> refTextFiles = new HashMap<String, File>();

        File refFile = null;
        String dictName = null;
        Collection<File> translationFiles = new ArrayList<File>();

        for (File subFile : fileOrDirs) {
            if (subFile.isDirectory()) {
                deliveredDicts.addAll(parse(rootDir, subFile, acceptedFiles, exceptions));
                continue;
            }


            String langCode = FilenameUtils.getBaseName(subFile.getName());
            if (StringUtils.isBlank(langCode)) continue;
            String normalizedName = normalize(subFile.getParent(), true);
            if (null != rootDir && normalizedName.startsWith(rootDir) && normalizedName.length() >= rootDir.length() + 1) {
                dictName = normalizedName.substring(rootDir.length() + 1);
            }
            // sub file at root of the package  or is invalid vital suite file
            if (StringUtils.isEmpty(dictName) || null == getVitalSuiteLines(subFile)) continue;

            // reference file must end with "en.txt"
            if (CollectionUtils.exists(REFERENCE_CODE,
                    PredicateUtils.invokerPredicate("equalsIgnoreCase", new Class[]{String.class}, new String[]{langCode}))) {
                refFile = subFile;
            } else {
                translationFiles.add(subFile);
            }
        }

        // skip the dictionary files at the root directory of the package
        if (StringUtils.isEmpty(dictName)) {
            return deliveredDicts;
        }

        try {
            deliveredDicts.add(parseDict(rootDir, file.getName(), translationFiles, refFile));
        } catch (BusinessException e) {
            e.addNestedException(exceptions);
        }
        acceptedFiles.add(refFile);
        acceptedFiles.addAll(translationFiles);

        return deliveredDicts;
    }


    /**
     * Read text file to string, return null if it is not a valid vital suite dictionary
     */
    private String getVitalSuiteLines(File file) {
        InputStream in = null;
        String langCode = FilenameUtils.getBaseName(file.getName());
        boolean isRefFile = CollectionUtils.exists(REFERENCE_CODE,
                PredicateUtils.invokerPredicate("equalsIgnoreCase", new Class[]{String.class}, new String[]{langCode}));
        try {
            in = new AutoCloseInputStream(new FileInputStream(file));
            String encoding = Util.detectEncoding(file);
            if (encoding.startsWith("UTF-")) {
                do {
                    in = new BOMInputStream(in, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE);
                } while (((BOMInputStream) in).hasBOM());
            } else if (!isRefFile) {
//                encoding is ISO-8859-1
                Language language = languageService.getLanguage(langCode);
                if (null != language) {
                    encoding = language.getDefaultCharset();
                }
            }
            String fileContent = IOUtils.toString(in, encoding);
            if (filePattern.matcher(fileContent).matches()) {
                return fileContent;
            }
            return null;
        } catch (Exception e) {
            log.error("Read file {} error.", file.getAbsolutePath());
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * @param files   Collection represent a dictionary except its reference language
     * @param refFile store the reference language text
     */
    private Dictionary parseDict(String rootDir,
                                 String dictName, Collection<File> files, File refFile) {
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();

        if (null == refFile) throw new BusinessException(BusinessException.VITAL_SUITE_REF_FILE_NOT_FOUND, dictName);
        log.info("Parsing vital suite dict '" + dictName + "'");

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(normalize(refFile.getAbsolutePath()));
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(DictionaryFormat.VITAL_SUITE.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);

        String refLangCode = FilenameUtils.getBaseName(refFile.getName());

        int sortNo = 1;

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setReferenceLanguage(refLangCode);
        // add reference language in dictionary language
        DictionaryLanguage refDl = new DictionaryLanguage();
        refDl.setLanguageCode(refLangCode);
        refDl.setSortNo(sortNo++);
        Language language = languageService.getLanguage(refLangCode);
        refDl.setCharset(languageService.getCharset(dictionary.getEncoding()));
        refDl.setLanguage(language);
        dictLanguages.add(refDl);


        BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, refFile.getName());
//      First parse reference text file
        dictionary.setLabels(readLabels(refFile, dictionary, warnings, refFileExceptions, refDl));
        if (null == files) {
            return dictionary;
        }
//      Parsing non-reference text files
        BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_PROP_ERROR);
        for (File file : files) {
            String langCode = FilenameUtils.getBaseName(file.getName());

            BusinessException fileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, file.getName());
            DictionaryLanguage dictLanguage = new DictionaryLanguage();

            dictLanguage.setLanguageCode(langCode);
            dictLanguage.setSortNo(sortNo);
            language = languageService.getLanguage(langCode);
            dictLanguage.setLanguage(language);
            dictLanguage.setCharset(languageService.getCharset("UTF-8"));
            dictLanguages.add(dictLanguage);
            readLabels(file, dictionary, warnings, fileExceptions, dictLanguage);
        }

        if (refFileExceptions.hasNestedException()) {
            dictExceptions.addNestedException(refFileExceptions);
        }
        return dictionary;
    }

    private Collection<Label> readLabels(File file, Dictionary dict, Collection<BusinessWarning> warnings, BusinessException exceptions, DictionaryLanguage dictionaryLanguage) {
        boolean isRef = CollectionUtils.exists(REFERENCE_CODE,
                PredicateUtils.invokerPredicate("equalsIgnoreCase", new Class[]{String.class}, new String[]{dictionaryLanguage.getLanguageCode()}));
        ArrayList<Label> result = new ArrayList<Label>();

        List<String> lines = null;
        try {
            lines = IOUtils.readLines(new StringReader(getVitalSuiteLines(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null == lines) return result;

        Writer comments = new StringWriter();
        PrintWriter pComments = new PrintWriter(comments);

        HashSet<String> keys = new HashSet<String>();

        int sortNo = 1;
        int lineNo = 0;

        for (String line : lines) {
            lineNo++;
            if (isCommentOrBlankLine(line) && !line.contains(DictionaryGenerator.GEN_SIGN)) {
                pComments.println(line);
                continue;
            }
            line = line.trim();

            Matcher matcher = linePattern.matcher(line);
            // skip lines are not label line
            if (!matcher.matches()) {
                warnings.add(new BusinessWarning(BusinessWarning.INVALID_LINE_IN_VITAL_SUITE_FILE, line, file.getAbsolutePath()));
                continue;
            }

            String key = matcher.group(1).trim();
            String text = matcher.group(2).trim();
            text = StringEscapeUtils.unescapeJava(text);

            String strComments = comments.toString();

            if (StringUtils.isNotEmpty(strComments)) {
                strComments = StringUtils.removeEnd(strComments, System.getProperty("line.separator"));

                pComments.close();
                comments = new StringWriter();
                pComments = new PrintWriter(comments);
            }


            /**
             * Language-English
             * Language-Display
             * lines are just language relevant parameters
             * */
            if (specialTextKeys.contains(key)) {

                if (StringUtils.isNotEmpty(strComments)) {
                    dictionaryLanguage.setAnnotation2(strComments);
                }

                String annotation = dictionaryLanguage.getAnnotation1();
                annotation = null == annotation ? "" : annotation + System.getProperty("line.separator");
                annotation += "    " + line;
                dictionaryLanguage.setAnnotation1(annotation);
                continue;
            }

            if (keys.contains(key)) {
                warnings.add(new BusinessWarning(
                        BusinessWarning.DUPLICATE_LABEL_KEY, lineNo,
                        key));
                continue;
            }
            keys.add(key);

            if (isRef) {
                Label label = new Label();
                if (StringUtils.isNotEmpty(strComments)) {
                    label.setAnnotation1(strComments);
                }
                label.setKey(key);
                label.setReference(text);
                label.setSortNo(sortNo++);
                label.setDictionary(dict);

                result.add(label);
            } else {
                Label refLabel = dict.getLabel(key);
                if (refLabel == null) {
                    log.warn("Label {} in file {} could not be found in reference file.", key, file.getName());
                    continue;
                }
                LabelTranslation trans = new LabelTranslation();
                String langCode = FilenameUtils.getBaseName(file.getName());

                trans.setLanguageCode(langCode);
                trans.setLanguage(dict.getDictLanguage(langCode).getLanguage());
                trans.setOrigTranslation(text);
                if (StringUtils.isNotEmpty(strComments)) {
                    trans.setAnnotation1(strComments);
                }
                trans.setSortNo(sortNo);
                refLabel.addOrigTranslation(trans);
            }

        }

        return result;
    }

    /**
     * Determine if the line is a comment line or blank line.
     *
     * @param line
     * @return
     */
    private boolean isCommentOrBlankLine(String line) {
        line = line.trim();
        return line.isEmpty() || line.startsWith("//");
    }

}



