package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.IOSParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class IOSGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(IOSGenerator.class);
    private static String CONTENT_FORMAT = "\"%s\" = \"%s\";";
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File target, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        File dictDir = new File(target, dict.getName());
        try {
            FileUtils.forceMkdir(new File(target, dict.getName()));
        } catch (IOException e) {
            throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, dictDir.getAbsolutePath());
        }


        // generate for each language
        if (org.springframework.util.CollectionUtils.isEmpty(dict.getDictLanguages())) return;
        for (DictionaryLanguage dl : dict.getDictLanguages()) {
            generateIOSStringFile(dictDir, dict, dl);
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.IOS_RESOURCE;
    }

    /**
     * Generate a single file
     *
     * @param dictDir
     * @param dict
     * @param dl      dictionar language, null for reference
     */
    private void generateIOSStringFile(File dictDir, Dictionary dict, DictionaryLanguage dl) {
        String refLangCode = IOSParser.REFERENCE_LANG_CODE;
        if (dict.getDictLanguage("GAE") != null) refLangCode = "GAE";
        // if dl is reference, set it to null
        if (dl != null && dl.getLanguageCode().equals(refLangCode)) dl = null;
        String parentDir = (null == dl ? "Base" : dl.getLanguageCode()) + IOSParser.PARENT_DIR_EXTENSION;
        File stringFile = new File(new File(dictDir, parentDir), IOSParser.STRING_FILE_NAME);

        try {
            FileUtils.touch(stringFile);
        } catch (IOException e) {
            throw new BusinessException(BusinessException.FAILED_TO_CREATE_FILE, stringFile.getAbsolutePath());
        }

//        doc.addComment("\n# " + getDMSGenSign() + " using language " + (dl == null ? refLangCode : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");

        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(stringFile), dict.getEncoding()));
        } catch (UnsupportedEncodingException e) {
            log.warn("Dictionary format incorrect {}", dict.getFormat());
        } catch (FileNotFoundException e) {
            //never happen
        }

        if (null == out) return;
        LabelTranslation lt = null;
        String comment = null;

        String content = null;
        for (Label label : dict.getAvailableLabels()) {
            comment = label.getAnnotation1();
            if (dl != null) {
                lt = label.getOrigTranslation(dl.getLanguageCode());
                if (null != lt) {
                    comment = lt.getAnnotation1();
                }
                content = label.getTranslation(lt.getLanguageCode());
            } else {
                content = label.getReference();
            }
            if (null != comment) {
                out.println(comment);
            }
            out.println(String.format(CONTENT_FORMAT, label.getKey(), content));
        }
        IOUtils.closeQuietly(out);
    }

}
