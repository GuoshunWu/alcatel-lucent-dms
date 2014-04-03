package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-7-25
 * Time: PM 3:07
 */
@Service("glossaryService")
@SuppressWarnings("unchecked")
public class GlossaryServiceImpl implements GlossaryService {

    private DaoService dao;
    private Collection<Glossary> glossaries;
    @Autowired
    private TextService textService;

    @Autowired
    private HistoryService historyService;

    private static Logger log = LoggerFactory.getLogger(GlossaryServiceImpl.class);

    @Autowired
    public void setDao(DaoService dao) {
        this.dao = dao;
        this.glossaries = dao.retrieve("from Glossary");
    }

    public Collection<Glossary> getNotDirtyGlossaries() {
        return CollectionUtils.subtract(glossaries, getDirtyGlossaries());
    }

    /**
     * Create a glossary.
     *
     * @param text glossary text
     * @return new Language object
     */
    @Override
    public Glossary createGlossary(String text, Boolean translate, String description) {
        Glossary glossary = findGlossaryByText(text);
        if (glossary != null) {
            throw new BusinessException(BusinessException.GLOSSARY_ALREADY_EXISTS, text);
        }
        User user = UserContext.getInstance().getUser();
        glossary = new Glossary(text, user);
        glossary.setTranslate(translate);
        glossary.setDescription(StringUtils.defaultString(description));

        glossary = (Glossary) dao.create(glossary);

        glossaries.add(glossary);
        return glossary;
    }

    /**
     * Update a language.
     *
     * @param id   old glossary text
     * @param text new glossary text
     * @return Glossary object
     */
    @Override
    public Glossary updateGlossary(String id, String text, Boolean translate, String description) {
        Glossary glossary = findGlossaryByText(id);
        dao.delete(glossary);

        if (StringUtils.isNotBlank(text)) glossary.setText(text);
        if (StringUtils.isNotBlank(description)) glossary.setDescription(description);
        if (null != translate) glossary.setTranslate(translate);

        return (Glossary) dao.create(glossary);
    }

    /**
     * Delete Glossaries.
     *
     * @param texts texts of the glossaries to be deleted.
     */
    @Override
    public void deleteGlossaries(Collection<String> texts) {
        String hSQL = "delete Glossary where text in :texts";
        Map params = new HashMap();
        params.put("texts", texts);
        dao.delete(hSQL, params);

        Iterator<Glossary> itrGlossaries = glossaries.iterator();
        Glossary g;
        while (itrGlossaries.hasNext()) {
            g = itrGlossaries.next();
            if (texts.contains(g.getText())) itrGlossaries.remove();
        }
    }


    /**
     * Make all the glossaries in Database consistent(execute on apply glossary)
     */
    @Override
    public void consistentGlossaries() {

        Collection<Glossary> dirtyGlossaries = getDirtyGlossaries();
        if (dirtyGlossaries.isEmpty()) return;

        String hSQL = "from Label where id in (select l.id from Label l, Glossary g where g.dirty=true and upper(l.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Label> labels = dao.retrieve(hSQL);

        //replace all the labels and labelTranslations
        int totalLabel = labels.size() + 1;
        int current = 1;
        float percent;
        for (final Label label : labels) {
            percent = current / (float) totalLabel * 100;
            ProgressQueue.setProgress(StringUtils.join(Arrays.asList(
                    String.format("[ %d/%d ]Process Labels...", 1, totalLabel),
                    String.format("Current Label '%s'", label.getKey())
            ), "<br/>"), percent);
            consistentGlossariesInLabel(label, dirtyGlossaries);

            current++;
        }

        // replace all the Text and Translations

        hSQL = "from Text where id in (select t.id from Text t, Glossary g where g.dirty=true and upper(t.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Text> texts = dao.retrieve(hSQL);

        int totalText = texts.size();
        current = 1;
        for (Text text : texts) {
            percent = current / (float) totalText * 100;
            ProgressQueue.setProgress(StringUtils.join(Arrays.asList(
                    String.format("[ %d/%d ]Process texts and translations...", 1, totalLabel),
                    String.format("Current text '%s'", text.getReference())
            ), "<br/>"), percent);

            consistentGlossariesInText(text, dirtyGlossaries);
        }

        // update dirty glossaries
        hSQL = "update Glossary g set dirty = false where g in (:glossaries)";
        dao.delete(hSQL, (Map) ImmutableMap.of("glossaries", dirtyGlossaries));
        glossaries = dao.retrieve("from Glossary");
    }

