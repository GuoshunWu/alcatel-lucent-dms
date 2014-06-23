package com.alcatel_lucent.dms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by guoshunw on 2014/6/11.
 */

@Service("contextService")
public class ContextServiceImpl extends BaseServiceImpl implements ContextService {

    private static Logger log = LoggerFactory.getLogger(ContextService.class);

    @Override
    public void mergeContext(Long ctxAId, Long ctxBId, Long mergedToId) {
        log.info("Merge context A(id={}), context B(id={}) into context(id={})", ctxAId, ctxBId, mergedToId);
        Long beMergeId = ctxAId == mergedToId ? ctxBId : ctxAId;
        // Now merge be merged id to mergedTo id
        // TODO:
        // 1. Set all the texts and labels which context id is beMergedId to context which id is mergedToId
        // 2. Query if there are some other text

    }
}
