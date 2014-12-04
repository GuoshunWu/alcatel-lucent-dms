package com.alcatel_lucent.dms.service.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;

@Component
public class POGenerator extends DictionaryGenerator {

	@Autowired
	private DaoService dao;
	
	@Override
	public void generateDict(File target, Long dictId, GeneratorSettings settings) throws BusinessException {
		Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
		if (dict.getDictLanguages() != null) {
			for (DictionaryLanguage dl : dict.getDictLanguages()) {
				generatePO(target, dict, dl);
			}
		}
		
	}

	private void generatePO(File target, Dictionary dict, DictionaryLanguage dl) {
		int pos = dict.getName().lastIndexOf("/*/");
		boolean isRefLang = dl.isReference();
		String parentFolder = null;
		String filename = null;
		if (pos != -1) {
			parentFolder = dict.getName().substring(0, pos);
			filename = dict.getName().substring(pos + 3);
		} else {
			parentFolder = dict.getName();
			filename = "lang.po";
		}
		PrintStream out = null;
		try {
			File file = new File(target, parentFolder + "/" + dl.getLanguageCode() + "/" + filename);
			if (!file.exists()) {
	            if (!file.getParentFile().exists()) {
	                file.getParentFile().mkdirs();
	            }
	            file.createNewFile();
	        }
			out = new PrintStream(new BufferedOutputStream(
	                new FileOutputStream(file)), true, dict.getEncoding());
			if (dl.getAnnotation1() != null) {
				out.print("msgid \"\"\n");
				out.print("msgstr \"\"\n");
				out.print(formatStr(dl.getAnnotation1()));
				out.print("\n\n");
			}
			if (dict.getLabels() != null) {
				ArrayList<Label> labels = new ArrayList<Label>(dict.getLabels());
				for (int i = 0; i < labels.size(); i++) {
					Label label = labels.get(i);
					Label nextLabel = i < labels.size() - 1 ? labels.get(i + 1) : null;
					LabelTranslation lt = isRefLang ? null : label.getOrigTranslation(dl.getLanguageCode());
					String comments = isRefLang ? label.getAnnotation1() : (lt == null ? null : lt.getAnnotation1());
					boolean hasPlural = nextLabel != null && nextLabel.getAnnotation1() != null && nextLabel.getAnnotation1().equals("plural");
					if (comments != null && !comments.equals("plural")) {	// comments
						out.print(comments);
					}
					String ctxt = isRefLang ? label.getAnnotation2() : (lt == null ? null : lt.getAnnotation2());
					if (ctxt != null && !(comments != null && comments.equals("plural"))) {	// msgctxt
						out.print("msgctxt ");
						out.print(formatStr(ctxt));
						out.print("\n");
					}
					out.print("msgid ");
					out.print(formatStr(removeDMSSuffix(label.getKey())));
					out.print("\n");
					if (hasPlural) {
						out.print("msgid_plural ");
						out.print(formatStr(removeDMSSuffix(nextLabel.getKey())));
						out.print("\n");
					}
					if (hasPlural) {
						out.print("msgstr[0] ");
						out.print(formatStr(isRefLang ? label.getReference() : label.getTranslation(dl.getLanguageCode()), dl.getCharset().getName(), dict.getEncoding()));
						out.print("\n");
						out.print("msgstr[1] ");
						out.print(formatStr(isRefLang ? nextLabel.getReference() : nextLabel.getTranslation(dl.getLanguageCode()), dl.getCharset().getName(), dict.getEncoding()));
						out.print("\n");
					} else {
						out.print("msgstr ");
						out.print(formatStr(isRefLang ? label.getReference() : label.getTranslation(dl.getLanguageCode()), dl.getCharset().getName(), dict.getEncoding()));
						out.print("\n");
					}
					out.print("\n");
					if (hasPlural) {
						i++;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.toString());
			throw new SystemError(e);
		} finally {
            if (null != out) {
                out.close();
            }
        }
		
	}

	// remove suffix "#DMS#" of key appended by DMS in order to remove duplication
	private String removeDMSSuffix(String key) {
		int suffixPos = key.lastIndexOf("#DMS#");
		if (suffixPos != -1) {
			return key.substring(0, suffixPos);
		}
		return key;
	}

	/**
	 * Split lines by \n
	 * @param s
	 * @return
	 */
	private String formatStr(String s) {
		return formatStr(s, null, null);
	}
	
	private String formatStr(String s, String charset, String dictEncoding) {
		StringBuffer str = new StringBuffer();
		String[] lines = s.split("\\\\n", -1);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0 && i == lines.length - 1 && lines[i].isEmpty()) continue;	// remove last empty element
			if (i > 0) {
				str.append("\n");
			}
			String text = lines[i];
			if (charset != null && !charset.equalsIgnoreCase(dictEncoding)) {	// convert charset
				try {
					text = new String(text.getBytes(charset), dictEncoding);
				} catch (UnsupportedEncodingException e) {
					log.error(e.toString());
					e.printStackTrace();
					throw new SystemError(e);
				}
			}
			str.append('\"').append(text);
			if (i < lines.length - 1) {
				str.append("\\n");
			}
			str.append('\"');
		}
		return str.toString();
	}

	@Override
	public DictionaryFormat getFormat() {
		return Constants.DictionaryFormat.PO;
	}

}