    /**
     * Make all the glossaries in Dictionary consistent
     * This method will be called when preview dictionaries.
     * Only label and label original translation need to be consistent in preview dictionaries.
     *
     * @param dict
     */
    @Override
    public void consistentGlossariesInDict(Dictionary dict) {
        Collection<Label> labels = dict.getLabels();
        if (null == labels || labels.isEmpty()) return;
        for (Label label : labels) {
            consistentGlossariesInLabel(label, getNotDirtyGlossaries());
//            consistentGlossariesInLabel(label, glossaries);
        }
    }

    /**
     * Make all the glossaries in Task consistent
     *
     * @param task
     */
    @Override
    public void consistentGlossariesInTask(Task task) {
        Collection<TaskDetail> taskDetails = task.getDetails();

        Collection<GlossaryMatchObject> GlossaryMatchObject = getGlossaryPatterns(getDirtyGlossaries());
//        consistentGlossariesInObject(glossaries, label, "reference", "origTranslations", "origTranslation");
        for (TaskDetail taskDetail : taskDetails) {
            final String reference = taskDetail.getText().getReference();
            for (GlossaryMatchObject gmo : GlossaryMatchObject) {
                String resultText;
                gmo.getProcessedString(reference);
                if (!gmo.isReplaced()) continue;
                resultText = gmo.getProcessedString(taskDetail.getNewTranslation());
                if (!gmo.isReplaced()) continue;
                taskDetail.setNewTranslation(resultText);
            }
        }
    }

