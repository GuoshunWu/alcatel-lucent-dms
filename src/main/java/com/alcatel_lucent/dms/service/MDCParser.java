package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Guoshun.Wu
 * Date: 12-7-31
 * Time: 上午11:57
 * To change this template use File | Settings | File Templates.
 */
public class MDCParser {

    @Autowired
    private LanguageService languageService;

    private Logger log= Logger.getLogger(MDCParser.class);
    
    Dictionary parse() throws IOException, DocumentException, ParserConfigurationException, SAXException {
        String file = "D:\\tmp\\CA\\6.6.000.107.a\\smart_prs\\prs\\smartprs\\etc\\conf\\dictionary.conf";
        FileInputStream fis = new FileInputStream(file);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(fis);

        DOMReader domReader = new DOMReader();
        Document document = domReader.read(doc);
        Application app=new Application();

        Dictionary dictionary=new Dictionary();
        dictionary.setName(file);
        dictionary.setPath(file);
        dictionary.setEncoding("UTF-8");
        dictionary.setFormat("xml");
        dictionary.setApplication(app);
        
        Context context=new Context(dictionary.getName());


        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dictionary.getName());

        HashSet<BusinessWarning> warnings=new HashSet<BusinessWarning>();

//        dictionary.setDictLanguages(readLanguages(document, dictionary, nonBreakExceptions));
        List nodes = document.selectNodes("/dictionary/messageString/*");
        Label label=null;
        for(Object node: nodes){
            try{
                label=readLabel((Element)node, dictionary, context,warnings);
            }catch (BusinessException e){
                nonBreakExceptions.addNestedException(e);
            }
            break;
        }

        fis.close();

        if(nonBreakExceptions.hasNestedException()){
            throw nonBreakExceptions;
        }
        return dictionary;
    }

    public static void main(String[] args) throws Exception {
        MDCParser parser = new MDCParser();
        Dictionary dict = parser.parse();

    }

    public Label readLabel(Element elem,Dictionary dictionary, Context context, Collection<BusinessWarning> warnings) throws BusinessException{
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

            if (!dictLangCodes.contains(langCode)) {
                exceptions.addNestedException(new BusinessException(
                        BusinessException.UNDEFINED_LANG_CODE, -1,langCode, label.getKey()));
            }

            String translatedString=subElement.getStringValue().toString();
            log.debug(String.format("langCode=%s, translatedString=%s", langCode, translatedString));
            entriesInLabel.put(langCode,translatedString);
       }

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
