package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static final String PATTERN_FMT = "((?<=[\\xf7\\xd7[^\\xc0-\\xff]&&\\W]|^))((?i)%s)((?=[\\xf7\\xd7[^\\xc0-\\xff]&&\\W]|$))";

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
        for (final Label label : labels) {
            consistentGlossariesInLabel(label, ctxExclusion, dirtyGlossaries);
        }

        // replace all the Text and Translations

        hSQL = "from Text where id in (select t.id from Text t, Glossary g where g.dirty=true and upper(t.reference) like concat( '%', upper(g.text), '%'))";
        Collection<Text> texts = dao.retrieve(hSQL);
        for (Text text : texts) {
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

        Collection<PatternPair> patternPair = getGlossaryPatterns();

        for (TaskDetail taskDetail : taskDetails) {
            final String reference = taskDetail.getText().getReference();
            Collection<PatternPair> matchedPatternPair = CollectionUtils.select(patternPair, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    PatternPair pp = (PatternPair) object;
                    return pp.getPattern().matcher(reference).find();
                }
            });

            for (PatternPair pp : matchedPatternPair) {
                Matcher matcher = pp.getPattern().matcher(taskDetail.getNewTranslation());
                if (!matcher.find()) continue;
                taskDetail.setNewTranslation(matcher.replaceAll(pp.getReplacement()));
            }
        }
    }


    /**
     * Make all the glossaries in Label consistent
     *
     * @param label
     * @param ctxExclusion
     */
    private void consistentGlossariesInLabel(final Label label, Context ctxExclusion, Collection<Glossary> dirtyGlossaries) {
//        Consistent label reference first
        if (StringUtils.isBlank(label.getReference())) return;
        Collection<PatternPair> glossaryPatternPairs = getGlossaryPatterns(dirtyGlossaries);
        for (PatternPair gpp : glossaryPatternPairs) {
            Matcher matcher = gpp.getPattern().matcher(label.getReference());
            if (!matcher.find()) continue;
            label.setReference(matcher.replaceAll(gpp.getReplacement()));

            if (isInGlossaries(label.getReference(), dirtyGlossaries)) {
                label.setContext(ctxExclusion);
            }

            Collection<LabelTranslation> labelTranslations = label.getOrigTranslations();
            if (null == labelTranslations) return;
            for (LabelTranslation lt : labelTranslations) {
                Matcher transMatcher = gpp.getPattern().matcher(lt.getOrigTranslation());
                if (!transMatcher.find()) continue;
                lt.setOrigTranslation(transMatcher.replaceAll(gpp.getReplacement()));
            }

        }
    }

    private void consistentGlossariesInText(final Text text, Collection<Glossary> dirtyGlossaries) {
        if (StringUtils.isBlank(text.getReference())) return;
        Collection<PatternPair> glossaryPatternPairs = getGlossaryPatterns(dirtyGlossaries);

        for (PatternPair gpp : glossaryPatternPairs) {
            Matcher matcher = gpp.getPattern().matcher(text.getReference());
            if (!matcher.find()) continue;
            text.setReference(matcher.replaceAll(gpp.getReplacement()));

            Collection<Translation> translations = text.getTranslations();
            if (null == translations) return;
            for (Translation translation : translations) {
                Matcher transMatcher = gpp.getPattern().matcher(translation.getTranslation());
                if (!transMatcher.find()) continue;
                translation.setTranslation(transMatcher.replaceAll(gpp.getReplacement()));
            }
        }
    }

    public Collection<PatternPair> getGlossaryPatterns() {
        return getGlossaryPatterns(null);
    }

    /**
     * Get glossary patterns pairs
     */
    public Collection<PatternPair> getGlossaryPatterns(Collection<Glossary> glossaries) {
        if (null == glossaries) glossaries = this.glossaries;
        return CollectionUtils.collect(glossaries, new Transformer() {
            @Override
            public Object transform(Object input) {
                String glossaryText = ((Glossary) input).getText();
                return new PatternPair(Pattern.compile(String.format(PATTERN_FMT, glossaryText)), String.format("$1%s$3", glossaryText));
            }
        });
    }

    /**
     * Get matched glossary pattern pairs
     */

    public Collection<PatternPair> getGlossaryMatchedPatterns(Collection<Glossary> glossaries, final String text) {
        if (null == glossaries) glossaries = this.glossaries;
        Collection<PatternPair> patternParis = getGlossaryPatterns(glossaries);
        return CollectionUtils.select(patternParis, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((PatternPair) object).getPattern().matcher(text).find();
            }
        });
    }


    public Collection<PatternPair> getGlossaryMatchedPatterns(final String text) {
        return getGlossaryMatchedPatterns((Collection<Glossary>) null, text);
    }

    /**
     * This method is called in DictionaryserviceImpl.updateLabelReference, only label reference need to be consistent
     * when Label reference is updated
     */
    public Collection<PatternPair> consistentGlossariesInLabelRef(Label label) {
        Collection<PatternPair> glossaryPatternPairs = getGlossaryPatterns();
        Collection<PatternPair> matchedGlossaryPatternPairs = new ArrayList<PatternPair>();
        for (PatternPair pp : glossaryPatternPairs) {
            Matcher matcher = pp.getPattern().matcher(label.getReference());
            if (!matcher.find()) continue;
            matchedGlossaryPatternPairs.add(pp);
            label.setReference(matcher.replaceAll(pp.getReplacement()));
        }
        if (isInGlossaries(label.getReference(), glossaries)) {
            label.setContext(textService.getContextByExpression(Context.EXCLUSION, (Dictionary) null));
        }
        return matchedGlossaryPatternPairs;
    }

    /**
     * Check if a string is in a glossary collection
     *
     * @param s
     * @return
     */
    private boolean isInGlossaries(String s, Collection<Glossary> glossaries) {
        s = s.trim();
        for (Glossary g : glossaries) {
            if (g.getText().equals(s)) return true;
        }
        return false;
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
