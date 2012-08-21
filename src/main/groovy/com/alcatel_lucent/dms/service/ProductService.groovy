package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.model.Product
import com.alcatel_lucent.dms.model.ProductBase

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-21
 * Time: 下午1:48
 * To change this template use File | Settings | File Templates.
 */
public interface ProductService {
    Product create()
    void delete(Long id)
    Product retrieve(Long id)
    Collection<ProductBase> retrieveAll()
}