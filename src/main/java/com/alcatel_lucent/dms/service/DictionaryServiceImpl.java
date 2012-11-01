package com.alcatel_lucent.dms.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.generator.DCTGenerator;
import com.alcatel_lucent.dms.service.generator.DictionaryGenerator;
import com.alcatel_lucent.dms.service.generator.LabelXMLGenerator;
import com.alcatel_lucent.dms.service.generator.MDCGenerator;
import com.alcatel_lucent.dms.service.generator.PropGenerator;
import com.alcatel_lucent.dms.service.generator.PropXMLGenerator;

@Service("dictionaryService")
@Scope("singleton")
@SuppressWarnings("unchecked")
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
    private LanguageService langService;

    @Autowired
    private com.alcatel_lucent.dms.service.parser.DictionaryParser[] parsers;
    
    @Autowired
    private DCTGenerator dctGenerator;
    
    @Autowired
    private MDCGenerator mdcGenerator;
    
    @Autowired
    private LabelXMLGenerator labelXMLGenerator;
    
    @Autowired
    private PropXMLGenerator propXMLGenerator;
    
    @Autowired
    private PropGenerator propGenerator;
    
    public DictionaryServiceImpl() {
        super();
    }

    public Collection<Dictionary> previewDictionaries(String rootDir, File file) throws BusinessException {
    	Collection<Dictionary> result = new ArrayList<Dictionary>();
    	BusinessException exceptions = new BusinessException(BusinessException.PREVIEW_DICT_ERRORS);
    	rootDir = rootDir.replace("\\", "/");
        long before = System.currentTimeMillis();
        HashSet<String> allAcceptedFiles = new HashSet<String>();
    	for (com.alcatel_lucent.dms.service.parser.DictionaryParser parser : parsers) {
    		Collection<File> acceptedFiles = new ArrayList<File>();
    		try {
    			result.addAll(parser.parse(rootDir, file, acceptedFiles));
    		} catch (BusinessException e) {
    			if (e.getErrorCode() == BusinessException.NESTED_ERROR) {
    				for (BusinessException subE : e.getNested()) {
    					exceptions.addNestedException(subE);
    				}
    			} else {
    				exceptions.addNestedException(e);
    			}
    		}
    		for (File acceptedFile : acceptedFiles) {
    			String filename = acceptedFile.getAbsolutePath().replace("\\", "/");
    			allAcceptedFiles.add(filename);
    		}
    	}
        long after = System.currentTimeMillis();
        log.info("**************preview directory '" + file.getAbsolutePath() + "' take " + (after - before)
                + " milliseconds of time.************");
    	// check files not accepted
    	Collection<String> notAcceptedFiles = getNotAcceptedFiles(file, allAcceptedFiles);
    	if (!notAcceptedFiles.isEmpty()) {
    		for (String notAcceptedFile : notAcceptedFiles) {
    			if (notAcceptedFile.startsWith(rootDir)) {
    				notAcceptedFile = notAcceptedFile.substring(rootDir.length());
    			}
    			exceptions.addNestedException(new BusinessException(BusinessException.UNRECOGNIZED_DICT_FILE, notAcceptedFile));
    		}
    	}
    	if (exceptions.hasNestedException()) {
    		throw exceptions;
    	}
    	return result;
    }
    
    private Collection<String> getNotAcceptedFiles(File file,
			HashSet<String> allAcceptedFiles) {
    	Collection<String> result = new ArrayList<String>();
    	if (!file.exists()) return result;
    	if (file.isDirectory()) {
    		File[] subFiles = file.listFiles();
    		for (File subFile : subFiles) {
    			result.addAll(getNotAcceptedFiles(subFile, allAcceptedFiles));
    		}
    	} else {
    		String filename = file.getAbsolutePath().replace("\\", "/");
    		if (!allAcceptedFiles.contains(filename)) {
    			result.add(filename);
    		}
    	}
    	return result;
	}

	/**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir   root directory save dct files
     * @param dtIds the collection of the id for dictionary to be generated.
     */
    public void generateDictFiles(String dir, Collection<Long> dtIds) {
        if (dtIds.isEmpty()) return;
        File target = new File(dir);
    	if (target.exists()) {
    		if (target.isFile()) {
    			throw new BusinessException(BusinessException.TARGET_IS_NOT_DIRECTORY, dir);
    		}
    	} else {
    		if (!target.mkdirs()) {
    			throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, dir);
    		}
    	}
        String idList = dtIds.toString().replace("[", "(").replace("]", ")");
        String hsql = "from Dictionary where id in " + idList;
        Collection<Dictionary> dicts = (Collection<Dictionary>) getDao().retrieve(hsql);

        for (Dictionary dict : dicts) {
            log.info("Generate dictionary: " + dict.getName());
        	DictionaryGenerator generator = getGenerator(dict.getFormat());
        	
        	generator.generateDict(target, dict.getId());
        }
    }
    
    private DictionaryGenerator getGenerator(String format) {
    	if (format.equals(Constants.DICT_FORMAT_MDC)) {
	        return mdcGenerator;
	    } else if (format.equals(Constants.DICT_FORMAT_DCT)){
	        return dctGenerator;
	    } else if (format.equals(Constants.DICT_FORMAT_XML_LABEL)) {
	    	return labelXMLGenerator;
	    } else if (format.equals(Constants.DICT_FORMAT_XML_PROP)) {
	    	return propXMLGenerator;
	    } else if (format.equals(Constants.DICT_FORMAT_TEXT_PROP)) {
	    	return propGenerator;
	    } else {
	    	throw new SystemError("Unsupported dict format: " + format);
	    }
	}

	@Override
    public Dictionary importDictionary(Long appId, Dictionary dict, String version, int mode, String[] langCodes,
                                Map<String, String> langCharset,
                                Collection<BusinessWarning> warnings) {
        log.info("Start importing dictionary in " + (mode == Constants.DELIVERY_MODE ? "DELIVERY" : "TRANSLATION") + " mode");
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
        Dictionary lastDict = null;



        // Associate base Dictionary
        Application app = (Application) getDao().retrieve(Application.class, appId);
        Map param = new HashMap();
        param.put("name", dict.getName());
        param.put("appBaseId", app.getBase().getId());
        DictionaryBase baseDBDict = (DictionaryBase) getDao().retrieveOne(
        		"from DictionaryBase where name=:name and applicationBase.id=:appBaseId", param);
        if(null==baseDBDict){
        	if (mode == Constants.TRANSLATION_MODE) {
        		throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dict.getName());
        	}
            baseDBDict = new DictionaryBase();
            baseDBDict.setName(dict.getName());
            baseDBDict.setFormat(dict.getFormat());
            baseDBDict.setEncoding(dict.getEncoding());
            baseDBDict.setPath(dict.getPath());
            baseDBDict.setApplicationBase(app.getBase());
            baseDBDict=(DictionaryBase)getDao().create(baseDBDict);
        } else{
            if (mode == Constants.DELIVERY_MODE) {
                baseDBDict.setName(dict.getName());
                baseDBDict.setFormat(dict.getFormat());
                baseDBDict.setEncoding(dict.getEncoding());
                baseDBDict.setPath(dict.getPath());
            }
        }


        param = new HashMap();
        param.put("version", version);
        param.put("baseId", baseDBDict.getId());
        Dictionary dbDict = (Dictionary) getDao().retrieveOne(
                "from Dictionary where version=:version and base.id=:baseId", param);
        // create dictionary if not exists
        if (null == dbDict) {
        	if (mode == Constants.TRANSLATION_MODE) {
        		throw new BusinessException(BusinessException.DICTIONARY_NOT_FOUND, dict.getName() + " " + version);
        	}
            // create dictionary
            log.info("Dictionary " + dict.getName()
                    + " not exist in database, create new one in database...");
            dbDict = new Dictionary();
            dbDict.setBase(baseDBDict);
            dbDict.setVersion(version);
            dbDict.setAnnotation1(dict.getAnnotation1());
            dbDict.setAnnotation2(dict.getAnnotation2());
            dbDict.setAnnotation3(dict.getAnnotation3());
            dbDict.setAnnotation4(dict.getAnnotation4());


            Collection<Dictionary> dictionaries= app.getDictionaries();
            if(null==dictionaries){
                dictionaries=new HashSet<Dictionary>();
                app.setDictionaries(dictionaries);
            }
            Iterator<Dictionary> iter = dictionaries.iterator();
            // remove previous dictionary version in the app
            while (iter.hasNext()) {
            	Dictionary appDict = iter.next();
            	if (appDict.getBase().getId().equals(baseDBDict.getId())) {
            		iter.remove();
            	}
            }
            dictionaries.add(dbDict);

            dbDict.setLocked(false);
            dbDict = (Dictionary) getDao().create(dbDict);
            if (mode == Constants.DELIVERY_MODE) {	// in case new dictionary version, compare with latest version
            	lastDict = getLatestDictionary(baseDBDict.getId(), dbDict.getId());
            }
        } else {
            if (mode == Constants.DELIVERY_MODE) {	// in case existing dictionary version, compare with the same version
            	lastDict = dbDict;
            	dbDict.setAnnotation1(dict.getAnnotation1());
            	dbDict.setAnnotation2(dict.getAnnotation2());
            	dbDict.setAnnotation3(dict.getAnnotation3());
            	dbDict.setAnnotation4(dict.getAnnotation4());
            }
        }
        

        // update dictionary languages in delivery mode
        if (mode == Constants.DELIVERY_MODE) {
        	if (dbDict.getDictLanguages() != null) {	//remove all existing dict languages
        		for (DictionaryLanguage dl : dbDict.getDictLanguages()) {
        			dao.delete(dl);
        		}
        	}
	        for (DictionaryLanguage dictLanguage : dict.getDictLanguages()) {
	            String uniLangCode = getUnifiedLangCode(dictLanguage.getLanguageCode());
	            if (langCodeList != null && !langCodeList.contains(uniLangCode)) {
	                continue;
	            }
	            String charsetName = null;
	            if (dictLanguage.getCharset() != null) {
	            	charsetName = dictLanguage.getCharset().getName();
	            }
	            if (charsetName == null) {
		            charsetName = langCharset.get(uniLangCode);
		            if (charsetName == null) {
		            	charsetName = langCharset.get("DEFAULT");
		            }
	            }
	            if (null == charsetName) {
	                nonBreakExceptions.addNestedException(new BusinessException(
	                        BusinessException.CHARSET_NOT_DEFINED, dictLanguage.getLanguageCode()));
	            } else {
	            	dictLanguage.setCharset(langService.getCharset(charsetName));
	            }
		            
            	dictLanguage.setDictionary(dbDict);
            	dictLanguage.setLanguage((Language) dao.retrieve(Language.class,
                        dictLanguage.getLanguage().getId()));
                dao.create(dictLanguage);
	        }
        }

        // prepare data for creation: textMap, labelMap indexed by context
        log.info("Prepare data to import");
        Map<String, Collection<Text>> textMap = new HashMap<String, Collection<Text>>();
        Map<String, Collection<Label>> labelMap = new HashMap<String, Collection<Label>>();
        Map<Long, String> langCodeMap = dict.getLangCodeMap();
