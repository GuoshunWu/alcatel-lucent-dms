/**
 *
 */
package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Guoshun.Wu
 */
public class DCTReader extends LineNumberReader {

    private Dictionary dictionary;

    private Collection<BusinessWarning> warnings;
    private LanguageService languageService;

    // Language pattern in dct file
    private static final Pattern patternLanguage = Pattern
            .compile("^LANGUAGES\\s*\\{((?:[\\w-]{2,5}\\s*,?\\s*)+)\\}$");
    private static final Pattern patternLabelLanguageCode = Pattern
            .compile("^([\\w-]{2,5})\\s*[\\w\\W]*$");

    private Logger log = LoggerFactory.getLogger(DCTReader.class);
    private String lastLine = ";";
    private String currentLine;
    private boolean firstLine = true;
    private StringBuffer commentLines = new StringBuffer();
    private StringBuffer suffixComment;

    protected DCTReader(Reader in, Dictionary dictionary) {
        super(in);
        this.dictionary = dictionary;
        this.warnings = new ArrayList<BusinessWarning>();
    }

    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

    public Collection<BusinessWarning> getWarnnings() {
        return warnings;
    }

    public Collection<DictionaryLanguage> readLanguages(BusinessException exception) throws IOException {

        String line = readLine();
        if (line == null) {
            log.error("Parser was broken on line " + getLineNumber() + ": " + line);
            throw new BusinessException(BusinessException.INVALID_DCT_FILE,
                    getLineNumber(), dictionary.getName());
        }
        Matcher m = patternLanguage.matcher(line);
        if (!m.matches()) {
            log.error("Parser was broken on line " + getLineNumber() + ": " + line);
            throw new BusinessException(BusinessException.INVALID_DCT_FILE,
                    getLineNumber(), dictionary.getName());
        }
        String[] languageCodes = m.group(1).split("\\s*,\\s*");

        Collection<DictionaryLanguage> dictLangs = new ArrayList<DictionaryLanguage>();
        DictionaryLanguage dl = null;

        int sortNo = 1;
        for (String languageCode : languageCodes) {
            if ("CHK".equals(languageCode))
                continue;

            dl = new DictionaryLanguage();

            dl.setDictionary(dictionary);
            dl.setLanguageCode(languageCode);
            Language language = languageService.getLanguage(languageCode);
            if(null == language){
//                exception.addNestedException(new BusinessException(BusinessException.UNKNOWN_LANG_CODE, getLineNumber(), languageCode));
            }
            dl.setLanguage(language);
            if (dictionary.getEncoding() == "ISO-8859-1") {	// ANSI
            	if (language != null) {
            		dl.setCharset(languageService.getCharset(language.getDefaultCharset()));
            	}
            } else {	// UTF-8 or UTF-16LE
            	dl.setCharset(languageService.getCharset(dictionary.getEncoding()));
            }
            dl.setSortNo(sortNo++);

            dictLangs.add(dl);
        }
        return dictLangs;
    }

    public Dictionary readDictionary() throws IOException {

        Collection<DictionaryLanguage> dictLanguages = null;

        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dictionary.getName());

        // first read Languages
        dictLanguages = readLanguages(nonBreakExceptions);

        dictionary.setDictLanguages(dictLanguages);
        dictionary.setAnnotation1(getCommentLines());

        // readLabels
        Label label = null;
        Collection<Label> labels = new ArrayList<Label>();
        String labelLine = readLine();
        if (null == labelLine) {
            dictionary.setLabels(labels);
            dictionary.setDictLanguages(new HashSet<DictionaryLanguage>());
            return dictionary;
        }
        StringBuilder labKeyLine = new StringBuilder(labelLine);
        HashSet<String> labelKeys = new HashSet<String>();

