package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.util.NoOpEntityResolver;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.ClosureUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.*;

@Component
public class AndroidXMLParser extends DictionaryParser {

    public static final String[] extensions = new String[]{"xml"};
    private SuffixFileFilter xmlFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
    public static String LANG_CODE_SEPARATOR = "-";
    public static String REFERENCE_LANG_CODE = "en";

    public static final String ELEMENT_STRING = "string";
    public static final String ELEMENT_STRING_ARRAY = "string-array";
    public static final String ELEMENT_PLURALS = "plurals";

    @Autowired
    private LanguageService languageService;

    /**
     * Parse dictionaries into object.
     *
     * @param rootDir       part of path to be trimmed in dictionary name
     * @param file          directory or file to be parsed, if file is a dictionary, files under the dictionary will be parsed recursively.
     * @param acceptedFiles output parameter, holder of accepted files list
     * @return list of Dictionary objects
     * @throws com.alcatel_lucent.dms.BusinessException
     *          fatal errors, multiple exceptions can be put into a NESTED_ERROR exception.
     *          It's suggested to go through all files before throw a fatal error, so that user can get as more as possible
     *          information about errors.
     */
    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        if (exceptions.hasNestedException()) {
            throw exceptions;
        } else {
            return result;
        }
    }

    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.XML_ANDROID;
    }

    protected String getRootName() {
        return "resources";
    }

    protected Collection<String> getSecondNodeName() {
        return Arrays.asList(ELEMENT_STRING, ELEMENT_STRING_ARRAY, ELEMENT_PLURALS);
    }

    protected String getKeyAttributeName() {
        return "name";
    }

    /**
     * Get file parent base name and language code
     *
     * @param file file
     * @return a pair which left element is parent base name and right is language code, if not language code, the
     *         right element would be empty string.
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
        if (tokens.length > 2) return Pair.of(path + tokens[0], tokens[1]);
        return Pair.of(path + tokens[0], StringUtils.EMPTY);
    }


    /**
     * Retrieves  and removes the reference file in the collection.
     *
     * @param files dictionary file collection
     * @return reference file or null if not found
     */
    private File takeReferenceFileFromCollection(String rootDir, Collection<File> files) {
        File referenceFile = null;
        File noLangCodeFile = null;
        for (File file : files) {
            Pair<String, String> pBaseNameAndLangCode = getFileLangCodeAndBaseName(rootDir, file);
            if (pBaseNameAndLangCode.getRight().equals(REFERENCE_LANG_CODE)) referenceFile = file;
            if (StringUtils.isEmpty(pBaseNameAndLangCode.getRight())) noLangCodeFile = file;
        }
        if (null == referenceFile) referenceFile = noLangCodeFile;
        // remove reference file from the collection
        files.remove(referenceFile);
        files.remove(noLangCodeFile);
        return referenceFile;
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
            dictionariesGroups.add(pBaseNameAndLangCode.getLeft() + "/" + subFile.getName(), subFile);
        }
        return dictionariesGroups;
    }

    private Dictionary parseDictionary(String rootDir, String dictName, Collection<File> filesInDict) throws BusinessException {
        File refFile = takeReferenceFileFromCollection(rootDir, filesInDict);
        if (null == refFile) {
            throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE);
        }
        Pair<String, String> nameLangCodePair = getFileLangCodeAndBaseName(rootDir, refFile);
        if (null == dictName) {
            dictName = nameLangCodePair.getKey();
        }
        Dictionary dict = readReferenceFile(dictName, refFile, nameLangCodePair.getValue());
        // read other language translations

        for (File file : filesInDict) {
            nameLangCodePair = getFileLangCodeAndBaseName(rootDir, refFile);
        }

        return null;
    }

    private Dictionary readReferenceFile(String dictName, File refFile, String refLangCode) {
        if (StringUtils.isEmpty(refLangCode)) refLangCode = REFERENCE_LANG_CODE;
        // create dictionary.
        log.info("Parsing label xml file '" + refFile.getAbsolutePath() + "'");
        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(refFile.getAbsolutePath());
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(getFormat().toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setReferenceLanguage(refLangCode);

        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_FILE_ERROR, refFile.getName());
        // read reference file
        // add reference dictLanguage object
        DictionaryLanguage refDictLanguage = new DictionaryLanguage();
        refDictLanguage.setLanguageCode(refLangCode);
        refDictLanguage.setSortNo(0);
        refDictLanguage.setLanguage(languageService.getLanguage(refLangCode));
        refDictLanguage.setCharset(languageService.getCharset("UTF-8"));
        dictLanguages.add(refDictLanguage);

        dictionary.setLabels(readLabels(refFile, dictionary, refDictLanguage, warnings, refFileExceptions));
        return dictionary;

    }

    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        rootDir = FilenameUtils.normalize(rootDir, true);
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        if (file.isFile()) { // single file import, only reference file is permitted.
            deliveredDicts.add(parseDictionary(rootDir, null, Arrays.asList(file)));
            return deliveredDicts;
        }

        // file is directory
        MultiValueMap<String, File> dictionariesGroups = groupingFiles(file);
        Set<Map.Entry<String, List<File>>> entries = dictionariesGroups.entrySet();
        for (Map.Entry<String, List<File>> entry : entries) {
            try {
                deliveredDicts.add(parseDictionary(rootDir, entry.getKey(), entry.getValue()));
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
        }
        return deliveredDicts;
    }

    /**
     * Get Comments for before a node, node iterator will forward to next node
     *
     * @param nodeIterator node iterator
     * @return A pair which left element will be the comment above this node and the right element
     *         will be the node.
     */
    private Pair<String, Node> readCommentsAboveNode(Iterator<Node> nodeIterator) {
        List<String> lines = new ArrayList<String>();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            if (node.getNodeType() != Node.COMMENT_NODE) {
                return Pair.of(StringUtils.join(lines, "\n"), node);
            }
            String text = node.getStringValue();
            if (!text.startsWith(DictionaryGenerator.GEN_SIGN)) {
                String escapedComment = node.getStringValue().replace("\\", "\\\\");
                escapedComment = escapedComment.replace("\n", "\\n");
                lines.add(escapedComment);
            }
        }
        return Pair.of(StringUtils.join(lines, "\n"), null);
    }

    private String elemAttributeToString(Element element) {
        return Util.map2String(Util.attributeList2Map(element.attributes()), "\n");
    }

    private String elemNamespacesToString(Element element) {
        List<Namespace> nsList = element.declaredNamespaces();
        List<String> nsStrings = new ArrayList<String>();
        for (Namespace ns : nsList) {
            nsStrings.add(StringUtils.join(Arrays.asList(ns.getPrefix(), ns.getURI()), "="));
        }
        return StringUtils.join(nsStrings, "\n");
    }

    private String elemProcessingInstructionsToString(Element element) {
        List<ProcessingInstruction> piList = element.processingInstructions();
        List<String> strings = new ArrayList<String>();
        for (ProcessingInstruction pi : piList) {
            strings.add(StringUtils.join(Arrays.asList(pi.getTarget(), pi.getText()), "="));
        }
        return StringUtils.join(strings, "\n");
    }

    public Collection<Label> readLabels(File file, Dictionary dict, DictionaryLanguage dl, Collection<BusinessWarning> warnings, BusinessException exceptions) {
        String langCode = dl.getLanguageCode();

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

        if (langCode.equals(dict.getLanguageReferenceCode())) { // ref root comments stored in dict level
            // read root comments
            dict.setAnnotation2(readCommentsAboveNode(document.nodeIterator()).getLeft());
            // read attributes of root element
            dict.setAnnotation1(elemAttributeToString(root));
            // read namespaces of root element
            dict.setAnnotation3(elemNamespacesToString(root));
            // read processing instructions
            dict.setAnnotation4(elemProcessingInstructionsToString(root));
        } else { // other language level saved in each label translation annotation

        }

        int sortNo = 1;

        Iterator<Node> nodeIterator = root.nodeIterator();
        HashSet<String> keys = new HashSet<String>();
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
            // precess label according to different element type

            if(element.getName().equals(ELEMENT_STRING)){

            }else if(element.getName().equals(ELEMENT_STRING_ARRAY)){

            }else if(element.getName().equals(ELEMENT_PLURALS)){

            }else {
                // nothing to do
            }

            String nodeAttributes = elemAttributeToString(element);
            String key = element.attributeValue(getKeyAttributeName());



            if (keys.contains(key)) {
                // add this type of warning only for label
                warnings.add(new BusinessWarning(BusinessWarning.DUPLICATE_LABEL_KEY, 0, key));
                continue;
            }
            keys.add(key);

            Label label = new Label();
            label.setKey(key);
            label.setReference(node.getStringValue().trim());

            label.setAnnotation1(nodeAttributes);
            label.setAnnotation2(comments);

            label.setSortNo(sortNo++);
        }

        return null;
    }
}
