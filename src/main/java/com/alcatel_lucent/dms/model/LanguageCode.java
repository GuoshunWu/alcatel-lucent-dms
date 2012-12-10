/**
 * 
 */
package com.alcatel_lucent.dms.model;

/**
 * @author guoshunw
 *
 */
public interface LanguageCode {
	
	String getCode();
    
	void setCode(String code);
    
	Language getLanguage();
    
	void setLanguage(Language language);
    
	boolean isDefaultCode();
    
	void setDefaultCode(boolean defaultCode);
}