        while (!labKeyLine.toString().equals("null")) {
            try {
                label = readLabel(labKeyLine, getCommentLines());
            } catch (BusinessException e) {
                nonBreakExceptions.addNestedException(e);
            }
            if (null == label)
                break;
            if (labelKeys.contains(label.getKey())) {
                warnings.add(new BusinessWarning(
                        BusinessWarning.DUPLICATE_LABEL_KEY, getLineNumber(),
                        label.getKey()));
            } else {
                labelKeys.add(label.getKey());
                labels.add(label);
            }
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }
        dictionary.setLabels(labels);
        return dictionary;
    }

    public Label readLabel(StringBuilder labelKey, String commentBeforeLabel) throws IOException,
            BusinessException {
        if (!lastLine.endsWith(";") && !lastLine.endsWith("}")) {
            warnings.add(new BusinessWarning(BusinessWarning.UNCLOSED_LABEL,
                    getLineNumber(), labelKey.toString()));
        }
        if (!labelKey.toString().endsWith(":")) {
            throw new BusinessException(BusinessException.INVALID_DCT_FILE,
                    getLineNumber(), dictionary.getPath());
        }

        /*
           * End sign is next Label begin, which is the line end with colon
           */
        String key = labelKey.substring(0, labelKey.length() - 1);


        Label label = new Label();
        label.setKey(key);
        label.setAnnotation1(commentBeforeLabel);
        label.setDictionary(dictionary);
        label.setDescription(null);

        Map<String, String> entriesInLabel = new HashMap<String, String>();
        HashSet<String> langCodesRequestTranslation = new HashSet<String>();
        ArrayList<String> orderedLangCodes = new ArrayList<String>();

        BusinessException exceptions = new BusinessException(
                BusinessException.NESTED_LABEL_ERROR, key);

        Set<String> dictLangCodes = dictionary.getAllLanguageCodes();
        dictLangCodes.add("CHK");

        String line = null;
        String langCode = null;
        // read until file end or next label start
        boolean firstLine = true;
        while ((null != line || null != (line = readLine()))
                && !isLabelKeyLine(line)) {
            // get an entry, current line should be an entry start
        	if (firstLine) {	// get comments after label
        		label.setAnnotation2(getCommentLines());
        		firstLine = false;
        	}
            langCode = isLabelEntryStart(line);

            if (!lastLine.endsWith(",") && !lastLine.endsWith(":")) {
                warnings.add(new BusinessWarning(
                        BusinessWarning.UNCLOSED_LABEL_ENTRY, getLineNumber(),
                        langCode, key));
            }

            if (null == langCode) {
                exceptions.addNestedException(new BusinessException(
                        BusinessException.NESTED_LABEL_ERROR, getLineNumber(),
                        key));
            }

            if (!dictLangCodes.contains(langCode)) {
                exceptions.addNestedException(new BusinessException(
                        BusinessException.UNDEFINED_LANG_CODE, getLineNumber(),
                        langCode, key));
            }

//            if (!isValidLangCode(langCode)) {
//                exceptions.addNestedException(new BusinessException(
//                        BusinessException.UNKNOWN_LANG_CODE, getLineNumber(),
//                        langCode));
//            }

            if (entriesInLabel.containsKey(langCode)) {
                warnings.add(new BusinessWarning(
                        BusinessWarning.DUPLICATE_LANG_CODE, getLineNumber(),
                        langCode));
            }

            if (isUnclosedQuota(line)) {
                warnings.add(new BusinessWarning(
                        BusinessWarning.UNCLOSED_QUOTA, getLineNumber(),
                        langCode, key));
            }

            // read entry content
            StringBuilder buffer = new StringBuilder();

            // remove the langCode, blank characters and quotation marks
            line = line.replaceFirst(langCode, "").trim();
            if (line.startsWith("\"")) {
            	line = line.substring(1);
            }
            if (line.endsWith(",") || line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }
            if (line.endsWith("\"")) {
                line = line.substring(0, line.length() - 1);
            }

            buffer.append(line);
            if (suffixComment != null && suffixComment.toString().substring(2).trim().startsWith("???")) {
            	langCodesRequestTranslation.add(langCode);
            }

            // read until file end or next label start or next entry start
            while (null != (line = readLine())
                    && (null == isLabelEntryStart(line))
                    && !isLabelKeyLine(line)) {

                if (isUnclosedQuota(line)) {
                    warnings.add(new BusinessWarning(
                            BusinessWarning.UNCLOSED_QUOTA, getLineNumber(),
                            langCode, key));
                }

                if (line.endsWith(",") || line.endsWith(";")) {
                    line = line.replace("\"", "");
                    buffer.append("\n");
                    buffer.append(line.substring(0, line.length() - 1));
                } else {
                    line = line.replace("\"", "");
                    buffer.append("\n");
                    buffer.append(line);
                }
                if (suffixComment != null && suffixComment.toString().startsWith("--???")) {
                	langCodesRequestTranslation.add(langCode);
                }
            }
            entriesInLabel.put(langCode, buffer.toString());
            orderedLangCodes.add(langCode);
        }

        // analysis entries for reference, maxLength, text
        String gae = entriesInLabel.get("GAE");
        if (null == gae) {
            exceptions.addNestedException(new BusinessException(
                    BusinessException.NO_REFERENCE_TEXT, getLineNumber(), label
                    .getKey()));
        }
        label.setReference(gae);

        Collection<LabelTranslation> translations = new HashSet<LabelTranslation>();
        LabelTranslation trans = null;
        int labelSortNo = 1;
        for (String oLangCode : orderedLangCodes) {

            if (oLangCode.equals("CHK") || oLangCode.equals("GAE")) {
                continue;
            }

            trans = new LabelTranslation();
            trans.setLabel(label);

            DictionaryLanguage dl=dictionary.getDictLanguage(oLangCode);
            Language language= null==dl ? null:dl.getLanguage();

//            Language language =languageService.getLanguage(entry.getKey());
//            if (null == language) {
//                exceptions.addNestedException(new BusinessException(
//                        BusinessException.UNKNOWN_LANG_CODE, getLineNumber(),
//                        entry.getKey(), key));
//
//            }

            trans.setLanguage(language);
            trans.setLanguageCode(oLangCode);
            trans.setOrigTranslation(entriesInLabel.get(oLangCode));
            trans.setSortNo(labelSortNo++);
            trans.setRequestTranslation(langCodesRequestTranslation.contains(oLangCode));

            translations.add(trans);
        }
        label.setOrigTranslations(translations);

        String maxLenStr = entriesInLabel.get("CHK");
        if (null != maxLenStr) {
            String[] maxLenArray = maxLenStr.split("\n");

            String maxLength = "" + maxLenArray[0].length();
            for (int i = 1; i < maxLenArray.length; ++i) {
                maxLength += "," + maxLenArray[i].length();
            }
            label.setMaxLength(maxLength);
        }

        labelKey.delete(0, labelKey.length());
        labelKey.append(line);

        if (exceptions.hasNestedException()) {
            throw exceptions;
        }

        return label;
    }

    private boolean isLabelKeyLine(String line) {
        return line.endsWith(":");
    }

    @Override
    public String readLine() throws IOException {
        String line = null;
        // skip comment and blank line
        // but record the comment and blank lines which may be useful
        clearCommentLines();
        while (null != (line = super.readLine())) {
        	// try to remove BOM
        	String rawLine = line;
            if (firstLine) {
            	if (line.length() >= 1 && line.charAt(0) == 0xfffe) {
            		line = line.substring(1);
            	} else if (line.length() >= 2 && line.charAt(0) == 0xff && line.charAt(1) == 0xfe) {
            		line = line.substring(2);
            	} else if (line.length() >= 3 && line.charAt(0) == 0xef && line.charAt(1) == 0xbb && line.charAt(2) == 0xbf) {
            		line = line.substring(3);
            	}
            	firstLine = false;
            }
            line = line.trim();
            if (isCommentOrBlankLine(line)) {
                log.debug(String.format(
                        "[line: %d]In file %s is comment or blank line, skip.",
                        getLineNumber(), dictionary.getPath()));
                if (!rawLine.startsWith("-- Generated by DMS") && rawLine.indexOf("#CHECKSUM=") == -1) {
                	addCommentLine(rawLine);
                }
                continue;
            }
            break;
        }
        if (null == line)
            return null;
        line = removeComments(line);
        lastLine = currentLine;
        currentLine = line;
        return line;
    }

    private void clearCommentLines() {
		commentLines = new StringBuffer();
	}
    
    private void addCommentLine(String line) {
    	if (commentLines.length() != 0 || !line.trim().isEmpty()) {
    		commentLines.append(line).append("\n");
    	}
    }
    
    /**
     * Return comment lines during the latest readLine()
     * @return
     */
    private String getCommentLines() {
    	return commentLines.length() == 0 ? null : commentLines.toString();
    }

	/**
     * Remove the trailing comments on line
     *
     * @return processed line
     * @throws java.io.IOException
     * @author Guoshun.Wu Date: 2012-07-04
     */
    private String removeComments(String line) throws IOException {
    	suffixComment = null;
        line.trim();
        StringBuilder sb = new StringBuilder();
        StringReader sr = new StringReader(line);
        int ch = -1;
        int quotNum = 0;
        while (-1 != (ch = sr.read())) {
            if (ch == '-') {
                int nextch = (char) sr.read();
                if (('-' == nextch && 0 == quotNum % 2) || -1 == nextch) {
                	// save comment info
                	suffixComment = new StringBuffer("--");
                	while (-1 != (ch = sr.read())) {
                		suffixComment.append((char) ch);
                	}
                    break;
                }
                if ('"' == nextch) {
                    quotNum++;
                }
                sb.append((char) ch);
                sb.append((char) nextch);
            } else {
                if (ch == '"') {
                    quotNum++;
                }
                sb.append((char) ch);
            }
        }
        sr.close();
        return sb.toString().trim();
    }

    private boolean isCommentOrBlankLine(String line) {
        return line.startsWith("--") || line.isEmpty()
                || line.charAt(0) == '\uFEFF';
    }

    /**
     * Test if a langCode is valid(exist in database)
     *
     * @param langCode
     * @author Guoshun.Wu
     */
//    private boolean isValidLangCode(String langCode) {
//        Set<String> allLangCodes = new HashSet<String>(languageService
//                .getAlcatelLanguageCodes().keySet());
//        allLangCodes.addAll(languageService.getISOLanguageCodes().keySet());
//        allLangCodes.add("CHK");
//        langCode = langCode.replace('_', '-');
//        return allLangCodes.contains(langCode);
//    }

    /**
     * Test if a line is a entry start line in an label the language code will
     * be returned or null if this is is not an entry start line
     *
     * @param line
     */
    private String isLabelEntryStart(String line) {
        Matcher m = patternLabelLanguageCode.matcher(line);
        if (!m.matches())
            return null;
        return m.group(1);
    }

    /**
     * Check if text has ending quota
     *
     * @param line
     * @return
     */
    private boolean isUnclosedQuota(String line) {
        if (line.endsWith(",") || line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).trim();
        }
        return !line.endsWith("\"");
    }
}
