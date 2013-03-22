package com.alcatel_lucent.dms.service.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import com.alcatel_lucent.dms.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DaoService;

@Component(Constants.DICT_FORMAT_MDC)
public class MDCGenerator extends DictionaryGenerator {
	
	private static Logger log = LoggerFactory.getLogger(MDCGenerator.class);
	
	@Autowired
	private DaoService dao;

	@Override
	public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        if (null == dict) {
            log.warn("ID for " + dictId
                    + " Dictionary is not found in database.");
            throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dictId);
        }

        // all the language code in dictionary
        HashSet<String> dictLangCodes = dict.getAllLanguageCodes();

        File file = new File(targetDir, dict.getName());
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        Collection<Label> labels = dict.getAvailableLabels();
        Collection<Translation> translations = null;

        //generate xml
        Document document = DocumentHelper.createDocument();
        document.addComment("Created by DMS");

        Element dictionaryElement = document.addElement("dictionary");
        Element messageStringElement = dictionaryElement.addElement("messageString");

        for (Label label : labels) {
            Element labelElement = messageStringElement.addElement(label.getKey());
            translations = label.getText().getTranslations();

            for (String dictLangCode : dictLangCodes) {
                Element langElement = labelElement.addElement("lang");
                String translatedString = label.getTranslation(dictLangCode);
                langElement.addAttribute("id", dictLangCode);
                langElement.setText(translatedString);
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
//      OutputFormat format = OutputFormat.createCompactFormat();

        format.setEncoding("UTF-8");

        try {
            XMLWriter output = new XMLWriter(new FileWriter(file), format);
            output.write(document);
            output.close();
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }

	}

}
