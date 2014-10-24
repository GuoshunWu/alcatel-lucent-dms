package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.subarray;

@Component
@SuppressWarnings("unchecked")
public class NOEStrParser extends DictionaryParser {

    private static final Logger log = LoggerFactory.getLogger(NOEStrParser.class);

    public static final String[] extensions = new String[]{"doc"};
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    public static final String REFERENCE_CODE = "gea";

    public static final String LABEL_KEY_PREFIX = "\u001B";
    public static final String LABEL_TRANS_PREFIX = "\\?";

    private static String[][] NOE_STRING_ESCAPE = new String[][]{

            {"\u00df", "\\LBETA"},   //ß
            {"\u00c6", "\\LA--E"},   //Æ
            {"\u00e6", "\\La--e"},   //æ
            {"\u00a3", "\\LLIRA"},   //£
            {"\u00bf", "\\LINV?"},   //¿
            {"\u00b6", "\\L-PI-"},   //¶
            {"\u03bc", "\\L-MU-"},   //μ

            // TODO: complement UA code mapping
            {"\u03c6", "\\x03"},    //φ
            {"\u00b0", "\\x04"},    //°

            {"\u2192", "\\Hc7"},
            {"\u2190", "\\Hc8"},
    };

    static {
        //fill accents NOE_STRING_ESCAPE array
        //upper case
        NOE_STRING_ESCAPE = ArrayUtils.addAll(NOE_STRING_ESCAPE, getNoeStringAccentMap());
        //lower case
        NOE_STRING_ESCAPE = ArrayUtils.addAll(NOE_STRING_ESCAPE, getNoeStringAccentMap(false));
    }

    public static final CharSequenceTranslator ESCAPE_NOE_STRING =
            new LookupTranslator(
            ).with(
                    new LookupTranslator(NOE_STRING_ESCAPE))
                    .with(
                            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())
                    )
