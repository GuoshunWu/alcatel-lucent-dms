package com.alcatel_lucent.dms.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 12-7-31
 * Time: 上午11:57
 * To change this template use File | Settings | File Templates.
 */

@Service("mdcParser")
@SuppressWarnings("unchecked")
public class MDCParser {

    @Autowired
    private LanguageService languageService;

    private Logger log= Logger.getLogger(MDCParser.class);
    
    public Dictionary parse(Application app, String dictionaryName,
                     String path, InputStream dctInputStream, Collection<BusinessWarning> warnings) throws IOException, DocumentException, ParserConfigurationException, SAXException {


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(dctInputStream);

        DOMReader domReader = new DOMReader();
        Document document = domReader.read(doc);

        Dictionary dictionary=new Dictionary();
        dictionary.setName(dictionaryName);
        dictionary.setPath(path);
        dictionary.setEncoding("UTF-8");
        dictionary.setFormat("xml");
        dictionary.setApplication(app);
        
        Context context=new Context(dictionary.getName());


        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dictionary.getName());

        dictionary.setDictLanguages(readLanguages(document, dictionary, nonBreakExceptions));
        List nodes = document.selectNodes("/dictionary/messageString/*");
        Label label=null;
        Collection<Label> labels=new ArrayList<Label>();
        HashSet<String> labelKeys = new HashSet<String>();
        for(Object node: nodes){
            try{
                label=readLabel((Element)node, dictionary, context,warnings);

            }catch (BusinessException e){
                nonBreakExceptions.addNestedException(e);
            }
           if (labelKeys.contains(label.getKey())) {
               //TODO: warning here.
                warnings.add(new BusinessWarning(
                        BusinessWarning.DUPLICATE_LABEL_KEY, -1,
                        label.getKey()));
            } else {
                labelKeys.add(label.getKey());
                labels.add(label);
            }
        }
        dictionary.setLabels(labels);
        if(nonBreakExceptions.hasNestedException()){
            throw nonBreakExceptions;
        }
        return dictionary;
    }

    private Label readLabel(Element elem,Dictionary dictionary, Context context, Collection<BusinessWarning> warnings) throws BusinessException{
        Label label=new Label();
        label.setKey(elem.getName());
        label.setDictionary(dictionary);
        label.setContext(context);
        label.setDescription(null);
        label.setMaxLength(null);

        //en-GB is reference

//        Set<String> dictLangCodes = dictionary.getAllLanguageCodes();

        BusinessException exceptions = new BusinessException(
                BusinessException.NESTED_LABEL_ERROR, label.getKey());

        Map<String, String> entriesInLabel = new HashMap<String, String>();
        ArrayList<String> orderedLangCodes = new ArrayList<String>();

        List<Element>  subElements=elem.elements();
        for(Element subElement: subElements) {
            Attribute attribute= (Attribute) subElement.selectSingleNode("@id");
            String langCode= attribute.getValue().trim();
            String translatedString=subElement.getStringValue().toString();
            log.debug(String.format("langCode=%s, translatedString=%s", langCode, translatedString));
            entriesInLabel.put(langCode,translatedString);
            orderedLangCodes.add(langCode);
       }
        String gae= entriesInLabel.get("en-GB");
        if (null == gae) {
            exceptions.addNestedException(new BusinessException(
                    BusinessException.NO_REFERENCE_TEXT, label
                    .getKey()));
        }
        label.setReference(gae);

        Collection<LabelTranslation> translations = new HashSet<LabelTranslation>();
        int labelSortNo = 1;
        for (String oLangCode : orderedLangCodes) {

            if (oLangCode.equals("GAE")) {
                continue;
            }

            LabelTranslation trans = new LabelTranslation();
            trans.setLabel(label);
            DictionaryLanguage dl=dictionary.getDictLanguage(oLangCode);
            Language language= null==dl ? null:dl.getLanguage();

            trans.setLanguage(language);
            trans.setLanguageCode(oLangCode);
            trans.setOrigTranslation(entriesInLabel.get(oLangCode));
            trans.setSortNo(labelSortNo++);

            translations.add(trans);
        }
        label.setOrigTranslations(translations);

        label.setMaxLength(null);

        if(exceptions.hasNestedException()){
            throw exceptions;
        }
        return label;
    }

    /**
     *
     * */
    private Collection<DictionaryLanguage> readLanguages(Document document, Dictionary dictionary, BusinessException exception) {
        HashSet<String> languageSet = new HashSet<String>();
        List nodes = document.selectNodes("/dictionary/messageString/*/lang/@id");
        for (Object node : nodes) {
            languageSet.add(((Attribute) node).getValue());
        }
        HashSet<DictionaryLanguage> dls = new HashSet<DictionaryLanguage>();
        DictionaryLanguage dl = null;
        int sortNo = 1;
        for (String langCode : languageSet) {
            dl = new DictionaryLanguage();
            dl.setLanguageCode(langCode);
            dl.setDictionary(dictionary);

            Language language = languageService.getLanguage(langCode);
            if (null == language) {
                exception.addNestedException(new BusinessException(BusinessException.UNKNOWN_LANG_CODE, -1, langCode));
            }
            dl.setLanguage(language);
            Charset charset = null;
            // DictionaryLanguage CharSet is the dictionary encoding
            charset = languageService.getCharsets().get(dictionary.getEncoding());
            if (null == charset) {
                exception.addNestedException(new BusinessException(BusinessException.CHARSET_NOT_FOUND, dictionary.getEncoding()));
            }
            dl.setCharset(charset);
            dl.setSortNo(sortNo++);
            dls.add(dl);
        }
        return dls;
    }
}
