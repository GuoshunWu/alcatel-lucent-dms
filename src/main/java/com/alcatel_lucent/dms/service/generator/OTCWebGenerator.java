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
    public void generateDict(File targetDir, Long dictId, GeneratorSettings settings) throws BusinessException {
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

        JSONObject jsonObject = JSONObject.fromObject(toHierarchyMap(translationMap));

        return StringUtils.join(Arrays.asList(
                "// " + DictionaryGenerator.getDMSGenSign(),
                String.format("define(%s);", jsonObject.toString(4))),
                "\n");
    }

    private static Map<String, Object> toHierarchyMap(Map<String, String> original) {
        return toHierarchyMap(original, null);
    }

    private static Map<String, Object> toHierarchyMap(Map<String, String> original, String separator) {
        if (separator == null) separator = ".";
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Set<Map.Entry<String, String>> entrySet = original.entrySet();

        Map<String, Map<String, String>> subMap = new LinkedHashMap<String, Map<String, String>>();

        for (Map.Entry<String, String> entry : entrySet) {
            String[] keyToken = StringUtils.split(entry.getKey(), separator);
            if (keyToken.length < 2) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }
            Map<String, String> descendantMap = subMap.get(keyToken[0]);
            if (null == descendantMap) {
                descendantMap = new LinkedHashMap<String, String>();
                subMap.put(keyToken[0], descendantMap);
            }
            descendantMap.put(entry.getKey().substring(entry.getKey().indexOf(separator) + separator.length()), entry.getValue());

        }

        //add sub map
        for (Map.Entry<String, Map<String, String>> entry : subMap.entrySet()) {
            result.put(entry.getKey(), toHierarchyMap(entry.getValue(), separator));
        }
        return result;
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_WEB;
    }
}
