package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Glossary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
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
        String hSQL = "from Glossary";
        Collection<Glossary> glossaries = dao.retrieve(hSQL);

        hSQL = "from Label where id in (select l.id from Label l, Glossary g where upper(l.reference) like concat( '%', upper(g.text), '%'))";
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

    }

    private Glossary findGlossaryByText(String text) {
        return (Glossary) dao.retrieve(Glossary.class, text);
    }
}
