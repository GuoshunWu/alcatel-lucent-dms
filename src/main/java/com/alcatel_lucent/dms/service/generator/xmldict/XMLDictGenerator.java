package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import static org.apache.commons.lang3.StringUtils.center;

@Component
public class XMLDictGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(XMLDictGenerator.class);
    @Autowired
    private DaoService dao;
    
    @Override
    public void generateDict(File target, Collection<Dictionary> dictList) throws BusinessException {
    	// generate xdcp file
    	try {
			generateXdcp(target, dictList);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new SystemError(e);
		}
    	super.generateDict(target, dictList);
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

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.XDCT;
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        try {
            Map<String, Collection<String>> xdctGroup = getXdctGroup(targetDir, dict);
            for (String xdctFileName : xdctGroup.keySet()) {
                Collection<String> langCodes = xdctGroup.get(xdctFileName);
                generateXdct(targetDir, dict, xdctFileName, langCodes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SystemError(e);
        }
    }

    /**
     * Group languages by xdct filename.
     * A single dictionary can consist of severl xdct files, each of the file consists of several languages.
     * The xdct filename is stored in annotation2, if not specified, all languages are generated in a single xdct file.
     * @param targetDir
     * @param dict
     * @return
     * @throws IOException
     */
    private Map<String, Collection<String>> getXdctGroup(File targetDir, Dictionary dict) throws IOException {
        Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                String xdctFileName = dl.getAnnotation2();
                if (xdctFileName == null) xdctFileName = dict.getName() + ".xdct";
                Collection<String> langCodes = result.get(xdctFileName);
                if (langCodes == null) {
                    langCodes = new ArrayList<String>();
                    result.put(xdctFileName, langCodes);
                }
                langCodes.add(dl.getLanguageCode());
            }
        }
        return result;
    }

    public void generateXdcp(File target, Collection<Dictionary> dictList) throws IOException {
    	Map<String, String> fileMap = new HashMap<String, String>();
    	String appName = null;
    	for (Dictionary dict : dictList) {
    		if (appName == null) {
    			appName = dict.getBase().getApplicationBase().getName();
    		}
			Map<String, Collection<String>> xdctGroup = getXdctGroup(target, dict);
			for (String fileName : xdctGroup.keySet()) {
				fileMap.put(fileName, dict.getName());
			}
		}
    	String xdcpFileName = appName + ".xdcp";
        log.info("Generating " + xdcpFileName);
        
    	File file = createNewFile(target, xdcpFileName);
    	OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
        format.setXHTML(true);

        XMLWriter writer = null;
        try {
	        writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(file)), format);
	        Document doc = DocumentHelper.createDocument();
	        doc.setXMLEncoding("UTF-8");
	        doc.addDocType("PROJECT", "", "XMLPROJECT.dtd");
	        Element xmlProj = doc.addElement("PROJECT");
	        xmlProj.addAttribute("name", appName);
	        for (String fileName : fileMap.keySet()) {
	        	String dictName = fileMap.get(fileName);
	        	Element xmlDict = xmlProj.addElement("DICTIONARY");
	        	xmlDict.addAttribute("name", dictName);
	        	xmlDict.addAttribute("path", fileName);
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

    public void generateXdct(File targetDir, Dictionary dict, String xdctFileName, Collection<String> langCodes) throws BusinessException, IOException {
        XMLWriter writer = null;

        try {
            File file = createNewFile(targetDir, xdctFileName);

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(4);
            format.setXHTML(true);

            log.info(center("Start generating dictionary " + xdctFileName + "...", 100, '='));
            writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(file)), format);

            long begin = System.currentTimeMillis();
            Document doc = generateDocument(dict, langCodes);
            long end = System.currentTimeMillis();
            String timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
            log.info(center("Generating dictionary " + xdctFileName + " using a total of " + timeStr, 100, '*'));


            begin = System.currentTimeMillis();
            log.info(center("Start writing dictionary " + xdctFileName + "...", 100, '='));
            writer.write(doc);
            end = System.currentTimeMillis();
            timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
            log.info(center("Writing dictionary " + xdctFileName + " using a total of " + timeStr, 100, '*'));
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createNewFile(File targetDir, String filename) throws IOException {
        File file = new File(targetDir, filename);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        return file;
    }

    public Document generateDocument(final Dictionary dict, Collection<String> langCodes) {

        Document doc = DocumentHelper.createDocument();

        doc.setXMLEncoding("UTF-8");
        if (null == dict) return doc;

        doc.addDocType("DICTIONARY", "", "XMLDICT.dtd");
        doc.addComment(StringUtils.center(getDMSGenSign() + ", total labels: " + dict.getLabelNum() + ".", 50, '='));

        final Element xmlDict = doc.addElement("DICTIONARY");

        Map<String, String> attributes = Util.string2Map(dict.getAnnotation1());

        xmlDict.addAttribute("name", attributes.get("name"));
        xmlDict.addAttribute("type", attributes.get("type"));
        xmlDict.addAttribute("appli", attributes.get("appli"));
        xmlDict.addAttribute("separator", attributes.get("separator"));

        // filter languages
        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        for (DictionaryLanguage dl : dict.getDictLanguages()) {
            if (langCodes.contains(dl.getLanguageCode())) {
                dictLanguages.add(dl);
            } else {
                Map<String, String> langAttrs = Util.string2Map(dl.getAnnotation1());
                String isReference = langAttrs.get("is_reference");
                if (isReference != null && isReference.equalsIgnoreCase("true")) {
                    dictLanguages.add(dl);
                }
            }
        }

        LanguageClosure ll = new LanguageClosure(xmlDict);
        CollectionUtils.forAllDo(dictLanguages, ll);

        LabelClosure lc = new LabelClosure(xmlDict, dict.getLabelNum(), dictLanguages);

        log.info(StringUtils.center("Start generating dictionary " + dict.getName() + " labels(total: " + dict.getLabelNum() + ").", 100, '='));
        CollectionUtils.forAllDo(dict.getAvailableLabels(), lc);
        System.out.println(" done(100%).");

        return doc;
    }

}
