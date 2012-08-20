package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:44
 * To change this template use File | Settings | File Templates.
 */
class Application extends BaseEntity{
    ApplicationBase base
    String version
    Product product
    
    Collection<Dictionary> dictionaries
}
