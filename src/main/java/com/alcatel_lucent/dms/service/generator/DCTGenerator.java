package com.alcatel_lucent.dms.service.generator;

import static com.alcatel_lucent.dms.util.Util.generateSpace;
import static org.apache.commons.lang.StringUtils.join;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import com.alcatel_lucent.dms.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;

@Component
public class DCTGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(DCTGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId, GeneratorSettings settings) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        ArrayList<String> dictLangCodes = dict.getAllLanguageCodesOrdered();

        PrintStream out = null;
        try {
            File file = new File(targetDir, dict.getName());
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(file)), true, dict.getEncoding());
            // output support languages
            if (dict.getEncoding().equals("UTF-16LE")) {
                out.write(new byte[]{(byte) 0xff, (byte) 0xfe});
            }
//            out.println("-- " + getDMSGenSign());
            if (dict.getAnnotation1() != null) {
                out.print(dict.getAnnotation1());
            }
            boolean hasCHK = false;
            for (Label label : dict.getAvailableLabels()) {
                if (label.getMaxLength() != null) {
                    hasCHK = true;
                    break;
                }
            }
            if (hasCHK) {
                dictLangCodes.add(0, "CHK");
            }
            out.println("LANGUAGES {" + join(dictLangCodes, ", ") + "}");
            out.println();
            dictLangCodes.remove("CHK");
            dictLangCodes.remove("GAE");

            // output labels

            Label label = null;
            String checkFieldLangCodeString = String.format("  %s ",
                    Label.CHECK_FIELD_NAME);
            String referenceFieldLangCodeString = String.format("  %s ",
                    Label.REFERENCE_FIELD_NAME);
            int indentSize = checkFieldLangCodeString.length();

            Label[] labels = dict.getAvailableLabels().toArray(new Label[0]);
            for (int i = 0; i < labels.length; ++i) {
                label = labels[i];

                if (i > 0) {
                    // output label separator
                    out.println(";");
                    out.println();
                }
                if (label.getAnnotation1() != null) {
                    out.print(label.getAnnotation1());
                }
                out.println(label.getKey() + ":");
                if (label.getAnnotation2() != null) {
                    out.print(label.getAnnotation2());
                }
                String chk = generateCHK(label.getMaxLength(),
                        label.getReference());
                if (null != chk) {
                    out.print(checkFieldLangCodeString + convertContent(indentSize, chk, "\n", System.getProperty("line.separator")));
                    // output translation separator
                    out.println(",");
                }
                out.print(referenceFieldLangCodeString
                        + convertContent(indentSize, label.getReference(),
                        "\n", System.getProperty("line.separator")));
                for (String langCode : dictLangCodes) {
//                for (LabelTranslation trans :  label.getOrigTranslations()) {
//                	LabelTranslation trans = label.getOrigTranslation(langCode);
                    // output translation separator
                    out.println(",");

                    out.print("  " + langCode + " ");

                    // output dictLangCode translation
                    String translationString = label.getTranslation(langCode);

                    // if need translation, get translation from context dictionary
                    // otherwise, get translation from original translation
                    DictionaryLanguage dl = dict.getDictLanguage(langCode);

                    String converedString = convertContent(indentSize,
                            translationString, "\n",
                            System.getProperty("line.separator"));

                    String charsetName = dl.getCharset().getName();
                    out.write(converedString.getBytes(charsetName));
                }
            }
            if (labels.length > 0) {
                out.println(";");
            }
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.DCT;
    }

    private String generateCHK(String maxLength, String reference) {
        if (null == maxLength) return null;
        log.debug("maxLength=" + maxLength + ", reference=" + reference);
        StringBuilder sb = new StringBuilder();
        //Trailing empty strings should not be discarded
        String[] sLineLens = maxLength.split(",", -1);
        String[] refers = reference.split("\n", -1);
        int maxLen = -1;

        int min = Math.min(sLineLens.length, refers.length);
        for (int i = 0; i < min; ++i) {
        	if (sLineLens[i].trim().isEmpty()) continue;
            maxLen = Integer.parseInt(sLineLens[i].trim());
            sb.append(refers[i].trim());
            int fill = maxLen - refers[i].length();
            int base = 0;
            while (fill-- > 0) {
                sb.append((char) (base++ % 10 + '0'));
            }
            sb.append("\n");
        }

        for (int i = min; i < sLineLens.length; ++i) {
            maxLen = Integer.parseInt(sLineLens[i].trim());
            int fill = maxLen;
            int base = 0;
            while (fill-- > 0) {
                sb.append((char) (base++ % 10 + '0'));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String convertContent(int indentSize, String content,
                                  String contentLineSeparator, String joinedStringLineSeparator) {
        if (content.equals(contentLineSeparator)) {
            return "\"\"";
        }
        String[] contents = content.split(contentLineSeparator);
        for (int i = 0; i < contents.length; ++i) {
            contents[i] = "\"" + escapeContent(contents[i]) + "\"";
        }
        return join(contents, joinedStringLineSeparator + generateSpace(indentSize));
    }

    private String escapeContent(String s) {
        StringBuffer result = new StringBuffer();
        char prev = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"' && prev != '\\') {
                result.append("\\");
            }
            result.append(c);
            prev = c;
        }
        return result.toString();
    }

}
