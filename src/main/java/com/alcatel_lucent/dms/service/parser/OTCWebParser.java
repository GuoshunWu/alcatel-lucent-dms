package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.apache.commons.lang3.StringUtils.stripStart;

//@Component
public class OTCWebParser extends DictionaryParser {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String[] extensions = new String[]{"json", "js"};
    @Autowired
    private LanguageService languageService;

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (null == file
                || !file.exists()
                || file.isFile() && !FilenameUtils.isExtension(file.getName(), extensions)
                ) return deliveredDicts;
        // It is a directory
        Collection<File> OTCWebFiles = FileUtils.listFiles(file, extensions, true);

        for (File OTCWebFile : OTCWebFiles) {
            try {
                deliveredDicts.add(parseDictionary(normalize(rootDir, true), OTCWebFile, acceptedFiles));
            } catch (BusinessException e) {
                if (e.getErrorCode() != BusinessException.INVALID_OTC_WEB_DICT_FILE) {
                    throw e;
                }
            }
        }
        return deliveredDicts;
    }

    public Dictionary parseDictionary(String rootDir, File file, Collection<File> acceptedFiles) {
        DictionaryBase dictBase = new DictionaryBase();
        Dictionary dictionary = null;


        String dictPath = normalize(file.getAbsolutePath(), true);
        String dictName = dictPath;
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }

        dictBase.setName(dictName);
        dictBase.setPath(dictPath);
        dictBase.setEncoding(DEFAULT_ENCODING);
        dictBase.setFormat(Constants.DictionaryFormat.OTC_PC.toString());

        dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);

        InputStream in = null;
        try {
            in = new AutoCloseInputStream(new FileInputStream(file));
            String encoding = Util.detectEncoding(file);
            if (encoding.startsWith("UTF-")) {
                do {
                    in = new BOMInputStream(in, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE);
                } while (((BOMInputStream) in).hasBOM());
            }
//            else  if(!isRef){
//                encoding is ISO-8859-1
//            encoding = languageService.getLanguage(splitFileName(file.getName())[2]).getDefaultCharset();
//            }

            String jsonString = stripEnd(stripStart(IOUtils.toString(in, encoding), "define("), ")");
            JSONObject jsonObject = JSONObject.fromObject(jsonString);
//            MapUtils.debugPrint(System.out,"jsonObj", jsonObject);
            Iterator<String> keysItr=jsonObject.keys();
            String key = null;
            String value = null;
            while(keysItr.hasNext()){
                key = keysItr.next();
                value = jsonObject.getString(key).trim();
                System.out.printf("key=%s, value=%s\n", key, value);
            }

            // TODO: parse dictionary


            acceptedFiles.add(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
        return dictionary;
    }
}
