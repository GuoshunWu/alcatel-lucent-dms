package com.alcatel_lucent.dms.model

import com.alcatel_lucent.dms.SystemError

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-17
 * Time: 下午1:27
 * To change this template use File | Settings | File Templates.
 */
class Label  extends BaseEntity{
    public static final String CHECK_FIELD_NAME = "CHK"
    public static final String REFERENCE_FIELD_NAME = "GAE"

    Dictionary dictionary
    String key
    int sortNo
    String reference
    String description
    String maxLength
	String annotation1
	String annotation2
    Context context
    Text text

    /**
     * Check if text meets max length constraint of the label
     * @param text
     * @return
     */
    public boolean checkLength(String text) {
        if (maxLength == null || maxLength.isEmpty()) {
            return true	// no constraint
        }
        String[] lens = maxLength.split(",")
        String[] texts = text.split("\n")
        for (int i = 0; i < texts.length; i++) {
            try {
                if (i >= lens.length || texts[i].getBytes("ISO-8859-1").length > Integer.parseInt(lens[i])) {
                    return false
                }
            } catch (NumberFormatException e) {
                throw new SystemError(e)
            } catch (UnsupportedEncodingException e) {
                throw new SystemError(e)
            }
        }
        return true
    }
}
