package com.alcatel_lucent.dms.service;

import static com.alcatel_lucent.dms.util.Util.generateSpace;
import static org.apache.commons.lang.StringUtils.join;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.Util;

import java.text.DateFormat;

@Service("dictionaryService")
@Scope("singleton")
public class DictionaryServiceImpl extends BaseServiceImpl implements
        DictionaryService {

    private static Logger log = Logger.getLogger(DictionaryServiceImpl.class);

    public static Logger logDictDeliverSuccess = Logger
            .getLogger("DictDeliverSuccess");
    public static Logger logDictDeliverFail = Logger
            .getLogger("DictDeliverFail");

    public static Logger logDictDeliverWarning = Logger
            .getLogger("DictDeliverWaning");

    @Autowired
    private TextService textService;

    @Autowired
    private DictionaryParser dictionaryParser;

    @Autowired
    private LanguageService langService;

    @Autowired
    private DictionaryProp dictProp;

    @Autowired MDCParser mdcParser;


    private DateFormat dFmt=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    public DictionaryServiceImpl() {
        super();
    }

    public int deleteDCT(String dctName) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", dctName);

        List<Dictionary> dicts = dao.retrieve(
                "from Dictionary where name=:name", params);
        int result = 0;
        for (Dictionary dict : dicts) {
            dao.delete(dict);
            result++;
        }
        return result;
    }

    public Dictionary deliverDCT(String dictionaryName, String path,
                                 InputStream dctInputStream, Long appId, String encoding,
                                 String[] langCodes, Map<String, String> langCharset,
                                 Collection<BusinessWarning> warnings) throws BusinessException,
            IOException {

        long before = System.currentTimeMillis();
        Dictionary dict = previewDCT(dictionaryName, path, dctInputStream,
                appId, encoding, warnings);
        long after = System.currentTimeMillis();
        log.info("**************previewDCT take " + (after - before)
                + " milliseconds of time.************");

        log.info("Dictionary " + dict.getName()
                + " is about to import to database");

        before = System.currentTimeMillis();
        dict = importDCT(dict, langCodes, langCharset, warnings);
        after = System.currentTimeMillis();
        log.info("************importDCT take " + (after - before)
                + " milliseconds of time.**************");

        return dict;
    }


    public Dictionary deliverMDC(String dictionaryName, String path,
                                 InputStream dctInputStream, Long appId, String[] langCodes, Map<String, String> langCharset,
                                 Collection<BusinessWarning> warnings) throws BusinessException,
            IOException {

        long before = System.currentTimeMillis();
        Dictionary dict = previewMDC(dictionaryName, path, dctInputStream,
                appId, warnings);
        long after = System.currentTimeMillis();
        log.info("**************previewMDC take " + (after - before)
                + " milliseconds of time.************");

        log.info("Dictionary " + dict.getName()
                + " is about to import to database");

        before = System.currentTimeMillis();
        dict = importDCT(dict, langCodes, langCharset, warnings);
        after = System.currentTimeMillis();
        log.info("************importMDC take " + (after - before)
                + " milliseconds of time.**************");

        return dict;
    }

    public Dictionary deliverDCT(String dictionaryName, String path,
                                 Long appId, String encoding, String[] langCodes,
                                 Map<String, String> langCharset,
                                 Collection<BusinessWarning> warnings) throws BusinessException {
        InputStream is;
        try {
            is = new FileInputStream(path);
            if (null == encoding) {
                byte[] bom = new byte[Util.UTF8_BOM_LENGTH];
                is.read(bom);
                encoding = Util.detectEncoding(bom);
                is.close();
                is = new FileInputStream(path);
            }
            return deliverDCT(dictionaryName, path, is, appId, encoding,
                    langCodes, langCharset, warnings);
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }

    }

    /**
     * Deliver dct files in a directory After using dictionary properties, now
     * encoding and langCharset parameter are useless.
     *
     * @param rootDir
     * @param file
     */
    public Collection<Dictionary> deliverDCTFiles(String rootDir, File file,
                                                  Long appId, String encoding, String[] langCodes,
                                                  Map<String, String> langCharset,
                                                  Collection<BusinessWarning> warnings) throws BusinessException {

        if (!file.exists())
            return null;

        Collection<Dictionary> deliveredDicts = new ArrayList<Dictionary>();

        if (file.isDirectory()) {
            File[] dctFileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isDCTFile(pathname)
                            || Util.isZipFile(pathname);
                }
            });
            for (File dctFile : dctFileOrDirs) {
                Collection<Dictionary> subDeliveredDicts = deliverDCTFiles(
                        rootDir, dctFile, appId, encoding, langCodes,
                        langCharset, warnings);
                deliveredDicts.addAll(subDeliveredDicts);
            }
            return deliveredDicts;
        }

        if (Util.isZipFile(file)) {
            try {
                Collection<Dictionary> zipDeliveredDicts = deliverZipDCTFile(
                        rootDir, new ZipFile(file), appId, encoding, langCodes,
                        langCharset, warnings);
                deliveredDicts.addAll(zipDeliveredDicts);
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
            return deliveredDicts;
        }

        // normal dct file
        Dictionary dict = null;
        try {
            rootDir = rootDir.replace("\\", "/");
            String dictPath = file.getAbsolutePath().replace("\\", "/");
            String dictName = dictPath.replace(rootDir, "");

            encoding = encoding==null?dictProp.getDictionaryEncoding(dictName):encoding;
            langCharset = null==langCharset? dictProp.getDictionaryCharsets(dictName):langCharset;

            warnings = new ArrayList<BusinessWarning>();

            dict = deliverDCT(dictName, dictPath, appId, encoding, langCodes,
                    langCharset, warnings);
            if (!warnings.isEmpty()) {
                join(warnings, '\n').replace("\"", "\"\"");
                String forCSV = warnings.toString().replace("\"", "\"\"");
                forCSV = join(warnings, '\n').replace("\"", "\"\"");
                logDictDeliverWarning.warn(String.format("%s,%s,%s,\"%s\"",
                        file.getName(), encoding, file.getAbsolutePath(),
                        forCSV));
            }
        } catch (BusinessException e) {
            String forCSV = e.toString().replace("\"", "\"\"");
            logDictDeliverFail.error(String.format("%s,%s,%s,\"%s\"",
                    file.getName(), encoding, file.getAbsolutePath(), forCSV));
            log.error(e);
        }
        if (null != dict) {
            dict.setDictLanguages(null);
            dict.setLabels(null);
            deliveredDicts.add(dict);
        }
        return deliveredDicts;
    }


    /**
     * Deliver dct files in a directory After using dictionary properties, now
     * encoding and langCharset parameter are useless.
     *
     * @param rootDir
     * @param file
     */
    public Collection<Dictionary> deliverMDCFiles(String rootDir, File file, Long appId,  String[] langCodes,
                                                  Map<String, String> langCharset,
                                                  Collection<BusinessWarning> warnings) throws BusinessException {

        if (!file.exists())
            return null;

        Collection<Dictionary> deliveredDicts = new ArrayList<Dictionary>();

        if (file.isDirectory()) {
            File[] dctFileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isMDCFile(pathname);
                }
            });
            for (File dctFile : dctFileOrDirs) {
                Collection<Dictionary> subDeliveredDicts = deliverMDCFiles(
                        rootDir, dctFile, appId, langCodes,
                        langCharset, warnings);
                deliveredDicts.addAll(subDeliveredDicts);
            }
            return deliveredDicts;
        }

        // normal mdc file
        Dictionary dict = null;
        String encoding="UTF-8";
        try {
            rootDir = rootDir.replace("\\", "/");
            String dictPath = file.getAbsolutePath().replace("\\", "/");
            String dictName = dictPath.replace(rootDir, "");

            langCharset = null==langCharset? dictProp.getDictionaryCharsets(dictName):langCharset;

            warnings = new ArrayList<BusinessWarning>();

            dict = deliverMDC(dictName, dictPath, appId, langCodes, langCharset, warnings);
            if (!warnings.isEmpty()) {
                join(warnings, '\n').replace("\"", "\"\"");
                String forCSV = warnings.toString().replace("\"", "\"\"");
                forCSV = join(warnings, '\n').replace("\"", "\"\"");
                logDictDeliverWarning.warn(String.format("%s,%s,%s,\"%s\"",
                        file.getName(), encoding, file.getAbsolutePath(),
                        forCSV));
            }
        } catch (BusinessException e) {
            String forCSV = e.toString().replace("\"", "\"\"");
            logDictDeliverFail.error(String.format("%s,%s,%s,\"%s\"", file.getName(), encoding, file.getAbsolutePath(), forCSV));
            log.error(e);
        }
        if (null != dict) {
            dict.setDictLanguages(null);
            dict.setLabels(null);
            deliveredDicts.add(dict);
        }
        return deliveredDicts;
    }

    private Dictionary deliverMDC(String dictName, String dictPath, Long appId, String[] langCodes, Map<String, String> langCharset, Collection<BusinessWarning> warnings) {
        Dictionary dict= null;
        try {
            FileInputStream is=new FileInputStream(dictPath);
            dict = deliverMDC(dictName,dictPath,is,appId,langCodes,langCharset, warnings);
            is.close();
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }
        return dict;
    }


    /**
     * Deliver a Zip file into database.
     */
    private Collection<Dictionary> deliverZipDCTFile(String rootDir,
                                                     ZipFile file, Long appId, String encoding, String[] langCodes,
                                                     Map<String, String> langCharset,
                                                     Collection<BusinessWarning> warnings) throws BusinessException {

        Collection<Dictionary> deliveredDicts = new ArrayList<Dictionary>();

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
        ZipEntry entry = null;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!Util.isDCTFile(entry.getName())) {
                continue;
            }
            try {
                InputStream is = file.getInputStream(entry);

                if (null == encoding) {
                    byte[] bom = new byte[Util.UTF8_BOM_LENGTH];
                    is.read(bom);
                    encoding = Util.detectEncoding(bom);
                }
                String dictionaryName = entry.getName();
                String path = file.getName() + dictionaryName;
                Dictionary dict = null;
                try {
                    dict = deliverDCT(dictionaryName, path, is, appId,
                            encoding, langCodes, langCharset, warnings);
                } catch (BusinessException e) {
                    log.error(e);
                }
                if (null != dict) {
                    deliveredDicts.add(dict);
                }
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        return deliveredDicts;
    }

    /**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir   root directory save dct files
     * @param dtIds the collection of the id for dictionary to be generated.
     */
    public void generateDCTFiles(String dir, Collection<Long> dtIds, String[] langCodes) {
        if (dtIds.isEmpty()) return;
        String idList = dtIds.toString().replace("[", "(").replace("]", ")");
        String hsql = "from Dictionary where id in " + idList;
        Collection<Dictionary> dicts = (Collection<Dictionary>) getDao().retrieve(hsql);

        for (Dictionary dict : dicts) {
            if(dict.getFormat().equals("xml")){
                generateMDC(new File(dir,dict.getName()),dict.getId(),null);
            }else{
                generateDCT(new File(dir, dict.getName()), dict, dict.getEncoding(), null);
            }
        }
    }

    /**
     * Generate a dct file for a specific dictionary.
     *
     * @param filename  the file name for dct file
     * @param dctId     dictionary id for the dictionary to be generated.
     * @param encoding  the encoding for the file to write.
     * @param langCodes the language codes for which languages to be generated.
     */

    public void generateDCT(String filename, Long dctId, String encoding,
                            String[] langCodes) {
        generateDCT(new File(filename), dctId, encoding, langCodes);
    }

    public void generateDCT(File file, Long dctId, String encoding,
                            String[] langCodes) {
        Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
                dctId);
        if (null == dict) {
            log.warn("ID for " + dctId
                    + " Dictionary is not found in database.");
            throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND,
                    dctId);
        }
        generateDCT(file, dict, encoding, langCodes);
    }

    private void generateDCT(File file, Dictionary dict, String encoding,
                             String[] langCodes)
            throws BusinessException {
        // all the language code in dictionary
        HashSet<String> dictLangCodes = dict.getAllLanguageCodes();

        if (langCodes != null) {
            List<String> listLangCodes = new ArrayList(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            if (!listLangCodes.isEmpty()) {
                throw new BusinessException(
                        BusinessException.UNKNOWN_LANG_CODE,
                        listLangCodes.get(0));
            }
            // used for iteration.
            dictLangCodes = new HashSet<String>(Arrays.asList(langCodes));
        }

        if (null == encoding) {
            encoding = dict.getEncoding();
        }

        PrintStream out = null;
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            log.info("**************************generate dictionary: " + file.getPath() + "***************************");
            out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(file)), true, encoding);
            // output support languages

            out.println("-- Generated by DMS at "+dFmt.format(new Date()));
            dictLangCodes.add("CHK");
            out.println("LANGUAGES {" + join(dictLangCodes, ", ") + "}");
            out.println();

            // output labels

            Label label = null;
            String checkFieldLangCodeString = String.format("  %s ",
                    Label.CHECK_FIELD_NAME);
            String referenceFieldLangCodeString = String.format("  %s ",
                    Label.REFERENCE_FIELD_NAME);
            int indentSize = checkFieldLangCodeString.length();

            Label[] labels = dict.getLabels().toArray(new Label[0]);
            for (int i = 0; i < labels.length; ++i) {
                label = labels[i];

                if (i > 0) {
                    // output label separator
                    out.println(";");
                    out.println();
                }
                out.println(label.getKey() + ":");
                String chk = generateCHK(label.getMaxLength(),
                        label.getReference());
                if(null!=chk){
                    out.print(checkFieldLangCodeString + convertContent( indentSize, chk , "\n", System.getProperty("line.separator")));
                    // output translation separator
                    out.println(",");
                }

                out.print(referenceFieldLangCodeString
                        + convertContent(indentSize, label.getReference(),
                        "\n", System.getProperty("line.separator")));
                // already done.
                dictLangCodes.remove("GAE");
                dictLangCodes.remove("CHK");
                String dictLang = null;

                for (Object objDictLang : dictLangCodes) {
                    dictLang = (String) objDictLang;
                    // output translation separator
                    out.println(",");

                    out.print("  " + dictLang + " ");

                    // output dictLangCode translation
                    String translationString = label.getReference();
                    DictionaryLanguage dl=dict.getDictLanguage(dictLang);

                    Language dictLangCodeLanguage = dl.getLanguage();
                    for (Translation translation : label.getText()
                            .getTranslations()) {
                        if (translation.getLanguage().getId()
                                .equals(dictLangCodeLanguage.getId())) {
                            translationString = translation.getTranslation();
                            break;
                        }
                    }

                    String converedString = convertContent(indentSize,
                            translationString, "\n",
                            System.getProperty("line.separator"));

                    String charsetName = dict.getDictLanguage(dictLang).getCharset().getName();
                    out.write(converedString.getBytes(charsetName));
                }

            }

        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    @Override
    public void generateMDC(String file, Long dctId, String[] langCodes) throws BusinessException{
        generateMDC(new File(file), dctId, langCodes);
    }


    public void generateMDC(File file, Long dctId, String[] langCodes) throws BusinessException{


        Dictionary dict = (Dictionary) getDao().retrieve(Dictionary.class,
                dctId);
        if (null == dict) {
            log.warn("ID for " + dctId
                    + " Dictionary is not found in database.");
            throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND,
                    dctId);
        }

        // all the language code in dictionary
        HashSet<String> dictLangCodes = dict.getAllLanguageCodes();

        if (langCodes != null) {
            List<String> listLangCodes = new ArrayList(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            if (!listLangCodes.isEmpty()) {
                throw new BusinessException(
                        BusinessException.UNKNOWN_LANG_CODE,
                        listLangCodes.get(0));
            }
            // used for iteration.
            dictLangCodes = new HashSet<String>(Arrays.asList(langCodes));
        }
        log.info("**************************generate dictionary: " + file.getPath() + "***************************");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new SystemError(e.getMessage());
            }
        }

        Collection<Label> labels=dict.getLabels();
        Collection<Translation> translations=null;

        //generate xml
        Document document = DocumentHelper.createDocument();
        document.addComment("Created by DMS at "+dFmt.format(new Date()));

        Element dictionaryElement = document.addElement("dictionary");
        Element messageStringElement = dictionaryElement.addElement("messageString");

        for(Label label:labels){
            Element labelElement=messageStringElement.addElement(label.getKey());
            translations=label.getText().getTranslations();

            for(String dictLangCode :dictLangCodes){
                Element langElement=labelElement.addElement("lang");
                String translatedString=label.getReference();

                DictionaryLanguage dl=dict.getDictLanguage(dictLangCode);

                Language dictLangCodeLanguage = dl.getLanguage();

                for(Translation translation:translations){
                    if(translation.getLanguage().getId().equals(dictLangCodeLanguage.getId())){
                        translatedString=translation.getTranslation();
                        break;
                    }
                }

                langElement.addAttribute("id", dictLangCode);
                langElement.setText(translatedString);
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
//      OutputFormat format = OutputFormat.createCompactFormat();

        format.setEncoding("UTF-8");

        try {
            XMLWriter output = new XMLWriter(new FileWriter(file), format);
            output.write(document);
            output.close();
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }

    }

    private String generateCHK(String maxLength, String reference) {
        if (null == maxLength) return null;
        log.debug("maxLength=" + maxLength + ", reference=" + reference);
        StringBuilder sb = new StringBuilder();
        //Trailing empty strings should not be discarded
        String[] sLineLens = maxLength.split(",", -1);
        String[] refers = reference.split("\n", -1);
        int maxLen = -1;

        int min = Math.min(sLineLens.length, refers.length);
        for (int i = 0; i < min; ++i) {
            maxLen = Integer.parseInt(sLineLens[i].trim());
            sb.append(refers[i].trim());
            int fill = maxLen - refers[i].length();
            int base = 0;
            while (fill-- > 0) {
                sb.append((char)(base++ % 10 + '0'));
            }
            sb.append("\n");
        }

        for (int i = min; i < sLineLens.length; ++i) {
            maxLen = Integer.parseInt(sLineLens[i].trim());
            int fill = maxLen;
            int base = 0;
            while (fill-- > 0) {
                sb.append((char) (base++ % 10 + '0'));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String convertContent(int indentSize, String content,
                                  String contentLineSeparator, String joinedStringLineSeparator) {
        String[] contents = content.split(contentLineSeparator);
        for (int i = 0; i < contents.length; ++i) {
            contents[i] = "\"" + contents[i] + "\"";
        }
        return join(contents, joinedStringLineSeparator
                + generateSpace(indentSize));
    }



    public Dictionary previewDCT(String dictionaryName, String filename,
                                 Long appId, String encoding, Collection<BusinessWarning> warnings)
            throws BusinessException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new BusinessException(BusinessException.DCT_FILE_NOT_FOUND,
                    file.getName());
        }
        InputStream is;
        try {

            if (!file.exists()) {
                throw new BusinessException(
                        BusinessException.DCT_FILE_NOT_FOUND, file.getName());
            }
            is = new FileInputStream(file);
            return previewDCT(dictionaryName, filename, is, appId, encoding,
                    warnings);
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        }

    }

    public Dictionary previewDCT(String dictionaryName, String path,
                                 InputStream dctInputStream, Long appId, String encoding,
                                 Collection<BusinessWarning> warnings) throws BusinessException,
            IOException {
        Application app = (Application) getDao().retrieve(Application.class,
                appId);

        if (null == app) {
            throw new BusinessException(
                    BusinessException.APPLICATION_NOT_FOUND, appId);
        }

        Dictionary dict = dictionaryParser.parse(app, dictionaryName, path,
                dctInputStream, encoding, warnings);

        return dict;
    }

    public Dictionary importDCT(Dictionary dict, String[] langCodes,
                                Map<String, String> langCharset,
                                Collection<BusinessWarning> warnings) {
        log.info("Start importing DCT");
        if (null == dict)
            return null;

        BusinessException nonBreakExceptions = new BusinessException(
                BusinessException.NESTED_DCT_PARSE_ERROR, dict.getName());

        // check langCodes parameter
        if (langCodes != null) {
            Collection<String> dictLangCodes = dict.getAllLanguageCodes();
            List<String> listLangCodes = new ArrayList<String>(Arrays.asList(langCodes));
            listLangCodes.removeAll(dictLangCodes);
            // TODO: restore here after parse 1 work done.
            // if (!listLangCodes.isEmpty()) {
            //
            // throw new BusinessException(
            // BusinessException.UNKNOWN_LANG_CODE,
            // listLangCodes.get(0));
            // }
        }
        
        // unified langCodeList
        Collection<String> langCodeList = null;
        if (langCodes != null) {
        	langCodeList = new ArrayList<String>();
	        for (String langCode : langCodes) {
	        	langCodeList.add(getUnifiedLangCode(langCode));
	        }
        }
        
        // unified langCharset
        Map<String, String> uniLangCharset = new HashMap<String, String>();
        for (String langCode : langCharset.keySet()) {
        	uniLangCharset.put(getUnifiedLangCode(langCode), langCharset.get(langCode));
        }
        langCharset = uniLangCharset;

        Dictionary dbDict = (Dictionary) getDao().retrieveOne(
                "from Dictionary where name=:name",
                JSONObject.fromObject(String.format("{'name':'%s'}",
                        dict.getName())));
        // create dictionary if not exists
        if (null == dbDict) {
            // create dictionary
            log.info("Dictionary " + dict.getName()
                    + " not exist in database, create new one in database...");
            dbDict = new Dictionary();
            dbDict.setApplication((Application) dao.retrieve(Application.class,
                    dict.getApplication().getId()));
            dbDict.setEncoding(dict.getEncoding());
            dbDict.setFormat(dict.getFormat());
            dbDict.setName(dict.getName());
            dbDict.setPath(dict.getPath());
            dbDict.setLocked(false);
            dbDict = (Dictionary) getDao().create(dbDict);
        }

        // update dictionary languages
        for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
        	String uniLangCode = getUnifiedLangCode(dictLanguage.getLanguageCode());
            if (langCodeList != null && !langCodeList.contains(uniLangCode)) {
                continue;
            }
            String charsetName = langCharset.get(uniLangCode);
            if (null == charsetName) {
                nonBreakExceptions.addNestedException(new BusinessException(
                        BusinessException.CHARSET_NOT_DEFINED, dictLanguage.getLanguageCode()));
            }else{
                mergeDictLanguage(dbDict, dictLanguage.getLanguage().getId(),dictLanguage.getLanguageCode(), charsetName);
            }
        }

        // prepare textMap, labelMap by context
        log.info("Prepare data to import");
        Map<String, Collection<Text>> textMap = new HashMap<String, Collection<Text>>();
        Map<String, Collection<Label>> labelMap = new HashMap<String, Collection<Label>>();
        Map<Long, String> langCodeMap = dict.getLangCodeMap();
        for (Label label : dict.getLabels()) {
            String contextName = label.getContext().getName();
            Text text = label.getText();

            Collection<Text> texts = textMap.get(contextName);
            if (texts == null) {
                texts = new ArrayList<Text>();
                textMap.put(contextName, texts);
            }
            texts.add(text);

            Collection<Label> labels = labelMap.get(contextName);
            if (labels == null) {
                labels = new ArrayList<Label>();
                labelMap.put(contextName, labels);
            }
            labels.add(label);

            // filter by langCodes parameter
            if (langCodeList != null) {
                for (Iterator<Translation> iterator = text.getTranslations()
                        .iterator(); iterator.hasNext(); ) {
                    Translation trans = iterator.next();
                    String langCode = langCodeMap.get(trans.getLanguage()
                            .getId());
                    String uniLangCode = getUnifiedLangCode(langCode);
                    if (!langCodeList.contains(uniLangCode)) {
                        iterator.remove();
                    }
                }
            }

            // convert charset of translation strings
            for (Translation trans : text.getTranslations()) {
                String langCode = langCodeMap.get(trans.getLanguage().getId());
                String charsetName = langCharset.get(getUnifiedLangCode(langCode));
                if (null == charsetName) {
                    nonBreakExceptions.addNestedException(new BusinessException(
                            BusinessException.CHARSET_NOT_DEFINED, langCode));
                    continue;
                }
                try {
                	boolean invalidText = false;
                	if (!dict.getEncoding().equals(charsetName)) {
                		byte[] source = trans.getTranslation().getBytes(dict.getEncoding());
                		String encodedTranslation = new String(source, charsetName);
                		byte[] target = encodedTranslation.getBytes(charsetName);
	                    if (!Arrays.equals(source, target)) {
	                        invalidText = true;
	                        trans.setTranslation(text.getReference());
	                        log.warn("Invalid encoding at label " + label.getKey() + " of dict " + dict.getPath());
	                    } else {
		                    trans.setTranslation(encodedTranslation);
	                    }
                	}

                    // check charset
                    if (invalidText || !trans.isValidText()) {
                        warnings.add(new BusinessWarning(
                                BusinessWarning.INVALID_TEXT,
                                trans.getTranslation(), charsetName, langCode,
                                label.getKey()));
                    }

                    // check length
                    if (!label.checkLength(trans.getTranslation())) {
                        warnings.add(new BusinessWarning(
                                BusinessWarning.EXCEED_MAX_LENGTH, langCode,
                                label.getKey()));
                    }
                } catch (UnsupportedEncodingException e) {
                    nonBreakExceptions.addNestedException(new BusinessException(
                            BusinessException.CHARSET_NOT_FOUND, charsetName));
                }
            }
        }

        // for each context, insert or update label/text/translation data
        for (String contextName : textMap.keySet()) {
            log.info("Importing data into context " + contextName);
            Context context = textService.getContextByName(contextName);
            if (context == null) {
                context = new Context();
                context.setName(contextName);
                context = (Context) dao.create(context);
            }
            Collection<Text> texts = textMap.get(contextName);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    context.getId(), texts);
            Collection<Label> labels = labelMap.get(contextName);
            int sortNo = 1;
            for (Label label : labels) {
                // create or update label
                Label dbLabel = dbDict.getLabel(label.getKey());
                if (dbLabel == null) {
                    label.setDictionary(dbDict);
                    label.setContext(context);
                    label.setText(dbTextMap.get(label.getReference()));
                    label.setSortNo(sortNo++);
                    dbLabel = (Label) dao.create(label, false);
                } else {
                    dbLabel.setContext(context);
                    dbLabel.setText(dbTextMap.get(label.getReference()));
                    dbLabel.setKey(label.getKey());
                    dbLabel.setDescription(label.getDescription());
                    dbLabel.setMaxLength(label.getMaxLength());
                    dbLabel.setReference(label.getReference());
                    dbLabel.setSortNo(sortNo++);
                }
            }
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }
        log.info("Import DCT finish");
        return dbDict;
    }

    private String getUnifiedLangCode(String langCode) {
		return langCode.toUpperCase().replace("_", "-");
	}

	private DictionaryLanguage mergeDictLanguage(Dictionary dbDict,
                                                 Long languageId, String languageCode, String charsetName) {
        DictionaryLanguage dbDictLang = dbDict.getDictLanguage(languageId);
        if (dbDictLang == null) {
            dbDictLang = new DictionaryLanguage();
            dbDictLang.setDictionary(dbDict);
            dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
                    languageId));
            dbDictLang.setCharset(langService.getCharset(charsetName));
            dbDictLang.setLanguageCode(languageCode);
            dbDictLang = (DictionaryLanguage) dao.create(dbDictLang);
        } else {
            dbDictLang.setLanguage((Language) dao.retrieve(Language.class,
                    languageId));
            dbDictLang.setCharset(langService.getCharset(charsetName));
            dbDictLang.setLanguageCode(languageCode);
        }
        return dbDictLang;
    }

    public Dictionary previewMDC(String dictionaryName, String path, InputStream is, Long appId,
                                 Collection<BusinessWarning> warnings) throws BusinessException {

        Application app = (Application) getDao().retrieve(Application.class,
                appId);

        if (null == app) {
            throw new BusinessException(
                    BusinessException.APPLICATION_NOT_FOUND, appId);
        }
        Dictionary dict=null;
        try {
            dict=mdcParser.parse(app,dictionaryName,path,is,warnings);
        }catch(BusinessException e){
            throw e;
        }catch (Exception e) {
            throw new BusinessException(BusinessException.INVALID_MDC_FILE,path);
        }
        return dict;
    }
}
