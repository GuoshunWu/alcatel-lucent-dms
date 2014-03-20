package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.parser.DictionaryParser;
import com.alcatel_lucent.dms.util.Util;
import com.alcatel_lucent.dms.util.XDCPDTDEntityResolver;
import com.alcatel_lucent.dms.util.XDCTDTDEntityResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Component("TMXParser")
@SuppressWarnings("unchecked")
public class TMXParser extends DictionaryParser {

    private static final String[] extensions = new String[]{"tmx"};
    private static final SuffixFileFilter tmxFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    private static final EntityResolver tmxDTDEntityResolver = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            URL dtdURL = getClass().getResource("/dtds/tmx14.dtd");
            InputSource is = new InputSource(dtdURL.toString());
            return is;
        }
    };
    private static Logger log = LoggerFactory.getLogger(TMXParser.class);

    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        //TODO: change this after deploy
        return null;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        Collection<File> tmxFiles = FileUtils.listFiles(file, tmxFilter, FileFilterUtils.directoryFileFilter());

        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);

        for (File tmxFile : tmxFiles) {
            try {
                deliveredDicts.add(processDictionary(tmxFile, FilenameUtils.normalize(rootDir, true), acceptedFiles));
            } catch (BusinessException e) {
                exceptions.addNestedException(e);
            }
        }

        if (exceptions.hasNestedException()) {
            throw exceptions;
        }

        return deliveredDicts;
    }

    public Dictionary processDictionary(File tmxFile, String rootDir, Collection<File> acceptedFiles) {
        log.info(center("Parsing dictionary '" + tmxFile, 50, '='));

        //validate if it is a valid tmx file
        SAXReader saxReader = new SAXReader();
        saxReader.setValidation(false);
        saxReader.setEntityResolver(tmxDTDEntityResolver);
        Document document;
        try {
            saxReader.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            document = saxReader.read(tmxFile);
        } catch (DocumentException e) {
            throw new BusinessException(BusinessException.INVALID_TMX_FILE, tmxFile.getName(), e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        DictionaryBase dictBase = new DictionaryBase();
        String dictName = FilenameUtils.normalize(tmxFile.getAbsolutePath(), true);
        if (dictName.startsWith(rootDir)) dictName = dictName.substring(rootDir.length() + 1);
        dictBase.setName(dictName);
        dictBase.setPath(FilenameUtils.normalize(tmxFile.getAbsolutePath()));
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(DictionaryFormat.TMX.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);


        acceptedFiles.add(tmxFile);

        return dictionary;
    }

}
