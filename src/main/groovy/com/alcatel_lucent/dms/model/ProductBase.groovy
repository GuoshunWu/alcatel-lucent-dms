package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:39
 * To change this template use File | Settings | File Templates.
 */
class ProductBase extends BaseEntity {
    String name;
    Collection<Product> products;
    Collection<ApplicationBase> applicationBases;
}
