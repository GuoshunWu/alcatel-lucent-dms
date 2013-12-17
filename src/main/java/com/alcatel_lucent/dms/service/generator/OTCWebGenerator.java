package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.OTCWebParser;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * OTC Web dictionary Generator
 * Created by Guoshun Wu on 13-12-17.
 */

@Component
public class OTCWebGenerator extends DictionaryGenerator {

    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        try {
            FileUtils.forceMkdir(targetDir);
            Set<String> langCodes = dict.getAllLanguageCodes();
            Collection<Label> labels = dict.getAvailableLabels();

            for (String langCode : langCodes) {
                //create file
                FileUtils.writeStringToFile(new File(new File(targetDir, langCode), dict.getName()),
                        generateLabelTranslations(labels, langCode), OTCWebParser.DEFAULT_FILE_ENCODING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateLabelTranslations(Collection<Label> labels, String langCode) {
        Map<String, String> translationMap = new LinkedHashMap<String, String>();
        for (Label label : labels) {
            translationMap.put(label.getKey(), label.getTranslation(langCode));
        }

        JSONObject jsonObject = JSONObject.fromObject(translationMap);

        return StringUtils.join(Arrays.asList(
                "// " + DictionaryGenerator.getDMSGenSign(),
                String.format("define(%s);", jsonObject.toString(4))),
                "\n");
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_WEB;
    }
}
