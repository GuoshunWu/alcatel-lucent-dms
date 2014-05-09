package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultAttribute;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.center;

@Component
public class TMXGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(TMXGenerator.class);
    @Autowired
    private DaoService dao;

    private String creationTool = "DMS";
    @Value("${version}")
    private String creationToolVersion;

    public static final String PREFIX = "ts";

    public static final String NOT_TRANSLATED = PREFIX + "NotTranslated";
    public static final String FOR_REVIEW = PREFIX + "ForReview";
    public static final String BEST_GUESS = PREFIX + "BestGuess";
    public static final String COMPLETE = PREFIX + "Complete";
    public static final String AUTO_TRANSLATED = PREFIX + "AutoTranslated";
    public static final String TRANSLATED = PREFIX + "Translated";


    private static String ISO8601Now() {
        return String.format("%tFT%<tRZ", new Date());
    }

    public void setCreationToolVersion(String creationToolVersion) {
        this.creationToolVersion = creationToolVersion;
    }

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        //        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
//        Improving performance
        long begin = System.currentTimeMillis();
        Dictionary dict = (Dictionary) dao.getSession().
                createCriteria(Dictionary.class).
                add(Restrictions.idEq(dictId)).
                setFetchMode("labels", FetchMode.JOIN).
                setCacheable(true).
//                setFetchMode("labels.params", FetchMode.SELECT).
//                setFetchMode("labels.origTranslations", FetchMode.DEFAULT).
//                setFetchMode("labels.text", FetchMode.DEFAULT).
//                setFetchMode("labels.text.translations", FetchMode.DEFAULT).
        uniqueResult();
        long end = System.currentTimeMillis();
        String timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
        log.info(center("Querying dictionary " + dict.getName() + " using a total of " + timeStr, 100, '*'));

        generateDict(targetDir, dict);
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        try {
            generateTMX(targetDir, dict);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SystemError(e);
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.TMX;
    }


    public void generateTMX(File target, Dictionary dict) throws IOException {

        String fileName = dict.getName();
        log.info("Generating " + fileName);
        File file = new File(target, fileName);
        FileUtils.touch(file);

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
//        format.setTrimText(true);
//        format.setPadText(true);
//        format.setNewlines(true);
        format.setXHTML(true);

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(file)), format);
            Document doc = DocumentHelper.createDocument();
            doc.setXMLEncoding("UTF-8");
            doc.addDocType("tmx", "", "tmx14.dtd");
            Element tmx = doc.addElement("tmx");
            tmx.addAttribute("version", "1.4");
            Element header = DocumentHelper.parseText(dict.getAnnotation1()).getRootElement();

            header.attribute("creationtool").setValue(this.creationTool);
            header.attribute("creationtoolversion").setValue(this.creationToolVersion);
            //add createiondate optional attribute
            Attribute creationDate = header.attribute("creationdate");
            String currentDateString = ISO8601Now();
            if (null == creationDate) {
                creationDate = new DefaultAttribute("creationdate", currentDateString);
                header.add(creationDate);
            } else {
                creationDate.setValue(currentDateString);
            }
            tmx.elements().add(0, header);
            generateTMXBody(tmx, dict);
            writer.write(doc);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void generateTMXBody(Element tmx, Dictionary dict) {
        Element body = tmx.addElement("body");
        Collection<Label> labels = dict.getAvailableLabels();
        Collection<DictionaryLanguage> dictionaryLanguages = dict.getDictLanguages();

        for (Label label : labels) {
            Element tu = null;
            try {
                tu = DocumentHelper.parseText(label.getAnnotation1()).getRootElement();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            body.elements().add(tu);

            for (DictionaryLanguage dctLanguage : dictionaryLanguages) {
                boolean isRef = dctLanguage.getLanguageCode().equals(dict.getLanguageReferenceCode());
                Element tuv = null;
                if (isRef) {
                    String tuvXmlString = label.getAnnotation2();
                    if (StringUtils.isBlank(tuvXmlString)) continue;
                    try {
                        tuv = DocumentHelper.parseText(tuvXmlString).getRootElement();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    if (Boolean.parseBoolean(Util.string2Map(label.getAnnotation3()).get("mixedContent"))) {
                        Element seg = null;
                        try {
                            seg = DocumentHelper.parseText(tuvXmlString).getRootElement();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                        tuv.elements().add(seg);
                    } else {
                        Element seg = tuv.addElement("seg");
                        seg.setText(label.getReference());
                    }
                } else {
                    LabelTranslation lt = label.getOrigTranslation(dctLanguage.getLanguageCode());
                    String tuvXmlString = lt == null ? null : lt.getAnnotation1();
                    if (StringUtils.isBlank(tuvXmlString)) continue;
                    try {
                        tuv = DocumentHelper.parseText(tuvXmlString).getRootElement();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }


//                    //get original translation status in tmx file
                    Element propStatus = (Element) tuv.selectSingleNode("prop[@type='status']");
                    int statusDMS = label.getTranslationStatus(dctLanguage.getLanguageCode());

                    if (null != propStatus) {
                        if (statusDMS == Translation.STATUS_UNTRANSLATED) {
                            propStatus.setText(NOT_TRANSLATED);
                        } else if (statusDMS == Translation.STATUS_TRANSLATED && propStatus.getTextTrim().equals(NOT_TRANSLATED)) {
                            propStatus.setText(TRANSLATED);
                        }
                    }
                    Boolean isMixedContent = Boolean.parseBoolean(Util.string2Map(lt.getAnnotation2()).get("mixedContent"));
                    Element seg;

                    String translation = label.getTranslation(dctLanguage.getLanguageCode());
                    if (isMixedContent) {
                        try {
                            seg = DocumentHelper.parseText(lt.getOrigTranslation()).getRootElement();
                            seg.setText(translation);
                            tuv.elements().add(seg);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        seg = tuv.addElement("seg");
                        seg.setText(translation);
                    }
                }
                if (null != tuv) tu.elements().add(tuv);
            }
        }
    }
}
