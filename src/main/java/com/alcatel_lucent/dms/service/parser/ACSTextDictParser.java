package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

@SuppressWarnings("unchecked")
@Component
public class ACSTextDictParser extends DictionaryParser {

    private static final Collection<String> REFERENCE_CODE = Arrays.asList("EN", "EN-EN");
    private FileFilter textFilter = new OrFileFilter(new SuffixFileFilter(Arrays.asList(".txt")), DirectoryFileFilter.INSTANCE);
    @Autowired
    private LanguageService languageService;

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

        for (File subFile : fileOrDirs) {
            if (subFile.isDirectory()) {
                deliveredDicts.addAll(parse(rootDir, subFile, acceptedFiles, exceptions));
                continue;
            }

            String[] nameParts = splitFileName(subFile.getName());
            String langCode = nameParts[2];
            if (null == nameParts) continue;
            // reference file must end with "en.txt"
            if (CollectionUtils.exists(REFERENCE_CODE,
                    PredicateUtils.invokerPredicate("equalsIgnoreCase", new Class[]{String.class}, new String[]{langCode}))) {
                refTextFiles.put(nameParts[0], subFile);
            } else {
                textFiles.add(nameParts[0], subFile);
            }
        }

        File refFile = null;
        for (String baseName : refTextFiles.keySet()) {
            try {
                refFile = refTextFiles.get(baseName);
                deliveredDicts.add(parseProp(rootDir, baseName, textFiles.get(baseName), refTextFiles.get(baseName)));
            } catch (BusinessException e) {
                e.addNestedException(exceptions);
                log.info(file + " is not a ACSText because {}", e.getMessage());
            }
            acceptedFiles.add(refFile);
            if (null != textFiles.get(baseName)) {
                acceptedFiles.addAll(textFiles.get(baseName));
            }
        }


        return deliveredDicts;
    }

    /**
     * @param files   Collection represent a dictionary except its reference language
     * @param refFile store the reference language text
     */
    private Dictionary parseProp(String rootDir,
                                 String baseName, Collection<File> files, File refFile) {
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        String[] nameParts = splitFileName(refFile.getName());

        String refLangCode = nameParts[2];
        String dictName = normalize(refFile.getAbsolutePath(), true);

        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }
        log.info("Parsing acs text file '" + dictName + "'");

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(normalize(refFile.getAbsolutePath()));
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(Constants.DictionaryFormat.ACS_TEXT.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);

        int sortNo = 1;

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
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
        dictionary.setLabels(readLabels(refFile, dictionary, warnings, refFileExceptions, true));
        if (null == files) {
            return dictionary;
        }
//      Parsing non-reference text files
        BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_PROP_ERROR);
        for (File file : files) {
            nameParts = splitFileName(file.getName());
            String langCode = nameParts[2];

            BusinessException fileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, file.getName());
            DictionaryLanguage dictLanguage = new DictionaryLanguage();

            dictLanguage.setLanguageCode(langCode);
            dictLanguage.setSortNo(sortNo);
            language = languageService.getLanguage(langCode);
            dictLanguage.setLanguage(language);
            dictLanguage.setCharset(languageService.getCharset("UTF-8"));
            dictLanguages.add(dictLanguage);
            readLabels(file, dictionary, warnings, fileExceptions, false);
        }

        if (refFileExceptions.hasNestedException()) {
            dictExceptions.addNestedException(refFileExceptions);
        }
        return dictionary;
    }

    private Collection<Label> readLabels(File file, Dictionary dict, Collection<BusinessWarning> warnings, BusinessException exceptions, boolean isRef) {
        ArrayList<Label> result = new ArrayList<Label>();
        InputStream in = null;

        try {
            in = new AutoCloseInputStream(new FileInputStream(file));
            String encoding = Util.detectEncoding(file);
            if (encoding.startsWith("UTF-")) {
                do {
                    in = new BOMInputStream(in, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE);
                } while (((BOMInputStream) in).hasBOM());
            } else if (!isRef) {
//                encoding is ISO-8859-1
                encoding = languageService.getLanguage(splitFileName(file.getName())[2]).getDefaultCharset();
            }
            List<String> lines = IOUtils.readLines(in, encoding);

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
                String[] keyElement = line.split("\\s+", 2);

                if (keys.contains(keyElement[0])) {
                    warnings.add(new BusinessWarning(
                            BusinessWarning.DUPLICATE_LABEL_KEY, lineNo,
                            keyElement[0]));
                    continue;
                }
                if (2 != keyElement.length) {
                    continue;
                }
                keys.add(keyElement[0]);
                if (isRef) {
                    Label label = new Label();
                    if (comments.toString().length() > 0) {
                        label.setAnnotation1(comments.toString());
                    }
                    label.setKey(keyElement[0].trim());
                    label.setReference(keyElement[1].trim());
                    label.setSortNo(sortNo++);
                    label.setDictionary(dict);

                    result.add(label);
                } else {
                    Label refLabel = dict.getLabel(keyElement[0]);
                    if (refLabel == null) {
                        log.warn("Label {} in file {} could not be found in reference file.", keyElement[0], file.getName());
                        continue;
                    }
                    LabelTranslation trans = new LabelTranslation();
                    String langCode = splitFileName(file.getName())[2];

                    trans.setLanguageCode(langCode);
                    trans.setLanguage(dict.getDictLanguage(langCode).getLanguage());
                    trans.setOrigTranslation(keyElement[1]);
                    trans.setAnnotation1(comments.toString());

                    trans.setSortNo(sortNo);
                    refLabel.addOrigTranslation(trans);
                }
                pComments.close();
                comments = new StringWriter();
                pComments = new PrintWriter(comments);
            }

        } catch (Exception e) {
            e.printStackTrace();
            exceptions.addNestedException(new BusinessException(e.toString()));
        } finally {
            IOUtils.closeQuietly(in);
        }
        return result;
    }

    /**
     * Determine if the line is a comment line or blank line.
     * A line is a comment line when the first non-space character is '#' or '!'.
     *
     * @param line
     * @return
     */
    private boolean isCommentOrBlankLine(String line) {
        line = line.trim();
        return line.isEmpty() || line.charAt(0) == '#';
    }

}



