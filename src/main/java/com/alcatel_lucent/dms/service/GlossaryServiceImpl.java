package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.sun.tools.javac.resources.compiler;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.*;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.openInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-7-25
 * Time: PM 3:07
 */
@Service("glossaryService")
public class GlossaryServiceImpl implements GlossaryService {

    private DaoService dao;
    private Collection<Glossary> glossaries;

    @Autowired
    private TextService textService;

    private static Logger log = LoggerFactory.getLogger(GlossaryServiceImpl.class);

    @Autowired
    public void setDao(DaoService dao) {
        this.dao = dao;
        this.glossaries = dao.retrieve("from Glossary");
    }

    /**
     * Create a glossary.
     *
     * @param text glossary text
     * @return new Language object
     */
    @Override
    public Glossary createGlossary(String text) {
        Glossary language = findGlossaryByText(text);
        if (language != null) {
            throw new BusinessException(BusinessException.GLOSSARY_ALREADY_EXISTS, text);
        }
        User user = UserContext.getInstance().getUser();
        Glossary glossary = new Glossary(text, user);
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
    public Glossary updateGlossary(String id, String text) {
        Glossary glossary = findGlossaryByText(id);
        if (StringUtils.isNotBlank(text)) {
            dao.delete(Glossary.class, id);
            glossaries.remove(glossary);
            glossary = createGlossary(text);
        }
        return glossary;
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
     * Make all the glossaries in Database consistent
     */
    @Override
    public void consistentGlossaries() {

        Collection<Glossary> dirtyGlossaries = getDirtyGlossaries();

        String hSQL = "from Label where id in (select l.id from Label l, Glossary g where g.dirty=true and upper(l.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Label> labels = dao.retrieve(hSQL);

        //replace all the labels and labelTranslations
        Context ctxExclusion = textService.getContextByExpression(Context.EXCLUSION, (Dictionary) null);
        int totalLabel = labels.size() + 1;
        int current = 1;
        float percent;
        for (final Label label : labels) {
            percent = current / (float) totalLabel * 100;
            ProgressQueue.setProgress(StringUtils.join(Arrays.asList(
                    String.format("[ %d/%d ]Process Labels...", 1, totalLabel),
                    String.format("Current Label '%s'", label.getKey())
            ), "<br/>"), percent);
            consistentGlossariesInLabel(label, ctxExclusion, dirtyGlossaries);
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
        CollectionUtils.forAllDo(dirtyGlossaries, ClosureUtils.invokerClosure("setDirty", new Class[]{Boolean.TYPE}, new Object[]{false}));

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
        Context ctxExclusion = textService.getContextByExpression(Context.EXCLUSION, (Dictionary) null);
        for (Label label : labels) {
            consistentGlossariesInLabel(label, ctxExclusion, glossaries);
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

        Collection<GlossaryMatchObject> GlossaryMatchObject = getGlossaryPatterns();
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
     *
     * */
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
                if (gmo.getGlossaryText().equals(resultText) && bean instanceof Label) {
                    Context ctxExclusion = textService.getContextByExpression(Context.EXCLUSION, (Dictionary) null);
                    ((Label) bean).setContext(ctxExclusion);
                    return matchedGlossaryMatchObjects;
                }

                if (StringUtils.isEmpty(subCollectionPropertyName)) continue;
                Collection subCollection = (Collection) PropertyUtils.getProperty(bean, subCollectionPropertyName);
                if (null == subCollection) return matchedGlossaryMatchObjects;
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
     * @param ctxExclusion
     */
    private void consistentGlossariesInLabel(final Label label, final Context ctxExclusion, final Collection<Glossary> glossaries) {
//        Consistent label reference first
        consistentGlossariesInObject(glossaries, label, "reference", "origTranslations", "origTranslation");
    }


    private void consistentGlossariesInText(final Text text, Collection<Glossary> glossaries) {
        consistentGlossariesInObject(glossaries, text, "reference", "translations", "translation");
    }

    public Collection<GlossaryMatchObject> getGlossaryPatterns() {
        return getGlossaryPatterns(null);
    }

    /**
     * Get glossary patterns pairs
     */
    public Collection<GlossaryMatchObject> getGlossaryPatterns(Collection<Glossary> glossaries) {
        if (null == glossaries) glossaries = this.glossaries;
        return CollectionUtils.collect(glossaries, new Transformer() {
            @Override
            public Object transform(Object input) {
                return new GlossaryMatchObject(((Glossary) input).getText());
            }
        });
    }

    /**
     * This method is called in DictionaryserviceImpl.updateLabelReference, only label reference need to be consistent
     * when Label reference is updated
     */
    public Collection<GlossaryMatchObject> consistentGlossariesInLabelRef(Label label) {
        return consistentGlossariesInObject(glossaries, label, "reference", null, null);
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
