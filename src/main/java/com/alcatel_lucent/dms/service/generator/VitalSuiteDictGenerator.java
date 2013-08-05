package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.TransformerUtils.chainedTransformer;
import static org.apache.commons.collections.TransformerUtils.invokerTransformer;
import static org.apache.commons.lang.ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;
import static org.apache.commons.lang3.math.NumberUtils.max;

@Component
public class VitalSuiteDictGenerator extends DictionaryGenerator {
    private static Logger log = LoggerFactory.getLogger(VitalSuiteDictGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);

        // generate for each language
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                generateProp(targetDir, dict, dl);
            }
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.VITAL_SUITE;
    }

    private void generateProp(File targetDir, Dictionary dict, DictionaryLanguage dl) {
        PrintStream out = null;
        File targetFile = new File(targetDir + "/" + dict.getName(), dl.getLanguageCode()+".txt");

        try {
            out = new PrintStream(new BufferedOutputStream(FileUtils.openOutputStream(targetFile)), true, "UTF-8");
            IOUtils.write(ByteOrderMark.UTF_8.getBytes(), out);
            out.println("// " + getDMSGenSign());
            out.println(dl.getLanguageCode());
            out.println("{");
            Collection<Label> labels = dict.getAvailableLabels();

            for (Label label : labels) {
                boolean isRef = dict.getReferenceLanguage().equals(dl.getLanguageCode());

                if (isRef) {    // reference language
                    if (label.getAnnotation1() != null) {
                        out.println(label.getAnnotation1());
                    }
//                    out.print(StringUtils.rightPad(label.getKey(), maxKeyLen + 4, ' '));
//                    out.println(label.getReference());
                } else {
//                    LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
//                    if (lt != null && lt.getAnnotation1() != null) {
//                        out.println(lt.getAnnotation1());
//                    }
//                    // populate translation result
//                    out.print(StringUtils.rightPad(label.getKey(), maxKeyLen + 4, ' '));
//                    out.println(label.getTranslation(dl.getLanguageCode()));
                }
            }
            out.println("}");
        } catch (IOException e) {
            log.error(e.toString());
            e.printStackTrace();
            throw new SystemError(e.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
