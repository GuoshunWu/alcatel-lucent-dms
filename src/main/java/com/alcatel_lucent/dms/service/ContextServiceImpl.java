package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Context;
import com.google.common.collect.ImmutableMap;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by guoshunw on 2014/6/11.
 */

@Service("contextService")
public class ContextServiceImpl extends BaseServiceImpl implements ContextService {

    private static Logger log = LoggerFactory.getLogger(ContextService.class);

    @Override
    public void mergeContext(Long ctxAId, Long ctxBId, Long mergedToId) {
        log.info("Merge context A(id={}), context B(id={}) into context(id={})", ctxAId, ctxBId, mergedToId);
        Long beMergeId = ctxAId.equals(mergedToId) ? ctxBId : ctxAId;

        // Now merge be merged id to mergedTo id
        // TODO:
        // 1. Set all the texts and labels which context id is beMergedId to context which id is mergedToId
//        @Language("HQL") String hql = "update Text set context = :ctx where context.id = :ctxId";
//        Context toContext = (Context) dao.retrieve(Context.class, mergedToId);
//        Map params = ImmutableMap.of("ctx", toContext, "ctxId", beMergeId);
//
//        dao.update(hql, params);
//        hql = "update Label set context = :ctx where context.id = :ctxId";
//        dao.update(hql, params);

        // 2. Query if there are some other text

        // 3. Remove the none referenced context
//        dao.delete(Context.class, beMergeId);
    }
}
