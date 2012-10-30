package com.alcatel_lucent.dms.model;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;

public class Dictionary extends BaseEntity {

    private static final long serialVersionUID = 4926531636839152201L;

    private Collection<DictionaryLanguage> dictLanguages;
    private Collection<Label> labels;
    private boolean locked;
    private DictionaryBase base;
    private String version;
    private String annotation1;
    private String annotation2;
    private String annotation3;
    private String annotation4;

    private static Map<String, String> refCodes = JSONObject.fromObject("{'DCT':'GAE','Dictionary conf':'EN-UK','Text properties':'en','XML labels':'en'}");

    public String getName() {
        return base.getName();
    }

    public String getLanguageReferenceCode() {
        String ref = refCodes.get(getFormat());
        return null == ref ? "en" : ref;
    }

    public void setName(String name) {
        base.setName(name);
    }

    public String getFormat() {
        return base.getFormat();
    }

    public void setFormat(String format) {
        base.setFormat(format);
    }

    public String getEncoding() {
        return base.getEncoding();
    }

    public void setEncoding(String encoding) {
        base.setEncoding(encoding);
    }

    public String getPath() {
        return base.getPath();
    }

    public void setPath(String path) {
        base.setPath(path);
    }

    public DictionaryBase getBase() {
        return base;
    }

    public int getLabelNum() {
        return labels == null ? 0 : labels.size();
    }

