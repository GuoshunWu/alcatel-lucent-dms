package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-17
 * Time: 下午1:29
 * To change this template use File | Settings | File Templates.
 */
class Text  extends BaseEntity{
    Context context;
    String reference;
    Collection<Translation> translations;

    int status;

    public static final int STATUS_NOT_TRANSLATED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_TRANSLATED = 2;

    public Translation getTranslation(Long languageId) {
        if (translations != null) {
            for (Translation trans : translations) {
                if (trans.getLanguage().getId().equals(languageId)) {
                    return trans;
                }
            }
        }
        return null;
    }
}