//        Map<Long, String> langCodeMap = new HashMap<Long, String>();
//        for (DictionaryLanguage dl : dict.getDictLanguages()) {
//            langCodeMap.put(dl.getLanguage().getId(), dl.getLanguageCode()) ;
//        }
        for (Label label : dict.getLabels()) {
            String contextName = label.getContext().getName();
            Text text = new Text();
            text.setReference(label.getReference());

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
                for (Iterator<LabelTranslation> iterator = label.getOrigTranslations()
                        .iterator(); iterator.hasNext(); ) {
                    LabelTranslation trans = iterator.next();
                    String langCode = langCodeMap.get(trans.getLanguage()
                            .getId());
                    String uniLangCode = getUnifiedLangCode(langCode);
                    if (!langCodeList.contains(uniLangCode)) {
                        iterator.remove();
                    }
                }
            }
            
            Label lastLabel = null;
            if (lastDict != null) {
            	lastLabel = lastDict.getLabel(label.getKey());
            }
            String labelWarnings = null;
            // for each translation
            // convert charset of translation strings
            // determine translation behaviors
            if (label.getOrigTranslations() != null) {
	            for (LabelTranslation trans : label.getOrigTranslations()) {
	            	// determine charset, first take value from DictionaryLanguage
	            	// if not specified in DictionaryLanguage, then take value from langCharset parameter
	                String langCode = langCodeMap.get(trans.getLanguage().getId());
	                DictionaryLanguage dl = dict.getDictLanguage(langCode);
	                String charsetName = dl.getCharset().getName();
	                try {
	                    boolean invalidText = false;
	                    if (!dict.getEncoding().equals(charsetName)) {
	                        byte[] source = trans.getOrigTranslation().getBytes(dict.getEncoding());
	                        String encodedTranslation = new String(source, charsetName);
	                        byte[] target = encodedTranslation.getBytes(charsetName);
	                        if (!Arrays.equals(source, target)) {
	                            invalidText = true;
	                            trans.setOrigTranslation(text.getReference());
	                            log.warn("Invalid encoding at label " + label.getKey() + " of dict " + dict.getPath());
	                        } else {
	                            trans.setOrigTranslation(encodedTranslation);
	                        }
	                    }
	
	                    labelWarnings = "";
	                    // check charset
	                    if (invalidText || !trans.isValidText()) {
	                        warnings.add(new BusinessWarning(
	                                BusinessWarning.INVALID_TEXT,
	                                trans.getOrigTranslation(), charsetName, langCode,
	                                label.getKey()));
	                        labelWarnings += BusinessWarning.INVALID_TEXT;
	                    }
	
	                    // check length
	                    if (!label.checkLength(trans.getOrigTranslation())) {
	                        warnings.add(new BusinessWarning(
	                                BusinessWarning.EXCEED_MAX_LENGTH, langCode,
	                                label.getKey()));
	                        if (!labelWarnings.isEmpty()) {
	                        	labelWarnings += ";";
	                        }
	                        labelWarnings += BusinessWarning.EXCEED_MAX_LENGTH;
	                    }
	                    trans.setWarnings(labelWarnings);
	                } catch (UnsupportedEncodingException e) {
	                    nonBreakExceptions.addNestedException(new BusinessException(
	                            BusinessException.CHARSET_NOT_FOUND, charsetName));
	                }
	                
	                // determine if the translation should take value from context dictionary
	                trans.setNeedTranslation(true);
	                if (lastLabel != null) {
	                	// get the original translation in latest version
	                	LabelTranslation lastTranslation = lastLabel.getOrigTranslation(trans.getLanguageCode());
	                	if (lastTranslation != null && 
	                			!lastTranslation.getOrigTranslation().equals(trans.getOrigTranslation()) &&
	                			!trans.getOrigTranslation().equals(label.getReference())) {
	                		// translation changed means the label was translated on developer side
	                		trans.setNeedTranslation(false);
	                	}
	                }
	                
	                Translation t = new Translation();
	                t.setText(text);
	                t.setTranslation(trans.getOrigTranslation());
	                t.setLanguage(trans.getLanguage());
	                
	                // determine translation status
	                if (trans.getRequestTranslation() != null && trans.getRequestTranslation().booleanValue()) {
	                	t.setStatus(Translation.STATUS_UNTRANSLATED);
	                } else if (!trans.isNeedTranslation()) {
	                	t.setStatus(Translation.STATUS_TRANSLATED);
	                } else if (label.getReference().equals(trans.getOrigTranslation())) {
	                	t.setStatus(Translation.STATUS_UNTRANSLATED);
	                } else {
	                	t.setStatus(Translation.STATUS_TRANSLATED);
	                }
	                text.addTranslation(t);
	            } //for
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
            Collection<Label> labels = labelMap.get(contextName);
            Map<String, Text> dbTextMap = textService.updateTranslations(
                    context.getId(), texts, mode);
            
            // in TRANSLATION_MODE, no change to label
            if (mode == Constants.TRANSLATION_MODE) {
            	continue;
            }
            
            // NOTE: following code is only executed in DELIVERY_MODE
            int sortNo = 1;
            for (Label label : labels) {
                // create or update label
                Label dbLabel = dbDict.getLabel(label.getKey());
                boolean newLabel = true;
                if (dbLabel == null) {
                    label.setDictionary(dbDict);
                    label.setContext(context);
                    label.setText(dbTextMap.get(label.getReference()));
                    label.setSortNo(sortNo++);
                    dbLabel = (Label) dao.create(label, false);
                } else {
                    newLabel = false;
                    dbLabel.setContext(context);
                    dbLabel.setText(dbTextMap.get(label.getReference()));
                    dbLabel.setKey(label.getKey());
                    dbLabel.setDescription(label.getDescription());
                    dbLabel.setMaxLength(label.getMaxLength());
                    dbLabel.setReference(label.getReference());
                    dbLabel.setAnnotation1(label.getAnnotation1());
                    dbLabel.setAnnotation2(label.getAnnotation2());
                    dbLabel.setSortNo(sortNo++);
                }
                
                // create or update LabelTranslation
                if (label.getOrigTranslations() != null) {
	                for (LabelTranslation trans : label.getOrigTranslations()) {
	                	LabelTranslation dbLabelTrans = null;
	                	if (!newLabel) {
	                		dbLabelTrans = dbLabel.getOrigTranslation(trans.getLanguageCode());
	                	}
	                	if (dbLabelTrans == null) {
	                		trans.setLabel(dbLabel);
	                		trans.setLanguage((Language) dao.retrieve(Language.class, trans.getLanguage().getId()));
	                		dbLabelTrans = (LabelTranslation) dao.create(trans, false);
	                	} else {
	                		dbLabelTrans.setOrigTranslation(trans.getOrigTranslation());
	                		dbLabelTrans.setAnnotation1(trans.getAnnotation1());
	                		dbLabelTrans.setAnnotation2(trans.getAnnotation2());
	                		dbLabelTrans.setWarnings(trans.getWarnings());
	                		dbLabelTrans.setNeedTranslation(trans.isNeedTranslation());
	                		dbLabelTrans.setRequestTranslation(trans.getRequestTranslation());
	                		dbLabelTrans.setLanguageCode(trans.getLanguageCode());
	                		dbLabelTrans.setSortNo(trans.getSortNo());
	                	}
	                }
                }
            }
        }

        if (nonBreakExceptions.hasNestedException()) {
            throw nonBreakExceptions;
        }
        log.info("Import dictionary finish");
        return dbDict;
    }
    
    public Map<String, Collection<BusinessWarning>> importDictionaries(Long appId, Collection<Dictionary> dictList, int mode) throws BusinessException {
    	Map<String, Collection<BusinessWarning>> warningMap = new TreeMap<String, Collection<BusinessWarning>>();
    	for (Dictionary dict : dictList) {
    		Map<String, String> langCharset = new HashMap<String, String>();
    		if (dict.getDictLanguages() != null) {
    			for (DictionaryLanguage dl : dict.getDictLanguages()) {
    				langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
    			}
    		}
    		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
    		importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, warnings);
    		warningMap.put(dict.getName(), warnings);
    	}
    	return warningMap;
    }

    private String getUnifiedLangCode(String langCode) {
        return langCode.toUpperCase().replace("_", "-");
    }

    public Dictionary getLatestDictionary(Long dictionaryBaseId, Long beforeDictionaryId) {
    	String hql = "from Dictionary where base.id=:baseId";
    	Map param = new HashMap();
    	param.put("baseId", dictionaryBaseId);
    	if (beforeDictionaryId != null) {
    		hql += " and id<:beforeId";
    		param.put("beforeId", beforeDictionaryId);
    	}
    	hql += " order by id desc";
    	return (Dictionary) dao.retrieveOne(hql, param);
    }
    
    public Dictionary findLatestDictionaryInApp(Long appId, String dictionaryName) {
    	String hql = "select obj from Application app join app.base.dictionaryBases db join db.dictionaries obj" +
    			" where app.id=:appId and db.name=:name order by obj.id desc";
    	Map param = new HashMap();
    	param.put("appId", appId);
    	param.put("name", dictionaryName);
    	Dictionary dict = (Dictionary) dao.retrieveOne(hql, param);
    	if (dict != null && dict.getDictLanguages() != null) {
    		for (DictionaryLanguage dl : dict.getDictLanguages()) {
    			Hibernate.initialize(dl);
    		}
    	}
    	return dict;
    }
    
    public void removeDictionaryFromApplication(Long appId, Long dictId) {
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	app.removeDictionary(dictId);
    }
    
    public void removeDictionaryFromApplication(Long appId, Collection<Long> idList) {
    	for (Long id : idList) {
    		removeDictionaryFromApplication(appId, id);
    	}
    }
    
    public Long deleteDictionary(Long id) {
    	String hql = "select distinct a from Application a join a.dictionaries as d where d.id=:id";
    	Map param = new HashMap();
    	param.put("id", id);
    	Collection<Application> apps = dao.retrieve(hql, param);
    	for (Application app : apps) {
    		app.removeDictionary(id);
    	}
    	Dictionary dictionary = (Dictionary) dao.retrieve(Dictionary.class, id);
    	DictionaryBase dictBase = dictionary.getBase();
    	dao.delete(Dictionary.class, id);
    	
    	// delete dictBase if it doesn't contain other dictionary
/*
    	if (dictBase.getDictionaries() == null || dictBase.getDictionaries().size() == 0 ||
    			dictBase.getDictionaries().size() == 1 && dictBase.getDictionaries().iterator().next().getId().equals(id)) {
    		dao.delete(dictBase);
    		return dictBase.getId();
    	}
*/
    	return null;
    }
    
    public void deleteDictionary(Collection<Long> idList) {
    	for (Long id : idList) {
    		deleteDictionary(id);
    	}
    }
    
