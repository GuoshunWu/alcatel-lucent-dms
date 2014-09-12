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
    public void mergeContext(Long ctxAId, Long ctxBId, Long mergedToId) {
        log.info("Merge context A(id={}), context B(id={}) into context(id={})", ctxAId, ctxBId, mergedToId);
        Long beMergeId = ctxAId.equals(mergedToId) ? ctxBId : ctxAId;

        // Now merge be merged id to mergedTo id
        // TODO:
        // 1. Set all the texts and labels which context id is beMergedId to context which id is mergedToId
        Context toContext = (Context) dao.retrieve(Context.class, mergedToId);
        @Language("HQL") String hql = "from Text where id = :ctxId";
        Map params = ImmutableMap.of("ctxId", beMergeId);

        List<Text> beMergedTextList = dao.retrieve(hql, params);

        params = ImmutableMap.of("ctxId", mergedToId);
        List<Text> mergedToTextList = dao.retrieve(hql, params);
        // convert to map
        Map<String, Text> textMap = new HashMap<String, Text>();
        for (Text text : mergedToTextList) {
            textMap.put(text.getReference(), text);
        }

        for (Text text : beMergedTextList) {
            Text correspondingText = textMap.get(text.getReference());
            // if no corresponding text exists
            if (null == correspondingText) {
                text.setContext(toContext);
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
                    correspondingTranslation.setTranslation(translation.getTranslation());
                    correspondingTranslation.setStatus(translation.getStatus());

                    if (null != translation.getHistories() && !translation.getHistories().isEmpty()) {
                        Collection<TranslationHistory> histories = correspondingTranslation.getHistories();
                        if (null == histories) {
                            histories = new ArrayList<TranslationHistory>();
                        }
                        histories.addAll(translation.getHistories());
                    }
                }
                dao.delete(translation);
            }
            // update labels
            hql = "from Label where text.id = :textId";
            List<Label> labels = dao.retrieve(hql, ImmutableMap.of("textId", text.getId()));
            for (Label label : labels) {
                label.setText(correspondingText);
            }

            // update task detail
            hql = "update TaskDetail set text=:text where text.id = :textId";
            dao.update(hql, ImmutableMap.of("text", correspondingText, "textId", text.getId()));
            dao.delete(text);
//            dao.delete("delete Text where id=:textId", (Map) ImmutableMap.of("textId", text.getId()));
        }

        // 4. Remove the none referenced context
        dao.delete(Context.class, beMergeId);

    }
}
