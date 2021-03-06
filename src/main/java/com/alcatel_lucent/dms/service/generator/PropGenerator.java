package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class PropGenerator extends DictionaryGenerator {

    private Logger log = LoggerFactory.getLogger(PropGenerator.class);

    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File target, Long dictId, GeneratorSettings settings) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);

        // create reference language file
//    	generateProp(target, dict, null);

        // generate for each language
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                generateProp(target, dict, dl);
            }
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.TEXT_PROP;
    }

    private void generateProp(File target, Dictionary dict, DictionaryLanguage dl) {
        PrintStream out = null;
        try {
            String filename = getFileName(dict.getName(), dl, "properties");

            File file = new File(target, filename);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            String outputEncoding = dl.getCharset().getName();
            out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(file)), true, outputEncoding);
            out.println("# " + getDMSGenSign());
            for (Label label : dict.getAvailableLabels()) {
                if (dl.getLanguageCode().equalsIgnoreCase("en") || dl.getLanguageCode().equalsIgnoreCase("en-en")) {    // reference language
                    if (label.getAnnotation1() != null) {
                        out.println(label.getAnnotation1());
                    }
                    out.print(escape(label.getKey(), true, false));
                    out.print("=");
                    out.println(escape(label.getReference(), false, "ISO-8859-1".equals(outputEncoding)));
                } else {
                    LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
                    if (lt != null && lt.getAnnotation1() != null) {
                        out.println(lt.getAnnotation1());
                    }
                    // populate translation result
                    String text = label.getTranslation(dl.getLanguageCode());
                    out.print(escape(label.getKey(), true, false));
                    out.print("=");
                    String translation =  escape(text, false, "ISO-8859-1".equals(outputEncoding));	// convert to \\uxxxx in case of ISO-8859-1
                    out.println(translation);
                }
            }
        } catch (IOException e) {
            log.error(e.toString());
            e.printStackTrace();
            throw new SystemError(e.getMessage());
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    /**
     * Escape property text
     * Convert unicode character to \\uXXXX
     * Convert ' ' to '\ '
     * Convert '=' to '\=' (not yet)
     * Convert ':' to '\:' (not yet)
     * Convert '#' to '\#' (not yet)
     * Convert '!' to '\!' (not yet)
     * Convert other characters '\t', '\n', '\r', '\f'
     * Convert '\' to '\\'
     * Split text into several natural lines by line seperator '\'
     *
     * @param text
     * @return escaped text
     *  see http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Properties.html#store%28java.io.OutputStream,%20java.lang.String%29
     *  see http://commons.apache.org/lang/api-release/index.html  StringEscapeUtils
     */
    private String escape(String text, boolean isKey, boolean allowNonAscii) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

//			if (c == '=' || c == ':' || c == '#' || c == '!' || c == '\\') {
            if (c == '\\') {
                result.append('\\').append(c);
            } else if (c == '\t') {
                result.append("\\t");
            } else if (c == '\n') {
                result.append("\\n");
            } else if (c == '\r') {
                result.append("\\r");
            } else if (c == '\f') {
                result.append("\\f");
            } else if (c < 0x20 || (c > 0x7e && !allowNonAscii)) {
                String hex = Integer.toHexString(c);
                while (hex.length() < 4) {
                    hex = "0" + hex;
                }
                result.append("\\u").append(hex);
            } else if (c == ' ' && (isKey || isLeadingOrTailingSpace(result, i))) {
                result.append("\\ ");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private boolean isLeadingOrTailingSpace(StringBuffer s, int index) {
        for (int i = 0; i <= index; i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        for (int i = index + 1; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }
}
