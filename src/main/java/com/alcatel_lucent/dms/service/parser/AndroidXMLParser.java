package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.util.NoOpEntityResolver;
import com.alcatel_lucent.dms.util.Util;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class AndroidXMLParser extends DictionaryParser {

    private static final String[] extensions = new String[]{"xml"};
    private static final SuffixFileFilter xmlFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
    private static final String PARENT_BASE_NAME = "values";

    public static final String LANG_CODE_SEPARATOR = "-";
    public static final String REFERENCE_LANG_CODE = "GAE";

    public static final String KEY_SEPARATOR = "_";

    private static final String ELEMENT_STRING = "string";
    public static final String ELEMENT_STRING_ARRAY = "string-array";
    public static final String ELEMENT_PLURALS = "plurals";

    private static final Collection<String> multiItemElements = Arrays.asList(ELEMENT_STRING_ARRAY, ELEMENT_PLURALS);

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
        return DictionaryFormat.XML_ANDROID;
    }

    private static String getRootName() {
        return "resources";
    }

    private static Collection<String> getSecondNodeName() {
        return Arrays.asList(ELEMENT_STRING, ELEMENT_STRING_ARRAY, ELEMENT_PLURALS);
    }

    public static String getKeyAttributeName() {
        return "name";
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
        String parentBaseName = FilenameUtils.getBaseName(parentFile.getName());
        String[] tokens = parentBaseName.split(LANG_CODE_SEPARATOR);
        //get parent's parent path
        String path = "/";
        if (null != parentFile.getParentFile()) {
            path = FilenameUtils.normalize(parentFile.getParent(), true) + path;
        }
        if (path.startsWith(rootDir)) path = path.substring(rootDir.length() + 1);
        if (tokens.length > 1) return Pair.of(path + tokens[0], tokens[1]);
        return Pair.of(path + tokens[0], StringUtils.EMPTY);
    }


    /**
     * Retrieves  and removes the reference file in the collection.
     *
     * @param files dictionary file collection
     * @return reference file or null if not found
     */
    private File takeReferenceFileFromCollection(String rootDir, Collection<File> files) {

        for (File file : files) {
            Pair<String, String> pBaseNameAndLangCode = getFileLangCodeAndBaseName(rootDir, file);
            if (StringUtils.isEmpty(pBaseNameAndLangCode.getRight())) {
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
        Collection<File> files = FileUtils.listFiles(rootFile, xmlFilter, TrueFileFilter.INSTANCE);
        MultiValueMap<String, File> dictionariesGroups = new LinkedMultiValueMap<String, File>();
        for (File subFile : files) {
            Pair<String, String> pBaseNameAndLangCode = getFileLangCodeAndBaseName(FilenameUtils.normalize(rootFile.getAbsolutePath(), true), subFile);
            if (pBaseNameAndLangCode.getLeft().endsWith(PARENT_BASE_NAME))
                dictionariesGroups.add(pBaseNameAndLangCode.getLeft() + "/" + subFile.getName(), subFile);
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
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        Dictionary dict = readReferenceFile(dictName, refFile, nameLangCodePair.getValue(), warnings);
        acceptedFiles.add(refFile);
        // read other language translations

        int sortNo = 1;

        BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_ANDROID_XML_ERROR);

        for (File file : filesInDict) {
            nameLangCodePair = getFileLangCodeAndBaseName(rootDir, file);
            // add DictionaryLanguage for dict
            DictionaryLanguage dictionaryLanguage = getDictionaryLanguage(nameLangCodePair.getValue(), sortNo);
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
        log.info("Parsing reference android xml file '" + refFile.getAbsolutePath() + "'");
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
        if (!file.exists()) return deliveredDictionaries;

        if (file.isFile()) { // single file import, only reference file is permitted.
            deliveredDictionaries.add(parseDictionary(rootDir, null, Arrays.asList(file), acceptedFiles));
            return deliveredDictionaries;
        }

        // file is directory
        MultiValueMap<String, File> dictionariesGroups = groupingFiles(file);
        Set<Map.Entry<String, List<File>>> entries = dictionariesGroups.entrySet();
        for (Map.Entry<String, List<File>> entry : entries) {
            try {
                String baseName = entry.getKey();
                List<File> dictFiles = entry.getValue();
                deliveredDictionaries.add(parseDictionary(rootDir, baseName, dictFiles, acceptedFiles));
            } catch (BusinessException e) {
                // Ignore INVALID_XML_FILE error because the file can be another type of xml dictionary.
                if (e.getErrorCode() != BusinessException.INVALID_XML_FILE) {
                    exceptions.addNestedException(e);
                }
            }
        }
        return deliveredDictionaries;
    }

    /**
     * Get Comments for before a node, node iterator will forward to next node
     *
     * @param nodeIterator node iterator
     * @return A pair which left element will be the comment above this node and the right element
     * will be the node.
     */
    private Pair<String, Node> readCommentsAboveNode(Iterator<Node> nodeIterator) {
        List<String> lines = new ArrayList<String>();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return Pair.of(StringUtils.join(lines, "\n"), node);
            }
            String text = node.getStringValue();
            if (!text.contains(DictionaryGenerator.GEN_SIGN)) {
                String comment = node.getStringValue();
                if (node.getNodeType() == Node.COMMENT_NODE) {
                    comment = StringEscapeUtils.escapeJava(comment);
                }
                lines.add(comment);
            }
        }
        return Pair.of(StringUtils.join(lines, "\n"), null);
    }

    @SuppressWarnings("unchecked")
    private String elemAttributeToString(Element element) {
        return Util.map2String(Util.attributeList2Map(element.attributes()), "\n");
    }

    @SuppressWarnings("unchecked")
    private String elemNamespacesToString(Element element) {
        List<Namespace> nsList = element.declaredNamespaces();
        List<String> nsStrings = new ArrayList<String>();
        for (Namespace ns : nsList) {
            nsStrings.add(StringUtils.join(Arrays.asList(ns.getPrefix(), ns.getURI()), "="));
        }
        return StringUtils.join(nsStrings, "\n");
    }

    @SuppressWarnings("unchecked")
    private String elemProcessingInstructionsToString(Element element) {
        List<ProcessingInstruction> piList = element.processingInstructions();
        List<String> strings = new ArrayList<String>();
        for (ProcessingInstruction pi : piList) {
            strings.add(StringUtils.join(Arrays.asList(pi.getTarget(), pi.getText()), "="));
        }
        return StringUtils.join(strings, "\n");
    }

    @SuppressWarnings("unchecked")
    private void readLabels(File file, Dictionary dict, DictionaryLanguage dl, Collection<BusinessWarning> warnings) {
        String langCode = dl.getLanguageCode();
        boolean isReference = langCode.equals(dict.getLanguageReferenceCode());

        Document document;
        Element root;
        try {
            SAXReader domReader = new SAXReader();
            // avoid downloading external DTD
            domReader.setEntityResolver(new NoOpEntityResolver());
            document = domReader.read(file);
        } catch (Exception e1) {
            log.error("Error parsing " + file.getName(), e1);
            throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
        }

        root = document.getRootElement();
        if (!root.getName().equals(getRootName())) {
            throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
        }

        if (isReference) { // ref root comments stored in dict level
            // read root comments
            Pair<String, Node> pair = readCommentsAboveNode(document.nodeIterator());
            dict.setAnnotation2(pair.getLeft());
            // read attributes of root element
            dict.setAnnotation1(elemAttributeToString(root));
            // read namespaces of root element
            dict.setAnnotation3(elemNamespacesToString(root));
            // read processing instructions
            dict.setAnnotation4(elemProcessingInstructionsToString(root));
        }
        // other language level saved in each label translation annotation


        int sortNo = 1;

        Iterator<Node> nodeIterator = root.nodeIterator();

        while (nodeIterator.hasNext()) {
            Pair<String, Node> pair = readCommentsAboveNode(nodeIterator);
            String comments = pair.getLeft();
            Node node = pair.getRight();
            if (null == node) break;

            if (Node.ELEMENT_NODE != node.getNodeType() || !(node instanceof Element)) continue;
            // not valid android xml file
            if (!getSecondNodeName().contains(node.getName()))
                throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());

            Element element = (Element) node;
            String key = element.attributeValue(getKeyAttributeName());

            // precess label according to different element type
            int size = 0;
            if (element.getName().equals(ELEMENT_STRING)) {
                size = readElement(dict, element, key, comments, sortNo, dl, warnings);
            } else if (multiItemElements.contains(element.getName())) {
                size = readStringArrayOrPluralsElement(dict, element, key, comments, sortNo, dl, warnings);
            }
            sortNo += size;
        }
    }

    private int readElement(Dictionary dict, Element element, String key, String comments, int sortNo, DictionaryLanguage dl, Collection<BusinessWarning> warnings) {
        return this.readElement(dict, element, key, comments, sortNo, dl, warnings, false);
    }

    private int readElement(Dictionary dict, Element element, String key, String comments, int sortNo, DictionaryLanguage dl, Collection<BusinessWarning> warnings, boolean isMultipleElement) {
        boolean isReference = dl.getLanguageCode().equals(REFERENCE_LANG_CODE);
        String nodeAttributes = elemAttributeToString(element);

        if (isReference) {
            Label label = new Label();
            label.setKey(key);
            label.setReference(element.getStringValue().trim());
            label.setAnnotation1(nodeAttributes);
            label.setAnnotation2(comments);
            if(isMultipleElement){
                label.setAnnotation3(Util.map2String(ImmutableMap.of("isMultipleElement", "true")));
            }
            label.setSortNo(sortNo);
            label.setOrigTranslations(new ArrayList<LabelTranslation>());

            dict.addLabel(label);
            label.setDictionary(dict);
            return 1;
        }

        // add a new label translation
        Label label = dict.getLabel(key);
        if (null == label) {
            warnings.add(new BusinessWarning(BusinessWarning.ANDROID_LABEL_KEY_BLANK, key, dl.getLanguageCode()));
            return 0;
        }

        LabelTranslation lt = new LabelTranslation();
        lt.setAnnotation1(nodeAttributes);
        lt.setAnnotation2(comments);
        lt.setSortNo(sortNo);


        lt.setLanguage(dl.getLanguage());
        lt.setLanguageCode(dl.getLanguageCode());
        String origTranslation = element.getStringValue().trim();

        lt.setStatus(Translation.STATUS_TRANSLATED);
        lt.setOrigTranslation(origTranslation);


        lt.setLabel(label);
        label.addLabelTranslation(lt);
        return 1;
    }

    private String base64Encoding(String source) {
        String charset = "iso8859-1";
        String result = null;
        try {
            result = new String(Base64.encodeBase64(source.getBytes(charset)), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private int readStringArrayOrPluralsElement(Dictionary dict, Element element, String key, String comments, int sortNo, DictionaryLanguage dl, Collection<BusinessWarning> warnings) {
        String elemName = element.getName();
        // how to save comments above this group which is parameter comments
        //get sub node

        Iterator<Node> nodeIterator = element.nodeIterator();
        int num = 0;

        //encode the first comment with base64
        while (nodeIterator.hasNext()) {
            Pair<String, Node> pair = readCommentsAboveNode(nodeIterator);
            String subNodeComments = pair.getLeft();
            Node node = pair.getRight();
            if (null == node) break;
            if (Node.ELEMENT_NODE != node.getNodeType() || !(node instanceof Element)) continue;
            // not valid android xml file
            Element item = (Element) node;
            String subKey = elemName + KEY_SEPARATOR + key + KEY_SEPARATOR + num;
            if (0 == num) {
                subNodeComments = base64Encoding(comments) + ";" + base64Encoding(subNodeComments);
            }
            num += readElement(dict, item, subKey, subNodeComments, sortNo, dl, warnings, true);
        }
        return num;
    }


}
