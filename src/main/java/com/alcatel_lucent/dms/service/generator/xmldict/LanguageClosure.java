package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.Closure;
import org.dom4j.Element;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-25
 * Time: 下午2:35
 * To change this template use File | Settings | File Templates.
 */
public class LanguageClosure implements Closure {

    private Element xmlDict;

    public LanguageClosure(Element xmlDict) {
        this.xmlDict = xmlDict;
    }

    @Override
    public void execute(Object input) {
        DictionaryLanguage language = (DictionaryLanguage) input;
        Element xmlLanguage = xmlDict.addElement("LANGUAGE");
        xmlLanguage.addAttribute("id", language.getLanguageCode());
        Map<String, String> langAttrs = Util.string2Map(language.getAnnotation1());
        xmlLanguage.addAttribute("is_reference", langAttrs.get("is_reference"));
        xmlLanguage.addAttribute("is_context", langAttrs.get("is_context"));
    }
}
