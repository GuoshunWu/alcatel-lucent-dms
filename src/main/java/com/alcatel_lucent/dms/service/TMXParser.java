package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.parser.DictionaryParser;
import com.alcatel_lucent.dms.util.Util;
import com.alcatel_lucent.dms.util.XDCPDTDEntityResolver;
import com.alcatel_lucent.dms.util.XDCTDTDEntityResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Component("TMXParser")
@SuppressWarnings("unchecked")
public class TMXParser extends DictionaryParser {

    private static final String[] extensions = new String[]{"tmx"};
    private static final SuffixFileFilter tmxFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    public final String REFERENCE_LANG_CODE = "en";

    private static final EntityResolver tmxDTDEntityResolver = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            URL dtdURL = getClass().getResource("/dtds/tmx14.dtd");
            InputSource is = new InputSource(dtdURL.toString());
            return is;
        }
    };
    private static Logger log = LoggerFactory.getLogger(TMXParser.class);

    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        return DictionaryFormat.TMX;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        Collection<File> tmxFiles = FileUtils.listFiles(file, tmxFilter, FileFilterUtils.directoryFileFilter());

        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);

        for (File tmxFile : tmxFiles) {
            try {
                deliveredDicts.add(processDictionary(tmxFile, FilenameUtils.normalize(rootDir, true), acceptedFiles));
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
        }

        if (exceptions.hasNestedException()) {
            throw exceptions;
        }

        return deliveredDicts;
    }

    public Dictionary processDictionary(File tmxFile, String rootDir, Collection<File> acceptedFiles) {
        log.info(center("Parsing dictionary '" + tmxFile, 50, '='));

        //validate if it is a valid tmx file
        SAXReader saxReader = new SAXReader();
        saxReader.setValidation(false);
        saxReader.setEntityResolver(tmxDTDEntityResolver);
        Document document = null;
        try {
            saxReader.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            document = saxReader.read(tmxFile);
        } catch (DocumentException e) {
            throw new BusinessException(BusinessException.INVALID_TMX_FILE, tmxFile.getName(), e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        DictionaryBase dictBase = new DictionaryBase();
        String dictName = FilenameUtils.normalize(tmxFile.getAbsolutePath(), true);
        if (dictName.startsWith(rootDir)) dictName = dictName.substring(rootDir.length() + 1);
        dictBase.setName(dictName);
        dictBase.setPath(FilenameUtils.normalize(tmxFile.getAbsolutePath()));
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(DictionaryFormat.TMX.toString());

        Dictionary dictionary = new Dictionary();

        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setPreviewErrors(new ArrayList<BusinessException>());
        dictionary.setParseWarnings(new ArrayList<BusinessWarning>());

        dictionary.setBase(dictBase);
        dictionary.setReferenceLanguage(REFERENCE_LANG_CODE);
        parseTMX(document, dictionary);
        acceptedFiles.add(tmxFile);
        return dictionary;
    }

    private void parseTMX(Document tmxDoc, Dictionary dict) throws BusinessException {
        if (null == tmxDoc) return;
        Collection<DictionaryLanguage> dictionaryLanguages = dict.getDictLanguages();

        Element tmxElement = tmxDoc.getRootElement();
        // save header to annotation1
        dict.setAnnotation1(tmxElement.element("header").asXML());
        Element body = tmxElement.element("body");
        List<Element> allTu = body.elements("tu");

        int index = 0;
        for (Element tu : allTu) {
            /*
            * if there is reference language in tu
            * */
            Boolean isReferenceExists = (Boolean) tu.selectObject("boolean(tuv/@xml:lang='" + dict.getLanguageReferenceCode() + "')");
            if (!isReferenceExists) {
                throw new BusinessException(BusinessException.TMX_LABEL_NO_REFERENCE_FOUND, tu.attributeValue("tuid"), dict.getName());
            }
            List<Element> tuvInTu = tu.elements("tuv");


            Label label = new Label();
            label.setSortNo(index);
            label.setOrigTranslations(new ArrayList<LabelTranslation>());

            removeElements(tu, tuvInTu);
            label.setAnnotation1(tu.asXML());
            label.setKey(tu.attributeValue("tuid"));

            //save tu element attributes
            label.setDictionary(dict);
            dict.getLabels().add(label);
            for (Element tuv : tuvInTu) {
                String lang = tuv.attributeValue(new QName("lang", new Namespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI)));
                DictionaryLanguage dictionaryLanguage = dict.getDictLanguage(lang);
                if (null == dictionaryLanguage) {
                    dictionaryLanguage = new DictionaryLanguage();
                    dictionaryLanguage.setDictionary(dict);
                    dictionaryLanguage.setLanguageCode(lang);
                    dictionaryLanguage.setSortNo(index);
                    Language language = languageService.getLanguage(lang);
                    dictionaryLanguage.setLanguage(language);
                    String encoding = StringUtils.defaultString(tuv.attributeValue("o-encoding"), dict.getEncoding());
                    Charset charset = languageService.getCharset(encoding);
                    dictionaryLanguage.setCharset(charset);
                    dictionaryLanguages.add(dictionaryLanguage);
                }

                Element seg = tuv.element("seg");
                tuv.remove(seg);
                // tuv is reference language
                if (lang.equalsIgnoreCase(dict.getReferenceLanguage())) {
                    label.setAnnotation2(tuv.asXML());
                    label.setReference(seg.hasMixedContent() ? seg.asXML() : seg.getTextTrim());
                    label.setAnnotation3("mixedContent=" + seg.hasMixedContent());
                    continue;
                }

                // normal translation
                LabelTranslation lt = new LabelTranslation();
                label.getOrigTranslations().add(lt);
                lt.setLabel(label);

                lt.setLanguage(dictionaryLanguage.getLanguage());
                lt.setLanguageCode(dictionaryLanguage.getLanguageCode());
                lt.setSortNo(index);
                lt.setAnnotation1(tuv.asXML());

                lt.setOrigTranslation(seg.hasMixedContent() ? seg.asXML() : seg.getTextTrim());
                lt.setAnnotation2("mixedContent=" + seg.hasMixedContent());

                if (null == label.getReference()) label.setReference("");
            }

            index++;

        }
    }

    private void removeElements(Element me, List<Element> children) {
        for (Element child : children) {
            me.remove(child);
        }
    }

}
