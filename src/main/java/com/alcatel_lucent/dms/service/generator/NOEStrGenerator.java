package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.NOEStrParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Set;

import static com.alcatel_lucent.dms.service.parser.NOEStrParser.*;

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
            String text = langCode.equals(REFERENCE_CODE) ? label.getReference() : label.getTranslation(langCode);
            out.println(LABEL_KEY_PREFIX + label.getKey());
            String translation = escapeNOEString(text);

            // ignore character great than '\u0080'
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < translation.length(); ++i) {
                char ch = translation.charAt(i);
                sb.append(ch > '\u0080' ? '?' : ch);
            }
            out.println(LABEL_TRANS_PREFIX + sb.toString());
        }

        out.close();
        String result = sw.toString();
        IOUtils.closeQuietly(sw);
        return result;
    }

}
