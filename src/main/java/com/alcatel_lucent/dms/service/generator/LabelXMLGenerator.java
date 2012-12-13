package com.alcatel_lucent.dms.service.generator;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DaoService;

@Component("LabelXMLGenerator")
public class LabelXMLGenerator implements DictionaryGenerator {

	private static Logger log = Logger.getLogger(LabelXMLGenerator.class);
	
	@Autowired
	private DaoService dao;

	@Override
	public void generateDict(File target, Long dictId) throws BusinessException {
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	
    	// create reference language file
    	generateLabelXML(target, dict, null);
    	
    	// generate for each language
    	if (dict.getDictLanguages() != null) {
    		for (DictionaryLanguage dl : dict.getDictLanguages()) {
    			generateLabelXML(target, dict, dl);
    		}
    	}
    }

	private void generateLabelXML(File targetDir, Dictionary dict, DictionaryLanguage dl) {
    	Document doc = DocumentHelper.createDocument();
    	doc.addComment("\n# Generated by DMS using language " + (dl == null ? "en" : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
    	String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
    	String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
    	String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
    	String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());
    	if (dictComments != null) {
    		String[] comments = dictComments.split("\n");
			for (String comment : comments) {
				comment = comment.replace("\\n", "\n");
				comment = comment.replace("\\\\", "\\");
				doc.addComment(comment);
			}
    	}
    	Element eleLabels = doc.addElement("LABELS");
    	if (dictNamespaces != null) {
    		String[] nsList = dictNamespaces.split("\n");
    		for (String ns : nsList) {
    			String[] keyValue = ns.split("=", 2);
    			if (keyValue.length == 2) {
    				eleLabels.addNamespace(keyValue[0], keyValue[1]);
    			}
    		}
    	}
    	if (dictAttributes != null) {
    		String[] attributes = dictAttributes.split("\n");
			for (String entry : attributes) {
				String[] keyValue = entry.split("=", 2);
				if (keyValue.length == 2) {
					eleLabels.addAttribute(keyValue[0], keyValue[1]);
				}
			}
    	}
    	if (dict.getLabels() != null) {
	    	for (Label label : dict.getLabels()) {
	    		String text = label.getReference();
	    		String annotation1 = label.getAnnotation1();	// attributes
	    		String annotation2 = label.getAnnotation2();	// leading comments
	    		if (dl != null) {
	    			LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
	    			if (lt != null) {
	    				annotation1 = lt.getAnnotation1();
	    				annotation2 = lt.getAnnotation2();
	    			}
	    			text = label.getTranslation(dl.getLanguageCode());
	    		}
	    		// add leading comments
	    		if (annotation2 != null) {
	    			String[] comments = annotation2.split("\n");
	    			for (String comment : comments) {
	    				eleLabels.addComment(comment);
	    			}
	    		}
	    		// create label
	    		Element eleLabel = eleLabels.addElement("LABEL");
	    		eleLabel.addAttribute("label_id", label.getKey());
	    		if (annotation1 != null) {
	    			String[] attributes = annotation1.split("\n");
	    			for (String entry : attributes) {
	    				String[] keyValue = entry.split("=", 2);
	    				if (keyValue.length == 2) {
	    					eleLabel.addAttribute(keyValue[0], keyValue[1]);
	    				}
	    			}
	    		}
	    		eleLabel.addText(text);
	    		if (text.indexOf('\n') != -1) { // preserve line breaks among the text
		    		eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
	    		}
	    	}
    	}
    	
    	// output
    	String filename = dict.getName();
    	if (dl != null) {
    		int pos = filename.lastIndexOf("en.xml");
    		if (pos != -1) {
    			filename = filename.substring(0, pos) + dl.getLanguageCode() + ".xml";
    		} else {
    			filename += dl.getLanguageCode() + ".xml";
    		}
    	}
    	OutputFormat format = OutputFormat.createPrettyPrint();
    	XMLWriter output = null;
    	try {
    		File targetFile = new File(targetDir, filename);
    		if (!targetFile.getParentFile().exists()) {
    			targetFile.getParentFile().mkdirs();
    		}
    		output = new XMLWriter(new FileWriter(targetFile), format);
    		if (processingInstructions != null) {
    			String[] piList = processingInstructions.split("\n");
    			for (String pi : piList) {
    				String[] keyValue = pi.split("=", 2);
    				if (keyValue.length == 2) {
    					output.processingInstruction(keyValue[0], keyValue[1]);
    				}
    			}
    		}
    		output.write(doc);
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.error(e);
    		throw new SystemError(e);
    	} finally {
    		if (output != null) try {output.close();} catch (Exception e) {}
    	}
	}
}
