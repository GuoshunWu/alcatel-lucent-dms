package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.TSParser;
import com.alcatel_lucent.dms.util.Util;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TS dictionary Generator
 * Created by Guoshun Wu on 14-08-12.
 */

@Component
public class TSGenerator extends DictionaryGenerator {

    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId, GeneratorSettings settings) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        try {
            FileUtils.forceMkdir(targetDir);
            Set<String> langCodes = dict.getAllLanguageCodes();
            for (String langCode : langCodes) {
                //create file
                File targetFile = new File(targetDir, dict.getName() + "_" + langCode + ".ts");
                generateTS(langCode, targetFile, dict);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTS(String langCode, File target, Dictionary dict) throws IOException {
        log.info("Generating " + target.getAbsolutePath());
        FileUtils.touch(target);
        boolean isReferenceFile = dict.getLanguageReferenceCode().equals(langCode);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(target)), format);
            Document doc = DocumentHelper.createDocument();
            doc.setXMLEncoding(TSParser.DEFAULT_FILE_ENCODING);
            doc.addDocType("TS", "", "");
            doc.addComment(getDMSGenSign());
            Element ts = doc.addElement(TSParser.ROOT_ELEMENT_NAME);
            Map<String, String> rootAttrMap = Util.string2Map(dict.getAnnotation1());
            rootAttrMap.put("language", langCode);
            Set<Map.Entry<String, String>> entries = rootAttrMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                ts.addAttribute(entry.getKey(), entry.getValue());
            }

            LinkedMultiValueMap<String, Label> groupedLabels = groupLabelByContext(dict);
            Set<Map.Entry<String, List<Label>>> labelEntries = groupedLabels.entrySet();

            for (Map.Entry<String, List<Label>> entry : labelEntries) {
                String contextName = entry.getKey();
                Element ctxElem = ts.addElement(TSParser.SECOND_NODE_NAME);
                ctxElem.addElement("name").setText(contextName);
                for (Label label : entry.getValue()) {
                    Element msgElem = ctxElem.addElement("message");
                    String locationString = label.getAnnotation1();
                    Map<String, String> annotation3;
                    String translatorComment = null;
                    LabelTranslation lt;

                    if (!isReferenceFile && null != (lt = label.getOrigTranslation(langCode))) {
                        locationString = lt.getAnnotation1();
                    }
                    addLocations(msgElem, locationString);
                    Element source = msgElem.addElement("source");
                    source.setText(label.getKey().substring(contextName.length() + TSParser.CONTEXT_KEY_SEPARATOR.length()));

                    Element translation = msgElem.addElement("translation");

                    if (isReferenceFile) {
                        if (null != label.getAnnotation3() && null != (annotation3 = Util.string2Map(label.getAnnotation3()))) {
                            translatorComment = annotation3.get("translatorcomment");
                            if (!Strings.isNullOrEmpty(annotation3.get("type"))) {
                                translation.addAttribute("type", annotation3.get("type"));
                            }
                        }
                        translation.setText(label.getReference());
                    } else {
                        if (null != (lt = label.getOrigTranslation(langCode)) && null != lt.getAnnotation2()) {
                            translatorComment = Util.string2Map(lt.getAnnotation2()).get("translatorcomment");
                        }
                        if (label.getTranslationStatus(langCode) != Translation.STATUS_TRANSLATED) {
                            translation.addAttribute("type", "unfinished");
                        }
                        translation.setText(label.getTranslation(langCode));
                    }
                    if (!Strings.isNullOrEmpty(translatorComment)) {
                        msgElem.addElement("translatorcomment").setText(translatorComment);
                    }
                }
            }
            writer.write(doc);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addLocations(Element parent, String locString) {
        Map<String, String> locMap = Util.string2Map(locString);
        Set<Map.Entry<String, String>> entries = locMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String[] lineNumbers = entry.getValue().split(", ");
            for (String lineNumber : lineNumbers) {
                Element locElem = parent.addElement("location");
                locElem.addAttribute("filename", entry.getKey());
                locElem.addAttribute("line", lineNumber);
            }
        }
    }

    private LinkedMultiValueMap<String, Label> groupLabelByContext(Dictionary dictionary) {
        Collection<Label> labels = dictionary.getAvailableLabels();
        LinkedMultiValueMap<String, Label> labelsMap = new LinkedMultiValueMap<String, Label>();
        for (Label label : labels) {
            String contextName = Util.string2Map(label.getAnnotation2()).get("context");
            labelsMap.add(contextName, label);
        }
        return labelsMap;
    }


    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.TS;
    }
}
