package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:42
 * To change this template use File | Settings | File Templates.
 */
class Product extends BaseEntity{
    ProductBase base
    String version
    Collection<Application> applications
}
