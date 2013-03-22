package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.join;

@Component()
@SuppressWarnings("unchecked")
public class ACSTextDictParser extends DictionaryParser {

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        if (exceptions.hasNestedException()) {
            throw exceptions;
        } else {
            return result;
        }
    }

    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;
        rootDir = normalize(rootDir, true);


        return deliveredDicts;
    }
}