/*    
    public Map<Long, int[]> getDictTranslationSummary(Long dictId) {
    	Map<Long, int[]> result = new HashMap<Long, int[]>();
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	String hql = "select ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Dictionary d join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where d.id=:dictId and ot.language=t.language" +
    			" group by ot.language.id";
    	Map param = new HashMap();
    	param.put("dictId", dictId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		result.put((Long) row[0], new int[] {((Number)row[1]).intValue(), ((Number)row[2]).intValue(), ((Number)row[3]).intValue()});
    	}
    	return result;
    }
*/
    
    public Map<Long, Map<Long, int[]>> getDictTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select d.id,ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by d.id,ot.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }

    public Map<Long, Map<Long, int[]>> getAppTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select a.id,ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by a.id,ot.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }
    
    public int getLabelNumByApp(Long appId) {
    	String hql = "select count(l) from Application app join app.dictionaries d join d.labels l where app.id=:appId";
    	Map param = new HashMap();
    	param.put("appId", appId);
    	Number count = (Number) dao.retrieveOne(hql, param);
    	return count == null ? 0 : count.intValue();
    }

    public void updateDictionaryFormat(Long id, String format) throws BusinessException {
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
    	// TODO: update valid format list
    	String[] validFormats = {"DCT", "Dictionary conf", "XML labels", "XML properties", "Text properties"};
    	if (!Arrays.asList(validFormats).contains(format)) {
    		throw new BusinessException(BusinessException.INVALID_DICT_FORMAT, format);
    	}
    	dict.setFormat(format);
    }
    
    public void updateDictionaryEncoding(Long id, String encoding) throws BusinessException {
    	encoding = encoding.trim();
    	if (!encoding.equals("ISO-8859-1") && !encoding.equals("UTF-8") && !encoding.equals("UTF-16LE")) {
    		throw new BusinessException(BusinessException.INVALID_DICT_ENCODING, encoding);
    	}
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, id);
    	dict.setEncoding(encoding);
    }
    
    public void changeDictionaryInApp(Long appId, Long oldDictId, Long newDictId) throws BusinessException {
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	Dictionary oldDict = (Dictionary) dao.retrieve(Dictionary.class, oldDictId);
    	Dictionary newDict = (Dictionary) dao.retrieve(Dictionary.class, newDictId);
    	if (!oldDict.getBase().getId().equals(newDict.getBase().getId())) {
    		throw new BusinessException(BusinessException.DICTIONARIES_NOT_SAME_BASE);
    	}
    	Collection<Dictionary> dictionaries = app.getDictionaries();
    	boolean found = false;
    	if (dictionaries != null) {
    		Iterator<Dictionary> iterator = dictionaries.iterator();
    		while (iterator.hasNext()) {
    			Dictionary dict = iterator.next();
    			if (dict.getId().equals(oldDictId)) {
    				iterator.remove();
    				found = true;
    				break;
    			}
    		}
    	}
    	if (found) {
    		dictionaries.add(newDict);
    	} else {
    		throw new BusinessException(BusinessException.DICTIONARY_NOT_IN_APP);
    	}
    }
    
    public DictionaryLanguage addLanguage(Long dictId, String code, Long languageId, Long charsetId) throws BusinessException {
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	if (dict.getDictLanguage(code) != null) {
    		throw new BusinessException(BusinessException.DUPLICATE_LANG_CODE, dict.getName(), code);
    	}
    	DictionaryLanguage dl = new DictionaryLanguage();
    	dl.setDictionary(dict);
    	dl.setLanguageCode(code);
    	dl.setLanguage((Language) dao.retrieve(Language.class, languageId));
    	dl.setCharset((Charset) dao.retrieve(Charset.class, charsetId));
    	dl.setSortNo(dict.getMaxSortNo());
    	return (DictionaryLanguage) dao.create(dl);
    }
    
    public DictionaryLanguage updateDictionaryLanguage(Long id, String code, Long languageId, Long charsetId) {
    	DictionaryLanguage dl = (DictionaryLanguage) dao.retrieve(DictionaryLanguage.class, id);
    	if (code != null) {
    		dl.setLanguageCode(code);
    	}
    	if (languageId != null) {
        	dl.setLanguage((Language) dao.retrieve(Language.class, languageId));
    	}
    	if (charsetId != null) {
        	dl.setCharset((Charset) dao.retrieve(Charset.class, charsetId));
    	}
    	return dl;
    }
    
    public void removeDictionaryLanguage(Collection<Long> ids) {
    	for (Long id : ids) {
        	DictionaryLanguage dl = (DictionaryLanguage) dao.retrieve(DictionaryLanguage.class, id);
        	dao.delete(dl);
    	}
    }
    
    public void updateLabels(Collection<Long> idList, String maxLength,
			String description, String context) {
    	Map<String, Context> newContextMap = new HashMap<String, Context>();
    	for (Long id : idList) {
    		Label label = (Label) dao.retrieve(Label.class, id);
    		if (maxLength != null) {
    			label.setMaxLength(maxLength);
    		}
    		if (description != null) {
    			label.setDescription(description);
    		}
    		if (context != null) {
    			Context ctx = newContextMap.get(context);
    			if (ctx == null) {
	    			ctx = textService.getContextByName(context);
	    			if (ctx == null) {
	    				ctx = new Context();
	    				ctx.getName();
	    				ctx = (Context) dao.create(ctx);
	    				newContextMap.put(context, ctx);
	    			}
    			}
    			label.setContext(ctx);
    		}
    	}
    }
    

	

	
}
