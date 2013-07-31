package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-7-25
 * Time: PM 3:07
 */
@Service
public class GlossaryServiceImpl implements GlossaryService {

    @Autowired
    private DaoService dao;

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
//        return (Glossary) dao.create(new Glossary(text));
        return (Glossary) dao.create(new Glossary(new Date(), text));
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
    }

    /**
     * Make all the glossaries in Database consistent
     */
    @Override
    public void consistentGlossaries() {
        Collection<Glossary> glossaries = getAllGlossaries();

        String hSQL = "from Label where id in (select l.id from Label l, Glossary g where upper(l.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Label> labels = dao.retrieve(hSQL);
        for (Label label : labels) {
            label.setReference(Util.consistentGlossaries(label.getReference(), glossaries));
        }

        hSQL = "from Text where id in (select t.id from Text t, Glossary g where upper(t.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Text> texts = dao.retrieve(hSQL);
        for (Text text : texts) {
            text.setReference(Util.consistentGlossaries(text.getReference(), glossaries));
        }

        hSQL = "from Translation where id in (select t.id from Translation t, Glossary g where upper(t.translation) like concat( '%', upper(g.text), '%'))";
        Collection<Translation> translations = dao.retrieve(hSQL);
        for (Translation translation : translations) {
            translation.setTranslation(Util.consistentGlossaries(translation.getTranslation(), glossaries));
        }

        hSQL = "from LabelTranslation where id in (select lt.id from LabelTranslation lt, Glossary g where upper(lt.origTranslation) like concat( '%', upper(g.text), '%'))";
        Collection<LabelTranslation> lbTranslations = dao.retrieve(hSQL);
        for (LabelTranslation lbTranslation : lbTranslations) {
            lbTranslation.setOrigTranslation(Util.consistentGlossaries(lbTranslation.getOrigTranslation(), glossaries));
        }

    }

    /**
     * Make all the glossaries in Dictionary consistent
     *
     * @param dict
     */
    @Override
    public void consistentGlossariesInDict(Dictionary dict, Collection<Glossary> glossaries) {
        Collection<Label> labels = dict.getLabels();
        for (Label label : labels) {
            consistentGlossariesInLabel(label, glossaries);
        }
    }

    /**
     * Make all the glossaries in Dictionary consistent
     *
     * @param task
     */
    @Override
    public void consistentGlossariesInTask(Task task, Collection<Glossary> glossaries) {
        Collection<TaskDetail> taskDetails = task.getDetails();
        for (TaskDetail taskDetail : taskDetails) {
//            taskDetail.setOrigTranslation(Util.consistentGlossaries(taskDetail.getOrigTranslation(), glossaries));
            taskDetail.setNewTranslation(Util.consistentGlossaries(taskDetail.getNewTranslation(), glossaries));
        }
    }

    public void consistentGlossariesInTask(Task task) {
        consistentGlossariesInTask(task, getAllGlossaries());
    }



    /**
     * Make all the glossaries in Label consistent
     *
     * @param label
     */
    @Override
    public void consistentGlossariesInLabel(Label label, Collection<Glossary> glossaries) {
        label.setReference(Util.consistentGlossaries(label.getReference(), glossaries));
//        Text text = label.getText();
//        text.setReference(Util.consistentGlossaries(text.getReference(), glossaries));

        Collection<LabelTranslation> labelTranslations = label.getOrigTranslations();
        for (LabelTranslation ltTranslation : labelTranslations) {
            ltTranslation.setOrigTranslation(Util.consistentGlossaries(ltTranslation.getOrigTranslation(), glossaries));
        }

//        Collection<Translation> translations = text.getTranslations();
//        for (Translation translation : translations) {
//            translation.setTranslation(Util.consistentGlossaries(translation.getTranslation(), glossaries));
//        }
    }

    public void consistentGlossariesInLabelRef(Label label){
        label.setReference(getConsistentGlossariesText(label.getReference()));
    }

    @Override
    public String getConsistentGlossariesText(String text) {
        return getConsistentGlossariesText(text, getAllGlossaries());
    }

    @Override
    public String getConsistentGlossariesText(String text, Collection<Glossary> glossaries) {
        return Util.consistentGlossaries(text, glossaries);
    }

    @Override
    public Collection<Glossary> getAllGlossaries() {
        return dao.retrieve("from Glossary");
    }

    private Glossary findGlossaryByText(String text) {
        return (Glossary) dao.retrieve(Glossary.class, text);
    }
}
