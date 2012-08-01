package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Text;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 12-7-31
 * Time: 上午11:57
 * To change this template use File | Settings | File Templates.
 */

@Service("mdcParser")
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
                labels.add(label);

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

        Set<String> dictLangCodes = dictionary.getAllLanguageCodes();

        BusinessException exceptions = new BusinessException(
                BusinessException.NESTED_LABEL_ERROR, label.getKey());

        Map<String, String> entriesInLabel = new HashMap<String, String>();

        List<Element>  subElements=elem.elements();
        for(Element subElement: subElements) {
            Attribute attribute= (Attribute) subElement.selectSingleNode("@id");
            String langCode= attribute.getValue().trim();
            String translatedString=subElement.getStringValue().toString();
            log.debug(String.format("langCode=%s, translatedString=%s", langCode, translatedString));
            entriesInLabel.put(langCode,translatedString);
       }
        String gae= entriesInLabel.get("en-GB");
        if (null == gae) {
            exceptions.addNestedException(new BusinessException(
                    BusinessException.NO_REFERENCE_TEXT, label
                    .getKey()));
        }
        label.setReference(gae);
        Text text = new Text();
        text.setContext(context);
        text.setReference(gae);
        text.setStatus(0);

        Collection<Translation> translations = new HashSet<Translation>();
        Translation trans = null;

        for (Map.Entry<String, String> entry : entriesInLabel.entrySet()) {

            if (entry.getKey().equals("GAE")) {
                continue;
            }

            trans = new Translation();
            trans.setText(text);
            DictionaryLanguage dl=dictionary.getDictLanguage(entry.getKey());
            Language language= null==dl ? null:dl.getLanguage();

            trans.setLanguage(language);
            trans.setTranslation(entry.getValue());
            translations.add(trans);
        }
        text.setTranslations(translations);

        label.setText(text);
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
            dls.add(dl);
        }
        return dls;
    }
}
