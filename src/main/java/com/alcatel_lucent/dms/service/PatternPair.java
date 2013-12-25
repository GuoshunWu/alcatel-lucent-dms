package com.alcatel_lucent.dms.service;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.openInputStream;

/**
 * Created by guoshunw on 13-12-11.
 */
public class PatternPair {

    public static String substitutePattern;
    public static Collection<String> exceptionPatterns = new ArrayList<String>();

    private static final File glossaryConfigFile =
            new File(GlossaryServiceImpl.class.getResource("glossaryConfig.properties").getPath());

    private static Logger log = LoggerFactory.getLogger(PatternPair.class);

    /**
     *  Load config file and update substitutePattern and exceptionPatterns with its content
     * */
    private static Properties loadPatternConfigFile() {
        Properties properties = new Properties();
        try {
            properties.load(openInputStream(glossaryConfigFile));
        } catch (IOException e) {
            e.printStackTrace();
            return properties;
        }
        substitutePattern = properties.getProperty("substitutePattern");
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();

        exceptionPatterns.clear();
        for (Map.Entry entry : entries) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!key.startsWith("exceptionPatterns")) continue;
            exceptionPatterns.add(value);
        }
        return properties;
    }

    /**
     * Load the config file when the PatternPair class is loaded.
     * Watch the glossaries config file every second and reload the file if it is modified.
     * */
    static {
        FileAlterationObserver observer = new FileAlterationObserver(glossaryConfigFile.getParentFile(),
                FileFilterUtils.nameFileFilter(glossaryConfigFile.getName()));
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(File file) {
                Properties properties = loadPatternConfigFile();
                log.info("{} file changed, content:\n{}", file.getName(), properties.toString());
            }
        });
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);
        try {
            loadPatternConfigFile();
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    PatternPair(String glossaryText) {
        this.glossaryText = glossaryText;
        this.pattern = Pattern.compile(String.format(substitutePattern, glossaryText));
        this.replacement = String.format("$1%s$3", glossaryText);
    }

    private Pattern pattern;
    private String replacement;
    private String glossaryText;

    private Matcher matcher;

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getGlossaryText() {
        return glossaryText;
    }

    public Matcher getMatcher(String text) {
        this.matcher = pattern.matcher(text);
        return matcher;
    }

    /**
     * Find the glossary in text and not match exceptions
     * */
    public boolean find(String text) {
        if (null == matcher) getMatcher(text);
        boolean isFound = matcher.find();
        if (!isFound) return false;

        boolean isIncludeInExceptions = false;
        Pattern exceptionPattern;
        for (String exceptionPatternStr : exceptionPatterns) {
            exceptionPattern = Pattern.compile(String.format(exceptionPatternStr, glossaryText));
            Matcher exceptionMatcher = exceptionPattern.matcher(text);
            if (!exceptionMatcher.find()) continue;
            if (exceptionMatcher.start() <= matcher.start() && exceptionMatcher.end() >= matcher.end()) {
                isIncludeInExceptions = true;
                break;
            }
        }

        return isFound && !isIncludeInExceptions;
    }

    public String getProcessedString(String originalText) {
        return find(originalText) ? matcher.replaceAll(replacement) : originalText;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
