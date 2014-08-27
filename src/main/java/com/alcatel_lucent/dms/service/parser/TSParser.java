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
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

@Component("TsParser")
public class TSParser extends DictionaryParser {

    public static final String REFERENCE_LANG_CODE = "en_US";
    private static final String[] extensions = new String[]{"ts"};

    public static final String DEFAULT_FILE_ENCODING = "UTF-8";
    public static final String ROOT_ELEMENT_NAME = "TS";
    public static final String SECOND_NODE_NAME = "context";

    public static final String CONTEXT_KEY_SEPARATOR = "/";

    private final SuffixFileFilter tsFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    @Autowired
    private LanguageService languageService;

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        if (exceptions.hasNestedException()) throw exceptions;
        return result;
    }

    private String[] splitFileName(File file) {
        return splitFileName(file.getName());
    }

    private String getFileDictName(String rootDir, File file) {
        String normalizedRoot = normalize(rootDir);
        String dictName = normalize(file.getAbsolutePath());
        dictName = dictName.substring(normalizedRoot.length() + 1);
        String[] nameParts = splitFileName(file);
        if (null == nameParts) throw new BusinessException(BusinessException.INVALID_XML_FILE);
        dictName = dictName.replaceAll(String.format("[-_/\\.\\\\]?%s(?:\\.%s)?$", nameParts[2], FilenameUtils.getExtension(dictName)), "");
        return dictName;
    }

    private ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        ArrayList<Dictionary> deliveredDictionaries = new ArrayList<Dictionary>();
        File rootFile = new File(rootDir);
        if (!rootFile.exists()) return deliveredDictionaries;


        if (rootFile.isFile()) { // single file import
//            only reference file is permitted.
            String[] nameParts = splitFileName(rootFile);
            if (null == nameParts || !nameParts[2].equalsIgnoreCase(REFERENCE_LANG_CODE))
                return deliveredDictionaries;

            String dictName = getFileDictName(rootDir, rootFile);
            Dictionary dictionary = parseDictionary(dictName, Arrays.asList(rootFile), acceptedFiles);
            deliveredDictionaries.add(dictionary);
            return deliveredDictionaries;
        }

        //group dictionaries
        Collection<File> files = FileUtils.listFiles(rootFile, tsFilter, TrueFileFilter.INSTANCE);
        MultiValueMap<String, File> dictionaries = new LinkedMultiValueMap<String, File>();

        for (File subFile : files) {
            dictionaries.add(getFileDictName(rootDir, subFile), subFile);
        }

        Set<Map.Entry<String, List<File>>> dictionaryEntries = dictionaries.entrySet();
        for (Map.Entry<String, List<File>> dictionaryEntry : dictionaryEntries) {
            try {
                deliveredDictionaries.add(parseDictionary(dictionaryEntry.getKey(), dictionaryEntry.getValue(), acceptedFiles));
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
        }
        if (exceptions.hasNestedException()) {
            throw exceptions;
        }

        return deliveredDictionaries;
    }

    private Dictionary parseDictionary(String dictName, List<File> dictionaryFiles, Collection<File> acceptedFiles) {
        //check if the reference file exists
        File referenceFile = null;
        Iterator<File> dictIterator = dictionaryFiles.iterator();
        while (dictIterator.hasNext()) {
            File dictFile = dictIterator.next();
            String[] nameParts = splitFileName(dictFile);
            if (REFERENCE_LANG_CODE.equalsIgnoreCase(nameParts[2])) {
                referenceFile = dictFile;
                dictIterator.remove();
                break;
            }
        }
        if (null == referenceFile) {
            throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE);
        }
        Dictionary dictionary = parseReferenceFile(dictName, referenceFile);
        acceptedFiles.add(referenceFile);
        int sortNo = 1;
        for (File langFile : dictionaryFiles) {
            readLabels(langFile, dictionary, sortNo++);
            acceptedFiles.add(langFile);
        }
        return dictionary;
    }


    private Dictionary parseReferenceFile(String dictName, File file) {
        log.info("Parsing reference js file '" + file.getName() + "'");
        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        String path = normalize(file.getAbsolutePath());
        path = path.substring(0, FilenameUtils.indexOfLastSeparator(path)) + dictName;
        dictBase.setPath(path);
        dictBase.setEncoding(DEFAULT_FILE_ENCODING);
        dictBase.setFormat(getFormat().toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setReferenceLanguage(REFERENCE_LANG_CODE);

        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setParseWarnings(new ArrayList<BusinessWarning>());

        readLabels(file, dictionary, 0);

        return dictionary;
    }

    private void readLabels(File file, Dictionary dict, int dictLangSortNo) {
        String[] nameParts = splitFileName(file);
        String languageCode = nameParts[2];
        Collection<BusinessWarning> warnings = dict.getParseWarnings();

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
        if (!root.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
        }

        // read root comments
        StringBuffer rootComments = new StringBuffer();
        Iterator<Node> nodeIter = document.nodeIterator();
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
            if (node.getNodeType() == Node.COMMENT_NODE) {
                String text = node.getStringValue();
                if (!text.contains(DictionaryGenerator.GEN_SIGN)) {
                    String escapedComment = node.getStringValue().replace("\\", "\\\\");
                    escapedComment = escapedComment.replace("\n", "\\n");
                    rootComments.append(escapedComment).append("\n");
                }
            }
        }
        if (rootComments.length() > 0) {
            dict.setAnnotation2(rootComments.substring(0, rootComments.length() - 1));
        }

        Attribute rootAttrLanguage = root.attribute("language");
        if (null != rootAttrLanguage) {
            languageCode = rootAttrLanguage.getText().trim();
        }
        // add reference dictLanguage object
        boolean isReferenceFile = dict.getLanguageReferenceCode().equals(languageCode);
        DictionaryLanguage dl = getDictionaryLanguage(languageCode, dictLangSortNo);
        dl.setDictionary(dict);
        dict.addDictLanguage(dl);


        // read attributes of root element
        List<Attribute> rootAttributes = root.attributes();
        dict.setAnnotation1(Util.map2String(Util.attributeList2Map(root.attributes())));

        int sortNo = 1;
        StringBuffer comments = new StringBuffer();
        Iterator<Node> iter = root.nodeIterator();

        while (iter.hasNext()) {
            Node context = iter.next();
            if (context.getNodeType() == Node.COMMENT_NODE) {
                comments.append(context.getStringValue()).append("\n");
                continue;
            }
            if (context.getNodeType() != Node.ELEMENT_NODE || !context.getName().equals(SECOND_NODE_NAME)) {
                continue;
            }
            if (!(context instanceof Element)) continue;
            Element ctx = (Element) context;
            String contextName = (String) ctx.selectObject("string(name)");
            List<Element> messages = ctx.selectNodes("message");
            // each message is a label.
            for (Element message : messages) {
                List<Element> locations = message.selectNodes("location");
                String key = (String) message.selectObject("string(source)");
                // skip those message with no key
                if (Strings.isNullOrEmpty(key)) continue;

                key = contextName + CONTEXT_KEY_SEPARATOR + key;
                Element transElem = (Element) message.selectSingleNode("translation");
                String value = transElem.getTextTrim();
                String translatorComment = (String) message.selectObject("string(translatorcomment)");
                Label label = null;
                Attribute typeAttr = transElem.attribute("type");
                if (isReferenceFile) {
                    label = new Label(key);
                    label.setReference(value);
                    label.setSortNo(sortNo);
                    label.setDictionary(dict);
                    // add locations as label attachments
                    label.setAnnotation1(locationListToString(locations));
                    label.putKeyValuePairToField("context", contextName, Label.ANNOTATION2);
                    if (!Strings.isNullOrEmpty(translatorComment)) {
                        label.putKeyValuePairToField("translatorcomment", translatorComment, Label.ANNOTATION3);
                    }
                    label.setOrigTranslations(new ArrayList<LabelTranslation>());
                    if (null != typeAttr) {
                        label.putKeyValuePairToField("type", typeAttr.getStringValue(), Label.ANNOTATION3);
                        warnings.add(new BusinessWarning(BusinessWarning.TS_REFERENCE_UNFINISHED, key, dict.getName()));
                    }
                    dict.addLabel(label);
                } else if (null == typeAttr || !"unfinished".equals(typeAttr.getStringValue())) {
                    label = dict.getLabel(key);
                    LabelTranslation lt = new LabelTranslation();
                    lt.setLabel(label);
                    lt.setLanguageCode(languageCode);
                    lt.setSortNo(dictLangSortNo);
                    lt.setOrigTranslation(value);
                    lt.setAnnotation1(locationListToString(locations));

                    if (!Strings.isNullOrEmpty(translatorComment)) {
                        lt.putKeyValuePairToField("translatorcomment", translatorComment, LabelTranslation.ANNOTATION2);
                    }
                    lt.setLanguage(label.getDictionary().getLanguageByCode(languageCode));
                    lt.setTranslationType(Translation.STATUS_TRANSLATED);
                    label.addLabelTranslation(lt);
                }
                sortNo++;
            }
        }
    }

    /**
     * convert a note list into string
     */
    private Map<String, String> locationListToString(List<Element> locations) {
        Map<String, String> locMap = new LinkedHashMap<String, String>();
        for (Element location : locations) {
            String fileName = location.attributeValue("filename");
            String lineNumber = location.attributeValue("line");
            String current = locMap.get(fileName);
            locMap.put(fileName, null == current ? lineNumber : current + ", " + lineNumber);
        }
        return locMap;
    }

    private DictionaryLanguage getDictionaryLanguage(String langCode, int sortNo) {
        DictionaryLanguage dictLanguage = new DictionaryLanguage();
        dictLanguage.setLanguageCode(langCode);
        dictLanguage.setLanguage(languageService.getLanguage(langCode));
        dictLanguage.setCharset(languageService.getCharset(DEFAULT_FILE_ENCODING));
        dictLanguage.setSortNo(sortNo);
        return dictLanguage;
    }


    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.TS;
    }

}
