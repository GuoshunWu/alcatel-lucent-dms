package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Glossary;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.lang3.Range.between;

/**
 * Created by guoshunw on 13-12-11.
 */
public class GlossaryMatchObject {

    public static String substitutePattern;
    public static Collection<Pattern> exceptionPatterns = new ArrayList<Pattern>();
    private Glossary glossary;

    private static final File glossaryConfigFile =
            new File(GlossaryServiceImpl.class.getResource("glossaryConfig.properties").getPath());

    private static Logger log = LoggerFactory.getLogger(GlossaryMatchObject.class);

    /**
     * Load config file and update substitutePattern and exceptionPatterns with its content
     */
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
            exceptionPatterns.add(Pattern.compile(value, Pattern.CASE_INSENSITIVE));
        }
        return properties;
    }

    /**
     * Load the config file when the GlossaryMatchObject class is loaded.
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

    GlossaryMatchObject(Glossary glossary) {
        this.glossary = glossary;
        this.glossaryText = glossary.getText();
        this.pattern = Pattern.compile(String.format(substitutePattern, glossaryText), Pattern.CASE_INSENSITIVE);
    }

    private Pattern pattern;
    private boolean replaced = false;
    private String glossaryText;

    public Glossary getGlossary() {
        return glossary;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getGlossaryText() {
        return glossaryText;
    }

    /*
    *  Only replace the text when the match is not in any of the exception patterns
    * */

    private void processMatchInExceptions(Matcher matcher, String text, StringBuffer sb) {
        Range<Integer> matchedRange = between(matcher.start(), matcher.end());

        boolean replace = true;
        for (Pattern exceptionPattern : exceptionPatterns) {
            Matcher exceptionMatcher = exceptionPattern.matcher(text);
            while (exceptionMatcher.find() & replace) {
                Range<Integer> exceptionMatchedRange = between(exceptionMatcher.start(), exceptionMatcher.end());
                if (exceptionMatchedRange.containsRange(matchedRange)) {
                    replace = false;
                }
            }
            if (!replace) break;
        }
        if (replace) {
            matcher.appendReplacement(sb, glossaryText);
            this.replaced = true;
        }
    }


    public String getProcessedString(String originalText) {
    	replaced = false;
    	if (StringUtils.isEmpty(originalText)) return originalText;
        Matcher matcher = pattern.matcher(originalText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            processMatchInExceptions(matcher, originalText, sb);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public boolean isReplaced() {
        return replaced;
    }

}
