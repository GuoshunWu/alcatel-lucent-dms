package com.alcatel_lucent.dms.service.parser;

import java.io.File;

/**
 * Created by guoshunw on 13-12-16.
 */
public class DictEntry {
    private File file;

    DictEntry(File file) {
        this.file = file;
    }

    public String getLangCode() {
        if (file == null || !file.exists() || null == file.getParentFile()) return null;
        return file.getParentFile().getName().toLowerCase();
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictEntry dictEntry = (DictEntry) o;

        return dictEntry.getLangCode().equalsIgnoreCase(getLangCode()) && dictEntry.file.getName().equals(file.getName());
    }

    @Override
    public int hashCode() {
        int result = getLangCode().hashCode();
        result = 31 * result + file.hashCode();
        return result;
    }

    public boolean isReferenceFile() {
        return null != file.getParentFile() && getLangCode().equalsIgnoreCase(OTCWebParser.REFERENCE_LANG_CODE);
    }

    public boolean isOTEWebFile() {
        return null != getLangCode() && getLangCode().matches(OTCWebParser.LANG_PATTERN);
    }

    public String getFileName() {
        return file.getName();
    }
}
