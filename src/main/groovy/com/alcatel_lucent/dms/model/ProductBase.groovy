package com.alcatel_lucent.dms.model

import javax.xml.bind.annotation.XmlRootElement

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:39
 * To change this template use File | Settings | File Templates.
 */

@XmlRootElement
class ProductBase extends BaseEntity {
    String name;
    Collection<Product> products;
    Collection<ApplicationBase> applicationBases;
}
