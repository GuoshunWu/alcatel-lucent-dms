package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Product;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午11:26
 * To change this template use File | Settings | File Templates.
 */
public interface ProductService {
    Product create();
    void delete(Long id);
    Product retrieve(Long id);
}
