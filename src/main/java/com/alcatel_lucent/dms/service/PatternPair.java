package com.alcatel_lucent.dms.service;

import java.util.regex.Pattern;

/**
 * Created by guoshunw on 13-12-11.
 */
public class PatternPair {

    PatternPair(Pattern pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    private Pattern pattern;
    private String replacement;

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
