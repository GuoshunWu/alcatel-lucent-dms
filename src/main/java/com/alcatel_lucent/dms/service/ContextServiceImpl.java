package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.*;
import com.google.common.collect.ImmutableMap;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by guoshunw on 2014/6/11.
 */

@Service("contextService")
public class ContextServiceImpl extends BaseServiceImpl implements ContextService {

    private static Logger log = LoggerFactory.getLogger(ContextService.class);

    @Override
    @SuppressWarnings("unchecked")
    public Context mergeContext(Long ctxAId, Long ctxATextId, Long ctxBId, Long ctxBTextId, Long mergedToCtxId) {
        return mergeTextInContext(ctxAId, ctxATextId, ctxBId, ctxBTextId, mergedToCtxId);
    }


    /**
     * Merge text a in context a into text b in context b or vice versa
     */
    private Context mergeTextInContext(Long ctxAId, Long ctxATextId, Long ctxBId, Long ctxBTextId, Long mergedToCtxId) {
        //default context a merged to context b
        Text textA = (Text) dao.retrieve(Text.class, ctxATextId);
        Text textB = (Text) dao.retrieve(Text.class, ctxBTextId);

        Context ctxA = (Context) dao.retrieve(Context.class, ctxAId);
        Context ctxB = (Context) dao.retrieve(Context.class, ctxBId);

        // if merged to a, swap context a and b
        if (mergedToCtxId.equals(ctxAId)) {
            Text tempText = textA;
            textA = textB;
            textB = tempText;

            Context tempCtx = ctxA;
            ctxA = ctxB;
            ctxB = tempCtx;
        }

        @Language("HQL") String hql = "from Label where text.id = :textId";
        List<Label> labels = dao.retrieve(hql, ImmutableMap.of("textId", textA.getId()));

        // update labels
        for (Label label : labels) {
            label.setText(textB);
            label.setContext(ctxB);
        }

        Collection<Translation> translations = textA.getTranslations();
        for (Translation translation : translations) {
            Translation correspondingTranslation = textB.getTranslation(translation.getLanguage().getId());
            if (null == correspondingTranslation) {
                translation.setText(textB);
                continue;
            }

            if (correspondingTranslation.getStatus() == Translation.STATUS_UNTRANSLATED && Translation.STATUS_TRANSLATED == translation.getStatus()) {
                translation.setText(textB);
            }
            dao.delete(correspondingTranslation, false);
        }

        // update task detail
        hql = "from TaskDetail td where text.id = :textId";
        List<TaskDetail> taskDetails = dao.retrieve(hql, ImmutableMap.of("textId", textA.getId()));
        for (TaskDetail td : taskDetails) {
            td.setText(textB);
        }
        dao.delete(textA, false);

        //if context a is empty then delete context a
        hql = "select count(t) from Text t where context.id = :ctxId";
        Long textsInTextA = (Long) dao.retrieveOne(hql, ImmutableMap.of("ctxId", ctxA.getId()));
        if (textsInTextA > 0L) {
            return null;
        }

        dao.delete(ctxA, false);
        return ctxA;
    }

    /**
     * Merge text a in context a into text b in context b or vice versa
     */

    private void mergeContextAtoContextB(Long ctxAId, Long ctxBId, Long mergedToCtxId) {
        Long beMergeId = ctxAId.equals(mergedToCtxId) ? ctxBId : ctxAId;

        // Now merge be merged id to mergedTo id
        // 1. Set all the texts and labels which context id is beMergedId to context which id is mergedToId
        Context toContext = (Context) dao.retrieve(Context.class, mergedToCtxId);
        Context beMergedContext = (Context) dao.retrieve(Context.class, beMergeId);

        // query textA and textB

        log.info("Merge context {}, context {} into context{}", beMergedContext, toContext, toContext);

        @Language("HQL") String hql = "from Text where context.id = :ctxId";
        Map params = ImmutableMap.of("ctxId", beMergeId);

        List<Text> beMergedTextList = dao.retrieve(hql, params);

        params = ImmutableMap.of("ctxId", mergedToCtxId);
        List<Text> mergedToTextList = dao.retrieve(hql, params);
        // convert to map
        Map<String, Text> textMap = new HashMap<String, Text>();
        for (Text text : mergedToTextList) {
            textMap.put(text.getReference(), text);
        }

        for (Text text : beMergedTextList) {

            hql = "from Label where text.id = :textId";
            List<Label> labels = dao.retrieve(hql, ImmutableMap.of("textId", text.getId()));

            Text correspondingText = textMap.get(text.getReference());
            // if no corresponding text exists
            if (null == correspondingText) {
                text.setContext(toContext);
                // update labels
                for (Label label : labels) {
                    label.setContext(toContext);
                }
                continue;
            }
            Collection<Translation> translations = text.getTranslations();
            // translations in text
            for (Translation translation : translations) {
                Translation correspondingTranslation = correspondingText.getTranslation(translation.getLanguage().getId());
                if (null == correspondingTranslation) {
                    translation.setText(correspondingText);
                    continue;
                }

                if (correspondingTranslation.getStatus() == Translation.STATUS_UNTRANSLATED && Translation.STATUS_TRANSLATED == translation.getStatus()) {
                    translation.setText(correspondingText);
                }
                dao.delete(translation, false);
            }
            // update labels
            for (Label label : labels) {
                label.setText(correspondingText);
                label.setContext(correspondingText.getContext());
            }

            // update task detail
            hql = "from TaskDetail td where text.id = :textId";
            List<TaskDetail> taskDetails = dao.retrieve(hql, ImmutableMap.of("textId", text.getId()));
            for (TaskDetail td : taskDetails) {
                td.setText(correspondingText);
            }
            dao.delete(text, false);
        }

        // 4. Remove the none referenced context
        dao.delete(beMergedContext, false);
    }


}
