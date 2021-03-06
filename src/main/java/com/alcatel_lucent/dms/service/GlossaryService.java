package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.*;

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
     * @return new glossary object
     */
    Glossary createGlossary(String text, Boolean translate, String description);


    /**
     * Update a Glossary.
     *
     * @param id   old glossary text
     * @param text new glossary text
     * @return Glossary object
     */
    Glossary updateGlossary(String id, String text, Boolean translate, String description);


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

    Collection<GlossaryMatchObject> consistentGlossariesInLabelRef(Label label);

    Collection<Glossary> getAllGlossaries();
    Collection<GlossaryMatchObject> getGlossaryPatterns();
    Collection<GlossaryMatchObject> getGlossaryPatterns(Collection<Glossary> glossaries);

    Collection<GlossaryMatchObject> getNotDirtyGlossaryPatterns();
    Collection<Glossary> getNotDirtyGlossaries();
    
    /**
     * Replace glossaries in a single string.
     * @param glossaryMatchObjects
     * @param reference
     * @return
     */
	String consistentGlossariesInString(
			Collection<GlossaryMatchObject> glossaryMatchObjects,
			String reference);
	
	Collection<GlossaryMatchObject> consistentGlossariesInObject(Object bean, String propertyName, String subCollectionPropertyName, String subObjectPropertyName);
}
