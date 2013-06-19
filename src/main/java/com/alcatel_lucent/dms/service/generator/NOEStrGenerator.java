package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.NOEStrParser;
import com.alcatel_lucent.dms.service.parser.XMLHelpParser;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.split;

@Component
public class NOEStrGenerator extends DictionaryGenerator {
    private static Logger log = LoggerFactory.getLogger(NOEStrGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        generateDict(targetDir, dict);
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.NOE_STRING;
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        try {
            // write doc file
            File targetFile = new File(targetDir, dict.getName() + ".doc");
            FileUtils.writeStringToFile(targetFile, StringUtils.defaultString(dict.getAnnotation1()), NOEStrParser.DEFAULT_ENCODING);

            //write lang files
            Set<String> langSet = dict.getAllLanguageCodes();

            for (String langCode : langSet) {
                targetFile = new File(targetDir, dict.getName() + "." + langCode);
                FileUtils.writeStringToFile(targetFile, getLanguageFileString(langCode, dict));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Generate a specific language translations as a String
     */
    public String getLanguageFileString(String langCode, Dictionary dictionary) {
        Collection<Label> labels = dictionary.getLabels();
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        for (Label label : labels) {
            String text = langCode.equals(NOEStrParser.REFERENCE_CODE) ? label.getReference() : label.getTranslation(langCode);
            out.println(NOEStrParser.LABEL_KEY_PREFIX + label.getKey());
            out.println(NOEStrParser.LABEL_TRANS_PREFIX + text);
        }

        out.close();
        String result = sw.toString();
        IOUtils.closeQuietly(sw);
        return result;
    }

}
