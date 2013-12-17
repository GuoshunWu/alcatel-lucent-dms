package com.alcatel_lucent.dms.service;

import java.util.regex.Pattern;

/**
 * Created by guoshunw on 13-12-11.
 */
public class PatternPair {
    PatternPair(Pattern pattern, String replacement, String glossary) {
        this.pattern = pattern;
        this.replacement = replacement;
        this.glossary = glossary;
    }

    private Pattern pattern;
    private String replacement;
    private String glossary;

    public PatternPair(String glossary) {
        this.glossary = glossary;
    }

    public String getGlossary() {
        return glossary;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