//                    .with(
//                    UnicodeEscaper.outsideOf(32, 0x7f)
//            )
            ;

    public static final CharSequenceTranslator UNESCAPE_NOE_STRING =
            new AggregateTranslator(
//                    new OctalUnescaper(),     // .between('\1', '\377'),
//                    new UnicodeUnescaper(),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
                    new LookupTranslator(EntityArrays.invert(NOE_STRING_ESCAPE)),
                    new LookupTranslator(
                            new String[][]{
                                    //many to one mappings
                                    {"\\Hb2", "\u00b0"},
                                    {"\\a_o", "\u00b0"},

//                                    {"\\x02","\u03c6" },        //φ
//                                    {"\\Lphi-","\u03c6" },      //φ

//                                    {"\\\\", "\\"},
//                                    {"\\", ""}
                            })
            );


    private static String[][] getNoeStringAccentMap(boolean uppercase) {
        List<Character> accents = Arrays.asList('`', '\'', '^', '~', '"', '*');

        final int firstAccentLetterIndex = 6;

        Map<Character, int[]> accentRect = MapUtils.typedMap(ArrayUtils.toMap(new Object[][]{
                {'a', new int[]{1, 1, 1, 1, 1, 1, 'à'}},
                {'e', new int[]{1, 1, 1, 0, 1, 0, 'è'}},
                {'n', new int[]{0, 0, 0, 0, 1, 0, 'ñ'}},
                {'o', new int[]{1, 1, 1, 1, 1, 0, 'ò'}},
                {'y', new int[]{0, 1, 0, 0, 0, 0, 'ý'}},
        }), Character.class, int[].class);

        accentRect.put('i', add(subarray(accentRect.get('e'), 0, firstAccentLetterIndex), 'ì'));
        accentRect.put('u', add(subarray(accentRect.get('e'), 0, firstAccentLetterIndex), 'ù'));
        List<String[]> accentMap = new ArrayList<String[]>();

        Set<Character> vowelLetters = accentRect.keySet();
        for (Character vowelLetter : vowelLetters) {
            int offset = 0;
            for (int i = 0; i < firstAccentLetterIndex; ++i) {
                int exists = 0;
                if (1 == (exists = accentRect.get(vowelLetter)[i])) {
                    int firstAccentLetter = accentRect.get(vowelLetter)[firstAccentLetterIndex];
                    Character accentLetter = (char) (firstAccentLetter + i + offset);
                    char convertedVowelLetter = vowelLetter;
                    if (uppercase) {
                        accentLetter = Character.toUpperCase(accentLetter);
                        convertedVowelLetter = Character.toUpperCase(vowelLetter);
                    }
                    Character escapeLetter = accents.get(i);
                    accentMap.add(getAccentLetterEscapePair(accentLetter, vowelLetter, escapeLetter, uppercase));
                } else --offset;
            }
        }

        /**
         * remain letter
         * */
        //add special ones like z,c,y
        accentMap.add(getAccentLetterEscapePair('ÿ', 'y', '\"', uppercase));
        accentMap.add(getAccentLetterEscapePair('ç', 'c', ',', uppercase));
        accentMap.add(getAccentLetterEscapePair('ø', 'o', '/', uppercase));

//        accentMap.add(getAccentLetterEscapePair('°', 'o', '_', uppercase));

        return accentMap.toArray(new String[0][]);
    }

    private static String[][] getNoeStringAccentMap() {
        return getNoeStringAccentMap(true);
    }

    private static String[] getAccentLetterEscapePair(char accentLetter, char vowelLetter, char escapeLetter, boolean isUpperCase) {
        if (isUpperCase) {
            accentLetter = Character.toUpperCase(accentLetter);
            vowelLetter = Character.toUpperCase(vowelLetter);
        }
        String escapeString = "\\a" + escapeLetter + vowelLetter;
        log.debug("{}: {}", accentLetter, escapeString);
        return new String[]{accentLetter + "", escapeString};
    }

    public static String escapeNOEString(String input) {
        return ESCAPE_NOE_STRING.translate(input);
    }

    public static String unescapeNOEString(String input) {
        return UNESCAPE_NOE_STRING.translate(input);
    }


    private SuffixFileFilter NOEStrFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.NOE_STRING;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        /**
         * single file parse is not supported for this parser.
         * */
        if (!file.exists() || file.isFile()) return deliveredDicts;

        //group all the noe string dictionary file
        Collection<File> docFiles = FileUtils.listFiles(file, extensions, false);

        IOFileFilter notDocFilter = FileFilterUtils.notFileFilter(NOEStrFilter);
        File rootDirFile = new File(rootDir);
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        for (File docFile : docFiles) {
            String pattern = "^" + FilenameUtils.getBaseName(docFile.getName()) + "\\.\\w*";
            Collection<File> langFiles = FileUtils.listFiles(
                    rootDirFile,
                    new AndFileFilter(new RegexFileFilter(pattern), notDocFilter),
                    FileFilterUtils.falseFileFilter());

            deliveredDicts.add(parseDictionary(rootDir, docFile, langFiles, acceptedFiles, warnings));
        }
        return deliveredDicts;
    }

    /**
     * adjust original language file and make sure the reference file is the first one
     */
    private File adjustLangFileOrder(Collection<File> langFiles) {
        File refFile = (File) CollectionUtils.find(langFiles, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                File file = (File) object;
                return FilenameUtils.getExtension(file.getName()).equals(REFERENCE_CODE);
            }
        });
        if (null == refFile) return null;
        langFiles.remove(refFile);
        File[] langFilesArray = langFiles.toArray(new File[0]);
        langFilesArray = add(langFilesArray, 0, refFile);
        langFiles.clear();
        langFiles.addAll(Arrays.asList(langFilesArray));
        return refFile;
    }

    /**
     * Parse one dictionary
     */
    private Dictionary parseDictionary(String rootDir, File docFile, Collection<File> langFiles, Collection<File> acceptedFiles, Collection<BusinessWarning> warnings) throws BusinessException {

        DictionaryBase dictBase = new DictionaryBase();
        Pair<String, String> namePair = getDictNamePair(rootDir, docFile);
        dictBase.setPath(namePair.getRight());
        dictBase.setName(FilenameUtils.getBaseName(namePair.getLeft()));
        dictBase.setEncoding(dictBase.getName().endsWith("_stu") ? "UTF-8" : DEFAULT_ENCODING);
        dictBase.setFormat(DictionaryFormat.NOE_STRING.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setBase(dictBase);

        dictionary.setReferenceLanguage(REFERENCE_CODE);

        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);

        Map<String, Pair<Map<String, String>, Map<String, String>>> map = parseDocFile(docFile);
        try {
            dictionary.setAnnotation1(FileUtils.readFileToString(docFile, DEFAULT_ENCODING));
        } catch (IOException e) {
            log.error("Save doc file {} error!", docFile);
        }
        acceptedFiles.add(docFile);

        int sortNo = 1;
        File refFile = adjustLangFileOrder(langFiles);
        if (null == refFile) {
            throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE);
        }

        for (File langFile : langFiles) { // we need to parse reference language file first.
            // parse a lang file
            String langCode = FilenameUtils.getExtension(langFile.getName());
            DictionaryLanguage dl = new DictionaryLanguage();
            dl.setLanguageCode(langCode);
            dl.setSortNo(sortNo++);

            NOELanguageCode nlc = languageService.getNOELanguageCode(langCode.toUpperCase());
            Language language = nlc == null ? null : nlc.getLanguage();

            dl.setLanguage(language);
            dl.setCharset(new Charset(dictBase.getEncoding()));

            dl.setDictionary(dictionary);
            dictionary.getDictLanguages().add(dl);
            try {
                parseLangFile(dictionary, map, langFile, warnings);
                if (!warnings.isEmpty()) dictionary.setParseWarnings(warnings);
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
            acceptedFiles.add(langFile);
        }

//        try {
//            File f = new File("debugout.txt");
//            System.out.println("F=" + f);
//            MapUtils.debugPrint(new PrintStream(FileUtils.openOutputStream(f)), "parsed doc file", map);
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
        if (exceptions.hasNestedException()) {
            throw exceptions;
        }
        return dictionary;
    }

    private void parseLangFile(Dictionary dict, Map<String, Pair<Map<String, String>, Map<String, String>>> map,
                               File langFile, Collection<BusinessWarning> warnings) throws BusinessException {
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(langFile);


            String langCode = FilenameUtils.getExtension(langFile.getName());
            int lineNo = 1;
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.trim().isEmpty()) {
                    lineNo++;
                    continue;
                }

                if (line.startsWith(LABEL_KEY_PREFIX)) { // new label start
                    String labelKey = line.trim();
                    String translation = null;

                    if (it.hasNext()) {
                        line = it.nextLine();
                        translation = line.trim();
                    }

                    if (null == translation) {
                        warnings.add(new BusinessWarning(BusinessWarning.LABEL_TRANS_BLANK, lineNo, langFile.getAbsolutePath()));
                        translation = "";
                    }

                    Label label = null;
                    if (langCode.equals(REFERENCE_CODE)) { // create new label
                        label = new Label(labelKey);
                        String maxLength = null;
                        if (null != map.get(labelKey) && null != (maxLength = map.get(labelKey).getLeft().get("LEN"))) {
                            label.setMaxLength(maxLength);
                        }
                        dict.addLabel(label);
                        label.setDictionary(dict);
                        label.setOrigTranslations(new ArrayList<LabelTranslation>());
                        if (translation.startsWith(LABEL_TRANS_PREFIX)) {
                            translation = translation.substring(LABEL_TRANS_PREFIX.length());
                            //process escape character
                            translation = unescapeNOEString(translation);
                        }
                        label.setReference(translation);
                        if (-1 != translation.indexOf("\\*")) {
                            label.setDescription("The char following the \\* sequence must be the same in every other translation of the string.");
                        }
                    } else {
                        createTranslation(lineNo, langCode, dict.getLabel(labelKey), translation);
                    }
                }
                lineNo++;
            }

        } catch (IOException e) {
            throw new BusinessException(BusinessException.NESTED_VLE_LANG_FILE_ERROR, langFile.getAbsolutePath());
        } finally {
            it.close();
        }
    }

    private LabelTranslation createTranslation(int lineNo, String langCode, Label label, String text) {
        LabelTranslation lt = new LabelTranslation();
        lt.setLabel(label);
        lt.setLanguageCode(langCode);
        lt.setSortNo(lineNo);
        lt.setLanguage(label.getDictionary().getLanguageByCode(langCode));
        if (text.startsWith(LABEL_TRANS_PREFIX)) {
            lt.setStatus(Translation.STATUS_UNTRANSLATED);
            text = text.substring(LABEL_TRANS_PREFIX.length());
        } else {
            lt.setStatus(Translation.STATUS_TRANSLATED);
        }

        text = unescapeNOEString(text);
        lt.setOrigTranslation(text);

        label.addLabelTranslation(lt);
        return lt;
    }

    /**
     * Parse a doc file
     *
     * @param docFile the doc file
     * @return map map reference ref to a pair which left is a map of REF line and the right is the FLD line it belong to
     */
    private Map<String, Pair<Map<String, String>, Map<String, String>>> parseDocFile(File docFile) throws
            BusinessException {
        Map<String, Pair<Map<String, String>, Map<String, String>>> fileResult =
                new HashMap<String, Pair<Map<String, String>, Map<String, String>>>();

        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(docFile, DEFAULT_ENCODING);
            Map<String, String> field = null;
            String line = null;
            Collection<Map<String, String>> refEntries = null;
            while (null != (line = getValidLine(it))) {
                Map<String, String> items = parseDocLine(line);
                if (items.containsKey("FLD")) { //enter next new field
                    field = items;
                } else {
                    String ref = items.get("REF");
                    if (null == ref) continue;
                    fileResult.put(ref, Pair.of(field, items));
                }
            }
        } catch (IOException e) {
            throw new BusinessException(BusinessException.FILE_NOT_FOUND, docFile.getAbsolutePath());
        } finally {
            it.close();
        }
        return fileResult;
    }

    /**
     * Read a valid line from the line iterator.
     */

    private String getValidLine(LineIterator it) {

        final String singleLineCommentSign = "//";
        final String multiLineCommentStartSign = "/*";
        final String multiLineCommentEndSign = "*/";
        boolean inMultiLineComment = false;

        String preHalfLine = "";
        String sufHalfLine = "";

        while (it.hasNext()) {
            String line = it.nextLine().trim();
            // skip empty line and comment line
            if (line.isEmpty() || line.startsWith(singleLineCommentSign)) continue;

            int beginIndex = line.indexOf(multiLineCommentStartSign);
            int endIndex = line.lastIndexOf(multiLineCommentEndSign);
            int sBeginIndex = line.indexOf(singleLineCommentSign);

            if (-1 == sBeginIndex) { // no single comment in the middle of the line
                if (-1 != beginIndex && -1 != endIndex) {
                    preHalfLine = line.substring(0, beginIndex);
                    sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length());
                    line = preHalfLine + sufHalfLine;
                } else if (-1 != beginIndex && -1 == endIndex) {
                    preHalfLine = line.substring(0, beginIndex);
                    inMultiLineComment = true;
                } else if (-1 == beginIndex && -1 != endIndex) {
                    sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length());
                    line = preHalfLine + "\n" + sufHalfLine;
                    inMultiLineComment = false;
                }
            } else { //sBeginIndex > 0
                if (-1 != beginIndex && -1 != endIndex) {
                    if (beginIndex < sBeginIndex && sBeginIndex < endIndex) {
                        preHalfLine = line.substring(0, beginIndex);
                        sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length());
                        line = preHalfLine + sufHalfLine;
                    } else if (beginIndex < sBeginIndex && sBeginIndex > endIndex) {
                        preHalfLine = line.substring(0, beginIndex);
                        sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length(), sBeginIndex);
                        line = preHalfLine + sufHalfLine;
                    } else if (sBeginIndex < beginIndex && beginIndex < endIndex) {
                        sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length());
                        line = preHalfLine + "\n" + sufHalfLine;
                        inMultiLineComment = false;
                    } else {
                        log.error("comment syntax error.");
                    }
                } else if (-1 != beginIndex && -1 == endIndex) {
                    if (beginIndex < sBeginIndex) {
                        preHalfLine = line.substring(0, beginIndex);
                        inMultiLineComment = true;
                    } else {
                        line = line.substring(0, sBeginIndex);
                    }
                } else if (-1 == beginIndex && -1 != endIndex) {
                    if (sBeginIndex < endIndex) {
                        sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length());
                        line = preHalfLine + sufHalfLine;
                        inMultiLineComment = false;
                    } else {
                        preHalfLine = line.substring(0, beginIndex);
                        sufHalfLine = line.substring(endIndex + multiLineCommentEndSign.length(), sBeginIndex);
                        line = preHalfLine + sufHalfLine;
                        inMultiLineComment = false;
                    }
                } else {
                    line = line.substring(0, sBeginIndex);
                }
            }
            if (inMultiLineComment || line.trim().isEmpty()) {
                continue;
            }
            return line;

        }
        return null;
    }

    private Map<String, String> parseDocLine(String line) {
        Map<String, String> items = new HashMap<String, String>();
        String[] tokens = line.split("\\|");
        for (String token : tokens) {
            String[] entry = token.split("=");
            String key = entry[0];
            if (entry.length > 1) {
                items.put(key.trim(), entry[1].trim());
            }
        }
        return items;
    }
}