    /**
     * @param glossaries                glossaries used for match, all glossaries will be used if null
     * @param propertyName
     * @param subCollectionPropertyName
     * @param subObjectPropertyName
     * @return Matched GlossaryMatchObject collection
     */
    private Collection<GlossaryMatchObject> consistentGlossariesInObject(final Collection<Glossary> glossaries, Object bean, String propertyName, String subCollectionPropertyName, String subObjectPropertyName) {
        Collection<GlossaryMatchObject> matchedGlossaryMatchObjects = new ArrayList<GlossaryMatchObject>();
        try {
            String propertyValue = (String) PropertyUtils.getProperty(bean, propertyName);
            if (StringUtils.isBlank(propertyValue)) return matchedGlossaryMatchObjects;

            Collection<GlossaryMatchObject> glossaryGlossaryMatchObjects = getGlossaryPatterns(glossaries);
            for (GlossaryMatchObject gmo : glossaryGlossaryMatchObjects) {
                propertyValue = (String) PropertyUtils.getProperty(bean, propertyName);
                String resultText = gmo.getProcessedString(propertyValue);
                if (!gmo.isReplaced()) continue;
                PropertyUtils.setProperty(bean, propertyName, resultText);
                matchedGlossaryMatchObjects.add(gmo);
                if (gmo.getGlossaryText().equals(resultText) && !gmo.getGlossary().getTranslate() && bean instanceof Label) {
                    Context ctxExclusion = textService.getContextByExpression(Context.EXCLUSION, (Dictionary) null);
                    ((Label) bean).setContext(ctxExclusion);
                    return matchedGlossaryMatchObjects;
                }

                if (StringUtils.isEmpty(subCollectionPropertyName)) continue;
                Collection subCollection = (Collection) PropertyUtils.getProperty(bean, subCollectionPropertyName);
                if (null == subCollection) continue;
                for (Object subObj : subCollection) {
                    String subPropertyValue = (String) PropertyUtils.getProperty(subObj, subObjectPropertyName);
                    resultText = gmo.getProcessedString(subPropertyValue);
                    PropertyUtils.setProperty(subObj, subObjectPropertyName, resultText);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
        return matchedGlossaryMatchObjects;
    }


    /**
     * Make all the glossaries in Label consistent
     *
     * @param label
     */
    private void consistentGlossariesInLabel(final Label label, final Collection<Glossary> glossaries) {
//        Consistent label reference first
        consistentGlossariesInObject(glossaries, label, "reference", "origTranslations", "origTranslation");
    }


    private void consistentGlossariesInText(final Text text, Collection<Glossary> glossaries) {
        //consistentGlossariesInObject(glossaries, text, "reference", "translations", "translation");
        if (StringUtils.isBlank(text.getReference())) return;

        String replacedReference = text.getReference();
        Collection<GlossaryMatchObject> glossaryGlossaryMatchObjects = getGlossaryPatterns(glossaries);
        for (GlossaryMatchObject gmo : glossaryGlossaryMatchObjects) {
            replacedReference = gmo.getProcessedString(replacedReference);
            //If glossary does not exists in text reference, then do not process the translations
            if (!gmo.isReplaced()) continue;

            Collection<Translation> translations = text.getTranslations();
            for (Translation translation : translations) {
                String replacedTranslation = gmo.getProcessedString(translation.getTranslation());
                translation.setTranslation(replacedTranslation);
            }
        }

        if (text.getReference().equals(replacedReference)) return;

        text.setReference(replacedReference);
        // query if the same reference and context in db
        @Language("HQL") String sameTextHQL = "from Text where reference= :reference and context.id = :cid and id !=:tid";

        Map params = ImmutableMap.of(
                "reference", replacedReference,
                "cid", text.getContext().getId(),
                "tid", text.getId());
        Collection<Text> sameTexts = dao.retrieve(sameTextHQL, params);


        if (sameTexts.isEmpty()) return;

        Context context = textService.getContextByExpression("[UNIQUE]", text);
        text.setContext(context);

        @Language("HQL") String textLabelsHQL = "from Label where text.id = :tid";
        Collection<Label> labels = dao.retrieve(textLabelsHQL, ImmutableMap.of("tid", text.getId()));
        for (Label label : labels) {
            label.setContext(context);
        }

//        Merge same translation
        for (Text sameText : sameTexts) {
            Collection<Translation> translations = sameText.getTranslations();

            for (Translation translation : translations) {
                Translation transInText = text.getTranslation(translation.getLanguage().getId());
                if (null == transInText) continue;
                if (translation.getStatus() == Translation.STATUS_TRANSLATED) {
                    if (transInText.getStatus() == Translation.STATUS_UNTRANSLATED) {
                        transInText.setTranslation(translation.getTranslation());
                        transInText.setStatus(Translation.STATUS_TRANSLATED);

                        historyService.addTranslationHistory(transInText, null, TranslationHistory.TRANS_OPER_SUGGEST, "");
                    }
                } else if (translation.getStatus() == Translation.STATUS_UNTRANSLATED) {
                    if (transInText.getStatus() == Translation.STATUS_TRANSLATED) {
                        translation.setTranslation(transInText.getTranslation());
                        translation.setStatus(Translation.STATUS_TRANSLATED);

                        historyService.addTranslationHistory(translation, null, TranslationHistory.TRANS_OPER_SUGGEST, "");
                    }
                }

            }
        }
    }

    public Collection<GlossaryMatchObject> getGlossaryPatterns() {
        return getGlossaryPatterns(null);
    }

    public Collection<GlossaryMatchObject> getNotDirtyGlossaryPatterns() {
        return getGlossaryPatterns(getNotDirtyGlossaries());
    }


    /**
     * Get glossary patterns pairs
     */
    public Collection<GlossaryMatchObject> getGlossaryPatterns(Collection<Glossary> glossaries) {
        if (null == glossaries) glossaries = this.glossaries;
        return CollectionUtils.collect(glossaries, new Transformer() {
            @Override
            public Object transform(Object input) {
                return new GlossaryMatchObject(((Glossary) input));
            }
        });
    }

    /**
     * This method is called in DictionaryserviceImpl.updateLabelReference, only label reference need to be consistent
     * when Label reference is updated
     */
    public Collection<GlossaryMatchObject> consistentGlossariesInLabelRef(Label label) {
        return consistentGlossariesInObject(getNotDirtyGlossaries(), label, "reference", null, null);
    }

    private Glossary findGlossaryByText(String text) {
        return (Glossary) dao.retrieve(Glossary.class, text);
    }


    @Override
    public Collection<Glossary> getAllGlossaries() {
        return glossaries;
    }


    //    @Override
    public Collection<Glossary> getDirtyGlossaries() {
        return CollectionUtils.select(glossaries, PredicateUtils.invokerPredicate("isDirty"));
    }

}