    public void setBase(DictionaryBase base) {
        this.base = base;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Dictionary() {
        super();
    }


    public Collection<DictionaryLanguage> getDictLanguages() {
        return dictLanguages;
    }

    public void setDictLanguages(Collection<DictionaryLanguage> dictLanguages) {
        this.dictLanguages = dictLanguages;
    }

    public Collection<Label> getLabels() {
        return labels;
    }

    public void setLabels(Collection<Label> labels) {
        this.labels = labels;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Language getLanguageByCode(String langCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(langCode)) {
                    return dl.getLanguage();
                }
            }
        }
        return null;
    }

    public String getLanguageCode(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl.getLanguageCode();
                }
            }
        }
        return null;
    }

    public HashSet<String> getAllLanguageCodes() {
        HashSet<String> result = new HashSet<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }

    public ArrayList<String> getAllLanguageCodesOrdered() {
        ArrayList<String> result = new ArrayList<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }


    public ArrayList<Language> getAllLanguages() {
        ArrayList<Language> result = new ArrayList<Language>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguage());
            }
        }
        return result;
    }

    public Map<Long, String> getLangCodeMap() {
        Map<Long, String> result = new HashMap<Long, String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.put(dl.getLanguage().getId(), dl.getLanguageCode());
            }
        }
        return result;
    }

    public DictionaryLanguage getDictLanguage(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl;
                }
            }
        }
        return null;
    }

    public DictionaryLanguage getDictLanguage(String languageCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(languageCode)) {
                    return dl;
                }
            }
        }
        return null;
    }


    public Label getLabel(String key) {
        if (labels != null) {
            for (Label label : labels) {
                if (label.getKey().equals(key)) {
                    return label;
                }
            }
        }
        return null;
    }

    private Map<String, int[]> summaryCache;

    /**
     * Get translation status summary by language, used by front
     *
     * @return
     */
    public Map<String, int[]> getS() {
        return summaryCache;
    }

    public void setS(Map<Long, int[]> summary) {
        this.summaryCache = new HashMap<String, int[]>();
        if (summary == null) return;
        for (Long langId : summary.keySet()) {
            summaryCache.put(langId.toString(), summary.get(langId));
        }
    }

    private Application app;    // transient variable for REST service

    public void setApp(Application app) {
        this.app = app;
    }

    public Application getApp() {
        return app;
    }
    
    private Collection<BusinessWarning> parseWarnings;		// transient variable for parse warnings information
    private Collection<BusinessWarning> importWarnings;		// transient variable for import warnings information
    private Collection<BusinessException> previewErrors;			// transient variable for errors information;
    
	public Collection<BusinessWarning> getParseWarnings() {
		return parseWarnings;
	}

	public void setParseWarnings(Collection<BusinessWarning> parseWarnings) {
		this.parseWarnings = parseWarnings;
	}
	
	public Collection<BusinessWarning> getImportWarnings() {
		return importWarnings;
	}

	public void setImportWarnings(Collection<BusinessWarning> importWarnings) {
		this.importWarnings = importWarnings;
	}

	public Collection<BusinessException> getPreviewErrors() {
		return previewErrors;
	}

	public void setPreviewErrors(Collection<BusinessException> previewErrors) {
		this.previewErrors = previewErrors;
	}

	public int getWarningCount() {
		return (parseWarnings == null ? 0 : parseWarnings.size()) + 
				(importWarnings == null ? 0 : importWarnings.size());
	}
	
	public int getErrorCount() {
		return previewErrors == null ? 0 : previewErrors.size();
	}
	
	public Collection<String> getWarnings() {
		Collection<String> result = new ArrayList<String>();
		if (parseWarnings != null) {
			for (BusinessWarning warning : parseWarnings) {
				result.add(warning.toString());
			}
		}
		if (importWarnings != null) {
			for (BusinessWarning warning : importWarnings) {
				result.add(warning.toString());
			}
		}
		return result;
	}
	
	public Collection<String> getErrors() {
		Collection<String> result = new ArrayList<String>();
		if (previewErrors != null) {
			for (BusinessException e : previewErrors) {
				result.add(e.toString());
			}
		}
		return result;
	}
	
	/**
	 * Validate dictionary before importing.
	 * The method will refresh "previewErrors" and "importWarnings" properties.
	 */
	public void validate() {
		previewErrors = new ArrayList<BusinessException>();
		importWarnings = new ArrayList<BusinessWarning>();
		if (getName() == null || getName().trim().isEmpty()) {
			previewErrors.add(new BusinessException(BusinessException.LACK_DICT_NAME));
		}
		if (getVersion() == null || getVersion().trim().isEmpty()) {
			previewErrors.add(new BusinessException(BusinessException.LACK_DICT_VERSION));
		}
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				if (dl.getLanguage() == null) {
					previewErrors.add(new BusinessException(BusinessException.UNKNOWN_LANG_CODE, dl.getLanguageCode()));
				}
				if (dl.getCharset() == null) {
					previewErrors.add(new BusinessException(BusinessException.CHARSET_NOT_DEFINED, dl.getLanguageCode()));
				}
			}
		}
		if (labels != null) {
			for (Label label : labels) {
				if (!label.checkLength(label.getReference())) {
					importWarnings.add(new BusinessWarning(
							BusinessWarning.EXCEED_MAX_LENGTH, "Reference", label.getKey()));
					if (label.getOrigTranslations() != null) {
						for (LabelTranslation lt : label.getOrigTranslations()) {
							if (!label.checkLength(lt.getOrigTranslation())) {
								importWarnings.add(new BusinessWarning(
										BusinessWarning.EXCEED_MAX_LENGTH, 
										lt.getLanguageCode(), label.getKey()));
							}
						}
						for (LabelTranslation lt : label.getOrigTranslations()) {
							String langCode = lt.getLanguageCode();
							DictionaryLanguage dl = getDictLanguage(langCode);
							if (dl.getLanguage() == null || dl.getCharset() == null) continue;
							String charsetName = dl.getCharset().getName();
							if (!getEncoding().equals(charsetName)) {
								try {
									byte[] source = lt.getOrigTranslation().getBytes(getEncoding());
									String encodedTranslation = new String(source, charsetName);
									byte[] target = encodedTranslation.getBytes(charsetName);
			                        if (!Arrays.equals(source, target) || lt.isValidText()) {
			                        	importWarnings.add(new BusinessWarning(
			                        			BusinessWarning.INVALID_TEXT, 
			                        			encodedTranslation, dl.getCharset().getName(), 
			                        			langCode, label.getKey()));
			                        }
		                        } catch (UnsupportedEncodingException e) {
		                        	previewErrors.add(new BusinessException(
		                        			BusinessException.CHARSET_NOT_FOUND, charsetName));
		                        }
							}
						}
					}
				}
			}
		}
	}
	
   public int getMaxSortNo() {
    	if (dictLanguages == null) {
    		return 0;
    	}
    	int max = 0;
    	for (DictionaryLanguage dl : dictLanguages) {
    		if (dl.getSortNo() > max) {
    			max = dl.getSortNo();
    		}
    	}
    	return max;
    }

	public String getAnnotation1() {
		return annotation1;
	}

	public void setAnnotation1(String annotation1) {
		this.annotation1 = annotation1;
	}

	public String getAnnotation2() {
		return annotation2;
	}

	public void setAnnotation2(String annotation2) {
		this.annotation2 = annotation2;
	}

	public String getAnnotation3() {
		return annotation3;
	}

	public void setAnnotation3(String annotation3) {
		this.annotation3 = annotation3;
	}

	public String getAnnotation4() {
		return annotation4;
	}

	public void setAnnotation4(String annotation4) {
		this.annotation4 = annotation4;
	}

}
