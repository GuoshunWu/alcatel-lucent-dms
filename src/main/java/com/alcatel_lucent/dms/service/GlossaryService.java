package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Glossary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Task;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-7-25
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public interface GlossaryService {

    /**
     * Create a glossary.
     *
     * @param text glossary text
     * @return new Language object
     */
    Glossary createGlossary(String text);


    /**
     * Update a language.
     *
     * @param id   old glossary text
     * @param text new glossary text
     * @return Glossary object
     */
    Glossary updateGlossary(String id, String text);


    /**
     * Delete Glossaries.
     *
     * @param texts texts of the glossaries to be deleted.
     */
    void deleteGlossaries(Collection<String> texts);


    /**
     * Make all the glossaries in Database consistent
     * */
    void consistentGlossaries();

    /**
     * Make all the glossaries in Dictionary consistent
     * @param dict
     * */
    void consistentGlossariesInDict(Dictionary dict);

    /**
     * Make all the glossaries in Task consistent
     * @param task
     * */
    void consistentGlossariesInTask(Task task);

    /**
     * Make all the glossaries in Label consistent
     * @param label
     * */

    void consistentGlossariesInLabel(Label label);
    void consistentGlossariesInLabelRef(Label label);

    String getConsistentGlossariesText(String text);


    Collection<Glossary> getAllGlossaries();

}
