package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:59
 * To change this template use File | Settings | File Templates.
 */
class DictionaryBase extends BaseEntity{
    String name
    String format
    String encoding
    String path

    ApplicationBase applicationBase
    Collection<Dictionary> dictionaries

}
