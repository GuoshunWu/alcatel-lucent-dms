package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.ClosureUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-7-25
 * Time: PM 3:07
 */
@Service
public class GlossaryServiceImpl implements GlossaryService {

    private DaoService dao;
    private Collection<Glossary> glossaries;

    @Autowired
    private TextService textService;


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
        Collection<Glossary> glossaries = dao.retrieve("from Glossary where dirty = true");

        String hSQL = "from Label where id in (select l.id from Label l, Glossary g where g.dirty=true and upper(l.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Label> labels = dao.retrieve(hSQL);
        Context ctxExclusion = textService.getContextByExpression(Context.EXCLUSION, (Dictionary)null);
        for (Label label : labels) {
            label.setReference(Util.consistentGlossaries(label.getReference(), glossaries));
            if (isGlossary(label.getReference())) {
                label.setContext(ctxExclusion);
            }
        }

        hSQL = "from Text where id in (select t.id from Text t, Glossary g where g.dirty=true and upper(t.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Text> texts = dao.retrieve(hSQL);
        for (Text text : texts) {
            text.setReference(Util.consistentGlossaries(text.getReference(), glossaries));
        }

        hSQL = "from Translation where id in (select t.id from Translation t, Glossary g where g.dirty=true and upper(t.translation) like concat( '%', upper(g.text), '%'))";
        Collection<Translation> translations = dao.retrieve(hSQL);
        for (Translation translation : translations) {
            translation.setTranslation(Util.consistentGlossaries(translation.getTranslation(), glossaries));
        }

        hSQL = "from LabelTranslation where id in (select lt.id from LabelTranslation lt, Glossary g where g.dirty=true and upper(lt.origTranslation) like concat( '%', upper(g.text), '%'))";
        Collection<LabelTranslation> lbTranslations = dao.retrieve(hSQL);
        for (LabelTranslation lbTranslation : lbTranslations) {
            lbTranslation.setOrigTranslation(Util.consistentGlossaries(lbTranslation.getOrigTranslation(), glossaries));
        }
        // update dirty glossaries
        CollectionUtils.forAllDo(glossaries, ClosureUtils.invokerClosure("setDirty", new Class[]{Boolean.TYPE}, new Object[]{false}));

    }

    /**
     * Make all the glossaries in Dictionary consistent
     *
     * @param dict
     */
    @Override
    public void consistentGlossariesInDict(Dictionary dict) {
        Collection<Label> labels = dict.getLabels();
        for (Label label : labels) {
            consistentGlossariesInLabel(label);
        }
    }

    /**
     * Make all the glossaries in Dictionary consistent
     *
     * @param task
     */
    @Override
    public void consistentGlossariesInTask(Task task) {
        Collection<TaskDetail> taskDetails = task.getDetails();
        for (TaskDetail taskDetail : taskDetails) {
//            taskDetail.setOrigTranslation(Util.consistentGlossaries(taskDetail.getOrigTranslation(), glossaries));
            taskDetail.setNewTranslation(Util.consistentGlossaries(taskDetail.getNewTranslation(), glossaries));
        }
    }


    /**
     * Make all the glossaries in Label consistent
     *
     * @param label
     */
    @Override
    public void consistentGlossariesInLabel(Label label) {
        label.setReference(Util.consistentGlossaries(label.getReference(), glossaries));
//        Text text = label.getText();
//        text.setReference(Util.consistentGlossaries(text.getReference(), glossaries));

        Collection<LabelTranslation> labelTranslations = label.getOrigTranslations();
        if (null == labelTranslations) return;
        for (LabelTranslation ltTranslation : labelTranslations) {
            ltTranslation.setOrigTranslation(Util.consistentGlossaries(ltTranslation.getOrigTranslation(), glossaries));
        }

//        Collection<Translation> translations = text.getTranslations();
//        for (Translation translation : translations) {
//            translation.setTranslation(Util.consistentGlossaries(translation.getTranslation(), glossaries));
//        }
    }

    public void consistentGlossariesInLabelRef(Label label) {
        label.setReference(getConsistentGlossariesText(label.getReference()));
        if (isGlossary(label.getReference())) {
            label.setContext(textService.getContextByExpression(Context.EXCLUSION, (Dictionary)null));
        }
    }

    /**
     * Check if a string is in glossary list
     *
     * @param s
     * @return
     */
    private boolean isGlossary(String s) {
        s = s.trim();
        for (Glossary g : glossaries) {
            if (g.getText().equals(s)) return true;
        }
        return false;
    }

    @Override
    public String getConsistentGlossariesText(String text) {
        return Util.consistentGlossaries(text, glossaries);
    }


    private Glossary findGlossaryByText(String text) {
        return (Glossary) dao.retrieve(Glossary.class, text);
    }


    @Override
    public Collection<Glossary> getAllGlossaries() {
        return glossaries;
    }
}
