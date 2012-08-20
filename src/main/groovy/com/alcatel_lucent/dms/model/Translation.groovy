package com.alcatel_lucent.dms.model

import com.alcatel_lucent.dms.util.CharsetUtil

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-17
 * Time: 下午1:32
 * To change this template use File | Settings | File Templates.
 */
class Translation extends BaseEntity{
    Text text;
    Language language;
    String translation;
    String memo;

    public boolean isValidText() {
        if (translation != null) {
            return CharsetUtil.isValid(translation, language.getName());
        }
        return true;
    }
}
