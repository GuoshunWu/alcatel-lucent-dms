package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-17
 * Time: 下午1:18
 * To change this template use File | Settings | File Templates.
 */
public interface LanguageCode{
    String getCode()

    void setCode(String code)

    Language getLanguage()

    void setLanguage(Language language)

    boolean isDefaultCode()

    void setDefaultCode(boolean defaultCode)
}