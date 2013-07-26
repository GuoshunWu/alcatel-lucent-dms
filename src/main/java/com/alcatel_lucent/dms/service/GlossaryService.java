package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Glossary;

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

}
