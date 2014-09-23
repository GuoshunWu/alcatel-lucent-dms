package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Product;
import com.google.common.collect.ImmutableMap;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Created by guoshunw on 2014/5/21.
 */
@Path("applications/version")
@Component
public class ApplicationVersionREST extends BaseREST {
    @Override
    String doGetOrPost(Map<String, String> requestMap) throws Exception {
        String base = requestMap.get("base");
        @Language("HQL") String hql = "from Application where base.id=:baseId";
        @Language("HQL") String countHql = "select count(*) from Application where base.id=:baseId";
        Map param = ImmutableMap.of("baseId", Long.parseLong(base));

        Collection<Product> products = retrieve(hql, param, countHql, param, requestMap);
        return toJSON(products, requestMap);
    }

    @Override
    Class getEntityClass() {
        return Application.class;
    }
}
